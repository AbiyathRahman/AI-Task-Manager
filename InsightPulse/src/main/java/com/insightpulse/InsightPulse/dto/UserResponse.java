package com.insightpulse.InsightPulse.dto;

import com.insightpulse.InsightPulse.model.Tier;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserResponse {
    private String username;
    private String name;
    private Tier tier;
}
