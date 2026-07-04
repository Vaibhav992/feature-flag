package com.example.feature.flag.strategy;

import com.example.feature.flag.entity.StrategyType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class FeatureEvaluationStrategyFactory {

	private final Map<StrategyType, FeatureEvaluationStrategy> strategies = new EnumMap<>(StrategyType.class);

	public FeatureEvaluationStrategyFactory(List<FeatureEvaluationStrategy> strategyList) {
		for (FeatureEvaluationStrategy strategy : strategyList) {
			strategies.put(strategy.getType(), strategy);
		}
	}

	public FeatureEvaluationStrategy resolve(StrategyType strategyType) {
		if (strategyType == null) {
			throw new IllegalArgumentException("strategyType is required");
		}
		FeatureEvaluationStrategy strategy = strategies.get(strategyType);
		if (strategy == null) {
			throw new IllegalArgumentException("No strategy found for type: " + strategyType);
		}
		return strategy;
	}
}
