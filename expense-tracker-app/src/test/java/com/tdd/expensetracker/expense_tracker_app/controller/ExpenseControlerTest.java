package com.tdd.expensetracker.expense_tracker_app.controller;

import static org.mockito.Mockito.*;

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

import com.tdd.expensetracker.expense_tracker_app.model.Category;
import com.tdd.expensetracker.expense_tracker_app.model.Expense;
import com.tdd.expensetracker.expense_tracker_app.repository.ExpenseRepository;
import com.tdd.expensetracker.expense_tracker_app.view.ExpenseView;

public class ExpenseControlerTest {
	@Mock
	private ExpenseRepository expenseRepository;
	@Mock
	private ExpenseView expenseView;

	@InjectMocks
	private ExpenseController expenseController;

	private AutoCloseable closeable;

	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);
	}

	@After
	public void releaseMocks() throws Exception {
		closeable.close();
	}

	@Test
	public void testAllExpense() {
		List<Expense> expense = Arrays.asList(new Expense());
		when(expenseRepository.findAll()).thenReturn(expense);
		expenseController.allExpense();
		verify(expenseView).showAllExpense(expense);
	}

	@Test
	public void testNewExpenseWhenExpenseDoesNotAlreadyExist() {

		Category existingCategory = new Category("1", "name1","description1");
		Expense expense = new Expense("1", 5000, "testExpense", LocalDate.now(),existingCategory);
		when(expenseRepository.findById("1")).thenReturn(null);
		expenseController.newExpense(expense);
		InOrder inOrder = inOrder(expenseRepository, expenseView);
		inOrder.verify(expenseRepository).save(expense);
		inOrder.verify(expenseView).expenseAdded(expense);
	}

	@Test
	public void testNewExpenseWhenExpenseAlreadyExists() {

		Category existingCategory = new Category("1", "name1","description1");
		Expense newExpense = new Expense("1", 5000, "testExpense", LocalDate.now(),existingCategory);
		Expense existingExpense = new Expense("1", 5000, "testExpense", LocalDate.now(),existingCategory);
		when(expenseRepository.findById("1")).thenReturn(existingExpense);
		expenseController.newExpense(newExpense);
		verify(expenseView).showError("Already existing expense with id 1", existingExpense);
		verifyNoMoreInteractions(ignoreStubs(expenseRepository));

	}

	@Test
	public void testDeleteExpenseWhenExist() {
		Category existingCategory = new Category("1", "name1","description1");
		Expense expenseToDelete = new Expense("1", 5000, "testExpense", LocalDate.now(),existingCategory);
		when(expenseRepository.findById("1")).thenReturn(expenseToDelete);
		expenseController.DeleteExpense(expenseToDelete);
		InOrder inOrder = inOrder(expenseRepository, expenseView);
		inOrder.verify(expenseRepository).delete("1");
		inOrder.verify(expenseView).expenseDeleted("1");
	}

	@Test
	public void testDeleteExpenseWhenDoesNotExist() {
		Category existingCategory = new Category("1", "name1","description1");
		Expense expenseToDelete = new Expense("1", 5000, "testExpense", LocalDate.now(),existingCategory);
		when(expenseRepository.findById("1")).thenReturn(null);
		expenseController.DeleteExpense(expenseToDelete);
		verify(expenseView).showError("Expense does not exist with id 1", expenseToDelete);
		verifyNoMoreInteractions(ignoreStubs(expenseRepository));
	}

	@Test
	public void testUpdateExpenseWhenExist() {
		Category existingCategory = new Category("1", "name1","description1");
		Expense updatedExpense = new Expense("1", 10000, "testExpense", LocalDate.now(),existingCategory);
		Expense existingExpense = new Expense("1", 5000, "testExpense", LocalDate.now(),existingCategory);
		when(expenseRepository.findById("1")).thenReturn(existingExpense);
		expenseController.updateExpense(updatedExpense);
		InOrder inOrder = inOrder(expenseRepository, expenseView);
		inOrder.verify(expenseRepository).update(updatedExpense);
		inOrder.verify(expenseView).expenseUpdated(updatedExpense);
	}

	@Test
	public void testUpdateExpenseWhenDoesNotExist() {
		Category existingCategory = new Category("1", "name1","description1");
		Expense updatedExpense = new Expense("1", 10000, "testExpense", LocalDate.now(),existingCategory);
		when(expenseRepository.findById("1")).thenReturn(null);
		expenseController.updateExpense(updatedExpense);
		verify(expenseView).showError("Expense does not exist with id 1", updatedExpense);
		verifyNoMoreInteractions(ignoreStubs(expenseRepository));

	}
	
}
