package com.example.feature.flag.strategy;

import com.example.feature.flag.entity.FeatureFlag;
import com.example.feature.flag.entity.StrategyType;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class CountryStrategy implements FeatureEvaluationStrategy {

	@Override
	public StrategyType getType() {
		return StrategyType.COUNTRY;
	}

	@Override
	public boolean evaluate(FeatureFlag featureFlag, Map<String, Object> context) {
		String expectedCountry = featureFlag.getRules().path("country").asText(null);
		Object actualCountry = context.get("country");
		if (expectedCountry == null || actualCountry == null) {
			return false;
		}
		return expectedCountry.equalsIgnoreCase(actualCountry.toString());
	}
}
