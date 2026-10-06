package com.rumi.riskassessment.domain.strategy;

public interface RiskCalculationStrategy {

    double calculateRiskIndex(double normalizedStructuralResponse);
}
