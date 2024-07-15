package com.tdd.expensetracker.controller;

import static org.mockito.Mockito.*;

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
	public void testNewCategoryWhenNameIsEmpty() {
		Category newCategory = new Category("1", "","description1");
		categoryController.newCategory(newCategory);
		verify(categoryView).showError("Name is required and cannot be null or empty", newCategory);
		verifyNoMoreInteractions(ignoreStubs(categoryRepository));

	}
	
	@Test
	public void testNewCategoryWhenDescriptionIsEmpty() {
		Category newCategory = new Category("1", "Name","");
		categoryController.newCategory(newCategory);
		verify(categoryView).showError("Description is required and cannot be null or empty", newCategory);
		verifyNoMoreInteractions(ignoreStubs(categoryRepository));

	}
	
	@Test
	public void testDeleteCategoryWhenExist() {
		Category categoryToDelete = new Category("1", "name1","description1");
		when(categoryRepository.findById("1")).thenReturn(categoryToDelete);
			categoryController.deleteCategory(categoryToDelete);
		InOrder inOrder = inOrder(categoryRepository, categoryView);
		inOrder.verify(categoryRepository).delete(categoryToDelete);
		inOrder.verify(categoryView).categoryDeleted(categoryToDelete);
	}

	@Test
	public void testDeleteCategoryWhenDoesNotExist() {
		Category categoryToDelete = new Category("1", "name1","description1");
		when(categoryRepository.findById("1")).thenReturn(null);
		categoryController.deleteCategory(categoryToDelete);
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
	

	@Test
	public void testUpdateCategoryWhenNameIsEmpty() {
		Category updatedCategory = new Category("1", "","description1");
		categoryController.updateCategory(updatedCategory);
		verify(categoryView).showError("Name is required and cannot be null or empty", updatedCategory);
		verifyNoMoreInteractions(ignoreStubs(categoryRepository));

	}
	
	@Test
	public void testUpdateCategoryWhenDescriptionIsEmpty() {
		Category updatedCategory = new Category("1", "name","");
		categoryController.updateCategory(updatedCategory);
		verify(categoryView).showError("Description is required and cannot be null or empty", updatedCategory);
		verifyNoMoreInteractions(ignoreStubs(categoryRepository));

	}
	
	
	

}
