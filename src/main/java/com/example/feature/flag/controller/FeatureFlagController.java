package com.example.feature.flag.controller;

import com.example.feature.flag.dto.request.CreateFeatureFlagRequest;
import com.example.feature.flag.dto.request.EvaluateFeatureFlagRequest;
import com.example.feature.flag.dto.request.UpdateFeatureFlagRequest;
import com.example.feature.flag.dto.response.EvaluateFeatureFlagResponse;
import com.example.feature.flag.dto.response.FeatureFlagResponse;
import com.example.feature.flag.service.FeatureFlagService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/flags")
public class FeatureFlagController {

	private final FeatureFlagService service;

	public FeatureFlagController(FeatureFlagService service) {
		this.service = service;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public FeatureFlagResponse create(@Valid @RequestBody CreateFeatureFlagRequest request) {
		return service.create(request);
	}

	@PutMapping("/{id}")
	public FeatureFlagResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateFeatureFlagRequest request) {
		return service.update(id, request);
	}

	@GetMapping("/{featureName}")
	public FeatureFlagResponse getByFeatureName(@PathVariable String featureName) {
		return service.getByFeatureName(featureName);
	}

	@PostMapping("/evaluate")
	public EvaluateFeatureFlagResponse evaluate(@Valid @RequestBody EvaluateFeatureFlagRequest request) {
		return service.evaluate(request);
	}
}
