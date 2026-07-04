package com.example.feature.flag.strategy;

import com.example.feature.flag.entity.FeatureFlag;
import com.example.feature.flag.entity.StrategyType;
import java.util.Map;

public interface FeatureEvaluationStrategy {

	StrategyType getType();

	boolean evaluate(FeatureFlag featureFlag, Map<String, Object> context);
}
