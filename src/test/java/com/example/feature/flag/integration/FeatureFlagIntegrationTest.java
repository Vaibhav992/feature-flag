package com.example.feature.flag.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.feature.flag.repository.FeatureFlagRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FeatureFlagIntegrationTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private FeatureFlagRepository repository;

	@BeforeEach
	void setUp() {
		repository.deleteAll();
	}

	@Test
	void shouldCreateGetUpdateAndEvaluateFeatureFlow() throws Exception {
		// Given
		String createPayload = """
			{
			  "featureName":"NEW_CHECKOUT",
			  "enabled":true,
			  "strategy":"COUNTRY",
			  "rules":{"country":"IN"}
			}
			""";

		// When - create
		MvcResult createResult = mockMvc.perform(post("/api/v1/flags")
				.contentType(MediaType.APPLICATION_JSON)
				.content(createPayload))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.featureName").value("NEW_CHECKOUT"))
			.andReturn();

		JsonNode createBody = objectMapper.readTree(createResult.getResponse().getContentAsString());
		String id = createBody.get("id").asText();
		assertThat(repository.findByFeatureName("NEW_CHECKOUT")).isPresent();

		// Then - get
		mockMvc.perform(get("/api/v1/flags/{featureName}", "NEW_CHECKOUT"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.enabled").value(true))
			.andExpect(jsonPath("$.strategy").value("COUNTRY"));

		// When - evaluate before update
		String evaluatePayload = """
			{
			  "featureName":"NEW_CHECKOUT",
			  "context":{"country":"IN"}
			}
			""";
		mockMvc.perform(post("/api/v1/flags/evaluate")
				.contentType(MediaType.APPLICATION_JSON)
				.content(evaluatePayload))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.enabled").value(true));

		// When - update to disabled
		String updatePayload = """
			{
			  "featureName":"NEW_CHECKOUT",
			  "enabled":false,
			  "strategy":"COUNTRY",
			  "rules":{"country":"IN"}
			}
			""";
		mockMvc.perform(put("/api/v1/flags/{id}", id)
				.contentType(MediaType.APPLICATION_JSON)
				.content(updatePayload))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.enabled").value(false));

		// Then - evaluate after update
		mockMvc.perform(post("/api/v1/flags/evaluate")
				.contentType(MediaType.APPLICATION_JSON)
				.content(evaluatePayload))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.enabled").value(false));
	}

	@Test
	void shouldReturn404ForMissingFeature() throws Exception {
		// Given + When + Then
		mockMvc.perform(get("/api/v1/flags/{featureName}", "UNKNOWN"))
			.andExpect(status().isNotFound())
			.andExpect(jsonPath("$.status").value(404));
	}

	@Test
	void shouldRejectInvalidCreatePayload() throws Exception {
		// Given
		String invalidPayload = """
			{
			  "featureName":"",
			  "enabled":true,
			  "strategy":"COUNTRY",
			  "rules":{"country":"IN"}
			}
			""";

		// When + Then
		mockMvc.perform(post("/api/v1/flags")
				.contentType(MediaType.APPLICATION_JSON)
				.content(invalidPayload))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.status").value(400));
	}

	@Test
	void shouldReturnFalseForDisabledFeatureEvaluation() throws Exception {
		// Given
		String createPayload = """
			{
			  "featureName":"BETA_FEATURE",
			  "enabled":false,
			  "strategy":"COUNTRY",
			  "rules":{"country":"IN"}
			}
			""";
		mockMvc.perform(post("/api/v1/flags")
				.contentType(MediaType.APPLICATION_JSON)
				.content(createPayload))
			.andExpect(status().isCreated());

		String evaluatePayload = """
			{
			  "featureName":"BETA_FEATURE",
			  "context":{"country":"IN"}
			}
			""";

		// When + Then
		mockMvc.perform(post("/api/v1/flags/evaluate")
				.contentType(MediaType.APPLICATION_JSON)
				.content(evaluatePayload))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.enabled").value(false));
	}
}
