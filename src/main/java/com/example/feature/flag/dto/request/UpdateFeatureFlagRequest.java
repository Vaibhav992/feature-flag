package com.example.feature.flag.dto.request;

import com.example.feature.flag.entity.StrategyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;

public record UpdateFeatureFlagRequest(
	@NotBlank(message = "featureName is required")
	@Size(max = 100, message = "featureName must be at most 100 characters")
	String featureName,
	@NotNull(message = "enabled is required")
	Boolean enabled,
	@NotNull(message = "strategy is required")
	StrategyType strategy,
	@NotNull(message = "rules is required")
	Map<String, Object> rules
) {
}
