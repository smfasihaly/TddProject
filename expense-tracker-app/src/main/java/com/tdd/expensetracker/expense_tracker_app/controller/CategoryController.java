package com.tdd.expensetracker.expense_tracker_app.controller;

import com.tdd.expensetracker.expense_tracker_app.model.Category;
import com.tdd.expensetracker.expense_tracker_app.repository.CategoryRepository;
import com.tdd.expensetracker.expense_tracker_app.view.CategoryView;

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

	public void newCategory(Category category) {
		// TODO Auto-generated method stub
		Category existingCategory = categoryRepository.findById(category.getId());
		if (existingCategory != null) {
			categoryView.showError("Already existing category with id " + category.getId(), existingCategory);
			return;
		}
		categoryRepository.save(category);
		categoryView.categoryAdded(category);

	}

	public void DeleteCategory(Category categoryToDelete) {
		Category existingCategory = categoryRepository.findById(categoryToDelete.getId());
		if (existingCategory == null) {
			categoryView.showError("Category does not exist with id " + categoryToDelete.getId(), categoryToDelete);
			return;
		}
		categoryRepository.delete(categoryToDelete.getId());
		categoryView.categoryDeleted(categoryToDelete.getId());

	}

	public void updateCategory(Category categoryToUpdate) {
		
		Category existingCategory = categoryRepository.findById(categoryToUpdate.getId());
		if(existingCategory == null) {
			categoryView.showError("Category does not exist with id " + categoryToUpdate.getId(), categoryToUpdate);
			return;
		}
		categoryRepository.update(categoryToUpdate);
		categoryView.categoryUpdated(categoryToUpdate);
	}
}
