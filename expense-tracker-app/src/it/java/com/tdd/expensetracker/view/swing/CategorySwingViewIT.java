package com.tdd.expensetracker.view.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import org.assertj.swing.annotation.GUITest;
import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import com.tdd.expensetracker.controller.CategoryController;
import com.tdd.expensetracker.model.Category;
import com.tdd.expensetracker.model.Expense;
import com.tdd.expensetracker.repository.mysql.CategoryMySqlRepository;

public class CategorySwingViewIT extends AssertJSwingJUnitTestCase {

	// Testcontainers setup for MySQL
	@SuppressWarnings("resource")
	@ClassRule
	public static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>(DockerImageName.parse("mysql:8.0.28"))
			.withDatabaseName("test").withUsername("test").withPassword("test");

	private static SessionFactory sessionFactory;
	private static StandardServiceRegistry registry;

	private FrameFixture window;
	private CategorySwingView categorySwingView;
	private CategoryController categoryController;
	private CategoryMySqlRepository categoryRepository;

	// Set up the MySQL container and Hibernate session before tests
	@BeforeClass
	public static void setupContainer() {
		mysqlContainer.start();
		registry = new StandardServiceRegistryBuilder().configure("hibernate-IT.cfg.xml")
				.applySetting("hibernate.connection.url", mysqlContainer.getJdbcUrl())
				.applySetting("hibernate.connection.username", mysqlContainer.getUsername())
				.applySetting("hibernate.connection.password", mysqlContainer.getPassword()).build();
	}

	// Tear down the session and stop the MySQL container after tests
	@AfterClass
	public static void shutdownServer() {
		StandardServiceRegistryBuilder.destroy(registry);
		if (sessionFactory != null) {
			sessionFactory.close();
		}
		mysqlContainer.stop();
	}

	// Set up the UI and repositories before each test
	@Override
	protected void onSetUp() throws Exception {
		MetadataSources metadataSources = new MetadataSources(registry);
		sessionFactory = metadataSources.buildMetadata().buildSessionFactory();
		categoryRepository = new CategoryMySqlRepository(sessionFactory);
		GuiActionRunner.execute(() -> {
			categorySwingView = new CategorySwingView();
			categoryController = new CategoryController(categorySwingView, categoryRepository);
			categorySwingView.setCategoryController(categoryController);
			return categorySwingView;
		});
		window = new FrameFixture(robot(), categorySwingView);
		window.show();
	}

	// Test fetching and displaying all categories in the view
	@Test
	@GUITest
	public void testAllCategory() {
		Category category = new Category("bills", "description");
		Category category2 = new Category("shopping", "description2");
		categoryRepository.save(category);
		categoryRepository.save(category2);
		GuiActionRunner.execute(() -> categoryController.allCategory());
		assertThat(window.list().contents()).containsExactlyInAnyOrder(getDisplayString(category),
				getDisplayString(category2));
	}

	// Test adding a new category successfully
	@Test
	@GUITest
	public void testAddButtonSuccess() {
		setFieldValues("bills", "other");
		window.button(JButtonMatcher.withText("Add Category")).click();
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(categoryRepository.findAll()).isNotEmpty());
		Category createdCategory = categoryRepository.findAll().get(0);
		assertThat(window.list().contents())
				.containsExactlyInAnyOrder(getDisplayString(new Category(createdCategory.getId(), "bills", "other")));
	}

	// Test adding a category that already exists, expecting an error
	@Test
	@GUITest
	public void testAddButtonError() {
		Category category = new Category("bills", "description");
		categoryRepository.save(category);
		setFieldValues("bills", "other");
		window.button(JButtonMatcher.withText("Add Category")).click();
		await().atMost(5, TimeUnit.SECONDS)
				.untilAsserted(() -> assertThat(window.label("errorMessageLabel").text().trim()).isNotBlank());
		assertThat(window.list().contents()).isEmpty();
		window.label("errorMessageLabel").requireText("Already existing category with name " + category.getName() + ": "
				+ new Category(category.getId(), "bills", "description").toString());
	}

	// Test deleting a category successfully
	@Test
	@GUITest
	public void testDeleteButtonSuccess() {
		GuiActionRunner.execute(() -> categoryController.newCategory(new Category("bills", "description")));
		window.list().selectItem(0);
		window.button(JButtonMatcher.withText("Delete Selected")).click();
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(window.list().contents()).isEmpty());
	}

	// Test deleting a category that doesn't exist, expecting an error
	@Test
	@GUITest
	public void testDeleteButtonError() {
		Category category = new Category("1", "bills", "description");
		GuiActionRunner.execute(() -> categorySwingView.getListCategoryModel().addElement(category));
		window.list().selectItem(0);
		window.button(JButtonMatcher.withText("Delete Selected")).click();
		await().atMost(5, TimeUnit.SECONDS)
				.untilAsserted(() -> assertThat(window.label("errorMessageLabel").text().trim()).isNotBlank());
		assertThat(window.list().contents()).isEmpty();
		window.label("errorMessageLabel").requireText("Category does not exist with id 1: " + category);
	}

	// Test updating a category successfully
	@Test
	@GUITest
	public void testUpdateButtonSuccess() {
		Category category = new Category("bills", "description");
		GuiActionRunner.execute(() -> categoryController.newCategory(category));
		window.list().selectItem(0);
		window.button(JButtonMatcher.withText("Update Selected")).click();
		Category updatedCategory = new Category(category.getId(), "bills", "changes");
		window.textBox("descriptionTextBox").setText(updatedCategory.getDescription());
		window.button(JButtonMatcher.withText("Update Category")).click();
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(
				() -> assertThat(window.list().contents()).containsExactly(getDisplayString(updatedCategory)));
	}

	// Test updating a category that has a name conflict, expecting an error
	@Test
	@GUITest
	public void testUpdateButtonError() {
		Category category = new Category("bills", "utility");
		Category category1 = new Category("Rent", "description");
		GuiActionRunner.execute(() -> {
			categoryController.newCategory(category);
			categoryController.newCategory(category1);
		});
		window.list().selectItem(1);
		window.button(JButtonMatcher.withText("Update Selected")).click();
		window.textBox("nameTextBox").setText("bills");
		window.button(JButtonMatcher.withText("Update Category")).click();
		await().atMost(5, TimeUnit.SECONDS)
				.untilAsserted(() -> assertThat(window.label("errorMessageLabel").text().trim()).isNotBlank());
		assertThat(window.list().contents()).containsExactly(getDisplayString(category), getDisplayString(category1));
		window.label("errorMessageLabel").requireText("Already existing category with name bills: " + category);
	}

	// Test showing expenses for a category
	@Test
	public void testExpenseTableShouldBePopulatedWithExpensesWhenShowExpensesButtonClicked() {
		Category category = new Category("bills", "utility");
		GuiActionRunner.execute(() -> {
			categoryController.newCategory(category);
		});

		// Saving an expense in the database
		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		Expense expense = new Expense(550d, "expense", LocalDate.now(), category);
		session.save(expense);
		category.getExpenses().add(expense);
		tx.commit();
		session.close();

		Object[][] expectedContents = { { "550.0", "expense", LocalDate.now().toString() } };
		window.list("categoryList").selectItem(0);
		window.button(JButtonMatcher.withText("Show Expenses")).click();
		assertThat(window.table("expenseTable").contents()).isEqualTo(expectedContents);
	}

	// Helper method to set field values in the form
	private void setFieldValues(String name, String description) {
		window.textBox("nameTextBox").enterText(name);
		window.textBox("descriptionTextBox").enterText(description);
	}

	// Helper method to format category display strings
	private String getDisplayString(Category category) {
		return category.getId() + " | " + category.getName() + " | " + category.getDescription();
	}
}
