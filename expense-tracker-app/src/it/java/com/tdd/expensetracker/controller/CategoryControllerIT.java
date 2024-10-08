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
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

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

	// Using MySQLContainer from Testcontainers for integration testing
	@SuppressWarnings("resource")
	@ClassRule
	public static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>(DockerImageName.parse("mysql:8.0.28"))
			.withDatabaseName("test").withUsername("test").withPassword("test");

	// Setup database connection using Testcontainers or local MySQL instance
	@BeforeClass
	public static void configureDB() {
		// Check if running in Eclipse
		if (System.getProperty("surefire.test.class.path") == null) {
			// Using Testcontainers (Eclipse environment)
			mysqlContainer.start();
			// Configure Hibernate to use Testcontainers' MySQL
			registry = new StandardServiceRegistryBuilder().configure("hibernate-IT.cfg.xml")
					.applySetting("hibernate.connection.url", mysqlContainer.getJdbcUrl())
					.applySetting("hibernate.connection.username", mysqlContainer.getUsername())
					.applySetting("hibernate.connection.password", mysqlContainer.getPassword()).build();
		} else {
			// Using Maven Docker (Maven environment)
			registry = new StandardServiceRegistryBuilder().configure("hibernate-IT.cfg.xml")
					.applySetting("hibernate.connection.url", "jdbc:mysql://localhost:3307/expense_tracker")
					.applySetting("hibernate.connection.password", "test").build();
		}
	}

	// Tear down the database and stop the container if necessary
	@AfterClass
	public static void shutdownDB() {
		StandardServiceRegistryBuilder.destroy(registry);
		if (sessionFactory != null) {
			sessionFactory.close();
		}
		if (System.getProperty("surefire.test.class.path") == null) {
			// Using Testcontainers (Eclipse environment)
			mysqlContainer.stop();
		}
	}

	// Initialize mocks and set up Hibernate session factory and repository
	@Before
	public void setup() {
		closeable = MockitoAnnotations.openMocks(this);
		MetadataSources metadataSources = new MetadataSources(registry);
		sessionFactory = metadataSources.buildMetadata().buildSessionFactory();
		categoryRepository = new CategoryMySqlRepository(sessionFactory);
		categoryController = new CategoryController(categoryView, categoryRepository);
	}

	// Close mocks after each test
	@After
	public void releaseMocks() throws Exception {
		closeable.close();
	}

	// Test that the controller retrieves all categories and displays them in the
	// view
	@Test
	public void testAllCategory() {
		Category category = new Category("name2", "description2");
		categoryRepository.save(category);
		categoryController.allCategory();
		verify(categoryView).showAllCategory(asList(category));
	}

	// Test that the controller adds a new category and updates the view
	@Test
	public void testNewCategory() {
		Category category = new Category("name1", "description1");
		categoryController.newCategory(category);
		verify(categoryView).categoryAdded(category);
	}

	// Test that the controller deletes a category and updates the view
	@Test
	public void testDeleteCategory() {
		Category categoryToDelete = new Category("name1", "description1");
		categoryRepository.save(categoryToDelete);
		categoryController.deleteCategory(categoryToDelete);
		verify(categoryView).categoryDeleted(categoryToDelete);
	}

	// Test that the controller updates a category and refreshes the view
	@Test
	public void testUpdateCategory() {
		Category category = new Category("name1", "description1");
		categoryRepository.save(category);
		Category updatedCategory = new Category(category.getId(), "name2", "description2");
		categoryController.updateCategory(updatedCategory);
		verify(categoryView).categoryUpdated(updatedCategory);
	}
}
