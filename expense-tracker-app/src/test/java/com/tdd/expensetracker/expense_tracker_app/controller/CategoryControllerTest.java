package com.tdd.expensetracker.expense_tracker_app.controller;

import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.tdd.expensetracker.expense_tracker_app.model.Category;
import com.tdd.expensetracker.expense_tracker_app.model.Expense;
import com.tdd.expensetracker.expense_tracker_app.repository.CategoryRepository;
import com.tdd.expensetracker.expense_tracker_app.view.CategoryView;

public class CategoryControllerTest {
	@Mock
	private CategoryRepository categoryRepository;
	@Mock
	private CategoryView categoryView;

	@InjectMocks
	private CategoryController categoryController;

	private AutoCloseable closeable;

	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);
	}

	@After
	public void releaseMocks() throws Exception {
		closeable.close();
	}

	@Test
	public void testAllCategory() {
		List<Category> category = Arrays.asList(new Category());
		when(categoryRepository.findAll()).thenReturn(category);
		categoryController.allCategory();
		verify(categoryView).showAllCategory(category);
	}

	@Test
	public void testNewCategoryWhenCategoryDoesNotAlreadyExist() {
		Category category = new Category("1", "name1","description1");
		when(categoryRepository.findById("1")).thenReturn(null);
		categoryController.newCategory(category);
		InOrder inOrder = inOrder(categoryRepository, categoryView);
		inOrder.verify(categoryRepository).save(category);
		inOrder.verify(categoryView).categoryAdded(category);
	}
	
	@Test
	public void testNewCategoryWhenCategoryAlreadyExists() {
		Category newCategory = new Category("1", "name1","description1");
		Category existingCategory = new Category("1", "name1","description1");
		when(categoryRepository.findById("1")).thenReturn(existingCategory);
		categoryController.newCategory(newCategory);
		verify(categoryView).showError("Already existing category with id 1", existingCategory);
		verifyNoMoreInteractions(ignoreStubs(categoryRepository));

	}
	
	@Test
	public void testDeleteCategoryWhenExist() {
		Category categoryToDelete = new Category("1", "name1","description1");
		when(categoryRepository.findById("1")).thenReturn(categoryToDelete);
			categoryController.DeleteCategory(categoryToDelete);
		InOrder inOrder = inOrder(categoryRepository, categoryView);
		inOrder.verify(categoryRepository).delete("1");
		inOrder.verify(categoryView).categoryDeleted("1");
	}

	@Test
	public void testDeleteCategoryWhenDoesNotExist() {
		Category categoryToDelete = new Category("1", "name1","description1");
		when(categoryRepository.findById("1")).thenReturn(null);
		categoryController.DeleteCategory(categoryToDelete);
		verify(categoryView).showError("Category does not exist with id 1", categoryToDelete);
		verifyNoMoreInteractions(ignoreStubs(categoryRepository));
	}

	@Test
	public void testUpdateCategoryWhenExist() {
		Category updatedCategory = new Category("1", "name1","description1");
		Category existingCategory = new Category("1", "name2","description2");
		when(categoryRepository.findById("1")).thenReturn(existingCategory);
		categoryController.updateCategory(updatedCategory);
		InOrder inOrder = inOrder(categoryRepository, categoryView);
		inOrder.verify(categoryRepository).update(updatedCategory);
		inOrder.verify(categoryView).categoryUpdated(updatedCategory);
	}

	@Test
	public void testUpdateCategoryWhenDoesNotExist() {
		Category updatedCategory = new Category("1", "name1","description1");
		when(categoryRepository.findById("1")).thenReturn(null);
		categoryController.updateCategory(updatedCategory);
		verify(categoryView).showError("Category does not exist with id 1", updatedCategory);
		verifyNoMoreInteractions(ignoreStubs(categoryRepository));

	}
	
	

}
