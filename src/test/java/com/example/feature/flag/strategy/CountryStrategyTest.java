package com.example.feature.flag.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.feature.flag.entity.FeatureFlag;
import com.example.feature.flag.entity.StrategyType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CountryStrategyTest {

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final CountryStrategy strategy = new CountryStrategy();

	@Test
	void shouldReturnTrueWhenCountryMatches() {
		FeatureFlag flag = FeatureFlag.builder()
			.strategy(StrategyType.COUNTRY)
			.rules(objectMapper.createObjectNode().put("country", "IN"))
			.enabled(true)
			.featureName("NEW_CHECKOUT")
			.build();

		boolean result = strategy.evaluate(flag, Map.of("country", "IN"));

		assertThat(result).isTrue();
	}

	@Test
	void shouldReturnFalseWhenCountryDoesNotMatch() {
		FeatureFlag flag = FeatureFlag.builder()
			.strategy(StrategyType.COUNTRY)
			.rules(objectMapper.createObjectNode().put("country", "IN"))
			.enabled(true)
			.featureName("NEW_CHECKOUT")
			.build();

		boolean result = strategy.evaluate(flag, Map.of("country", "US"));

		assertThat(result).isFalse();
	}
}
