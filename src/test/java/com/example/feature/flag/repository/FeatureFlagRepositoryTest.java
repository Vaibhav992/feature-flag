package com.example.feature.flag.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.feature.flag.entity.FeatureFlag;
import com.example.feature.flag.entity.StrategyType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class FeatureFlagRepositoryTest {

	@Autowired
	private FeatureFlagRepository repository;

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void shouldSaveAndFindByFeatureName() {
		FeatureFlag saved = repository.save(FeatureFlag.builder()
			.featureName("NEW_CHECKOUT")
			.enabled(true)
			.strategy(StrategyType.COUNTRY)
			.rules(objectMapper.createObjectNode().put("country", "IN"))
			.build());

		assertThat(saved.getId()).isNotNull();
		assertThat(repository.findByFeatureName("NEW_CHECKOUT")).isPresent();
	}

	@Test
	void shouldRejectDuplicateFeatureName() {
		repository.saveAndFlush(FeatureFlag.builder()
			.featureName("UPI_ROLLOUT")
			.enabled(true)
			.strategy(StrategyType.USER)
			.rules(objectMapper.createObjectNode().put("userId", "123"))
			.build());

		assertThatThrownBy(() -> repository.saveAndFlush(FeatureFlag.builder()
			.featureName("UPI_ROLLOUT")
			.enabled(false)
			.strategy(StrategyType.COUNTRY)
			.rules(objectMapper.createObjectNode().put("country", "IN"))
			.build()))
			.isInstanceOf(DataIntegrityViolationException.class);
	}
}
