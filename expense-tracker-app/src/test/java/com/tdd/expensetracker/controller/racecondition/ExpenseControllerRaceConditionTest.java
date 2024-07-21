package com.tdd.expensetracker.controller.racecondition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.tdd.expensetracker.controller.ExpenseController;
import com.tdd.expensetracker.model.Category;
import com.tdd.expensetracker.model.Expense;
import com.tdd.expensetracker.repository.CategoryRepository;
import com.tdd.expensetracker.repository.ExpenseRepository;
import com.tdd.expensetracker.view.ExpenseView;

public class ExpenseControllerRaceConditionTest {

	@Mock
	private ExpenseRepository expenseRepository; // Mock the ExpenseRepository
	@Mock
	private CategoryRepository categoryRepository; // Mock the CategoryRepository
	@Mock
	private ExpenseView expenseView; // Mock the ExpenseView

	@InjectMocks
	private ExpenseController expenseController; // Inject mocks into the ExpenseController

	private AutoCloseable closeable; // Resource to close mocks after tests

	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this); // Initialize mocks
	}

	@After
	public void releaseMocks() throws Exception {
		closeable.close(); // Release mocks after tests
	}

	@Test
	public void testNewExpenseConcurrent() {
		List<Expense> expenses = new ArrayList<>(); // List to act as a fake database

		// Setup existing category mock
		Category existingCategory = new Category("1", "name1", "description1");
		when(categoryRepository.findById("1")).thenReturn(existingCategory);

		// Create a new expense
		Expense expense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);

		// Stub the findById method to simulate repository behavior
		when(expenseRepository.findById(anyString()))
				.thenAnswer(invocation -> expenses.stream().findFirst().orElse(null));

		// Stub the save method to add the expense to the list
		doAnswer(invocation -> {
			expenses.add(expense);
			return null;
		}).when(expenseRepository).save(any(Expense.class));

		// Create and start 10 threads to simulate concurrent access
		List<Thread> threads = IntStream.range(0, 10)
				.mapToObj(i -> new Thread(() -> expenseController.newExpense(expense))).peek(t -> t.start())
				.collect(Collectors.toList());

		// Wait for all threads to finish
		await().atMost(10, TimeUnit.SECONDS).until(() -> threads.stream().noneMatch(t -> t.isAlive()));

		// Verify that only one expense was added to the list
		assertThat(expenses).containsExactly(expense);
	}

	@Test
	public void testDeleteExpenseConcurrent() {
		List<Expense> expenses = new ArrayList<>(); // List to act as a fake database
		List<Expense> deletedExpenses = new ArrayList<>();
		// Create and add an expense to the list
		Category existingCategory = new Category("1", "name1", "description1");
		Expense expense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);
		expenses.add(expense);

		// Setup existing category mock
		when(categoryRepository.findById("1")).thenReturn(existingCategory);

		// Stub the findById method to simulate repository behavior
		when(expenseRepository.findById(anyString()))
				.thenAnswer(invocation -> expenses.stream().findFirst().orElse(null));

		// Stub the delete method to remove the expense from the list
		doAnswer(invocation -> {
			expenses.remove(expense);
			deletedExpenses.add(expense);
			return null;
		}).when(expenseRepository).delete(any(Expense.class));

		// Create and start 10 threads to simulate concurrent access
		List<Thread> threads = IntStream.range(0, 10)
				.mapToObj(i -> new Thread(() -> expenseController.deleteExpense(expense))).peek(t -> t.start())
				.collect(Collectors.toList());

		// Wait for all threads to finish
		await().atMost(10, TimeUnit.SECONDS).until(() -> threads.stream().noneMatch(t -> t.isAlive()));

		// Verify that the list is empty (expense was deleted)
		assertThat(expenses).isEmpty();
		assertThat(deletedExpenses).containsExactly(expense);
		// Verify that the delete method was called exactly once
		verify(expenseRepository, times(1)).delete(any(Expense.class));
	}

	@Test
	public void testUpdateExpenseConcurrent() {
		List<Expense> expenses = new ArrayList<>(); 
		List<Expense> updatedExpenses = new ArrayList<>(); // List to act as a fake database
		
		// Create and add an initial expense to the list
		Category existingCategory = new Category("1", "name1", "description1");
		Expense initialExpense = new Expense("1", 5000d, "testExpense", LocalDate.now(), existingCategory);
		expenses.add(initialExpense);

		// Create an updated expense
		Expense updatedExpense = new Expense("1", 6000d, "updatedExpense", LocalDate.now(), existingCategory);

		// Setup existing category mock
		when(categoryRepository.findById("1")).thenReturn(existingCategory);

		// Stub the findById method to simulate repository behavior
		when(expenseRepository.findById(anyString()))
		.thenAnswer(invocation -> expenses.stream().findFirst().orElse(null));

		// Stub the save method to update the expense in the list
		doAnswer(invocation -> {
			//.. as initial expense is now updated
			expenses.remove(initialExpense);
			updatedExpenses.add(updatedExpense);
			return null;
		}).when(expenseRepository).update(any(Expense.class));

		// Create and start 10 threads to simulate concurrent access
		List<Thread> threads = IntStream.range(0, 10)
				.mapToObj(i -> new Thread(() -> expenseController.updateExpense(updatedExpense))).peek(t -> t.start())
				.collect(Collectors.toList());

		// Wait for all threads to finish
		await().atMost(10, TimeUnit.SECONDS).until(() -> threads.stream().noneMatch(t -> t.isAlive()));

		// Verify that the expense was updated correctly
		assertThat(expenses).isEmpty();
		assertThat(updatedExpenses).containsExactly(updatedExpense);

		// Verify that the save method was called exactly once
		verify(expenseRepository, times(1)).update(updatedExpense);
	}
}
