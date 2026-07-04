package com.example.feature.flag.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.feature.flag.dto.request.CreateFeatureFlagRequest;
import com.example.feature.flag.dto.response.FeatureFlagResponse;
import com.example.feature.flag.entity.FeatureFlag;
import com.example.feature.flag.entity.StrategyType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class FeatureFlagMapperTest {

	private final ObjectMapper objectMapper = new ObjectMapper();
	private final FeatureFlagMapper mapper = new FeatureFlagMapper(objectMapper);

	@Test
	void shouldMapCreateRequestToEntity() {
		CreateFeatureFlagRequest request = new CreateFeatureFlagRequest(
			"NEW_CHECKOUT",
			true,
			StrategyType.COUNTRY,
			Map.of("country", "IN")
		);

		FeatureFlag entity = mapper.toEntity(request);

		assertThat(entity.getFeatureName()).isEqualTo("NEW_CHECKOUT");
		assertThat(entity.isEnabled()).isTrue();
		assertThat(entity.getStrategy()).isEqualTo(StrategyType.COUNTRY);
		assertThat(entity.getRules().get("country").asText()).isEqualTo("IN");
	}

	@Test
	void shouldMapEntityToResponse() {
		FeatureFlag entity = FeatureFlag.builder()
			.id(UUID.randomUUID())
			.featureName("UPI_ROLLOUT")
			.enabled(false)
			.strategy(StrategyType.USER)
			.rules(objectMapper.createObjectNode().put("userId", "123"))
			.build();

		FeatureFlagResponse response = mapper.toResponse(entity);

		assertThat(response.featureName()).isEqualTo("UPI_ROLLOUT");
		assertThat(response.enabled()).isFalse();
		assertThat(response.strategy()).isEqualTo(StrategyType.USER);
		assertThat(response.rules().get("userId")).isEqualTo("123");
	}
}
