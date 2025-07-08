package com.insightpulse.InsightPulse.controller;

import com.google.api.services.calendar.model.Event;
import com.insightpulse.InsightPulse.service.impl.GoogleCalendarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/calendar")
public class GoogleCalendarController {

    @Autowired
    private GoogleCalendarService googleCalendarService;

    @GetMapping("auth-url")
    public ResponseEntity<String> getGoogleAuthUrl(){
        try{
            return ResponseEntity.ok(googleCalendarService.getAuthorizationUrl());
        }catch(Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/oauth2callback")
    public ResponseEntity<List<String>> oauthCallBack(@RequestParam("code") String code){
        try{
            List<Event> events = googleCalendarService.getEvents(code);
            List<String> formatted = events.stream().map(event -> {
                String start = event.getStart().getDateTime() != null ?
                        event.getStart().getDateTime().toStringRfc3339() : event.getStart().getDate().toStringRfc3339();
                return String.format("%s - %s", start, event.getSummary());
            }).toList();
            return ResponseEntity.ok(formatted);
        }catch(Exception e){
            e.printStackTrace();
            return ResponseEntity.status(500).body(List.of("Error fetching events"));
        }
    }
}
