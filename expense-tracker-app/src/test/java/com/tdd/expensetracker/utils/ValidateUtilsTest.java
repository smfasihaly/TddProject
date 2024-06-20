package com.tdd.expensetracker.utils;

import static org.junit.Assert.*;

import java.time.LocalDate;

import org.junit.Test;

public class ValidateUtilsTest {

	@Test
	public void testValidateRequiredStringIfStringisNotEmpty() {

		boolean isValidString = ValidateUtils.validateRequiredString("this is valid string");
		assertTrue(isValidString);
	}

	@Test
	public void testValidateRequiredStringIfStringisEmpty() {

		ValidationException e = assertThrows(ValidationException.class, () -> ValidateUtils.validateRequiredString(""));
		assertEquals("String is required and cannot be null or empty",e.getMessage());
	}

	@Test
	public void testValidateRequiredStringIfStringisNull() {

		ValidationException e = assertThrows(ValidationException.class, () -> ValidateUtils.validateRequiredString(null));
		assertEquals("String is required and cannot be null or empty",e.getMessage());
	
	}

	@Test
	public void testValidateAmountIfAmountisPositive() {

		boolean isValidAmount = ValidateUtils.validateAmount(100);
		assertTrue(isValidAmount);
	}

	@Test
	public void testValidateAmountIfAmountisNegative() {

		ValidationException e = assertThrows(ValidationException.class, () -> ValidateUtils.validateAmount(-100));
		// perform assertions on the thrown exception
		assertEquals("Amount must be greater than zero", e.getMessage());
	}

	@Test
	public void testValidateAmountIfAmountisZero() {

		ValidationException e = assertThrows(ValidationException.class, () -> ValidateUtils.validateAmount(0));
		assertEquals("Amount must be greater than zero", e.getMessage());
	}

	@Test
	public void testIsValidDateIfDateIsValid() {

		boolean isValidDate = ValidateUtils.validateDate(LocalDate.now());
		assertTrue(isValidDate);
	}

	@Test
	public void testValidateDateIfDateIsNull() {


		ValidationException e = assertThrows(ValidationException.class, () -> ValidateUtils.validateDate(null));
		assertEquals("Date is required and cannot be null",e.getMessage());
	
	}

	@Test
	public void testValidateDateIfDateIsInPast() {
		
		boolean isValidDate = ValidateUtils.validateDate(LocalDate.now().minusDays(30));
		assertTrue(isValidDate);
	}

	@Test
	public void testValidateDateIfDateIsInFuture() {

		ValidationException e = assertThrows(ValidationException.class,
				() -> ValidateUtils.validateDate(LocalDate.now().plusDays(1)));
		// perform assertions on the thrown exception
		assertEquals("Date cannot be in the future", e.getMessage());

	}

}
