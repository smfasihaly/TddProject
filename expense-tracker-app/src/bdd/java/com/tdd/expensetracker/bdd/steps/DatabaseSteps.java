package com.tdd.expensetracker.bdd.steps;

import java.time.LocalDate;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import com.tdd.expensetracker.model.Category;
import com.tdd.expensetracker.model.Expense;

import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.Given;

public class DatabaseSteps {

	// Static constants for test data
	static final String CATEGORY_DESCRIPTION1 = "utilities";
	static final String CATEGORY_NAME1 = "bills";
	static final double EXPENSE_AMOUNT1 = 5000.0;
	static final String EXPENSE_NAME1 = "first expense";
	static final Double EXPENSE_AMOUNT2 = 10.0;
	static final String EXPENSE_NAME2 = "ExpenseName2";
	static final String CATEGORY_NAME2 = "categoryName2";
	static final String CATEGORY_DESCRIPTION2 = "categoryDescription2";

	// Hibernate session management
	private static SessionFactory sessionFactory;
	private static StandardServiceRegistry registry;
	static String dbURL = "";
	static final String DB_USER = "test";
	static final String DB_PASS = "test";

	// Variables to hold references to first created expense and category
	private Expense firstExpense;
	private Category firstCategory;
	// Testcontainers MySQL container
	@SuppressWarnings("resource")
	public static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>(DockerImageName.parse("mysql:8.0.28"))
			.withDatabaseName("test").withUsername("test").withPassword("test");

	@BeforeAll
	public static void configureDB() {
		if (isRunningInEclipse()) {
			mysqlContainer.start();
			dbURL = mysqlContainer.getJdbcUrl();
			System.setProperty("ENVIRONMENT", "testWithEclipes");
			registry = new StandardServiceRegistryBuilder().configure("hibernate-IT.cfg.xml")
					.applySetting("hibernate.connection.url", dbURL)
					.applySetting("hibernate.connection.username", DB_USER)
					.applySetting("hibernate.connection.password", DB_PASS).build();
		} else {
			// For Maven or any other environment, use the default configuration
			registry = new StandardServiceRegistryBuilder().configure("hibernate-IT.cfg.xml")
					.applySetting("hibernate.connection.url", "jdbc:mysql://localhost:3307/expense_tracker")
					.applySetting("hibernate.connection.password", "test").build();
		}
	}

	private static boolean isRunningInEclipse() {
		return System.getProperty("surefire.test.class.path") == null;
	}

	@AfterAll
	public static void shutdownDB() {
		// Clean up Hibernate resources after each test
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
		MetadataSources metadataSources = new MetadataSources(registry);
		sessionFactory = metadataSources.buildMetadata().buildSessionFactory();
	}

	@Given("The database contains the Expense with the following values")
	public void the_database_contains_the_expenses_with_the_following_values(List<List<String>> values) {
		// Adding multiple expenses to the database based on the provided values
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		values.forEach(expenseValues -> {
			Category category = new Category(expenseValues.get(3), expenseValues.get(4));
			session.save(category); // Save category first because Expense has a foreign key to Category
			Expense expense = new Expense(Double.parseDouble(expenseValues.get(0)), expenseValues.get(1),
					LocalDate.parse(expenseValues.get(2)), category);
			session.save(expense); // Save the expense with the category reference
		});
		session.getTransaction().commit();
		session.close();
	}

	@Given("The database contains category with the following values")
	public void the_database_contains_category_with_the_following_values(List<List<String>> values) {
		// Adding multiple categories to the database based on the provided values
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		values.forEach(expenseValues -> {
			Category category = new Category(expenseValues.get(0), expenseValues.get(1));
			session.save(category);
		});
		session.getTransaction().commit();
		session.close();
	}

	@Given("The database contains a few expense")
	public void the_database_contains_a_few_expense() {
		// Add a few expenses to the database using predefined test data
		Category newCat = new Category(CATEGORY_NAME1, CATEGORY_DESCRIPTION1);
		addTestCategoryToDatabase(newCat);
		firstExpense = new Expense(EXPENSE_AMOUNT1, EXPENSE_NAME1, LocalDate.now(), newCat);
		addTestExpenseToDatabase(firstExpense);
		addTestExpenseToDatabase(new Expense(EXPENSE_AMOUNT2, EXPENSE_NAME2, LocalDate.now(), newCat));
	}

	@Given("The expense is in the meantime removed from the database")
	public void the_expense_is_in_the_meantime_removed_from_the_database() {
		// Remove the previously saved expense from the database
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		session.delete(firstExpense);
		session.getTransaction().commit();
		session.close();
	}

	@Given("The database contains a few category")
	public void the_database_contains_a_few_category() {
		// Add a few categories to the database using predefined test data
		firstCategory = new Category(CATEGORY_NAME1, CATEGORY_DESCRIPTION1);
		addTestCategoryToDatabase(firstCategory);
		addTestCategoryToDatabase(new Category(CATEGORY_NAME2, CATEGORY_DESCRIPTION2));
	}

	@Given("The category is in the meantime removed from the database")
	public void the_category_is_in_the_meantime_removed_from_the_database() {
		// Remove the previously saved category from the database
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		session.delete(firstCategory);
		session.getTransaction().commit();
		session.close();
	}

	private void addTestExpenseToDatabase(Expense expense) {
		// Utility method to save a test expense to the database
		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		session.save(expense);
		tx.commit();
		session.close();
	}

	private void addTestCategoryToDatabase(Category category) {
		// Utility method to save a test category to the database
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		session.save(category);
		session.getTransaction().commit();
		session.close();
	}
}