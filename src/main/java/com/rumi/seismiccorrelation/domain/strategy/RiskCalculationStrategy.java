package com.rumi.seismiccorrelation.domain.strategy;

public interface RiskCalculationStrategy {

    double calculateRiskIndex(double normalizedStructuralResponse);
}
