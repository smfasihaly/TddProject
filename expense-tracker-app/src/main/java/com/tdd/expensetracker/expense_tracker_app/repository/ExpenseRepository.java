package com.tdd.expensetracker.expense_tracker_app.repository;

import java.util.List;

import com.tdd.expensetracker.expense_tracker_app.model.Expense;

public interface ExpenseRepository {

	public List<Expense> findAll();

	public Expense findById(String id);

	public void save(Expense expense);

	public void delete(String id);

	public void update(Expense updatedExpense);



}
