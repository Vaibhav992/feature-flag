package com.example.feature.flag.strategy;

import com.example.feature.flag.entity.FeatureFlag;
import com.example.feature.flag.entity.StrategyType;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class UserStrategy implements FeatureEvaluationStrategy {

	@Override
	public StrategyType getType() {
		return StrategyType.USER;
	}

	@Override
	public boolean evaluate(FeatureFlag featureFlag, Map<String, Object> context) {
		String expectedUser = featureFlag.getRules().path("userId").asText(null);
		Object actualUser = context.get("userId");
		if (expectedUser == null || actualUser == null) {
			return false;
		}
		return expectedUser.equals(actualUser.toString());
	}
}
