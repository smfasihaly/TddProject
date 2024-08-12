	package com.tdd.expensetracker.utils;
	
	import java.time.LocalDate;
	
	import org.apache.logging.log4j.LogManager;
	import org.apache.logging.log4j.Logger;
	
	public class ValidateUtils {
	
		private static final Logger LOGGER = LogManager.getLogger(ValidateUtils.class);
	
		private ValidateUtils() {
		}
	
		public static boolean validateRequiredString(String string, String fieldName){
			LOGGER.debug("Validating required string for field: {}", fieldName);
	
			if (string == null || string.trim().isEmpty()) {
				LOGGER.error("Validation failed: {} is null or empty", fieldName);
				throw new ValidationException(fieldName + " is required and cannot be null or empty");
			}
	
			LOGGER.debug("Validation passed for field: {}", fieldName);
			return true;
		}
	
		public static boolean validateAmount(Double amount) {
			LOGGER.debug("Validating amount: {}", amount);
	
			if (amount <= 0) {
				LOGGER.error("Validation failed: Amount must be greater than zero, but was {}", amount);
				throw new ValidationException("Amount must be greater than zero");
			}
	
			LOGGER.debug("Validation passed for amount: {}", amount);
			return true;
		}
	
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
