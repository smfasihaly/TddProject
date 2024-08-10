package com.tdd.expensetracker.view;

import java.util.List;

import com.tdd.expensetracker.model.Category;
import com.tdd.expensetracker.model.Expense;

public interface ExpenseView {

	void showAllExpense(List<Expense> expense);

	void expenseAdded(Expense expense);

	void showError(String message, Expense expense);

	void expenseDeleted(Expense expense);

	void expenseUpdated(Expense updatedExpense);

	void showAllCategory(List<Category> categories);

	void showErrorExpenseNotFound(String message, Expense expense);


}
