package com.tdd.expensetracker.controller;

import static java.util.Arrays.asList;
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

import com.tdd.expensetracker.model.Category;
import com.tdd.expensetracker.model.Expense;
import com.tdd.expensetracker.repository.CategoryRepository;
import com.tdd.expensetracker.view.CategoryView;

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
		// Setup for initializing mocks
		closeable = MockitoAnnotations.openMocks(this);
	}

	@After
	public void releaseMocks() throws Exception {
		// Clean up resources after tests
		closeable.close();
	}

	// Test case for fetching all categories
	@Test
	public void testAllCategory() {
		List<Category> category = Arrays.asList(new Category());
		when(categoryRepository.findAll()).thenReturn(category);
		categoryController.allCategory();
		verify(categoryView).showAllCategory(category);
	}

	// Test case for adding a new category when it does not already exist
	@Test
	public void testNewCategoryWhenCategoryDoesNotAlreadyExist() {
		Category category = new Category("1", "name1", "description1");
		when(categoryRepository.findById("1")).thenReturn(null);
		when(categoryRepository.findByName("name1")).thenReturn(null);
		categoryController.newCategory(category);
		InOrder inOrder = inOrder(categoryRepository, categoryView);
		inOrder.verify(categoryRepository).save(category);
		inOrder.verify(categoryView).categoryAdded(category);
	}

	// Test case for adding a new category that already exists
	@Test
	public void testNewCategoryWhenCategoryAlreadyExists() {
		Category newCategory = new Category("1", "name1", "description1");
		Category existingCategory = new Category("1", "name1", "description1");
		when(categoryRepository.findById("1")).thenReturn(existingCategory);
		categoryController.newCategory(newCategory);
		verify(categoryView).showError("Already existing category with id 1", existingCategory);
		verifyNoMoreInteractions(ignoreStubs(categoryRepository));
	}

	// Test case for adding a new category with the same name as an existing one
	@Test
	public void testNewCategoryWhenCategoryWithSameNameAlreadyExists() {
		Category newCategory = new Category("2", "name1", "description1");
		Category existingCategory = new Category("1", "name1", "description1");
		when(categoryRepository.findById("2")).thenReturn(null);
		when(categoryRepository.findByName("name1")).thenReturn(existingCategory);
		categoryController.newCategory(newCategory);
		verify(categoryView).showError("Already existing category with name name1", existingCategory);
		verifyNoMoreInteractions(ignoreStubs(categoryRepository));
	}

	// Test case for adding a new category when the name is empty
	@Test
	public void testNewCategoryWhenNameIsEmpty() {
		Category newCategory = new Category("1", "", "description1");
		categoryController.newCategory(newCategory);
		verify(categoryView).showError("Name is required and cannot be null or empty", newCategory);
		verifyNoMoreInteractions(ignoreStubs(categoryRepository));
	}

	// Test case for adding a new category when the description is empty
	@Test
	public void testNewCategoryWhenDescriptionIsEmpty() {
		Category newCategory = new Category("1", "Name", "");
		categoryController.newCategory(newCategory);
		verify(categoryView).showError("Description is required and cannot be null or empty", newCategory);
		verifyNoMoreInteractions(ignoreStubs(categoryRepository));
	}

	// Test case for deleting a category when it exists
	@Test
	public void testDeleteCategoryWhenExist() {
		Category categoryToDelete = new Category("1", "name1", "description1");
		when(categoryRepository.findById("1")).thenReturn(categoryToDelete);
		categoryController.deleteCategory(categoryToDelete);
		InOrder inOrder = inOrder(categoryRepository, categoryView);
		inOrder.verify(categoryRepository).delete(categoryToDelete);
		inOrder.verify(categoryView).categoryDeleted(categoryToDelete);
	}

	// Test case for deleting a category when it does not exist
	@Test
	public void testDeleteCategoryWhenDoesNotExist() {
		Category categoryToDelete = new Category("1", "name1", "description1");
		when(categoryRepository.findById("1")).thenReturn(null);
		categoryController.deleteCategory(categoryToDelete);
		verify(categoryView).showErrorCategoryNotFound("Category does not exist with id 1", categoryToDelete);
		verifyNoMoreInteractions(ignoreStubs(categoryRepository));
	}

	// Test case for deleting a category that has associated expenses
	@Test
	public void testDeleteCategoryWhenHasExpenses() {
		Category categoryToDelete = new Category("1", "name1", "description1");
		categoryToDelete.setExpenses(asList(new Expense("1", 5000d, "espense1", LocalDate.now(), categoryToDelete)));
		when(categoryRepository.findById("1")).thenReturn(categoryToDelete);
		categoryController.deleteCategory(categoryToDelete);
		verify(categoryView).showError("Category cannot be deleted. Expenses are associated with it", categoryToDelete);
		verifyNoMoreInteractions(ignoreStubs(categoryRepository));
	}

	// Test case for updating a category when the description is changed
	@Test
	public void testUpdateCategoryDesctriptionWhenExist() {
		Category updatedCategory = new Category("1", "name1", "description1");
		Category existingCategory = new Category("1", "name1", "description2");
		when(categoryRepository.findById("1")).thenReturn(existingCategory);
		when(categoryRepository.findByName("name1")).thenReturn(existingCategory);
		categoryController.updateCategory(updatedCategory);
		InOrder inOrder = inOrder(categoryRepository, categoryView);
		inOrder.verify(categoryRepository).update(updatedCategory);
		inOrder.verify(categoryView).categoryUpdated(updatedCategory);
	}

	// Test case for updating a category when the name is changed
	@Test
	public void testUpdateCategoryNameWhenExist() {
		Category updatedCategory = new Category("1", "name1", "description2");
		Category existingCategory = new Category("1", "name2", "description2");
		when(categoryRepository.findById("1")).thenReturn(existingCategory);
		when(categoryRepository.findByName("name2")).thenReturn(existingCategory);
		categoryController.updateCategory(updatedCategory);
		InOrder inOrder = inOrder(categoryRepository, categoryView);
		inOrder.verify(categoryRepository).update(updatedCategory);
		inOrder.verify(categoryView).categoryUpdated(updatedCategory);
	}

	// Test case for updating a category when it does not exist
	@Test
	public void testUpdateCategoryWhenDoesNotExist() {
		Category updatedCategory = new Category("1", "name1", "description1");
		when(categoryRepository.findById("1")).thenReturn(null);
		categoryController.updateCategory(updatedCategory);
		verify(categoryView).showError("Category does not exist with id 1", updatedCategory);
		verifyNoMoreInteractions(ignoreStubs(categoryRepository));
	}

	// Test case for updating a category when the name is empty
	@Test
	public void testUpdateCategoryWhenNameIsEmpty() {
		Category updatedCategory = new Category("1", "", "description1");
		categoryController.updateCategory(updatedCategory);
		verify(categoryView).showError("Name is required and cannot be null or empty", updatedCategory);
		verifyNoMoreInteractions(ignoreStubs(categoryRepository));
	}

	// Test case for updating a category when a category with the same name already
	// exists
	@Test
	public void testUpdateCategoryWhenCategoryWithSameNameAlreadyExists() {
		Category updatedCategory = new Category("2", "name1", "any bill");
		Category existingCategoryWithSameName = new Category("1", "name1", "description1");
		when(categoryRepository.findById("2")).thenReturn(updatedCategory);
		when(categoryRepository.findByName("name1")).thenReturn(existingCategoryWithSameName);
		categoryController.updateCategory(updatedCategory);
		verify(categoryView).showError("Already existing category with name name1", existingCategoryWithSameName);
		verifyNoMoreInteractions(ignoreStubs(categoryRepository));
	}

	// Test case for updating a category when the description is empty
	@Test
	public void testUpdateCategoryWhenDescriptionIsEmpty() {
		Category updatedCategory = new Category("1", "name", "");
		categoryController.updateCategory(updatedCategory);
		verify(categoryView).showError("Description is required and cannot be null or empty", updatedCategory);
		verifyNoMoreInteractions(ignoreStubs(categoryRepository));
	}

	// Test case for fetching all expenses when expenses exist for the category
	@Test
	public void testGetAllExpenseWhenExpensesExist() {
		Category category = new Category("1", "name1", "description1");
		Expense expense = new Expense("1", 5000d, "espense1", LocalDate.now(), category);
		category.setExpenses(asList(expense));
		categoryController.getAllExpenses(category);
		InOrder inOrder = inOrder(categoryView);
		inOrder.verify(categoryView).getAllExpenses(category.getExpenses());
	}

	// Test case for fetching all expenses when no expenses exist for the category
	@Test
	public void testGetAllExpenseWhenExpensesNotExist() {
		Category category = new Category("1", "name1", "description1");
		categoryController.getAllExpenses(category);
		InOrder inOrder = inOrder(categoryView);
		inOrder.verify(categoryView).showError("No Expense created for this category", category);
	}
}
