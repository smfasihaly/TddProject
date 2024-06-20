package com.tdd.expensetracker.view;

import java.util.List;

import com.tdd.expensetracker.model.Category;

public interface CategoryView {

	void showAllCategory(List<Category> category);

	void categoryAdded(Category category);

	void showError(String id, Category existingCategory);

	void categoryDeleted(String id);

	void categoryUpdated(Category updatedCategory);

}
