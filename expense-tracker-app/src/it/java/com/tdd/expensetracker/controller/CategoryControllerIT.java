package com.tdd.expensetracker.controller;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.verify;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.tdd.expensetracker.model.Category;
import com.tdd.expensetracker.repository.CategoryRepository;
import com.tdd.expensetracker.repository.mysql.CategoryMySqlRepository;
import com.tdd.expensetracker.view.CategoryView;

public class CategoryControllerIT {

	private CategoryRepository categoryRepository;
	@Mock
	private CategoryView categoryView;
	private AutoCloseable closeable;
	private static SessionFactory sessionFactory;

	private static StandardServiceRegistry registry;
	private CategoryController categoryController;

	@BeforeClass
	public static void configureDB() {
		registry = new StandardServiceRegistryBuilder().configure("hibernate.cfg.xml")
				.applySetting("hibernate.connection.url", "jdbc:mysql://localhost:3307/expense_tracker")
				.applySetting("hibernate.connection.password", "test").build();

	}

	@AfterClass
	public static void shutdownDB() {

		StandardServiceRegistryBuilder.destroy(registry);
		if (sessionFactory != null) {
			sessionFactory.close();
		}
	}

	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);

		MetadataSources metadataSources = new MetadataSources(registry);
		sessionFactory = metadataSources.buildMetadata().buildSessionFactory();

		categoryRepository = new CategoryMySqlRepository(sessionFactory);
		categoryController = new CategoryController(categoryView, categoryRepository);

	}

	@After
	public void releaseMocks() throws Exception {
		closeable.close();
	}

	@Test
	public void testAllCategory() {

		Category category = new Category("name2", "description2");
		categoryRepository.save(category);

		categoryController.allCategory();

		verify(categoryView).showAllCategory(asList(category));
	}

	@Test
	public void testNewCategory() {

		Category category = new Category("name1", "description1");

		categoryController.newCategory(category);

		verify(categoryView).categoryAdded(category);
	}

	@Test
	public void testDeleteCategory() {
		Category categoryToDelete = new Category( "name1", "description1");
		categoryRepository.save(categoryToDelete);
		
		categoryController.deleteCategory(categoryToDelete);
		
		verify(categoryView).categoryDeleted(categoryToDelete);
	}
	
	@Test
	public void testUpdateCategory() {
		
		Category category = new Category( "name1","description1");
		categoryRepository.save(category);
		
		Category updatedCategory = new Category(category.getId(), "name2","description2");
		categoryController.updateCategory(updatedCategory);
		
		verify(categoryView).categoryUpdated(updatedCategory);
	}

}
