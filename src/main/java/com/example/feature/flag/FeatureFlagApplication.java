package com.example.feature.flag;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@Slf4j
@SpringBootApplication
public class FeatureFlagApplication {

	public static void main(String[] args) {
		log.info("Starting Feature Flag Service");
		ConfigurableApplicationContext context = SpringApplication.run(FeatureFlagApplication.class, args);
		log.info(
			"Feature Flag Service started. activeProfiles={}",
			Arrays.toString(context.getEnvironment().getActiveProfiles())
		);
	}

}
