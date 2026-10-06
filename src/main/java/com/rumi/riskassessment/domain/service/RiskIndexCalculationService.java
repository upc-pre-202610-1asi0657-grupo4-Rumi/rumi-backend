package com.rumi.riskassessment.domain.service;

import com.rumi.riskassessment.domain.strategy.RiskCalculationStrategy;

import java.util.Objects;

public final class RiskIndexCalculationService {

    private final RiskCalculationStrategy strategy;

    public RiskIndexCalculationService(RiskCalculationStrategy strategy) {
        this.strategy = Objects.requireNonNull(strategy, "Risk calculation strategy is required");
    }

    public double calculateRiskIndex(double normalizedStructuralResponse) {
        return strategy.calculateRiskIndex(normalizedStructuralResponse);
    }
}
