package com.tdd.expensetracker.repository.mysql;

import java.time.LocalDate;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import com.tdd.expensetracker.model.Category;
import com.tdd.expensetracker.model.Expense;

public class ExpenseMysqlRepositoryTestContainerIT {

	// Using MySQLContainer from Testcontainers for integration testing
	@SuppressWarnings("resource")
	@ClassRule
	public static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>(DockerImageName.parse("mysql:8.0.28"))
			.withDatabaseName("test").withUsername("test").withPassword("test");

	private static SessionFactory sessionFactory;
	private ExpenseMysqlRepository expenseMysqlRepository;
	private static StandardServiceRegistry registry;
	private Category category;

	// Set up the MySQL container and Hibernate configuration before tests
	@BeforeClass
	public static void setupContainer() {
		mysqlContainer.start();
		registry = new StandardServiceRegistryBuilder().configure("hibernate-IT.cfg.xml")
				.applySetting("hibernate.connection.url", mysqlContainer.getJdbcUrl())
				.applySetting("hibernate.connection.username", mysqlContainer.getUsername())
				.applySetting("hibernate.connection.password", mysqlContainer.getPassword()).build();
	}

	// Tear down the database and stop the container after tests
	@AfterClass
	public static void shutdownServer() {
		StandardServiceRegistryBuilder.destroy(registry);
		if (sessionFactory != null) {
			sessionFactory.close();
		}
		mysqlContainer.stop();
	}

	// Initialize Hibernate session factory and repository before each test
	@Before
	public void setup() {
		MetadataSources metadataSources = new MetadataSources(registry);
		sessionFactory = metadataSources.buildMetadata().buildSessionFactory();
		expenseMysqlRepository = new ExpenseMysqlRepository(sessionFactory);
		category = new Category("1", "name1", "description1");
		saveCategory(category);
	}

	// Test that the repository retrieves all expenses from the database
	@Test
	public void testFindAll() {
		String id1 = addTestExpenseToDatabase(new Expense(50.0, "test1", LocalDate.now(), category));
		String id2 = addTestExpenseToDatabase(new Expense(50.0, "test2", LocalDate.now(), category));
		Assertions.assertThat(expenseMysqlRepository.findAll()).containsExactlyInAnyOrder(
				new Expense(id1, 50d, "test1", LocalDate.now(), category),
				new Expense(id2, 50d, "test2", LocalDate.now(), category));
	}

	// Test that the repository finds an expense by its ID
	@Test
	public void testFindById() {
		addTestExpenseToDatabase(new Expense(50.0, "test1", LocalDate.now(), category));
		String id = addTestExpenseToDatabase(new Expense(50.0, "test2", LocalDate.now(), category));
		String expected = new Expense(id, 50.0, "test2", LocalDate.now(), category).toString();
		String actual = expenseMysqlRepository.findById(id).toString();
		Assertions.assertThat(actual).isEqualTo(expected);
	}

	// Test that the repository saves a new expense to the database
	@Test
	public void testSave() {
		Expense expense = new Expense(50.0, "test2", LocalDate.now(), category);
		expenseMysqlRepository.save(expense);
		Assertions.assertThat(readAllExpenseFromDatabase()).containsExactly(expense);
	}

	// Test that the repository deletes an expense from the database
	@Test
	public void testDelete() {
		String id = addTestExpenseToDatabase(new Expense(50.0, "test1", LocalDate.now(), category));
		Expense expenseToDelete = new Expense(id, 50d, "test1", LocalDate.now(), category);
		expenseMysqlRepository.delete(expenseToDelete);
		Assertions.assertThat(readAllExpenseFromDatabase()).isEmpty();
	}

	// Test that the repository updates an expense in the database
	@Test
	public void testUpdate() {
		String expenseToUpdate = addTestExpenseToDatabase(new Expense(50.0, "test1", LocalDate.now(), category));
		Expense updatedExpense = new Expense(expenseToUpdate, 60.0, "updated name", LocalDate.now().plusDays(5),
				category);
		expenseMysqlRepository.update(updatedExpense);
		Assertions.assertThat(readAllExpenseFromDatabase()).containsExactly(updatedExpense);
	}

	// Utility method to retrieve all expenses from the database
	private List<Expense> readAllExpenseFromDatabase() {
		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		List<Expense> allExpense = session.createQuery("from Expense", Expense.class).list();
		tx.commit();
		session.close();
		return allExpense;
	}

	// Utility method to add an expense to the database and return its ID
	private String addTestExpenseToDatabase(Expense expense) {
		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		session.save(expense);
		tx.commit();
		session.close();
		return expense.getId();
	}

	// Utility method to save a category in the database
	private void saveCategory(Category category) {
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		session.save(category);
		session.getTransaction().commit();
		session.close();
	}
}
