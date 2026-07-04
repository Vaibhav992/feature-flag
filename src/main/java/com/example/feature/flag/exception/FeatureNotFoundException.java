package com.example.feature.flag.exception;

public class FeatureNotFoundException extends RuntimeException {

	public FeatureNotFoundException(String message) {
		super(message);
	}
}
