package com.tdd.expensetracker.bdd.steps;

import java.time.LocalDate;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import com.tdd.expensetracker.model.Category;
import com.tdd.expensetracker.model.Expense;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;

public class DatabaseSteps {

	static final String CATEGORY_DESCRIPTION1 = "utilities";

	static final String CATEGORY_NAME1 = "bills";

	static final double EXPENSE_AMOUNT1 = 5000.0;

	static final String EXPENSE_NAME1 = "first expense";

	static final Double EXPENSE_AMOUNT2 = 10.0;

	static final String EXPENSE_NAME2 = "ExpenseName2";

	static final String CATEGORY_NAME2 = "categoryName2";

	static final String CATEGORY_DESCRIPTION2 = "categoryDescription2";

	private static SessionFactory sessionFactory;

	private static StandardServiceRegistry registry;

	private Expense firstExpense;

	private Category firstCategory;

	@Before
	public void setup() {
		registry = new StandardServiceRegistryBuilder().configure("hibernate-IT.cfg.xml")
				.applySetting("hibernate.connection.url", "jdbc:mysql://localhost:3307/expense_tracker")
				.applySetting("hibernate.connection.password", "test").build();

		MetadataSources metadataSources = new MetadataSources(registry);
		sessionFactory = metadataSources.buildMetadata().buildSessionFactory();
	}

	@After
	public void tearDown() {
		StandardServiceRegistryBuilder.destroy(registry);
		if (sessionFactory != null) {
			sessionFactory.close();
		}
	}

	@Given("The database contains the Expense with the following values")
	public void the_database_contains_the_expenses_with_the_following_values(List<List<String>> values) {

		Session session = sessionFactory.openSession();
		session.beginTransaction();

		values.forEach(expenseValues -> {
			Category category = new Category(expenseValues.get(3), expenseValues.get(4));
			session.save(category);
			Expense expense = new Expense(Double.parseDouble(expenseValues.get(0)), expenseValues.get(1),
					LocalDate.parse(expenseValues.get(2)), category);
			session.save(expense);
		});

		session.getTransaction().commit();
		session.close();
	}

	@Given("The database contains category with the following values")
	public void the_database_contains_category_with_the_following_values(List<List<String>> values) {
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

		Category newCat = new Category(CATEGORY_NAME1, CATEGORY_DESCRIPTION1);
		addTestCategoryToDatabase(newCat);
		firstExpense = new Expense(EXPENSE_AMOUNT1, EXPENSE_NAME1, LocalDate.now(), newCat);
		addTestExpenseToDatabase(firstExpense);
		addTestExpenseToDatabase(new Expense(EXPENSE_AMOUNT2, EXPENSE_NAME2, LocalDate.now(), newCat));
	}

	@Given("The expense is in the meantime removed from the database")
	public void the_expense_is_in_the_meantime_removed_from_the_database() {
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		session.delete(firstExpense);

		session.getTransaction().commit();
		session.close();
	}

	@Given("The database contains a few category")
	public void the_database_contains_a_few_category() {

		firstCategory = new Category(CATEGORY_NAME1, CATEGORY_DESCRIPTION1);
		addTestCategoryToDatabase(firstCategory);
		addTestCategoryToDatabase(new Category(CATEGORY_NAME2, CATEGORY_DESCRIPTION2));

	}

	@Given("The category is in the meantime removed from the database")
	public void the_category_is_in_the_meantime_removed_from_the_database() {
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		session.delete(firstCategory);

		session.getTransaction().commit();
		session.close();
	}

	private void addTestExpenseToDatabase(Expense expense) {

		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();

		session.save(expense);

		tx.commit();
		session.close();

	}

	private void addTestCategoryToDatabase(Category category) {

		Session session = sessionFactory.openSession();
		session.beginTransaction();

		session.save(category);

		session.getTransaction().commit();
		session.close();
	}

}
