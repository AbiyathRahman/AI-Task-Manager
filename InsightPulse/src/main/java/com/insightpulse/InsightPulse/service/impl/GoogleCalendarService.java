package com.insightpulse.InsightPulse.service.impl;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
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

    public List<Event> getEvents(String authCode, String userId) throws Exception {
        try {
            // Check if we already have a valid token for this user
            GoogleTokenResponse storedToken = tokenStore.get(userId);

            Credential credential;
            if (storedToken != null) {
                // Use stored token
                credential = createCredentialFromToken(storedToken);
                logger.info("Using stored token for user: {}", userId);
            } else {
                // Exchange authorization code for tokens
                logger.info("Exchanging authorization code for tokens");
                GoogleAuthorizationCodeTokenRequest request = new GoogleAuthorizationCodeTokenRequest(
                        httpTransport,
                        jsonFactory,
                        clientId,
                        clientSecret,
                        authCode,
                        redirectUri // Use the same redirectUri
                );

                GoogleTokenResponse tokenResponse = request.execute();

                // Store the token for future use
                tokenStore.put(userId, tokenResponse);

                credential = createCredentialFromToken(tokenResponse);
            }

            // Create Calendar service
            Calendar service = new Calendar.Builder(
                    httpTransport,
                    jsonFactory,
                    credential)
                    .setApplicationName("InsightPulse")
                    .build();



            // Fetch events
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
                tokenStore.remove(userId);
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