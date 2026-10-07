package com.rumi.seismiccorrelation.domain.strategy;

public final class RuleBasedRiskStrategy implements RiskCalculationStrategy {

    @Override
    public double calculateRiskIndex(double normalizedStructuralResponse) {
        validate(normalizedStructuralResponse);

        // Demonstration thresholds; they are not calibrated structural limits.
        if (normalizedStructuralResponse < 0.3) {
            return 0.2;
        }
        if (normalizedStructuralResponse < 0.7) {
            return 0.5;
        }
        return 0.8;
    }

    private void validate(double normalizedStructuralResponse) {
        if (!Double.isFinite(normalizedStructuralResponse)
                || normalizedStructuralResponse < 0.0
                || normalizedStructuralResponse > 1.0) {
            throw new IllegalArgumentException("Structural response must be between 0 and 1");
        }
    }
}
