package com.tdd.expensetracker.view.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.sql.Date;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import com.tdd.expensetracker.controller.ExpenseController;
import com.tdd.expensetracker.model.Category;
import com.tdd.expensetracker.model.Expense;
import com.tdd.expensetracker.repository.mysql.CategoryMySqlRepository;
import com.tdd.expensetracker.repository.mysql.ExpenseMysqlRepository;
import com.toedter.calendar.JDateChooser;

@RunWith(GUITestRunner.class)
public class ExpenseModelViewControllerIT extends AssertJSwingJUnitTestCase {

	// Setting up a MySQL TestContainer to simulate the database for tests
	@SuppressWarnings("resource")
	@ClassRule
	public static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>(DockerImageName.parse("mysql:8.0.28"))
			.withDatabaseName("test").withUsername("test").withPassword("test");

	private static SessionFactory sessionFactory;
	private static StandardServiceRegistry registry;

	private FrameFixture window;
	private ExpenseMysqlRepository expenseRepository;
	private ExpenseController expenseController;

	private Category category;

	// Start the MySQL container and set up the Hibernate configuration
	@BeforeClass
	public static void setupContainer() {
		mysqlContainer.start();
		registry = new StandardServiceRegistryBuilder().configure("hibernate-IT.cfg.xml")
				.applySetting("hibernate.connection.url", mysqlContainer.getJdbcUrl())
				.applySetting("hibernate.connection.username", mysqlContainer.getUsername())
				.applySetting("hibernate.connection.password", mysqlContainer.getPassword()).build();
	}

	// Shut down the container and session factory after all tests
	@AfterClass
	public static void shutdownServer() {
		StandardServiceRegistryBuilder.destroy(registry);
		if (sessionFactory != null) {
			sessionFactory.close();
		}
		mysqlContainer.stop();
	}

	// Set up the view, controller, and repositories for each test case
	@Override
	protected void onSetUp() throws Exception {
		MetadataSources metadataSources = new MetadataSources(registry);
		sessionFactory = metadataSources.buildMetadata().buildSessionFactory();
		expenseRepository = new ExpenseMysqlRepository(sessionFactory);
		category = new Category("1", "name1", "description1");
		CategoryMySqlRepository categoryRepository = new CategoryMySqlRepository(sessionFactory);
		categoryRepository.save(category);

		// Initialize the ExpenseSwingView and set its controller
		window = new FrameFixture(robot(), GuiActionRunner.execute(() -> {
			ExpenseSwingView expenseSwingView = new ExpenseSwingView();
			expenseController = new ExpenseController(expenseSwingView, expenseRepository, categoryRepository);
			expenseSwingView.setExpenseController(expenseController);
			return expenseSwingView;
		}));
		window.show(); // shows the frame for testing

		// Load available categories into the combo box
		GuiActionRunner.execute(() -> expenseController.allCategory());
		window.comboBox("categoryComboBox").clearSelection();
	}

	// Test the ability to add a new expense
	@Test
	public void testAddExpense() {
		setFieldValues("testExpense", "5000", LocalDate.now(), category);
		window.button(JButtonMatcher.withText("Add Expense")).click();
		await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> assertThat(expenseRepository.findAll()).isNotEmpty());

		Expense createdExpense = expenseRepository.findAll().get(0);
		assertThat(expenseRepository.findById(createdExpense.getId()))
				.isEqualTo(new Expense(createdExpense.getId(), 5000d, "testExpense", LocalDate.now(), category));
	}

	// Test the ability to update an existing expense
	@Test
	public void testUpdateExpense() {
		Expense expense = new Expense(5000d, "testExpense", LocalDate.now(), category);
		expenseRepository.save(expense);

		// Populate the view's list with the existing expense
		GuiActionRunner.execute(() -> expenseController.allExpense());
		window.list().selectItem(0);
		window.button(JButtonMatcher.withText("Update Selected")).click();

		Expense updatedExpense = new Expense(expense.getId(), expense.getAmount(), "change description",
				expense.getDate(), expense.getCategory());
		window.textBox("descriptionTextBox").setText(updatedExpense.getDescription());
		window.button(JButtonMatcher.withText("Update Expense")).click();

		await().atMost(5, TimeUnit.SECONDS).untilAsserted(
				() -> assertThat(assertThat(expenseRepository.findById(expense.getId())).isEqualTo(updatedExpense)));
	}

	// Test the ability to delete an existing expense
	@Test
	public void testDeleteExpense() {
		Expense expense = new Expense(5000d, "testExpense", LocalDate.now(), category);
		expenseRepository.save(expense);

		// Populate the view's list with the existing expense
		GuiActionRunner.execute(() -> expenseController.allExpense());
		window.list().selectItem(0);
		window.button(JButtonMatcher.withText("Delete Selected")).click();

		await().atMost(5, TimeUnit.SECONDS)
				.untilAsserted(() -> assertThat(expenseRepository.findById(expense.getId())).isNull());
	}

	// Helper method to populate the expense form fields with given values
	private void setFieldValues(String description, String amount, LocalDate date, Category category) {
		window.textBox("descriptionTextBox").enterText(description);
		window.textBox("amountTextBox").enterText(amount);

		if (category != null) {
			window.comboBox("categoryComboBox").selectItem(0);
		} else {
			window.comboBox("categoryComboBox").clearSelection();
		}

		if (date != null) {
			JDateChooser jdateChooser = window.robot().finder().findByName("expenseDateChooser", JDateChooser.class,
					false);
			GuiActionRunner.execute(() -> {
				Date localDateToDate = java.sql.Date.valueOf(date);
				jdateChooser.setDate(localDateToDate);
			});
		}
	}
}
