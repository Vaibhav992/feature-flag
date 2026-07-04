package com.example.feature.flag.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.feature.flag.dto.request.CreateFeatureFlagRequest;
import com.example.feature.flag.dto.request.EvaluateFeatureFlagRequest;
import com.example.feature.flag.dto.request.UpdateFeatureFlagRequest;
import com.example.feature.flag.dto.response.EvaluateFeatureFlagResponse;
import com.example.feature.flag.dto.response.FeatureFlagResponse;
import com.example.feature.flag.entity.FeatureFlag;
import com.example.feature.flag.entity.StrategyType;
import com.example.feature.flag.exception.DuplicateFeatureException;
import com.example.feature.flag.exception.FeatureNotFoundException;
import com.example.feature.flag.exception.InvalidStrategyException;
import com.example.feature.flag.mapper.FeatureFlagMapper;
import com.example.feature.flag.repository.FeatureFlagRepository;
import com.example.feature.flag.strategy.FeatureEvaluationStrategy;
import com.example.feature.flag.strategy.FeatureEvaluationStrategyFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class FeatureFlagServiceTest {

	@Mock
	private FeatureFlagRepository repository;

	@Mock
	private FeatureFlagMapper mapper;

	@Mock
	private FeatureEvaluationStrategyFactory strategyFactory;

	@Mock
	private FeatureEvaluationStrategy strategy;

	private FeatureFlagService service;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void setUp() {
		service = new FeatureFlagService(repository, mapper, strategyFactory);
	}

	@Test
	void shouldCreateFeatureFlagSuccessfully() {
		// Given
		CreateFeatureFlagRequest request = new CreateFeatureFlagRequest(
			"NEW_CHECKOUT",
			true,
			StrategyType.COUNTRY,
			Map.of("country", "IN")
		);
		FeatureFlag entity = FeatureFlag.builder().featureName("NEW_CHECKOUT").enabled(true).build();
		FeatureFlag saved = FeatureFlag.builder()
			.id(UUID.randomUUID())
			.featureName("NEW_CHECKOUT")
			.enabled(true)
			.strategy(StrategyType.COUNTRY)
			.rules(objectMapper.createObjectNode().put("country", "IN"))
			.build();
		FeatureFlagResponse response = new FeatureFlagResponse(
			saved.getId(),
			"NEW_CHECKOUT",
			true,
			StrategyType.COUNTRY,
			Map.of("country", "IN")
		);

		when(repository.findByFeatureName("NEW_CHECKOUT")).thenReturn(Optional.empty());
		when(mapper.toEntity(request)).thenReturn(entity);
		when(repository.save(entity)).thenReturn(saved);
		when(mapper.toResponse(saved)).thenReturn(response);

		// When
		FeatureFlagResponse result = service.create(request);

		// Then
		assertThat(result.featureName()).isEqualTo("NEW_CHECKOUT");
		verify(repository).findByFeatureName("NEW_CHECKOUT");
	}

	@Test
	void shouldThrowDuplicateFeatureExceptionWhenFeatureAlreadyExists() {
		// Given
		CreateFeatureFlagRequest request = new CreateFeatureFlagRequest(
			"NEW_CHECKOUT",
			true,
			StrategyType.COUNTRY,
			Map.of("country", "IN")
		);
		when(repository.findByFeatureName("NEW_CHECKOUT"))
			.thenReturn(Optional.of(FeatureFlag.builder().build()));

		// When + Then
		assertThatThrownBy(() -> service.create(request))
			.isInstanceOf(DuplicateFeatureException.class);
	}

	@Test
	void shouldUpdateFeatureFlagSuccessfully() {
		// Given
		UUID id = UUID.randomUUID();
		UpdateFeatureFlagRequest request = new UpdateFeatureFlagRequest(
			"UPI_ROLLOUT",
			false,
			StrategyType.USER,
			Map.of("userId", "123")
		);
		FeatureFlag existing = FeatureFlag.builder().id(id).featureName("OLD").enabled(true).build();
		FeatureFlagResponse response = new FeatureFlagResponse(
			id,
			"UPI_ROLLOUT",
			false,
			StrategyType.USER,
			Map.of("userId", "123")
		);

		when(repository.findById(id)).thenReturn(Optional.of(existing));
		when(repository.save(existing)).thenReturn(existing);
		when(mapper.toResponse(existing)).thenReturn(response);

		// When
		FeatureFlagResponse result = service.update(id, request);

		// Then
		verify(mapper).updateEntity(existing, request);
		assertThat(result.featureName()).isEqualTo("UPI_ROLLOUT");
	}

	@Test
	void shouldThrowFeatureNotFoundWhenUpdatingMissingFeature() {
		// Given
		UUID id = UUID.randomUUID();
		UpdateFeatureFlagRequest request = new UpdateFeatureFlagRequest(
			"UPI_ROLLOUT",
			false,
			StrategyType.USER,
			Map.of("userId", "123")
		);
		when(repository.findById(id)).thenReturn(Optional.empty());

		// When + Then
		assertThatThrownBy(() -> service.update(id, request))
			.isInstanceOf(FeatureNotFoundException.class);
	}

	@Test
	void shouldGetFeatureFlagByNameSuccessfully() {
		// Given
		FeatureFlag entity = FeatureFlag.builder()
			.id(UUID.randomUUID())
			.featureName("NEW_CHECKOUT")
			.enabled(true)
			.strategy(StrategyType.COUNTRY)
			.rules(objectMapper.createObjectNode().put("country", "IN"))
			.build();
		FeatureFlagResponse response = new FeatureFlagResponse(
			entity.getId(),
			entity.getFeatureName(),
			true,
			StrategyType.COUNTRY,
			Map.of("country", "IN")
		);

		when(repository.findByFeatureName("NEW_CHECKOUT")).thenReturn(Optional.of(entity));
		when(mapper.toResponse(entity)).thenReturn(response);

		// When
		FeatureFlagResponse result = service.getByFeatureName("NEW_CHECKOUT");

		// Then
		assertThat(result.featureName()).isEqualTo("NEW_CHECKOUT");
	}

	@Test
	void shouldThrowFeatureNotFoundWhenGettingMissingFeature() {
		// Given
		when(repository.findByFeatureName("UNKNOWN")).thenReturn(Optional.empty());

		// When + Then
		assertThatThrownBy(() -> service.getByFeatureName("UNKNOWN"))
			.isInstanceOf(FeatureNotFoundException.class);
	}

	@Test
	void shouldReturnFalseWhenFeatureIsDisabled() {
		// Given
		FeatureFlag entity = FeatureFlag.builder()
			.featureName("NEW_CHECKOUT")
			.enabled(false)
			.strategy(StrategyType.COUNTRY)
			.rules(objectMapper.createObjectNode().put("country", "IN"))
			.build();
		EvaluateFeatureFlagRequest request = new EvaluateFeatureFlagRequest(
			"NEW_CHECKOUT",
			Map.of("country", "IN")
		);

		when(repository.findByFeatureName("NEW_CHECKOUT")).thenReturn(Optional.of(entity));

		// When
		EvaluateFeatureFlagResponse result = service.evaluate(request);

		// Then
		verify(strategyFactory, never()).resolve(any());
		assertThat(result.enabled()).isFalse();
	}

	@Test
	void shouldEvaluateFeatureUsingResolvedStrategy() {
		// Given
		FeatureFlag entity = FeatureFlag.builder()
			.featureName("NEW_CHECKOUT")
			.enabled(true)
			.strategy(StrategyType.COUNTRY)
			.rules(objectMapper.createObjectNode().put("country", "IN"))
			.build();
		EvaluateFeatureFlagRequest request = new EvaluateFeatureFlagRequest(
			"NEW_CHECKOUT",
			Map.of("country", "IN")
		);

		when(repository.findByFeatureName("NEW_CHECKOUT")).thenReturn(Optional.of(entity));
		when(strategyFactory.resolve(StrategyType.COUNTRY)).thenReturn(strategy);
		when(strategy.evaluate(entity, request.context())).thenReturn(true);

		// When
		EvaluateFeatureFlagResponse result = service.evaluate(request);

		// Then
		assertThat(result.enabled()).isTrue();
		verify(strategyFactory).resolve(StrategyType.COUNTRY);
		verify(strategy).evaluate(entity, request.context());
	}

	@Test
	void shouldThrowFeatureNotFoundWhenEvaluatingMissingFeature() {
		// Given
		EvaluateFeatureFlagRequest request = new EvaluateFeatureFlagRequest(
			"UNKNOWN",
			Map.of("country", "IN")
		);
		when(repository.findByFeatureName("UNKNOWN")).thenReturn(Optional.empty());

		// When + Then
		assertThatThrownBy(() -> service.evaluate(request))
			.isInstanceOf(FeatureNotFoundException.class);
	}

	@Test
	void shouldThrowInvalidStrategyWhenFactoryCannotResolve() {
		// Given
		FeatureFlag entity = FeatureFlag.builder()
			.featureName("NEW_CHECKOUT")
			.enabled(true)
			.strategy(StrategyType.COUNTRY)
			.rules(objectMapper.createObjectNode().put("country", "IN"))
			.build();
		EvaluateFeatureFlagRequest request = new EvaluateFeatureFlagRequest(
			"NEW_CHECKOUT",
			Map.of("country", "IN")
		);

		when(repository.findByFeatureName("NEW_CHECKOUT")).thenReturn(Optional.of(entity));
		when(strategyFactory.resolve(StrategyType.COUNTRY))
			.thenThrow(new InvalidStrategyException("No strategy found for type: COUNTRY"));

		// When + Then
		assertThatThrownBy(() -> service.evaluate(request))
			.isInstanceOf(InvalidStrategyException.class);
	}

	@Test
	void shouldPropagateUnexpectedExceptionWhenRepositoryFails() {
		// Given
		CreateFeatureFlagRequest request = new CreateFeatureFlagRequest(
			"NEW_CHECKOUT",
			true,
			StrategyType.COUNTRY,
			Map.of("country", "IN")
		);
		when(repository.findByFeatureName("NEW_CHECKOUT"))
			.thenThrow(new RuntimeException("DB unavailable"));

		// When + Then
		assertThatThrownBy(() -> service.create(request))
			.isInstanceOf(RuntimeException.class)
			.hasMessageContaining("DB unavailable");
	}

	@Test
	void shouldThrowDuplicateFeatureExceptionWhenConcurrentCreateConflictsAtSave() {
		// Given
		CreateFeatureFlagRequest request = new CreateFeatureFlagRequest(
			"NEW_CHECKOUT",
			true,
			StrategyType.COUNTRY,
			Map.of("country", "IN")
		);
		FeatureFlag entity = FeatureFlag.builder().featureName("NEW_CHECKOUT").enabled(true).build();

		when(repository.findByFeatureName("NEW_CHECKOUT")).thenReturn(Optional.empty());
		when(mapper.toEntity(request)).thenReturn(entity);
		when(repository.save(entity)).thenThrow(new DataIntegrityViolationException("duplicate key"));

		// When + Then
		assertThatThrownBy(() -> service.create(request))
			.isInstanceOf(DuplicateFeatureException.class)
			.hasMessageContaining("Feature already exists");
	}

	@Test
	void shouldThrowDuplicateFeatureExceptionWhenConcurrentUpdateConflictsAtSave() {
		// Given
		UUID id = UUID.randomUUID();
		UpdateFeatureFlagRequest request = new UpdateFeatureFlagRequest(
			"NEW_CHECKOUT",
			true,
			StrategyType.COUNTRY,
			Map.of("country", "IN")
		);
		FeatureFlag existing = FeatureFlag.builder().id(id).featureName("OLD_NAME").enabled(true).build();

		when(repository.findById(id)).thenReturn(Optional.of(existing));
		when(repository.save(existing)).thenThrow(new DataIntegrityViolationException("duplicate key"));

		// When + Then
		assertThatThrownBy(() -> service.update(id, request))
			.isInstanceOf(DuplicateFeatureException.class)
			.hasMessageContaining("Feature already exists");
	}
}
