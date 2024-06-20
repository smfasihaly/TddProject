package com.tdd.expensetracker.expense_tracker_app.repository;

import java.util.List;

import com.tdd.expensetracker.expense_tracker_app.model.Category;

public interface CategoryRepository {

	public List<Category> findAll();

	public Category findById(String id);

	public void save(Category category);

	public void delete(String id);

	public void update(Category updatedCategory);

}
