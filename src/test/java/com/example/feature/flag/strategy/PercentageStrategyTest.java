package com.example.feature.flag.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.feature.flag.entity.FeatureFlag;
import com.example.feature.flag.entity.StrategyType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PercentageStrategyTest {

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final PercentageStrategy strategy = new PercentageStrategy();

	@Test
	void shouldAlwaysReturnFalseForZeroPercentage() {
		FeatureFlag flag = FeatureFlag.builder()
			.strategy(StrategyType.PERCENTAGE)
			.rules(objectMapper.createObjectNode().put("percentage", 0))
			.enabled(true)
			.featureName("BETA_FEATURE")
			.build();

		boolean result = strategy.evaluate(flag, Map.of("userId", "user-123"));

		assertThat(result).isFalse();
	}

	@Test
	void shouldAlwaysReturnTrueForHundredPercentage() {
		FeatureFlag flag = FeatureFlag.builder()
			.strategy(StrategyType.PERCENTAGE)
			.rules(objectMapper.createObjectNode().put("percentage", 100))
			.enabled(true)
			.featureName("BETA_FEATURE")
			.build();

		boolean result = strategy.evaluate(flag, Map.of("userId", "user-123"));

		assertThat(result).isTrue();
	}

	@Test
	void shouldBeDeterministicForSameUser() {
		FeatureFlag flag = FeatureFlag.builder()
			.strategy(StrategyType.PERCENTAGE)
			.rules(objectMapper.createObjectNode().put("percentage", 50))
			.enabled(true)
			.featureName("BETA_FEATURE")
			.build();

		boolean first = strategy.evaluate(flag, Map.of("userId", "user-123"));
		boolean second = strategy.evaluate(flag, Map.of("userId", "user-123"));

		assertThat(first).isEqualTo(second);
	}
}
