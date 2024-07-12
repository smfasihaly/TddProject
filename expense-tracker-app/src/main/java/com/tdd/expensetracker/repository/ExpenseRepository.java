package com.tdd.expensetracker.repository;

import java.util.List;

import com.tdd.expensetracker.model.Expense;

public interface ExpenseRepository {

	public List<Expense> findAll();

	public Expense findById(String id);

	public void save(Expense expense);

	public void update(Expense updatedExpense);

	void delete(Expense expense);



}
