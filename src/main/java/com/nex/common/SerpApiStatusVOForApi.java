package com.nex.common;

import lombok.Data;

@Data
public class SerpApiStatusVOForApi {
    private String accountId;
    private String apiKey;
    private String accountEmail;
    private String planId;
    private String planName;
    private float planMonthlyPrice;
    private Integer searchesPerMonth;
    private Integer planSearchesLeft;
    private Integer extraCredits;
    private Integer totalSearchesLeft;
    private Integer thisMonthUsage;
    private Integer lastHourSearches;
    private Integer accountRateLimitPerHour;
    @Override
    public String toString(){
        return "SerpApiStatus{" +
                "  apiKey='" + apiKey + '\'' +
                ", totalSearchesLeft='" + totalSearchesLeft + '\'' +
                '}';
    }
}
