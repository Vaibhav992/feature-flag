package com.example.feature.flag.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.feature.flag.dto.request.CreateFeatureFlagRequest;
import com.example.feature.flag.dto.request.UpdateFeatureFlagRequest;
import com.example.feature.flag.dto.response.FeatureFlagResponse;
import com.example.feature.flag.entity.StrategyType;
import com.example.feature.flag.exception.FeatureNotFoundException;
import com.example.feature.flag.service.FeatureFlagService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(FeatureFlagController.class)
class FeatureFlagControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private FeatureFlagService service;

	@Test
	void shouldCreateFeatureFlagSuccessfully() throws Exception {
		// Given
		UUID id = UUID.randomUUID();
		CreateFeatureFlagRequest request = new CreateFeatureFlagRequest(
			"NEW_CHECKOUT",
			true,
			StrategyType.COUNTRY,
			Map.of("country", "IN")
		);
		FeatureFlagResponse response = new FeatureFlagResponse(
			id,
			"NEW_CHECKOUT",
			true,
			StrategyType.COUNTRY,
			Map.of("country", "IN")
		);

		when(service.create(any(CreateFeatureFlagRequest.class))).thenReturn(response);

		// When + Then
		mockMvc.perform(post("/api/v1/flags")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.featureName").value("NEW_CHECKOUT"))
			.andExpect(jsonPath("$.enabled").value(true));
	}

	@Test
	void shouldUpdateFeatureFlagSuccessfully() throws Exception {
		// Given
		UUID id = UUID.randomUUID();
		UpdateFeatureFlagRequest request = new UpdateFeatureFlagRequest(
			"UPI_ROLLOUT",
			false,
			StrategyType.USER,
			Map.of("userId", "123")
		);
		FeatureFlagResponse response = new FeatureFlagResponse(
			id,
			"UPI_ROLLOUT",
			false,
			StrategyType.USER,
			Map.of("userId", "123")
		);

		when(service.update(eq(id), any(UpdateFeatureFlagRequest.class))).thenReturn(response);

		// When + Then
		mockMvc.perform(put("/api/v1/flags/{id}", id)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.featureName").value("UPI_ROLLOUT"))
			.andExpect(jsonPath("$.enabled").value(false));
	}

	@Test
	void shouldGetFeatureFlagSuccessfully() throws Exception {
		// Given
		FeatureFlagResponse response = new FeatureFlagResponse(
			UUID.randomUUID(),
			"NEW_CHECKOUT",
			true,
			StrategyType.COUNTRY,
			Map.of("country", "IN")
		);

		when(service.getByFeatureName("NEW_CHECKOUT")).thenReturn(response);

		// When + Then
		mockMvc.perform(get("/api/v1/flags/{featureName}", "NEW_CHECKOUT"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.featureName").value("NEW_CHECKOUT"));
	}

	@Test
	void shouldRejectInvalidRequestWith400() throws Exception {
		// Given
		String invalidPayload = """
			{
			  "featureName": "",
			  "enabled": true,
			  "strategy": "COUNTRY",
			  "rules": {"country":"IN"}
			}
			""";

		// When + Then
		mockMvc.perform(post("/api/v1/flags")
				.contentType(MediaType.APPLICATION_JSON)
				.content(invalidPayload))
			.andExpect(status().isBadRequest());
	}

	@Test
	void shouldReturn404WhenFeatureDoesNotExist() throws Exception {
		// Given
		when(service.getByFeatureName("UNKNOWN"))
			.thenThrow(new FeatureNotFoundException("Feature not found: UNKNOWN"));

		// When + Then
		mockMvc.perform(get("/api/v1/flags/{featureName}", "UNKNOWN"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.message").value("Feature not found: UNKNOWN"));
	}
}
