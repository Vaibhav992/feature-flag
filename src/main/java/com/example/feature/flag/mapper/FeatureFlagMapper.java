package com.example.feature.flag.mapper;

import com.example.feature.flag.dto.request.CreateFeatureFlagRequest;
import com.example.feature.flag.dto.request.UpdateFeatureFlagRequest;
import com.example.feature.flag.dto.response.FeatureFlagResponse;
import com.example.feature.flag.entity.FeatureFlag;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class FeatureFlagMapper {

	private final ObjectMapper objectMapper;

	public FeatureFlagMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	public FeatureFlag toEntity(CreateFeatureFlagRequest request) {
		return FeatureFlag.builder()
			.featureName(request.featureName())
			.enabled(request.enabled())
			.strategy(request.strategy())
			.rules(objectMapper.valueToTree(request.rules()))
			.build();
	}

	public void updateEntity(FeatureFlag entity, UpdateFeatureFlagRequest request) {
		entity.setFeatureName(request.featureName());
		entity.setEnabled(request.enabled());
		entity.setStrategy(request.strategy());
		entity.setRules(objectMapper.valueToTree(request.rules()));
	}

	public FeatureFlagResponse toResponse(FeatureFlag entity) {
		Map<String, Object> rules = objectMapper.convertValue(
			entity.getRules(),
			new TypeReference<Map<String, Object>>() {}
		);
		return new FeatureFlagResponse(
			entity.getId(),
			entity.getFeatureName(),
			entity.isEnabled(),
			entity.getStrategy(),
			rules
		);
	}
}
