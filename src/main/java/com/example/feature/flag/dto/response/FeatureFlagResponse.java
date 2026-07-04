package com.example.feature.flag.dto.response;

import com.example.feature.flag.entity.StrategyType;
import java.util.Map;
import java.util.UUID;

public record FeatureFlagResponse(
	UUID id,
	String featureName,
	boolean enabled,
	StrategyType strategy,
	Map<String, Object> rules
) {
}
