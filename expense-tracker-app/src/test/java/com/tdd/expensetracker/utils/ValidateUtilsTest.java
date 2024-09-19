package com.tdd.expensetracker.utils;

import java.time.LocalDate;

import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

public class ValidateUtilsTest {

	// Test for validating a non-empty string
	@Test
	public void testValidateRequiredStringIfStringisNotEmpty() {
		boolean isValidString = ValidateUtils.validateRequiredString("this is valid string", "String");
		assertThat(isValidString).isTrue();
	}

	// Test for validating an empty string (should throw ValidationException)
	@Test
	public void testValidateRequiredStringIfStringisEmpty() {
		assertThatThrownBy(() -> ValidateUtils.validateRequiredString("", "String"))
				.isInstanceOf(ValidationException.class).hasMessage("String is required and cannot be null or empty");
	}

	// Test for validating a null string (should throw ValidationException)
	@Test
	public void testValidateRequiredStringIfStringisNull() {
		assertThatThrownBy(() -> ValidateUtils.validateRequiredString(null, "String"))
				.isInstanceOf(ValidationException.class).hasMessage("String is required and cannot be null or empty");
	}

	// Test for validating a positive amount
	@Test
	public void testValidateAmountIfAmountisPositive() {
		boolean isValidAmount = ValidateUtils.validateAmount(100.0);
		assertThat(isValidAmount).isTrue();
	}

	// Test for validating a negative amount (should throw ValidationException)
	@Test
	public void testValidateAmountIfAmountisNegative() {
		assertThatThrownBy(() -> ValidateUtils.validateAmount(-100.0)).isInstanceOf(ValidationException.class)
				.hasMessage("Amount must be greater than zero");
	}

	// Test for validating an amount of zero (should throw ValidationException)
	@Test
	public void testValidateAmountIfAmountisZero() {
		assertThatThrownBy(() -> ValidateUtils.validateAmount(0d)).isInstanceOf(ValidationException.class)
				.hasMessage("Amount must be greater than zero");
	}

	// Test for validating today's date (should pass)
	@Test
	public void testIsValidDateIfDateIsValid() {
		boolean isValidDate = ValidateUtils.validateDate(LocalDate.now());
		assertThat(isValidDate).isTrue();
	}

	// Test for validating a null date (should throw ValidationException)
	@Test
	public void testValidateDateIfDateIsNull() {
		assertThatThrownBy(() -> ValidateUtils.validateDate(null)).isInstanceOf(ValidationException.class)
				.hasMessage("Date is required and cannot be null");
	}

	// Test for validating a past date (should pass)
	@Test
	public void testValidateDateIfDateIsInPast() {
		boolean isValidDate = ValidateUtils.validateDate(LocalDate.now().minusDays(30));
		assertThat(isValidDate).isTrue();
	}

	// Test for validating a future date (should throw ValidationException)
	@Test
	public void testValidateDateIfDateIsInFuture() {
		LocalDate futureDate = LocalDate.now().plusDays(1);
		assertThatThrownBy(() -> ValidateUtils.validateDate(futureDate)).isInstanceOf(ValidationException.class)
				.hasMessage("Date cannot be in the future");
	}
}