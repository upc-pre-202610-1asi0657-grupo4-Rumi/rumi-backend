package com.rumi.riskassessment.domain.strategy;

public final class AiRiskStrategy implements RiskCalculationStrategy {

    @Override
    public double calculateRiskIndex(double normalizedStructuralResponse) {
        validate(normalizedStructuralResponse);

        // Deterministic placeholder until a trained risk model is integrated.
        return 0.7 * normalizedStructuralResponse
                + 0.3 * normalizedStructuralResponse * normalizedStructuralResponse;
    }

    private void validate(double normalizedStructuralResponse) {
        if (!Double.isFinite(normalizedStructuralResponse)
                || normalizedStructuralResponse < 0.0
                || normalizedStructuralResponse > 1.0) {
            throw new IllegalArgumentException("Structural response must be between 0 and 1");
        }
    }
}
