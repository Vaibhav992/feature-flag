package com.example.feature.flag.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.feature.flag.dto.request.CreateFeatureFlagRequest;
import com.example.feature.flag.dto.request.EvaluateFeatureFlagRequest;
import com.example.feature.flag.entity.StrategyType;
import java.util.Map;
import java.util.Set;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class FeatureFlagDtoValidationTest {

	private static Validator validator;

	@BeforeAll
	static void setup() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void shouldFailWhenCreateRequestIsInvalid() {
		CreateFeatureFlagRequest request = new CreateFeatureFlagRequest(
			"",
			true,
			null,
			null
		);

		Set<ConstraintViolation<CreateFeatureFlagRequest>> violations = validator.validate(request);
		assertThat(violations).isNotEmpty();
	}

	@Test
	void shouldPassWhenCreateRequestIsValid() {
		CreateFeatureFlagRequest request = new CreateFeatureFlagRequest(
			"NEW_CHECKOUT",
			true,
			StrategyType.COUNTRY,
			Map.of("country", "IN")
		);

		Set<ConstraintViolation<CreateFeatureFlagRequest>> violations = validator.validate(request);
		assertThat(violations).isEmpty();
	}

	@Test
	void shouldFailWhenEvaluateRequestIsMissingContext() {
		EvaluateFeatureFlagRequest request = new EvaluateFeatureFlagRequest("NEW_CHECKOUT", null);

		Set<ConstraintViolation<EvaluateFeatureFlagRequest>> violations = validator.validate(request);
		assertThat(violations).isNotEmpty();
	}
}
