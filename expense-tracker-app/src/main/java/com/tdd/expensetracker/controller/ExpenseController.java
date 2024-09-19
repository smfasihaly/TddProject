package com.tdd.expensetracker.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tdd.expensetracker.model.Category;
import com.tdd.expensetracker.model.Expense;
import com.tdd.expensetracker.repository.CategoryRepository;
import com.tdd.expensetracker.repository.ExpenseRepository;
import com.tdd.expensetracker.utils.ValidateUtils;
import com.tdd.expensetracker.utils.ValidationException;
import com.tdd.expensetracker.view.ExpenseView;

public class ExpenseController {

	private static final Logger LOGGER = LogManager.getLogger(ExpenseController.class);

	private ExpenseView expenseView;
	private ExpenseRepository expenseRepository;
	private CategoryRepository categoryRepository;

	// Constructor to initialize ExpenseController with view, expense repository,
	// and category repository
	public ExpenseController(ExpenseView expenseView, ExpenseRepository expenseRepository,
			CategoryRepository categoryRepository) {
		this.expenseView = expenseView;
		this.expenseRepository = expenseRepository;
		this.categoryRepository = categoryRepository;
		LOGGER.info("ExpenseController initialized with ExpenseView, ExpenseRepository, and CategoryRepository");
	}

	// Fetches and displays all expenses from the repository
	public void allExpense() {
		LOGGER.info("Getting all expenses");
		expenseView.showAllExpense(expenseRepository.findAll());
	}

	// Adds a new expense after validation and checks for existing expense or
	// category
	public synchronized void newExpense(Expense expense) {
		LOGGER.info("Attempting to create a new expense: {}", expense);

		// Validate the expense data
		boolean isValid = validateExpense(expense);
		if (!isValid) {
			LOGGER.warn("Expense validation failed: {}", expense);
			return;
		}

		// Check if the expense with the same ID already exists
		Expense existingExpense = expenseRepository.findById(expense.getId());
		if (existingExpense != null) {
			LOGGER.warn("Expense with id {} already exists", expense.getId());
			expenseView.showError("Already existing expense with id " + expense.getId(), existingExpense);
			return;
		}

		// Check if the category for the expense exists
		String newExpenseCategoryId = (expense.getCategory()).getId();
		Category existingCategory = categoryRepository.findById(newExpenseCategoryId);
		if (existingCategory == null) {
			LOGGER.warn("Category with id {} does not exist", newExpenseCategoryId);
			expenseView.showError("Category does not exist with id " + newExpenseCategoryId, expense);
			return;
		}

		// Save the expense and associate it with the category
		expenseRepository.save(expense);
		existingCategory.getExpenses().add(expense);
		expenseView.expenseAdded(expense);
		LOGGER.info("New expense created successfully: {}", expense);
	}

	// Deletes an expense if it exists in the repository
	public synchronized void deleteExpense(Expense expenseToDelete) {
		LOGGER.info("Attempting to delete expense: {}", expenseToDelete);

		// Check if the expense exists
		Expense existingExpense = expenseRepository.findById(expenseToDelete.getId());
		if (existingExpense == null) {
			LOGGER.warn("Expense with id {} does not exist", expenseToDelete.getId());
			this.expenseView.showErrorExpenseNotFound("Expense does not exist with id " + expenseToDelete.getId(),
					expenseToDelete);
			return;
		}

		// Delete the expense and notify the view
		expenseRepository.delete(expenseToDelete);
		expenseView.expenseDeleted(expenseToDelete);
		LOGGER.info("Expense deleted successfully: {}", expenseToDelete);
	}

	// Updates an existing expense after validation
	public synchronized void updateExpense(Expense updatedExpense) {
		LOGGER.info("Attempting to update expense: {}", updatedExpense);

		// Validate the expense data
		boolean isValid = validateExpense(updatedExpense);
		if (!isValid) {
			LOGGER.warn("Expense validation failed: {}", updatedExpense);
			return;
		}

		// Check if the expense exists by ID
		Expense existingExpense = expenseRepository.findById(updatedExpense.getId());
		if (existingExpense == null) {
			LOGGER.warn("Expense with id {} does not exist", updatedExpense.getId());
			this.expenseView.showError("Expense does not exist with id " + updatedExpense.getId(), updatedExpense);
			return;
		}

		// Check if the category for the expense exists
		String updatedExpenseCategoryId = (updatedExpense.getCategory()).getId();
		Category existingCategory = categoryRepository.findById(updatedExpenseCategoryId);
		if (existingCategory == null) {
			LOGGER.warn("Category with id {} does not exist", updatedExpenseCategoryId);
			expenseView.showError("Category does not exist with id " + updatedExpenseCategoryId, updatedExpense);
			return;
		}

		// Update the expense in the repository and notify the view
		expenseRepository.update(updatedExpense);
		expenseView.expenseUpdated(updatedExpense);
		LOGGER.info("Expense updated successfully: {}", updatedExpense);
	}

	// Fetches and displays all categories from the repository
	public void allCategory() {
		LOGGER.info("Getting all categories");
		expenseView.showAllCategory(categoryRepository.findAll());
	}

	// Validates the expense before saving or updating it
	private boolean validateExpense(Expense expense) {
		LOGGER.debug("Validating expense: {}", expense);

		try {
			// Validate the amount, date, and description of the expense
			ValidateUtils.validateAmount(expense.getAmount());
			ValidateUtils.validateDate(expense.getDate());
			ValidateUtils.validateRequiredString(expense.getDescription(), "Description");
		} catch (ValidationException exception) {
			// Log validation errors and display them in the view
			LOGGER.error("Validation error for expense: {}", expense, exception);
			expenseView.showError(exception.getMessage(), expense);
			return false;
		}

		// Ensure the expense has a valid category
		if (expense.getCategory() == null) {
			LOGGER.warn("Category cannot be null for expense: {}", expense);
			expenseView.showError("Category cannot be null", expense);
			return false;
		}
		return true;
	}
}
