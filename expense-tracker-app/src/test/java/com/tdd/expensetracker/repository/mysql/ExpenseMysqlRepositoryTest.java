package com.tdd.expensetracker.repository.mysql;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.junit.Test;

import com.tdd.expensetracker.model.Category;
import com.tdd.expensetracker.model.Expense;

public class ExpenseMysqlRepositoryTest {

	private SessionFactory sessionFactory;
	private static StandardServiceRegistry registry;
	private ExpenseMysqlRepository expenseMysqlRepository;
	private Category category;

	@BeforeClass
	public static void setupServer() {

		registry = new StandardServiceRegistryBuilder().configure("hibernate-test.cfg.xml").build();
	}

	@AfterClass
	public static void shutdownServer() {

		StandardServiceRegistryBuilder.destroy(registry);
	}

	@Before
	public void setup() {

		sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
		expenseMysqlRepository = new ExpenseMysqlRepository(sessionFactory);
		category = new Category("1", "name1", "description1");
		saveCategory(category);
	}

	@Test
	public void testFindAllWhenDatabaseIsEmpty() {

		assertThat(expenseMysqlRepository.findAll()).isEmpty();
	}

	@Test
	public void testFindAllWhenDatabaseIsNotEmpty() {

		String id1 = addTestExpenseToDatabase(new Expense(50d, "test1", LocalDate.now(), category));
		String id2 = addTestExpenseToDatabase(new Expense(500d, "test2", LocalDate.now(), category));

		Assertions.assertThat(expenseMysqlRepository.findAll())
				.containsExactly(new Expense[] { new Expense(id1, 50d, "test1", LocalDate.now(), category),
						new Expense(id2, 500d, "test2", LocalDate.now(), category) });
	}

	@Test
	public void testFindByIdNotFound() {

		Assertions.assertThat(expenseMysqlRepository.findById("1")).isNull();
	}

	@Test
	public void testFindByIdFound() {

		addTestExpenseToDatabase(new Expense(50d, "test1", LocalDate.now(), category));
		String id = addTestExpenseToDatabase(new Expense(50d, "test2", LocalDate.now(), category));

		String expected = new Expense(id, 50d, "test2", LocalDate.now(), category).toString();
		String actual = expenseMysqlRepository.findById(id).toString();

		Assertions.assertThat(actual).isEqualTo(expected);
	}

	@Test
	public void testSave() {

		Expense expense = new Expense(50d, "test2", LocalDate.now(), category);
		expenseMysqlRepository.save(expense);

		Assertions.assertThat(readAllExpenseFromDatabase()).containsExactly(new Expense[] { expense });
	}

	@Test
	public void testDelete() {

		String id = addTestExpenseToDatabase(new Expense(50d, "test2", LocalDate.now(), category));
		Expense expenseToDelete = new Expense(id, 50d, "test2", LocalDate.now(), category);

		expenseMysqlRepository.delete(expenseToDelete);
		Assertions.assertThat(readAllExpenseFromDatabase()).isEmpty();
	}

	@Test
	public void testUpdate() {

		String expenseToUpdate = addTestExpenseToDatabase(new Expense(50d, "test1", LocalDate.now(), category));
		Expense updatedExpense = new Expense(expenseToUpdate, 60.0, "updated name", LocalDate.now().plusDays(5),
				category);

		expenseMysqlRepository.update(updatedExpense);
		Assertions.assertThat(readAllExpenseFromDatabase()).containsExactly(new Expense[] { updatedExpense });
	}

	private List<Expense> readAllExpenseFromDatabase() {

		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();

		List<Expense> allExpense = session.createQuery("from Expense", Expense.class).list();

		tx.commit();
		session.close();

		return allExpense;

	}

	private String addTestExpenseToDatabase(Expense expense) {

		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();

		session.save(expense);

		tx.commit();
		session.close();

		return expense.getId();
	}

	private void saveCategory(Category category) {

		Session session = sessionFactory.openSession();
		session.beginTransaction();

		session.save(category);

		session.getTransaction().commit();
		session.close();
	}
}
