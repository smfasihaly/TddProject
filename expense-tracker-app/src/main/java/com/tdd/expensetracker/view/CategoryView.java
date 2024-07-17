package com.tdd.expensetracker.view;

import java.util.List;

import com.tdd.expensetracker.model.Category;
import com.tdd.expensetracker.model.Expense;

public interface CategoryView {

	void showAllCategory(List<Category> category);

	void categoryAdded(Category category);

	void showError(String message, Category category);

	void categoryDeleted(Category categoryToDelete);

	void categoryUpdated(Category categoryToUpdate);

	void getAllExpenses(List<Expense> expenses);
	


}
