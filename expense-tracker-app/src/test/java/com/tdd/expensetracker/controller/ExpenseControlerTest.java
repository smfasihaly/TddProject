package com.tdd.expensetracker.controller;

import static org.mockito.Mockito.ignoreStubs;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.tdd.expensetracker.model.Category;
import com.tdd.expensetracker.model.Expense;
import com.tdd.expensetracker.repository.CategoryRepository;
import com.tdd.expensetracker.repository.ExpenseRepository;
import com.tdd.expensetracker.view.ExpenseView;

public class ExpenseControlerTest {
	@Mock
	private ExpenseRepository expenseRepository;
	@Mock
	private CategoryRepository categoryRepository;
	@Mock
	private ExpenseView expenseView;

	@InjectMocks
	private ExpenseController expenseController;

	private AutoCloseable closeable;

	@Before
	public void setup() {
		// Initialize mocks
		closeable = MockitoAnnotations.openMocks(this);
	}

	@After
	public void releaseMocks() throws Exception {
		// Release mocks after tests
		closeable.close();
	}

	// Test for fetching all expenses
	@Test
	public void testAllExpense() {
		List<Expense> expense = Arrays.asList(new Expense());
		when(expenseRepository.findAll()).thenReturn(expense);
		expenseController.allExpense();
		verify(expenseView).showAllExpense(expense);
	}

	// Test for adding a new expense when it does not exist
	@Test
	public void testNewExpenseWhenExpenseDoesNotExist() {
		Category existingCategory = new Category("1", "name1", "description1");
		when(categoryRepository.findById("1")).thenReturn(existingCategory);
		Expense expense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);
		when(expenseRepository.findById("1")).thenReturn(null);
		expenseController.newExpense(expense);
		InOrder inOrder = inOrder(expenseRepository, expenseView);
		inOrder.verify(expenseRepository).save(expense);
		inOrder.verify(expenseView).expenseAdded(expense);
	}

	// Test for adding a new expense when it already exists
	@Test
	public void testNewExpenseWhenExpenseAlreadyExists() {
		Category existingCategory = new Category("1", "name1", "description1");
		Expense newExpense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);
		Expense existingExpense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);
		when(expenseRepository.findById("1")).thenReturn(existingExpense);
		expenseController.newExpense(newExpense);
		verify(expenseView).showError("Already existing expense with id 1", existingExpense);
		verifyNoMoreInteractions(ignoreStubs(expenseRepository));
	}

	// Test for adding a new expense when the associated category does not exist
	@Test
	public void testNewExpenseWhenCategoryDoesNotExists() {
		Category existingCategory = new Category("1", "name1", "description1");
		when(categoryRepository.findById("1")).thenReturn(null);
		Expense newExpense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);
		when(expenseRepository.findById("1")).thenReturn(null);
		expenseController.newExpense(newExpense);
		verify(expenseView).showError("Category does not exist with id 1", newExpense);
		verifyNoMoreInteractions(ignoreStubs(expenseRepository));
	}

	// Test for adding a new expense when the category is null
	@Test
	public void testNewExpenseExpenseWhenCategoryIsNull() {
		Expense updatedExpense = new Expense("1", 10000d, "testExpense", LocalDate.now(), null);
		when(expenseRepository.findById("1")).thenReturn(updatedExpense);
		expenseController.newExpense(updatedExpense);
		verify(expenseView).showError("Category cannot be null", updatedExpense);
		verifyNoMoreInteractions(ignoreStubs(expenseRepository));
	}

	// Test for adding a new expense when the description is empty
	@Test
	public void testNewExpenseWhenDescriptionIsEmpty() {
		Category existingCategory = new Category("1", "name1", "description1");
		Expense newExpense = new Expense("1", 10000d, "", LocalDate.now(), existingCategory);
		expenseController.newExpense(newExpense);
		verify(expenseView).showError("Description is required and cannot be null or empty", newExpense);
		verifyNoMoreInteractions(ignoreStubs(expenseRepository));
	}

	// Test for adding a new expense when the amount is zero
	@Test
	public void testNewExpenseWhenAmountIsZero() {
		Category existingCategory = new Category("1", "name1", "description1");
		Expense newExpense = new Expense("1", 0d, "testExpense", LocalDate.now(), existingCategory);
		expenseController.newExpense(newExpense);
		verify(expenseView).showError("Amount must be greater than zero", newExpense);
		verifyNoMoreInteractions(ignoreStubs(expenseRepository));
	}

	// Test for adding a new expense when the amount is negative
	@Test
	public void testNewExpenseWhenAmountIsNegative() {
		Category existingCategory = new Category("1", "name1", "description1");
		Expense newExpense = new Expense("1", -10000d, "testExpense", LocalDate.now(), existingCategory);
		expenseController.newExpense(newExpense);
		verify(expenseView).showError("Amount must be greater than zero", newExpense);
		verifyNoMoreInteractions(ignoreStubs(expenseRepository));
	}

	// Test for adding a new expense when the date is in the future
	@Test
	public void testNewExpenseWhenDateIsInFuture() {
		Category existingCategory = new Category("1", "name1", "description1");
		Expense newExpense = new Expense("1", 10000d, "Description1", LocalDate.now().plusDays(10), existingCategory);
		expenseController.newExpense(newExpense);
		verify(expenseView).showError("Date cannot be in the future", newExpense);
		verifyNoMoreInteractions(ignoreStubs(expenseRepository));
	}

	// Test for adding a new expense when the date is null
	@Test
	public void testNewExpenseWhenDateIsNull() {
		Category existingCategory = new Category("1", "name1", "description1");
		Expense newExpense = new Expense("1", 10000d, "Description1", null, existingCategory);
		expenseController.newExpense(newExpense);
		verify(expenseView).showError("Date is required and cannot be null", newExpense);
		verifyNoMoreInteractions(ignoreStubs(expenseRepository));
	}

	// Test for deleting an expense when it exists
	@Test
	public void testDeleteExpenseWhenExpenseExist() {
		Category existingCategory = new Category("1", "name1", "description1");
		Expense expenseToDelete = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);
		when(expenseRepository.findById("1")).thenReturn(expenseToDelete);
		expenseController.deleteExpense(expenseToDelete);
		InOrder inOrder = inOrder(expenseRepository, expenseView);
		inOrder.verify(expenseRepository).delete(expenseToDelete);
		inOrder.verify(expenseView).expenseDeleted(expenseToDelete);
	}

	// Test for deleting an expense when it does not exist
	@Test
	public void testDeleteExpenseWhenExpenseDoesNotExist() {
		Category existingCategory = new Category("1", "name1", "description1");
		Expense expenseToDelete = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);
		when(expenseRepository.findById("1")).thenReturn(null);
		expenseController.deleteExpense(expenseToDelete);
		verify(expenseView).showErrorExpenseNotFound("Expense does not exist with id 1", expenseToDelete);
		verifyNoMoreInteractions(ignoreStubs(expenseRepository));
	}

	// Test for updating an expense when it exists
	@Test
	public void testUpdateExpenseWhenExpenseExist() {
		Category existingCategory = new Category("1", "name1", "description1");
		when(categoryRepository.findById("1")).thenReturn(existingCategory);
		Expense updatedExpense = new Expense("1", 10000d, "testExpense", LocalDate.now(), existingCategory);
		Expense existingExpense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);
		when(expenseRepository.findById("1")).thenReturn(existingExpense);
		expenseController.updateExpense(updatedExpense);
		InOrder inOrder = inOrder(expenseRepository, expenseView);
		inOrder.verify(expenseRepository).update(updatedExpense);
		inOrder.verify(expenseView).expenseUpdated(updatedExpense);
	}

	// Test for updating an expense when it does not exist
	@Test
	public void testUpdateExpenseWhenExpenseDoesNotExist() {
		Category existingCategory = new Category("1", "name1", "description1");
		Expense updatedExpense = new Expense("1", 10000d, "testExpense", LocalDate.now(), existingCategory);
		when(expenseRepository.findById("1")).thenReturn(null);
		expenseController.updateExpense(updatedExpense);
		verify(expenseView).showError("Expense does not exist with id 1", updatedExpense);
		verifyNoMoreInteractions(ignoreStubs(expenseRepository));
	}

	// Test for updating an expense when the associated category does not exist
	@Test
	public void testUpdateExpenseWhenCategoryDoesNotExist() {
		Category existingCategory = new Category("1", "name1", "description1");
		when(categoryRepository.findById("1")).thenReturn(null);
		Expense updatedExpense = new Expense("1", 10000d, "testExpense", LocalDate.now(), existingCategory);
		when(expenseRepository.findById("1")).thenReturn(updatedExpense);
		expenseController.updateExpense(updatedExpense);
		verify(expenseView).showError("Category does not exist with id 1", updatedExpense);
		verifyNoMoreInteractions(ignoreStubs(expenseRepository));
	}

	// Test for updating an expense when the category is null
	@Test
	public void testUpdateExpenseWhenCategoryIsNull() {
		Expense updatedExpense = new Expense("1", 10000d, "testExpense", LocalDate.now(), null);
		when(expenseRepository.findById("1")).thenReturn(updatedExpense);
		expenseController.updateExpense(updatedExpense);
		verify(expenseView).showError("Category cannot be null", updatedExpense);
		verifyNoMoreInteractions(ignoreStubs(expenseRepository));
	}

	// Test for updating an expense when the amount is zero
	@Test
	public void testUpdateExpenseWhenAmountIsZero() {
		Category existingCategory = new Category("1", "name1", "description1");
		when(categoryRepository.findById("1")).thenReturn(existingCategory);
		Expense updatedExpense = new Expense("1", 0d, "testExpense", LocalDate.now(), existingCategory);
		when(expenseRepository.findById("1")).thenReturn(updatedExpense);
		expenseController.updateExpense(updatedExpense);
		verify(expenseView).showError("Amount must be greater than zero", updatedExpense);
		verifyNoMoreInteractions(ignoreStubs(expenseRepository));
	}

	// Test for updating an expense when the amount is negative
	@Test
	public void testUpdateExpenseWhenAmountIsNegative() {
		Category existingCategory = new Category("1", "name1", "description1");
		when(categoryRepository.findById("1")).thenReturn(existingCategory);
		Expense updatedExpense = new Expense("1", -10000d, "testExpense", LocalDate.now(), existingCategory);
		when(expenseRepository.findById("1")).thenReturn(updatedExpense);
		expenseController.updateExpense(updatedExpense);
		verify(expenseView).showError("Amount must be greater than zero", updatedExpense);
		verifyNoMoreInteractions(ignoreStubs(expenseRepository));
	}

	// Test for updating an expense when the description is empty
	@Test
	public void testUpdateExpenseWhenDescriptionIsEmpty() {
		Category existingCategory = new Category("1", "name1", "description1");
		Expense updatedExpense = new Expense("1", 10000d, "", LocalDate.now(), existingCategory);
		expenseController.updateExpense(updatedExpense);
		verify(expenseView).showError("Description is required and cannot be null or empty", updatedExpense);
		verifyNoMoreInteractions(ignoreStubs(expenseRepository));
	}

	// Test for updating an expense when the date is in the future
	@Test
	public void testUpdateExpenseWhenDateIsInFuture() {
		Category existingCategory = new Category("1", "name1", "description1");
		Expense updatedExpense = new Expense("1", 10000d, "Description1", LocalDate.now().plusDays(10),
				existingCategory);
		expenseController.updateExpense(updatedExpense);
		verify(expenseView).showError("Date cannot be in the future", updatedExpense);
		verifyNoMoreInteractions(ignoreStubs(expenseRepository));
	}

	// Test for updating an expense when the date is null
	@Test
	public void testUpdateExpenseWhenDateIsNull() {
		Category existingCategory = new Category("1", "name1", "description1");
		Expense updatedExpense = new Expense("1", 10000d, "Description1", null, existingCategory);
		expenseController.updateExpense(updatedExpense);
		verify(expenseView).showError("Date is required and cannot be null", updatedExpense);
		verifyNoMoreInteractions(ignoreStubs(expenseRepository));
	}

	// Test for fetching all categories
	@Test
	public void testAllCategory() {
		List<Category> categories = Arrays.asList(new Category());
		when(categoryRepository.findAll()).thenReturn(categories);
		expenseController.allCategory();
		verify(expenseView).showAllCategory(categories);
	}
}