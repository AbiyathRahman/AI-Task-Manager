package com.insightpulse.InsightPulse.controller;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.services.calendar.model.Event;
import com.insightpulse.InsightPulse.config.TierGuard;
import com.insightpulse.InsightPulse.model.User;
import com.insightpulse.InsightPulse.security.JwtUtil;
import com.insightpulse.InsightPulse.service.impl.BedrockAIService;
import com.insightpulse.InsightPulse.service.impl.GoogleCalendarService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/api/calendar")
public class GoogleCalendarController {

    private static final Logger logger = LoggerFactory.getLogger(GoogleCalendarController.class);

    @Autowired
    private GoogleCalendarService googleCalendarService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private com.insightpulse.InsightPulse.repository.UserRepository userRepository;
    @Autowired
    private BedrockAIService bedrockAIService;


    private User getCurrentUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }

    @GetMapping("auth-url")
    public ResponseEntity<Map<String, String>> getGoogleAuthUrl() {
        try {
            String url = googleCalendarService.getAuthorizationUrl();
            logger.info("Generated Google auth URL successfully");
            return ResponseEntity.ok(Map.of("url", url));
        } catch (Exception e) {
            logger.error("Error generating Google auth URL: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate authorization URL"));
        }
    }

    @GetMapping("/events")
    public ResponseEntity<?> oauthCallBack(@RequestParam("code") String code, HttpServletRequest request) {
        try {
            // Get current user with improved error handling
            User currentUser = getCurrentUser(request);
            logger.info("Processing calendar events for user: {}", currentUser.getUsername());

            // Validate authorization code
            if (code == null || code.trim().isEmpty()) {
                logger.warn("Authorization code is null or empty");
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Authorization code is required"));
            }
            TokenResponse tokens = googleCalendarService.exchangeCodeForTokens(code);
            if (tokens == null) {
                logger.warn("Failed to exchange authorization code for tokens");
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Failed to exchange authorization code for tokens"));
            }
            currentUser.setRefreshToken(tokens.getRefreshToken());
            userRepository.save(currentUser);

            // Fetch events from Google Calendar
            List<Event> events = googleCalendarService.getEvents(currentUser.getUsername());

            // Format events for response
            List<String> formattedEvents = events.stream()
                    .map(event -> {
                        String start = event.getStart().getDateTime() != null ?
                                event.getStart().getDateTime().toStringRfc3339() :
                                event.getStart().getDate().toStringRfc3339();
                        String summary = event.getSummary() != null ? event.getSummary() : "No title";
                        return String.format("%s - %s", start, summary);
                    })
                    .toList();

            logger.info("Successfully fetched {} events for user: {}",
                    formattedEvents.size(), currentUser.getUsername());

            return ResponseEntity.ok(Map.of(
                    "events", formattedEvents,
                    "count", formattedEvents.size()
            ));

        } catch (SecurityException e) {
            logger.warn("Authentication error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication failed"));

        } catch (Exception e) {
            logger.error("Error fetching calendar events: {}", e.getMessage(), e);

            // Handle specific Google API errors
            if (e.getMessage().contains("invalid_grant")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "Authorization code expired or invalid. Please re-authorize."));
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch calendar events"));
        }
    }

    @GetMapping("/event-insights")
    public ResponseEntity<String> getEventInsights(@RequestParam("code") String code, HttpServletRequest request) {
        try {
            User currentUser = getCurrentUser(request);
            if (code == null || code.trim().isEmpty()) {
                logger.warn("Authorization code is null or empty");
                return ResponseEntity.badRequest()
                        .body("Authorization code is required");
            }
            logger.info("Processing calendar events for user: {}", currentUser.getUsername());
            if(!TierGuard.canUseClaude(currentUser.getTier())){
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You cannot use this feature, Upgrade to Basic");
            }
            List<Event> currUserEvents = googleCalendarService.getEvents(currentUser.getUsername());
            List<String> formattedEvents = currUserEvents.stream()
                    .map(event -> {
                        String start = event.getStart().getDateTime() != null ?
                                event.getStart().getDateTime().toStringRfc3339() :
                                event.getStart().getDate().toStringRfc3339();
                        String summary = event.getSummary() != null ? event.getSummary() : "No title";
                        return String.format("%s - %s", start, summary);
                    })
                    .toList();
            String aiEventFeedback = bedrockAIService.getEventInsight(formattedEvents);
            return ResponseEntity.ok(aiEventFeedback);



        }
        catch (SecurityException e) {
            logger.warn("Authentication error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Authentication failed");

        }catch (AccessDeniedException e) {
            logger.warn("Access denied: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(e.getMessage());
        }

        catch (Exception e) {
            logger.error("Error fetching calendar events: {}", e.getMessage(), e);

            // Handle specific Google API errors
            if (e.getMessage().contains("invalid_grant")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Authorization code expired or invalid. Please re-authorize.");
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch calendar events");
        }
    }
}
