package com.insightpulse.InsightPulse.dto;

import com.insightpulse.InsightPulse.model.Tier;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UserRequest {
    private String username;
    private String name;
    private Tier tier;
}
