package com.insightpulse.InsightPulse.service.impl;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import com.insightpulse.InsightPulse.model.User;
import com.insightpulse.InsightPulse.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Service
@Getter
@Setter
public class GoogleCalendarService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleCalendarService.class);
    private final UserRepository userRepository;

    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    @Value("${google.redirect.uri}")
    private String redirectUri;

    private final List<String> scopes = List.of(CalendarScopes.CALENDAR_READONLY);
    private final NetHttpTransport httpTransport = new NetHttpTransport();
    private final GsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    // In-memory storage for tokens (replace with database in production)
    private final Map<String, GoogleTokenResponse> tokenStore = new ConcurrentHashMap<>();

    public GoogleCalendarService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String getAuthorizationUrl() throws Exception {
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport,
                jsonFactory,
                clientId,
                clientSecret,
                scopes)
                .setAccessType("offline")
                .setApprovalPrompt("force") // Force to get refresh token
                .build();

        return flow.newAuthorizationUrl()
                .setRedirectUri(redirectUri) // Use the same redirectUri everywhere
                .build();
    }

    public TokenResponse exchangeCodeForTokens(String authCode) throws Exception {
        return new GoogleAuthorizationCodeTokenRequest(
                new NetHttpTransport(),
                jsonFactory,
                clientId,
                clientSecret,
                authCode,
                redirectUri
        ).execute();
    }


    public List<Event> getEvents(String user) throws Exception {
        try {
            logger.info("Using refresh token to get access token for user: {}", user);

            // 1. Get the stored refresh token for this user
            String refreshToken = userRepository.findByUsername(user).get().getRefreshToken();
            if (refreshToken == null) {
                logger.error("No refresh token found for user: {}", user);
                throw new RuntimeException("No refresh token found for user: " + user);
            }

            // 2. Use the refresh token to get a new access token
            GoogleTokenResponse tokenResponse = new GoogleRefreshTokenRequest(
                    httpTransport,
                    jsonFactory,
                    refreshToken,
                    clientId,
                    clientSecret
            ).execute();

            // 3. Build a Credential object from the token response
            GoogleCredential credential = new GoogleCredential.Builder()
                    .setTransport(httpTransport)
                    .setJsonFactory(jsonFactory)
                    .setClientSecrets(clientId, clientSecret)
                    .build()
                    .setFromTokenResponse(tokenResponse);

            // 4. Create the Calendar service
            Calendar service = new Calendar.Builder(
                    httpTransport,
                    jsonFactory,
                    credential)
                    .setApplicationName("InsightPulse")
                    .build();

            // 5. Fetch events
            Events events = service.events().list("primary")
                    .setMaxResults(10)
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .setTimeMin(new com.google.api.client.util.DateTime(System.currentTimeMillis()))
                    .execute();

            logger.info("Successfully fetched {} events", events.getItems().size());
            return events.getItems();

        } catch (Exception e) {
            logger.error("Error fetching calendar events: {}", e.getMessage(), e);

            // If it's an invalid_grant error, clear stored tokens
            if (e.getMessage().contains("invalid_grant")) {
                tokenStore.remove(user);
                logger.info("Cleared stored tokens due to invalid_grant error");
            }

            throw e;
        }
    }


    private Credential createCredentialFromToken(GoogleTokenResponse tokenResponse) {
        try {
            // Create GoogleCredential using the Builder pattern with all required components
            GoogleCredential.Builder builder = new GoogleCredential.Builder()
                    .setJsonFactory(jsonFactory)
                    .setTransport(httpTransport)
                    .setClientSecrets(clientId, clientSecret);

            GoogleCredential credential = builder.build();

            // Set the access token
            credential.setAccessToken(tokenResponse.getAccessToken());

            // Set refresh token if available
            if (tokenResponse.getRefreshToken() != null) {
                credential.setRefreshToken(tokenResponse.getRefreshToken());
            }

            return credential;

        } catch (Exception e) {
            logger.error("Error creating credential from token: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create credential from token", e);
        }
    }

    public void clearTokenForUser(String userId) {
        tokenStore.remove(userId);
        logger.info("Cleared tokens for user: {}", userId);
    }

    public boolean hasValidToken(String userId) {
        return tokenStore.containsKey(userId);
    }
}