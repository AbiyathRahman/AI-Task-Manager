package com.insightpulse.InsightPulse.service.impl;

import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;
import org.springframework.beans.factory.annotation.Value;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
@Getter
@Setter
public class GoogleCalendarService {
    @Value("${google.client.id}")
    private String clientId;
    @Value("${google.client.secret}")
    private String clientSecret;
    @Value("${google.redirect.uri}")
    private String redirectUri;

    private final List<String> scopes = List.of(CalendarScopes.CALENDAR_READONLY);

    public String getAuthorizationUrl() throws Exception{
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                clientId,
                clientSecret,
                scopes).setAccessType("offline").build();
        return flow.newAuthorizationUrl().setRedirectUri(redirectUri).build();

    }

    public List<Event> getEvents(String authCode) throws Exception {
        GoogleAuthorizationCodeTokenRequest request = new GoogleAuthorizationCodeTokenRequest(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                clientId,
                clientSecret,
                authCode,
                redirectUri
        );
        GoogleTokenResponse tokenResponse = request.execute();
        Credential credential = new Credential(BearerToken.authorizationHeaderAccessMethod())
                .setAccessToken(tokenResponse.getAccessToken());

        com.google.api.services.calendar.Calendar service = new Calendar.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential).setApplicationName("InsightPulse").build();
        Events events = service.events().list("primary")
                .setMaxResults(10)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();
        return events.getItems();
    }

}
