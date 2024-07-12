package com.tdd.expensetracker.utils;

import java.time.LocalDate;

public class ValidateUtils {

	private ValidateUtils() {
	}

	public static boolean validateRequiredString(String string,String fieldName ) throws ValidationException {

		if (string == null || string.trim().isEmpty()) {
			throw new ValidationException(fieldName +" is required and cannot be null or empty");
		}

		return true;
	}


	public static boolean validateAmount(Double amount) throws ValidationException {

		if (amount <= 0) {
			throw new ValidationException("Amount must be greater than zero");
		}

		return true;
	}

	public static boolean validateDate(LocalDate date) throws ValidationException {

		if (date == null) {
			throw new ValidationException("Date is required and cannot be null");
		}

		if (date.isAfter(LocalDate.now())) {
			throw new ValidationException("Date cannot be in the future");
		}

		return true;
	}
}