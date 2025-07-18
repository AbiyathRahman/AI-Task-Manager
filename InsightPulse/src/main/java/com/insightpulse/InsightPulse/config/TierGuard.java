package com.insightpulse.InsightPulse.config;

import com.insightpulse.InsightPulse.model.Tier;
import com.insightpulse.InsightPulse.model.User;
import org.springframework.security.access.AccessDeniedException;


public class TierGuard {

    public static void checkBasicFeature(User user){
        if(user.getTier() == Tier.FREE){
            throw new AccessDeniedException("Basic feature not available for free users");
        }

    }

    public static void checkPremiumFeature(User user){
        if(user.getTier() == Tier.FREE || user.getTier() == Tier.BASIC){
            throw new AccessDeniedException("Pro feature not available for premium users");
        }
    }
    public static boolean canUseClaude(Tier tier){
        return tier == Tier.BASIC || tier == Tier.PREMIUM;
    }
    public static boolean canUseCalendar(Tier tier){
        return tier == Tier.BASIC || tier == Tier.PREMIUM;
    }

}
