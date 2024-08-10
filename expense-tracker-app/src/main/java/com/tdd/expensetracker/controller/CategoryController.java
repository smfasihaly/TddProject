package com.tdd.expensetracker.controller;

import java.util.List;

import com.tdd.expensetracker.model.Category;
import com.tdd.expensetracker.model.Expense;
import com.tdd.expensetracker.repository.CategoryRepository;
import com.tdd.expensetracker.utils.ValidateUtils;
import com.tdd.expensetracker.utils.ValidationException;
import com.tdd.expensetracker.view.CategoryView;

public class CategoryController {

	private CategoryView categoryView;
	private CategoryRepository categoryRepository;

	public CategoryController(CategoryView categoryView, CategoryRepository categoryRepository) {

		this.categoryView = categoryView;
		this.categoryRepository = categoryRepository;
	}

	public void allCategory() {

		categoryView.showAllCategory(categoryRepository.findAll());
	}

	public synchronized void newCategory(Category category) {

		if (!validateCategory(category)) {
			return;
		}

		Category existingCategory = categoryRepository.findById(category.getId());

		if (existingCategory != null) {
			categoryView.showError("Already existing category with id " + category.getId(), existingCategory);
			return;
		}
		
		existingCategory = categoryRepository.findByName(category.getName());

		if (existingCategory != null) {
			categoryView.showError("Already existing category with name " + category.getName(), existingCategory);
			return;
		}

		categoryRepository.save(category);
		categoryView.categoryAdded(category);
	}

	public synchronized void deleteCategory(Category categoryToDelete) {

		Category existingCategory = categoryRepository.findById(categoryToDelete.getId());

		if (existingCategory == null) {
			categoryView.showErrorCategoryNotFound("Category does not exist with id " + categoryToDelete.getId(), categoryToDelete);
			return;
		}
		
		if(categoryToDelete.getExpenses().size()>0) {
			categoryView.showError("Category cannot be deleted. Expenses are associated with it",categoryToDelete);
			return;
		}

		
		categoryRepository.delete(categoryToDelete);
		categoryView.categoryDeleted(categoryToDelete);

	}

	public synchronized void updateCategory(Category categoryToUpdate) {

		if (!validateCategory(categoryToUpdate)) {
			return;
		}

		Category existingCategory = categoryRepository.findById(categoryToUpdate.getId());

		if (existingCategory == null) {
			categoryView.showError("Category does not exist with id " + categoryToUpdate.getId(), categoryToUpdate);
			return;
		}
	
		existingCategory = categoryRepository.findByName(categoryToUpdate.getName());

		if (existingCategory != null && !existingCategory.getId().equals(categoryToUpdate.getId())) {
			categoryView.showError("Already existing category with name " + categoryToUpdate.getName(), existingCategory);
			return;
		}

		categoryRepository.update(categoryToUpdate);
		categoryView.categoryUpdated(categoryToUpdate);
	}
	 
	public void getAllExpenses(Category category) {
		 
		List<Expense> expenses = category.getExpenses();
		if(expenses.size() == 0 ) {
			categoryView.showError("No Expense created for this category", category);
			return;
		}
		
		categoryView.getAllExpenses(expenses);
	}
	/**
	 * @param categoryToUpdate
	 */
	private boolean validateCategory(Category categoryToUpdate) {
		try {
			ValidateUtils.validateRequiredString(categoryToUpdate.getName(), "Name");
			ValidateUtils.validateRequiredString(categoryToUpdate.getDescription(), "Description");
			return true;

		} catch (ValidationException exception) {
			categoryView.showError(exception.getMessage(), categoryToUpdate);
			return false;
		}
	}
}
