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

	public ExpenseController(ExpenseView expenseView, ExpenseRepository expenseRepository,
			CategoryRepository categoryRepository) {
		this.expenseView = expenseView;
		this.expenseRepository = expenseRepository;
		this.categoryRepository = categoryRepository;
		LOGGER.info("ExpenseController initialized with ExpenseView, ExpenseRepository, and CategoryRepository");
	}

	public void allExpense() {
		LOGGER.info("Getting all expenses");
		expenseView.showAllExpense(expenseRepository.findAll());
	}

	public synchronized void newExpense(Expense expense) {
		LOGGER.info("Attempting to create a new expense: {}", expense);

		boolean isValid = validateExpense(expense);

		if (!isValid) {
			LOGGER.warn("Expense validation failed: {}", expense);
			return;
		}

		Expense existingExpense = expenseRepository.findById(expense.getId());

		if (existingExpense != null) {
			LOGGER.warn("Expense with id {} already exists", expense.getId());
			expenseView.showError("Already existing expense with id " + expense.getId(), existingExpense);
			return;
		}

		String newExpenseCategoryId = (expense.getCategory()).getId();
		Category existingCategory = categoryRepository.findById(newExpenseCategoryId);

		if (existingCategory == null) {
			LOGGER.warn("Category with id {} does not exist", newExpenseCategoryId);
			expenseView.showError("Category does not exist with id " + newExpenseCategoryId, expense);
			return;
		}

		expenseRepository.save(expense);
		existingCategory.getExpenses().add(expense);
		expenseView.expenseAdded(expense);
		LOGGER.info("New expense created successfully: {}", expense);
	}

	public synchronized void deleteExpense(Expense expenseToDelete) {
		LOGGER.info("Attempting to delete expense: {}", expenseToDelete);

		Expense existingExpense = expenseRepository.findById(expenseToDelete.getId());

		if (existingExpense == null) {
			LOGGER.warn("Expense with id {} does not exist", expenseToDelete.getId());
			this.expenseView.showErrorExpenseNotFound("Expense does not exist with id " + expenseToDelete.getId(),
					expenseToDelete);
			return;
		}

		expenseRepository.delete(expenseToDelete);
		expenseView.expenseDeleted(expenseToDelete);
		LOGGER.info("Expense deleted successfully: {}", expenseToDelete);
	}

	public synchronized void updateExpense(Expense updatedExpense) {
		LOGGER.info("Attempting to update expense: {}", updatedExpense);

		boolean isValid = validateExpense(updatedExpense);

		if (!isValid) {
			LOGGER.warn("Expense validation failed: {}", updatedExpense);
			return;
		}

		Expense existingExpense = expenseRepository.findById(updatedExpense.getId());

		if (existingExpense == null) {
			LOGGER.warn("Expense with id {} does not exist", updatedExpense.getId());
			this.expenseView.showError("Expense does not exist with id " + updatedExpense.getId(), updatedExpense);
			return;
		}

		String updatedExpenseCategoryId = (updatedExpense.getCategory()).getId();
		Category existingCategory = categoryRepository.findById(updatedExpenseCategoryId);

		if (existingCategory == null) {
			LOGGER.warn("Category with id {} does not exist", updatedExpenseCategoryId);
			expenseView.showError("Category does not exist with id " + updatedExpenseCategoryId, updatedExpense);
			return;
		}

		expenseRepository.update(updatedExpense);
		expenseView.expenseUpdated(updatedExpense);
		LOGGER.info("Expense updated successfully: {}", updatedExpense);
	}

	public void allCategory() {
		LOGGER.info("Getting all categories");
		expenseView.showAllCategory(categoryRepository.findAll());
	}

	private boolean validateExpense(Expense expense) {
		LOGGER.debug("Validating expense: {}", expense);
		try {
			ValidateUtils.validateAmount(expense.getAmount());
			ValidateUtils.validateDate(expense.getDate());
			ValidateUtils.validateRequiredString(expense.getDescription(), "Description");
		} catch (ValidationException exception) {
			LOGGER.error("Validation error for expense: {}", expense, exception);
			expenseView.showError(exception.getMessage(), expense);
			return false;
		}
		if (expense.getCategory() == null) {
			LOGGER.warn("Category cannot be null for expense: {}", expense);
			expenseView.showError("Category cannot be null", expense);
			return false;
		}
		return true;
	}
}
