package com.rumi.seismiccorrelation.domain.service;

import com.rumi.seismiccorrelation.domain.strategy.AiRiskStrategy;
import com.rumi.seismiccorrelation.domain.strategy.RuleBasedRiskStrategy;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Offset.offset;

class RiskIndexCalculationServiceTest {

    @Test
    void calculatesTheRiskIndexWithTheAiStrategy() {
        RiskIndexCalculationService service = new RiskIndexCalculationService(new AiRiskStrategy());

        double riskIndex = service.calculateRiskIndex(0.8);

        assertThat(riskIndex).isCloseTo(0.752, offset(0.0001));
    }

    @Test
    void calculatesTheRiskIndexWithTheRuleBasedStrategy() {
        RiskIndexCalculationService service = new RiskIndexCalculationService(new RuleBasedRiskStrategy());

        double riskIndex = service.calculateRiskIndex(0.8);

        assertThat(riskIndex).isEqualTo(0.8);
    }
}
