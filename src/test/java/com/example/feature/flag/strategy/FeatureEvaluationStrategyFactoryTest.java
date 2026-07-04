package com.example.feature.flag.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.feature.flag.entity.StrategyType;
import java.util.List;
import org.junit.jupiter.api.Test;

class FeatureEvaluationStrategyFactoryTest {

	@Test
	void shouldResolveAllStrategiesByType() {
		FeatureEvaluationStrategyFactory factory = new FeatureEvaluationStrategyFactory(List.of(
			new CountryStrategy(),
			new UserStrategy(),
			new PercentageStrategy()
		));

		assertThat(factory.resolve(StrategyType.COUNTRY)).isInstanceOf(CountryStrategy.class);
		assertThat(factory.resolve(StrategyType.USER)).isInstanceOf(UserStrategy.class);
		assertThat(factory.resolve(StrategyType.PERCENTAGE)).isInstanceOf(PercentageStrategy.class);
	}

	@Test
	void shouldFailWhenTypeIsNull() {
		FeatureEvaluationStrategyFactory factory = new FeatureEvaluationStrategyFactory(List.of(
			new CountryStrategy()
		));

		assertThatThrownBy(() -> factory.resolve(null))
			.isInstanceOf(IllegalArgumentException.class);
	}
}
