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

	public CategoryController(CategoryView categoryView, CategoryRepository categoryRepository) {
		this.categoryView = categoryView;
		this.categoryRepository = categoryRepository;
		LOGGER.info("CategoryController initialized with CategoryView and CategoryRepository");
	}

	public void allCategory() {
		LOGGER.info("Getting all categories");
		categoryView.showAllCategory(categoryRepository.findAll());
	}

	public synchronized void newCategory(Category category) {
		LOGGER.info("Attempting to create a new category: {}", category);

		if (!validateCategory(category)) {
			LOGGER.warn("Category validation failed: {}", category);
			return;
		}

		Category existingCategory = categoryRepository.findById(category.getId());

		if (existingCategory != null) {
			LOGGER.warn("Category with id {} already exists", category.getId());
			categoryView.showError("Already existing category with id " + category.getId(), existingCategory);
			return;
		}

		existingCategory = categoryRepository.findByName(category.getName());

		if (existingCategory != null) {
			LOGGER.warn("Category with name {} already exists", category.getName());
			categoryView.showError("Already existing category with name " + category.getName(), existingCategory);
			return;
		}

		categoryRepository.save(category);
		categoryView.categoryAdded(category);
		LOGGER.info("New category created successfully: {}", category);
	}

	public synchronized void deleteCategory(Category categoryToDelete) {
		LOGGER.info("Attempting to delete category: {}", categoryToDelete);

		Category existingCategory = categoryRepository.findById(categoryToDelete.getId());

		if (existingCategory == null) {
			LOGGER.warn("Category with id {} does not exist", categoryToDelete.getId());
			categoryView.showErrorCategoryNotFound("Category does not exist with id " + categoryToDelete.getId(),
					categoryToDelete);
			return;
		}

		if (categoryToDelete.getExpenses().size() > 0) {
			LOGGER.warn("Category with id {} cannot be deleted because it has associated expenses",
					categoryToDelete.getId());
			categoryView.showError("Category cannot be deleted. Expenses are associated with it", categoryToDelete);
			return;
		}

		categoryRepository.delete(categoryToDelete);
		categoryView.categoryDeleted(categoryToDelete);
		LOGGER.info("Category deleted successfully: {}", categoryToDelete);

	}

	public synchronized void updateCategory(Category categoryToUpdate) {
		LOGGER.info("Attempting to update category: {}", categoryToUpdate);

		if (!validateCategory(categoryToUpdate)) {
			LOGGER.warn("Category validation failed: {}", categoryToUpdate);
			return;
		}

		Category existingCategory = categoryRepository.findById(categoryToUpdate.getId());

		if (existingCategory == null) {
			LOGGER.warn("Category with id {} does not exist", categoryToUpdate.getId());
			categoryView.showError("Category does not exist with id " + categoryToUpdate.getId(), categoryToUpdate);
			return;
		}

		existingCategory = categoryRepository.findByName(categoryToUpdate.getName());

		if (existingCategory != null && !existingCategory.getId().equals(categoryToUpdate.getId())) {
			LOGGER.warn("Category with name {} already exists", categoryToUpdate.getName());
			categoryView.showError("Already existing category with name " + categoryToUpdate.getName(),
					existingCategory);
			return;
		}

		categoryRepository.update(categoryToUpdate);
		categoryView.categoryUpdated(categoryToUpdate);
		LOGGER.info("Category updated successfully: {}", categoryToUpdate);
	}

	public void getAllExpenses(Category category) {
		LOGGER.info("Getting all expenses for category: {}", category);

		List<Expense> expenses = category.getExpenses();
		if (expenses.size() == 0) {
			LOGGER.warn("No expenses found for category: {}", category);
			categoryView.showError("No Expense created for this category", category);
			return;
		}

		categoryView.getAllExpenses(expenses);
		LOGGER.info("Expenses fetched successfully for category: {}", category);
	}

	private boolean validateCategory(Category categoryToUpdate) {
		LOGGER.debug("Validating category: {}", categoryToUpdate);
		try {
			ValidateUtils.validateRequiredString(categoryToUpdate.getName(), "Name");
			ValidateUtils.validateRequiredString(categoryToUpdate.getDescription(), "Description");
			return true;

		} catch (ValidationException exception) {
			LOGGER.error("Validation error for category: {}", categoryToUpdate, exception);
			categoryView.showError(exception.getMessage(), categoryToUpdate);
			return false;
		}
	}
}
