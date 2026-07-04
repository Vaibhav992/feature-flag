package com.example.feature.flag.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.feature.flag.entity.FeatureFlag;
import com.example.feature.flag.entity.StrategyType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;

class UserStrategyTest {

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final UserStrategy strategy = new UserStrategy();

	@Test
	void shouldReturnTrueWhenUserMatches() {
		FeatureFlag flag = FeatureFlag.builder()
			.strategy(StrategyType.USER)
			.rules(objectMapper.createObjectNode().put("userId", "123"))
			.enabled(true)
			.featureName("UPI_ROLLOUT")
			.build();

		boolean result = strategy.evaluate(flag, Map.of("userId", "123"));

		assertThat(result).isTrue();
	}

	@Test
	void shouldReturnFalseWhenUserDoesNotMatch() {
		FeatureFlag flag = FeatureFlag.builder()
			.strategy(StrategyType.USER)
			.rules(objectMapper.createObjectNode().put("userId", "123"))
			.enabled(true)
			.featureName("UPI_ROLLOUT")
			.build();

		boolean result = strategy.evaluate(flag, Map.of("userId", "999"));

		assertThat(result).isFalse();
	}
}
