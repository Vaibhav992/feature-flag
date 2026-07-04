package com.example.feature.flag.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record EvaluateFeatureFlagRequest(
	@NotBlank(message = "featureName is required")
	String featureName,
	@NotNull(message = "context is required")
	Map<String, Object> context
) {
}
