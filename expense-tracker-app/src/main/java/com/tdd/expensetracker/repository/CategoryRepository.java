package com.tdd.expensetracker.repository;

import java.util.List;

import com.tdd.expensetracker.model.Category;

public interface CategoryRepository {

	public List<Category> findAll();

	public Category findById(String id);

	public Category findByName(String name);

	public void save(Category category);

	public void delete(Category category);

	public void update(Category updatedCategory);

}
