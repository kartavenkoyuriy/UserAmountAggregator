package com.akka.userAmountAggregator;

import akka.util.Duration;

import java.math.BigDecimal;
import java.util.Map;

class AmountAggregation{
    private final Map<Long, BigDecimal> userIdToAmount;
    private final Duration duration;

    public AmountAggregation(Map<Long, BigDecimal> userIdToAmount, Duration duration) {
        this.userIdToAmount = userIdToAmount;
        this.duration = duration;
    }

    public Map<Long, BigDecimal> getUserIdToAmount() {
        return userIdToAmount;
    }

    public Duration getDuration() {
        return duration;
    }
}
