package com.tdd.expensetracker.controller;

import java.awt.image.RescaleOp;
import java.util.List;

import com.tdd.expensetracker.model.Expense;
import com.tdd.expensetracker.repository.ExpenseRepository;
import com.tdd.expensetracker.view.ExpenseView;

public class ExpenseController {

	private ExpenseView expenseView;
	private ExpenseRepository expenseRepository;

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
			this.expenseView.showError("Already existing expense with id " + expense.getId(), existingExpense);
			return;
		}
		expenseRepository.save(expense);
		expenseView.expenseAdded(expense);

	}

	public void DeleteExpense(Expense expenseToDelete) {
		Expense existingExpense = expenseRepository.findById(expenseToDelete.getId());
		if (existingExpense == null) {
			this.expenseView.showError("Expense does not exist with id "+expenseToDelete.getId(), expenseToDelete);
			return;
		}
		
		expenseRepository.delete(expenseToDelete.getId());
		expenseView.expenseDeleted(expenseToDelete.getId());
	}

	public void updateExpense(Expense updatedExpense) {
		Expense existingExpense = expenseRepository.findById(updatedExpense.getId());
		if(existingExpense == null) {
			this.expenseView.showError("Expense does not exist with id "+updatedExpense.getId(), updatedExpense);
			return;
		}
		expenseRepository.update(updatedExpense);
		expenseView.expenseUpdated(updatedExpense);
	}
	
	

}
