package com.tdd.expensetracker.controller;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.tdd.expensetracker.model.Category;
import com.tdd.expensetracker.model.Expense;
import com.tdd.expensetracker.repository.CategoryRepository;
import com.tdd.expensetracker.utils.ValidateUtils;
import com.tdd.expensetracker.utils.ValidationException;
import com.tdd.expensetracker.view.CategoryView;

public class CategoryController {

	private static final Logger LOGGER = LogManager.getLogger(CategoryController.class);

	private CategoryView categoryView;
	private CategoryRepository categoryRepository;

	// Constructor to initialize CategoryController with CategoryView and
	// CategoryRepository
	public CategoryController(CategoryView categoryView, CategoryRepository categoryRepository) {
		this.categoryView = categoryView;
		this.categoryRepository = categoryRepository;
		LOGGER.info("CategoryController initialized with CategoryView and CategoryRepository");
	}

	// Fetches and displays all categories from the repository
	public void allCategory() {
		LOGGER.info("Getting all categories");
		categoryView.showAllCategory(categoryRepository.findAll());
	}

	// Adds a new category if validation passes and category doesn't already exist
	public synchronized void newCategory(Category category) {
		LOGGER.info("Attempting to create a new category: {}", category);

		// Validate the category input
		if (!validateCategory(category)) {
			LOGGER.warn("Category validation failed: {}", category);
			return;
		}

		// Check if a category with the same ID exists
		Category existingCategory = categoryRepository.findById(category.getId());
		if (existingCategory != null) {
			LOGGER.warn("Category with id {} already exists", category.getId());
			categoryView.showError("Already existing category with id " + category.getId(), existingCategory);
			return;
		}

		// Check if a category with the same name exists
		existingCategory = categoryRepository.findByName(category.getName());
		if (existingCategory != null) {
			LOGGER.warn("Category with name {} already exists", category.getName());
			categoryView.showError("Already existing category with name " + category.getName(), existingCategory);
			return;
		}

		// Save the new category and notify the view
		categoryRepository.save(category);
		categoryView.categoryAdded(category);
		LOGGER.info("New category created successfully: {}", category);
	}

	// Deletes a category if it has no associated expenses
	public synchronized void deleteCategory(Category categoryToDelete) {
		LOGGER.info("Attempting to delete category: {}", categoryToDelete);

		// Check if the category exists in the repository
		Category existingCategory = categoryRepository.findById(categoryToDelete.getId());
		if (existingCategory == null) {
			LOGGER.warn("Category with id {} does not exist", categoryToDelete.getId());
			categoryView.showErrorCategoryNotFound("Category does not exist with id " + categoryToDelete.getId(),
					categoryToDelete);
			return;
		}

		// Check if the category has associated expenses, which would prevent deletion
		if (!categoryToDelete.getExpenses().isEmpty()) {
			LOGGER.warn("Category with id {} cannot be deleted because it has associated expenses",
					categoryToDelete.getId());
			categoryView.showError("Category cannot be deleted. Expenses are associated with it", categoryToDelete);
			return;
		}

		// Delete the category and notify the view
		categoryRepository.delete(categoryToDelete);
		categoryView.categoryDeleted(categoryToDelete);
		LOGGER.info("Category deleted successfully: {}", categoryToDelete);

	}

	// Updates an existing category after validation
	public synchronized void updateCategory(Category categoryToUpdate) {
		LOGGER.info("Attempting to update category: {}", categoryToUpdate);

		// Validate the category data
		if (!validateCategory(categoryToUpdate)) {
			LOGGER.warn("Category validation failed: {}", categoryToUpdate);
			return;
		}

		// Check if the category exists by ID
		Category existingCategory = categoryRepository.findById(categoryToUpdate.getId());
		if (existingCategory == null) {
			LOGGER.warn("Category with id {} does not exist", categoryToUpdate.getId());
			categoryView.showError("Category does not exist with id " + categoryToUpdate.getId(), categoryToUpdate);
			return;
		}

		// Check if a different category with the same name exists
		existingCategory = categoryRepository.findByName(categoryToUpdate.getName());
		if (existingCategory != null && !existingCategory.getId().equals(categoryToUpdate.getId())) {
			LOGGER.warn("Category with name {} already exists", categoryToUpdate.getName());
			categoryView.showError("Already existing category with name " + categoryToUpdate.getName(),
					existingCategory);
			return;
		}

		// Update the category and notify the view
		categoryRepository.update(categoryToUpdate);
		categoryView.categoryUpdated(categoryToUpdate);
		LOGGER.info("Category updated successfully: {}", categoryToUpdate);
	}

	// Fetches and displays all expenses for a given category
	public void getAllExpenses(Category category) {
		LOGGER.info("Getting all expenses for category: {}", category);

		// Retrieve the list of expenses associated with the category
		List<Expense> expenses = category.getExpenses();
		if (expenses.isEmpty()) {
			LOGGER.warn("No expenses found for category: {}", category);
			categoryView.showError("No Expense created for this category", category);
			return;
		}

		// Display the expenses in the view
		categoryView.getAllExpenses(expenses);
		LOGGER.info("Expenses fetched successfully for category: {}", category);
	}

	// Validates category data before any save/update operation
	private boolean validateCategory(Category categoryToUpdate) {
		LOGGER.debug("Validating category: {}", categoryToUpdate);

		try {
			// Check if the category name and description are valid strings
			ValidateUtils.validateRequiredString(categoryToUpdate.getName(), "Name");
			ValidateUtils.validateRequiredString(categoryToUpdate.getDescription(), "Description");
			return true;
		} catch (ValidationException exception) {
			// Log and show validation errors in the view
			LOGGER.error("Validation error for category: {}", categoryToUpdate, exception);
			categoryView.showError(exception.getMessage(), categoryToUpdate);
			return false;
		}
	}
}
