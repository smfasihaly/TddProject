package com.tdd.expensetracker.utils;

import java.time.LocalDate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Utility class for common validation functions (e.g., string validation, amount validation, date validation)
public class ValidateUtils {

	private static final Logger LOGGER = LogManager.getLogger(ValidateUtils.class);

	// Private constructor to prevent instantiation since this is a utility class
	private ValidateUtils() {
	}

	// Validates that a required string is not null or empty
	// Throws a ValidationException if the string is invalid
	public static boolean validateRequiredString(String string, String fieldName) {
		LOGGER.debug("Validating required string for field: {}", fieldName);

		if (string == null || string.trim().isEmpty()) {
			LOGGER.error("Validation failed: {} is null or empty", fieldName);
			throw new ValidationException(fieldName + " is required and cannot be null or empty");
		}

		LOGGER.debug("Validation passed for field: {}", fieldName);
		return true;
	}

	// Validates that the amount is greater than zero
	// Throws a ValidationException if the amount is invalid
	public static boolean validateAmount(Double amount) {
		LOGGER.debug("Validating amount: {}", amount);

		if (amount <= 0) {
			LOGGER.error("Validation failed: Amount must be greater than zero, but was {}", amount);
			throw new ValidationException("Amount must be greater than zero");
		}

		LOGGER.debug("Validation passed for amount: {}", amount);
		return true;
	}

	// Validates that the date is not null and is not in the future
	// Throws a ValidationException if the date is invalid
	public static boolean validateDate(LocalDate date) {
		LOGGER.debug("Validating date: {}", date);

		if (date == null) {
			LOGGER.error("Validation failed: Date is null");
			throw new ValidationException("Date is required and cannot be null");
		}

		if (date.isAfter(LocalDate.now())) {
			LOGGER.error("Validation failed: Date {} is in the future", date);
			throw new ValidationException("Date cannot be in the future");
		}

		LOGGER.debug("Validation passed for date: {}", date);
		return true;
	}
}