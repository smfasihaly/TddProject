package com.tdd.expensetracker.view.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.sql.Date;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import javax.swing.JTextField;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.runner.GUITestRunner;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.hibernate.Session;
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
import com.tdd.expensetracker.repository.CategoryRepository;
import com.tdd.expensetracker.repository.mysql.CategoryMySqlRepository;
import com.tdd.expensetracker.repository.mysql.ExpenseMysqlRepository;
import com.toedter.calendar.JDateChooser;

@RunWith(GUITestRunner.class)
public class ExpenseSwingViewIT extends AssertJSwingJUnitTestCase {

	@SuppressWarnings("resource")
	@ClassRule
	public static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>(DockerImageName.parse("mysql:8.0.28"))
			.withDatabaseName("test").withUsername("test").withPassword("test");

	private static SessionFactory sessionFactory;
	private static StandardServiceRegistry registry;

	private FrameFixture window;
	private ExpenseSwingView expenseSwingView;
	private ExpenseMysqlRepository expenseRepository;
	private ExpenseController expenseController;

	private CategoryRepository categoryRepository;

	private Category category;

	@BeforeClass
	public static void setupContainer() {
		mysqlContainer.start();
		registry = new StandardServiceRegistryBuilder().configure("hibernate-IT.cfg.xml")
				.applySetting("hibernate.connection.url", mysqlContainer.getJdbcUrl())
				.applySetting("hibernate.connection.username", mysqlContainer.getUsername())
				.applySetting("hibernate.connection.password", mysqlContainer.getPassword()).build();

	}

	@AfterClass
	public static void shutdownServer() {

		StandardServiceRegistryBuilder.destroy(registry);
		if (sessionFactory != null) {
			sessionFactory.close();
		}
		mysqlContainer.stop();
	}

	@Override
	protected void onSetUp() throws Exception {

		MetadataSources metadataSources = new MetadataSources(registry);
		sessionFactory = metadataSources.buildMetadata().buildSessionFactory();
		expenseRepository = new ExpenseMysqlRepository(sessionFactory);
		categoryRepository = new CategoryMySqlRepository(sessionFactory);
		category = new Category("1", "name1", "description1");
		saveCategory(category);

		GuiActionRunner.execute(() -> {
			expenseSwingView = new ExpenseSwingView();
			expenseController = new ExpenseController(expenseSwingView, expenseRepository, categoryRepository);
			expenseSwingView.setExpenseController(expenseController);
			return expenseSwingView;
		});

		window = new FrameFixture(robot(), expenseSwingView);
		window.show();

		GuiActionRunner.execute(() -> expenseController.allCategory());
		window.comboBox("categoryComboBox").clearSelection();
	}

	@Test
	@GUITest
	public void testAllExpense() {
		Expense expense = new Expense(5000d, "Bills", LocalDate.now(), category);
		Expense expense2 = new Expense(50d, "Shopping", LocalDate.now(), category);
		expenseRepository.save(expense);
		expenseRepository.save(expense2);

		GuiActionRunner.execute(() -> expenseController.allExpense());

		assertThat(window.list().contents()).containsExactlyInAnyOrder(expense.toString(), expense2.toString());
	}

	@Test
	@GUITest
	public void testAddButtonSuccess() {

		setFieldValues("testExpense", "5000", LocalDate.now(), category);
		window.button(JButtonMatcher.withText("Add Expense")).click();
		await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> assertThat(expenseRepository.findAll()).isNotEmpty());
		Expense createdExpense = expenseRepository.findAll().get(0);
		assertThat(window.list().contents()).containsExactly(
				new Expense(createdExpense.getId(), 5000d, "testExpense", LocalDate.now(), category).toString());
	}

	@Test
	@GUITest
	public void testAddButtonError() {

		setFieldValues("testExpense", "5000", LocalDate.now().plusDays(5), category);
		window.button(JButtonMatcher.withText("Add Expense")).click();

		await().atMost(5, TimeUnit.SECONDS)
				.untilAsserted(() -> assertThat(window.label("errorMessageLabel").text().trim()).isNotEmpty());

		assertThat(window.list().contents()).isEmpty();
		window.label("errorMessageLabel").requireText("Date cannot be in the future: "
				+ new Expense(5000d, "testExpense", LocalDate.now().plusDays(5), category));
	}

	@Test
	@GUITest
	public void testDeleteButtonSuccess() {
		// use the controller to populate the view's list...
		GuiActionRunner
				.execute(() -> expenseController.newExpense(new Expense(5000d, "Bills", LocalDate.now(), category)));
		// ...with a expense to select
		window.list().selectItem(0);
		window.button(JButtonMatcher.withText("Delete Selected")).click();
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(window.list().contents()).isEmpty());
	}

	@Test
	@GUITest
	public void testDeleteButtonError() {
		// use the controller to populate the view's list...
		Expense expense = new Expense("1", 5000d, "Bills", LocalDate.now(), category);
		GuiActionRunner.execute(() -> expenseSwingView.getListExpenseModel().addElement(expense));
		window.list().selectItem(0);
		window.button(JButtonMatcher.withText("Delete Selected")).click();
		await().atMost(5, TimeUnit.SECONDS)
				.untilAsserted(() -> assertThat(window.label("errorMessageLabel").text().trim()).isNotEmpty());

		assertThat(window.list().contents()).containsExactly(expense.toString());
		window.label("errorMessageLabel").requireText("Expense does not exist with id 1: " + expense);

	}

	@Test
	@GUITest
	public void testUpdateButtonSuccess() {

		Expense expense = new Expense(5000d, "Bills", LocalDate.now(), category);
		GuiActionRunner.execute(() -> expenseController.newExpense(expense));
		window.list().selectItem(0);
		window.button(JButtonMatcher.withText("Update Selected")).click();
		Expense updatedexpense = new Expense(expense.getId(), 5000d, "change description", LocalDate.now(), category);

		window.textBox("descriptionTextBox").setText(updatedexpense.getDescription());
		window.button(JButtonMatcher.withText("Update Expense")).click();
		await().atMost(15, TimeUnit.SECONDS)
				.untilAsserted(() -> assertThat(window.list().contents()).containsExactly(updatedexpense.toString()));

	}

	@Test
	@GUITest
	public void testUpdateButtonError() {

		Expense expense = new Expense("1", 5000d, "Bills", LocalDate.now(), category);
		GuiActionRunner.execute(() -> expenseSwingView.getListExpenseModel().addElement(expense));
		window.list().selectItem(0);
		window.button(JButtonMatcher.withText("Update Selected")).click();

		JTextField idTextBox = window.robot().finder().findByName("idTextBox", JTextField.class, false);
		GuiActionRunner.execute(() -> idTextBox.setText("1"));

		window.button(JButtonMatcher.withText("Update Expense")).click();

		await().atMost(5, TimeUnit.SECONDS)
				.untilAsserted(() -> assertThat(window.label("errorMessageLabel").text().trim()).isNotEmpty());

		assertThat(window.list().contents()).containsExactly(expense.toString());

		window.label("errorMessageLabel").requireText("Expense does not exist with id 1: " + expense);

	}

	private void saveCategory(Category category) {

		Session session = sessionFactory.openSession();
		session.beginTransaction();

		session.save(category);

		session.getTransaction().commit();
		session.close();
	}

	private void setFieldValues(String description, String amount, LocalDate date, Category category) {

		window.textBox("descriptionTextBox").enterText(description);
		window.textBox("amountTextBox").enterText(amount);
		if (category != null) {
			window.comboBox("categoryComboBox").selectItem(0);
		} else {
			window.comboBox("categoryComboBox").clearSelection();
		}
		if (date != null) {

			JDateChooser jdateChooser = window.robot().finder().findByName("JDateChooser", JDateChooser.class, false);
			GuiActionRunner.execute(() -> {
				Date localDateToDate = java.sql.Date.valueOf(date);
				jdateChooser.setDate(localDateToDate);
			});
		}
	}

}
