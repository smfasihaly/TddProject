package com.tdd.expensetracker.expense_tracker_app.view;

import java.util.List;

import com.tdd.expensetracker.expense_tracker_app.model.Category;

public interface CategoryView {

	void showAllCategory(List<Category> category);

	void categoryAdded(Category category);

	void showError(String id, Category existingCategory);

	void categoryDeleted(String id);

	void categoryUpdated(Category updatedCategory);

}
