package com.insightpulse.InsightPulse.dto;

import lombok.Getter;

@Getter
@SuppressWarnings("unused")
public class AuthResponse {
    private String token;

    public AuthResponse(String token) {
        this.token = token;
    }
}
