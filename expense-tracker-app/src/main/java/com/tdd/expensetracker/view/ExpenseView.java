package com.tdd.expensetracker.view;

import java.util.List;

import com.tdd.expensetracker.model.Expense;

public interface ExpenseView {

	void showAllExpense(Object all);

	void showAllExpense(List<Expense> expense);


	void expenseAdded(Expense expense);

	void showError(String message, Expense existingExpense);

	void expenseDeleted(String id);

	void expenseUpdated(Expense existingExpense);


}
