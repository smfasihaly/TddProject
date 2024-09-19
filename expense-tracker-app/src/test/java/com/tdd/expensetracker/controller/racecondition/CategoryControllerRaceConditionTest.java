package com.tdd.expensetracker.controller.racecondition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.tdd.expensetracker.controller.CategoryController;
import com.tdd.expensetracker.model.Category;
import com.tdd.expensetracker.repository.CategoryRepository;
import com.tdd.expensetracker.view.CategoryView;

public class CategoryControllerRaceConditionTest {

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
	public void testNewCategoryConcurrent() {
		List<Category> categorys = new ArrayList<>(); // List to act as a fake database
		// Create a new category
		Category category = new Category("1", "name1", "description1");
		// Stub the findById method to simulate repository behavior
		when(categoryRepository.findById(anyString()))
				.thenAnswer(invocation -> categorys.stream().findFirst().orElse(null));
		// Stub the save method to add the category to the list
		doAnswer(invocation -> {
			categorys.add(category);
			return null;
		}).when(categoryRepository).save(any(Category.class));
		// Create and start 10 threads to simulate concurrent access
		List<Thread> threads = IntStream.range(0, 10)
				.mapToObj(i -> new Thread(() -> categoryController.newCategory(category))).peek(t -> t.start())
				.collect(Collectors.toList());
		// Wait for all threads to finish
		await().atMost(10, TimeUnit.SECONDS).until(() -> threads.stream().noneMatch(t -> t.isAlive()));
		// Verify that only one category was added to the list
		assertThat(categorys).containsExactly(category);
	}

	@Test
	public void testDeleteCategoryConcurrent() {
		List<Category> categories = new ArrayList<>(); // List to act as a fake database
		List<Category> deletedCategories = new ArrayList<>(); // List to act as a fake database
		// Create and add an category to the list
		Category category = new Category("1", "name1", "description1");
		categories.add(category);
		// Stub the findById method to simulate repository behavior
		when(categoryRepository.findById(anyString()))
				.thenAnswer(invocation -> categories.stream().findFirst().orElse(null));
		// Stub the delete method to remove the category from the list
		doAnswer(invocation -> {
			categories.remove(category);
			deletedCategories.add(category);
			return null;
		}).when(categoryRepository).delete(any(Category.class));
		// Create and start 10 threads to simulate concurrent access
		List<Thread> threads = IntStream.range(0, 10)
				.mapToObj(i -> new Thread(() -> categoryController.deleteCategory(category))).peek(t -> t.start())
				.collect(Collectors.toList());
		// Wait for all threads to finish
		await().atMost(10, TimeUnit.SECONDS).until(() -> threads.stream().noneMatch(t -> t.isAlive()));
		// Verify that the list is empty (category was deleted)
		assertThat(categories).isEmpty();
		assertThat(deletedCategories).containsExactly(category);
		// Verify that the delete method was called exactly once
		verify(categoryRepository, times(1)).delete(any(Category.class));
	}

	@Test
	public void testUpdateCategoryConcurrent() {
		List<Category> categories = new ArrayList<>(); // List to act as a fake database
		List<Category> updatedCategories = new ArrayList<>(); // List to act as a fake database
		// Create and add an category to the list
		Category category = new Category("1", "name1", "description1");
		categories.add(category);
		// Stub the findById method to simulate repository behavior
		when(categoryRepository.findById(anyString()))
				.thenAnswer(invocation -> categories.stream().findFirst().orElse(null));
		// Stub the update method to remove the category from the list
		doAnswer(invocation -> {
			categories.remove(category);
			updatedCategories.add(category);
			return null;
		}).when(categoryRepository).update(any(Category.class));
		// Create and start 10 threads to simulate concurrent access
		List<Thread> threads = IntStream.range(0, 10)
				.mapToObj(i -> new Thread(() -> categoryController.updateCategory(category))).peek(t -> t.start())
				.collect(Collectors.toList());
		// Wait for all threads to finish
		await().atMost(10, TimeUnit.SECONDS).until(() -> threads.stream().noneMatch(t -> t.isAlive()));
		// Verify that the list is empty (category was updated)
		assertThat(categories).isEmpty();
		assertThat(updatedCategories).containsExactly(category);
		// Verify that the update method was called exactly once
		verify(categoryRepository, times(1)).update(any(Category.class));
	}
}
