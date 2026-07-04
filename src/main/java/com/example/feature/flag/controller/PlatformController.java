package com.example.feature.flag.controller;

import java.time.OffsetDateTime;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PlatformController {

	@GetMapping("/health")
	public Map<String, Object> health() {
		return Map.of(
			"status", "UP",
			"service", "feature-flag",
			"timestamp", OffsetDateTime.now().toString()
		);
	}

	@GetMapping("/swagger")
	public Map<String, String> swagger() {
		return Map.of(
			"swaggerUi", "/swagger-ui/index.html",
			"openApi", "/v3/api-docs"
		);
	}
}
