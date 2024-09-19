package com.tdd.expensetracker.repository.mysql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tdd.expensetracker.model.Category;
import com.tdd.expensetracker.model.Expense;

public class ExpenseMysqlRepositoryTest {

	private SessionFactory sessionFactory;
	private static StandardServiceRegistry registry;
	private ExpenseMysqlRepository expenseMysqlRepository;
	private Category category;

	// Setup the Hibernate session for testing
	@BeforeClass
	public static void setupServer() {
		registry = new StandardServiceRegistryBuilder().configure("hibernate-test.cfg.xml").build();
	}

	// Cleanup the session after tests are completed
	@AfterClass
	public static void shutdownServer() {
		StandardServiceRegistryBuilder.destroy(registry);
	}

	// Setup the session factory and save a test category before each test
	@Before
	public void setup() {
		sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
		expenseMysqlRepository = new ExpenseMysqlRepository(sessionFactory);
		category = new Category("1", "name1", "description1");
		saveCategory(category);
	}

	// Test for finding all expenses when the database is empty
	@Test
	public void testFindAllWhenDatabaseIsEmpty() {
		assertThat(expenseMysqlRepository.findAll()).isEmpty();
	}

	// Test for finding all expenses when the database is not empty
	@Test
	public void testFindAllWhenDatabaseIsNotEmpty() {
		String id1 = addTestExpenseToDatabase(new Expense(50d, "test1", LocalDate.now(), category));
		String id2 = addTestExpenseToDatabase(new Expense(500d, "test2", LocalDate.now(), category));

		Assertions.assertThat(expenseMysqlRepository.findAll())
				.containsExactly(new Expense[] { new Expense(id1, 50d, "test1", LocalDate.now(), category),
						new Expense(id2, 500d, "test2", LocalDate.now(), category) });
	}

	// Test for finding an expense by ID when it is not found
	@Test
	public void testFindByIdNotFound() {
		Assertions.assertThat(expenseMysqlRepository.findById("1")).isNull();
	}

	// Test for finding an expense by ID when it is found
	@Test
	public void testFindByIdFound() {
		addTestExpenseToDatabase(new Expense(50d, "test1", LocalDate.now(), category));
		String id = addTestExpenseToDatabase(new Expense(50d, "test2", LocalDate.now(), category));
		String expected = new Expense(id, 50d, "test2", LocalDate.now(), category).toString();
		String actual = expenseMysqlRepository.findById(id).toString();
		Assertions.assertThat(actual).isEqualTo(expected);
	}

	// Test for saving a new expense in the database
	@Test
	public void testSave() {
		Expense expense = new Expense(50d, "test2", LocalDate.now(), category);
		expenseMysqlRepository.save(expense);

		Assertions.assertThat(readAllExpenseFromDatabase()).containsExactly(new Expense[] { expense });
	}

	// Test for deleting an expense from the database
	@Test
	public void testDelete() {
		String id = addTestExpenseToDatabase(new Expense(50d, "test2", LocalDate.now(), category));
		Expense expenseToDelete = new Expense(id, 50d, "test2", LocalDate.now(), category);

		expenseMysqlRepository.delete(expenseToDelete);
		Assertions.assertThat(readAllExpenseFromDatabase()).isEmpty();
	}

	// Test for updating an existing expense in the database
	@Test
	public void testUpdate() {
		String expenseToUpdate = addTestExpenseToDatabase(new Expense(50d, "test1", LocalDate.now(), category));
		Expense updatedExpense = new Expense(expenseToUpdate, 60.0, "updated name", LocalDate.now().plusDays(5),
				category);
		expenseMysqlRepository.update(updatedExpense);
		Assertions.assertThat(readAllExpenseFromDatabase()).containsExactly(new Expense[] { updatedExpense });
	}

	// Test for exception handling when saving a null category
	@Test
	public void testSaveThrowsExceptionWhenNullexpense() {
		assertThatThrownBy(() -> expenseMysqlRepository.save(null)).isInstanceOf(HibernateException.class)
				.hasMessageContaining("Could not save expense.");
	}

	// Test for exception handling when updating a null expense
	@Test
	public void testUpdateThrowsExceptionWhenNullexpense() {
		assertThatThrownBy(() -> expenseMysqlRepository.update(null)).isInstanceOf(HibernateException.class)
				.hasMessageContaining("Could not update expense.");
	}

	// Test for exception handling when delete a null expense
	@Test
	public void testUpdateThrowsExceptionWhenDeleteexpense() {
		assertThatThrownBy(() -> expenseMysqlRepository.delete(null)).isInstanceOf(HibernateException.class)
				.hasMessageContaining("Could not delete expense.");
	}

	// Utility method to read all expenses from the database
	private List<Expense> readAllExpenseFromDatabase() {
		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		List<Expense> allExpense = session.createQuery("from Expense", Expense.class).list();
		tx.commit();
		session.close();
		return allExpense;
	}

	// Utility method to add a test expense to the database
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