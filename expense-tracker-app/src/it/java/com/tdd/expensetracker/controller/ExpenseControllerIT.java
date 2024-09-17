package com.tdd.expensetracker.controller;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;

import org.hibernate.Session;
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
import com.tdd.expensetracker.model.Expense;
import com.tdd.expensetracker.repository.CategoryRepository;
import com.tdd.expensetracker.repository.ExpenseRepository;
import com.tdd.expensetracker.repository.mysql.CategoryMySqlRepository;
import com.tdd.expensetracker.repository.mysql.ExpenseMysqlRepository;
import com.tdd.expensetracker.view.ExpenseView;

public class ExpenseControllerIT {

	private ExpenseRepository expenseRepository;

	private CategoryRepository categoryRepository;
	@Mock
	private ExpenseView expenseView;

	private AutoCloseable closeable;

	private static SessionFactory sessionFactory;

	private static StandardServiceRegistry registry;
	private ExpenseController expenseController;
	private Category existingCategory;

	@SuppressWarnings("resource")
	@ClassRule
	public static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>(DockerImageName.parse("mysql:8.0.28"))
			.withDatabaseName("test").withUsername("test").withPassword("test");

	@BeforeClass
	public static void configureDB() {
		// Check if running in Eclipse
		if (System.getProperty("surefire.test.class.path") == null) {
			// Using Test containers (Eclipse environment)
			mysqlContainer.start();

			// Configure Hibernate to use Test containers' MySQL
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

	@AfterClass
	public static void shutdownDB() {

		StandardServiceRegistryBuilder.destroy(registry);
		if (sessionFactory != null) {
			sessionFactory.close();
		}
		if (System.getProperty("surefire.test.class.path") == null) {
			// Using Test containers (Eclipse environment)
			mysqlContainer.stop();
		}
	}

	@Before
	public void setup() {

		closeable = MockitoAnnotations.openMocks(this);

		MetadataSources metadataSources = new MetadataSources(registry);
		sessionFactory = metadataSources.buildMetadata().buildSessionFactory();

		expenseRepository = new ExpenseMysqlRepository(sessionFactory);
		categoryRepository = new CategoryMySqlRepository(sessionFactory);
		expenseController = new ExpenseController(expenseView, expenseRepository, categoryRepository);

		existingCategory = new Category("name1", "description1");
		saveCategory(existingCategory);
	}

	@After
	public void releaseMocks() throws Exception {

		closeable.close();
	}

	@Test
	public void testAllExpense() {

		Expense expense = new Expense(5000d, "testExpense", LocalDate.now(), existingCategory);
		expenseRepository.save(expense);

		expenseController.allExpense();

		verify(expenseView).showAllExpense(asList(expense));
	}

	@Test
	public void testNewExpense() {

		Expense expense = new Expense(5000d, "testExpense", LocalDate.now(), existingCategory);
		expenseController.newExpense(expense);

		verify(expenseView).expenseAdded(expense);
	}

	@Test
	public void testDeleteExpense() {

		Expense expenseToDelete = new Expense(5000d, "testExpense", LocalDate.now(), existingCategory);

		expenseRepository.save(expenseToDelete);
		expenseController.deleteExpense(expenseToDelete);

		verify(expenseView).expenseDeleted(expenseToDelete);
	}

	@Test
	public void testUpdate() {

		Expense expenseToUpdate = new Expense(5000d, "testExpense", LocalDate.now(), existingCategory);
		expenseRepository.save(expenseToUpdate);

		Expense updatedExpense = new Expense(expenseToUpdate.getId(), 60.0, "updated name", LocalDate.now(),
				existingCategory);
		expenseController.updateExpense(updatedExpense);

		verify(expenseView).expenseUpdated(updatedExpense);
	}

	private void saveCategory(Category category) {
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		session.save(category);

		session.getTransaction().commit();
		session.close();
	}

}
