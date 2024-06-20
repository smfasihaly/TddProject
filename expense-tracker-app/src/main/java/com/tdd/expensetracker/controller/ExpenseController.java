package com.tdd.expensetracker.controller;


import com.tdd.expensetracker.model.Category;
import com.tdd.expensetracker.model.Expense;
import com.tdd.expensetracker.repository.CategoryRepository;
import com.tdd.expensetracker.repository.ExpenseRepository;
import com.tdd.expensetracker.view.ExpenseView;

public class ExpenseController {

	private ExpenseView expenseView;
	private ExpenseRepository expenseRepository;
	private CategoryRepository categoryRepository;

	public ExpenseController(ExpenseView expenseView, ExpenseRepository expenseRepository) {

		this.expenseView = expenseView;
		this.expenseRepository = expenseRepository;
	}

	public void allExpense() {

		expenseView.showAllExpense(expenseRepository.findAll());
	}

	public void newExpense(Expense expense) {

		Expense existingExpense = expenseRepository.findById(expense.getId());

		if (existingExpense != null) {
			expenseView.showError("Already existing expense with id " + expense.getId(), existingExpense);
			return;
		}
		
		
		String newExpenseCategoryId = (expense.getCategory()).getId();
		Category existingCategory = categoryRepository.findById(newExpenseCategoryId);
		
		if (existingCategory == null) {
			expenseView.showError("Category does not exist with id " + newExpenseCategoryId, expense);
			return;
		}

		expenseRepository.save(expense);
		expenseView.expenseAdded(expense);

	}

	public void DeleteExpense(Expense expenseToDelete) {

		Expense existingExpense = expenseRepository.findById(expenseToDelete.getId());

		if (existingExpense == null) {
			this.expenseView.showError("Expense does not exist with id " + expenseToDelete.getId(), expenseToDelete);
			return;
		}

		expenseRepository.delete(expenseToDelete.getId());
		expenseView.expenseDeleted(expenseToDelete.getId());
	}

	public void updateExpense(Expense updatedExpense) {

		Expense existingExpense = expenseRepository.findById(updatedExpense.getId());

		if (existingExpense == null) {
			this.expenseView.showError("Expense does not exist with id " + updatedExpense.getId(), updatedExpense);
			return;
		}
		
		String updatedExpenseCategoryId = (updatedExpense.getCategory()).getId();
		Category existingCategory = categoryRepository.findById(updatedExpenseCategoryId);
		
		if (existingCategory == null) {
			expenseView.showError("Category does not exist with id " + updatedExpenseCategoryId, updatedExpense);
			return;
		}

		expenseRepository.update(updatedExpense);
		expenseView.expenseUpdated(updatedExpense);
	}

	
	public void setCategoryRepository(CategoryRepository categoryRepository) {
		
		this.categoryRepository = categoryRepository;
	}

}
