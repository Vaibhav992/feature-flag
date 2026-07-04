package com.example.feature.flag.service;

import com.example.feature.flag.dto.request.CreateFeatureFlagRequest;
import com.example.feature.flag.dto.request.EvaluateFeatureFlagRequest;
import com.example.feature.flag.dto.request.UpdateFeatureFlagRequest;
import com.example.feature.flag.dto.response.EvaluateFeatureFlagResponse;
import com.example.feature.flag.dto.response.FeatureFlagResponse;
import com.example.feature.flag.entity.FeatureFlag;
import com.example.feature.flag.exception.DuplicateFeatureException;
import com.example.feature.flag.exception.FeatureNotFoundException;
import com.example.feature.flag.mapper.FeatureFlagMapper;
import com.example.feature.flag.repository.FeatureFlagRepository;
import com.example.feature.flag.strategy.FeatureEvaluationStrategy;
import com.example.feature.flag.strategy.FeatureEvaluationStrategyFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeatureFlagService {

	private final FeatureFlagRepository repository;
	private final FeatureFlagMapper mapper;
	private final FeatureEvaluationStrategyFactory strategyFactory;

	public FeatureFlagService(
		FeatureFlagRepository repository,
		FeatureFlagMapper mapper,
		FeatureEvaluationStrategyFactory strategyFactory
	) {
		this.repository = repository;
		this.mapper = mapper;
		this.strategyFactory = strategyFactory;
	}

	@Transactional
	public FeatureFlagResponse create(CreateFeatureFlagRequest request) {
		repository.findByFeatureName(request.featureName()).ifPresent(flag -> {
			throw new DuplicateFeatureException("Feature already exists: " + request.featureName());
		});

		try {
			FeatureFlag entity = mapper.toEntity(request);
			FeatureFlag saved = repository.save(entity);
			return mapper.toResponse(saved);
		} catch (DataIntegrityViolationException exception) {
			// Handles concurrent create requests racing after pre-check.
			throw new DuplicateFeatureException("Feature already exists: " + request.featureName());
		}
	}

	@Transactional
	public FeatureFlagResponse update(java.util.UUID id, UpdateFeatureFlagRequest request) {
		FeatureFlag existing = repository.findById(id)
			.orElseThrow(() -> new FeatureNotFoundException("Feature not found for id: " + id));

		try {
			mapper.updateEntity(existing, request);
			FeatureFlag saved = repository.save(existing);
			return mapper.toResponse(saved);
		} catch (DataIntegrityViolationException exception) {
			// Handles rename conflicts under concurrent updates.
			throw new DuplicateFeatureException("Feature already exists: " + request.featureName());
		}
	}

	@Transactional(readOnly = true)
	public FeatureFlagResponse getByFeatureName(String featureName) {
		FeatureFlag featureFlag = repository.findByFeatureName(featureName)
			.orElseThrow(() -> new FeatureNotFoundException("Feature not found: " + featureName));
		return mapper.toResponse(featureFlag);
	}

	@Transactional(readOnly = true)
	public EvaluateFeatureFlagResponse evaluate(EvaluateFeatureFlagRequest request) {
		FeatureFlag featureFlag = repository.findByFeatureName(request.featureName())
			.orElseThrow(() -> new FeatureNotFoundException("Feature not found: " + request.featureName()));

		if (!featureFlag.isEnabled()) {
			return new EvaluateFeatureFlagResponse(false);
		}

		FeatureEvaluationStrategy strategy = strategyFactory.resolve(featureFlag.getStrategy());
		boolean enabled = strategy.evaluate(featureFlag, request.context());
		return new EvaluateFeatureFlagResponse(enabled);
	}
}
