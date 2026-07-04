package com.example.feature.flag.strategy;

import com.example.feature.flag.entity.FeatureFlag;
import com.example.feature.flag.entity.StrategyType;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class PercentageStrategy implements FeatureEvaluationStrategy {

	@Override
	public StrategyType getType() {
		return StrategyType.PERCENTAGE;
	}

	@Override
	public boolean evaluate(FeatureFlag featureFlag, Map<String, Object> context) {
		Object userId = context.get("userId");
		if (userId == null) {
			return false;
		}

		int percentage = featureFlag.getRules().path("percentage").asInt(-1);
		if (percentage < 0 || percentage > 100) {
			return false;
		}
		if (percentage == 0) {
			return false;
		}
		if (percentage == 100) {
			return true;
		}

		int bucket = Math.floorMod(userId.toString().hashCode(), 100);
		return bucket < percentage;
	}
}
