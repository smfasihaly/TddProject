package com.tdd.expensetracker.view.swing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.concurrent.TimeUnit;

import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.hibernate.SessionFactory;
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
import com.tdd.expensetracker.repository.mysql.CategoryMySqlRepository;

public class CategoryModelViewControllerIT extends AssertJSwingJUnitTestCase {

	// MySQLContainer setup using Testcontainers for integration testing
	@SuppressWarnings("resource")
	@ClassRule
	public static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>(DockerImageName.parse("mysql:8.0.28"))
			.withDatabaseName("test").withUsername("test").withPassword("test");

	private static SessionFactory sessionFactory;
	private static StandardServiceRegistry registry;
	private FrameFixture window;
	private CategoryMySqlRepository categoryRepository;
	private CategoryController categoryController;

	// Set up the MySQL container and Hibernate session before running the tests
	@BeforeClass
	public static void setupContainer() {
		mysqlContainer.start();
		registry = new StandardServiceRegistryBuilder().configure("hibernate-IT.cfg.xml")
				.applySetting("hibernate.connection.url", mysqlContainer.getJdbcUrl())
				.applySetting("hibernate.connection.username", mysqlContainer.getUsername())
				.applySetting("hibernate.connection.password", mysqlContainer.getPassword()).build();
	}

	// Tear down the session and stop the MySQL container after the tests
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
		window = new FrameFixture(robot(), GuiActionRunner.execute(() -> {
			CategorySwingView categorySwingView = new CategorySwingView();
			categoryController = new CategoryController(categorySwingView, categoryRepository);
			categorySwingView.setCategoryController(categoryController);
			return categorySwingView;
		}));
		window.show(); // Shows the UI frame for testing
	}

	// Test adding a category through the UI
	@Test
	public void testAddCategory() {
		// Set the values for the category fields and click the "Add" button
		setFieldValues("bills", "other");
		window.button(JButtonMatcher.withText("Add Category")).click();

		// Wait and verify that the category is added to the repository
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(categoryRepository.findAll()).isNotEmpty());
		Category createdCategory = categoryRepository.findAll().get(0);
		assertThat(categoryRepository.findById(createdCategory.getId()))
				.isEqualTo(new Category(createdCategory.getId(), "bills", "other"));
	}

	// Test deleting a category through the UI
	@Test
	public void testDeleteCategory() {
		// Create a category in the repository
		Category category = new Category("bills", "description");
		categoryRepository.save(category);

		// Populate the view with the category
		GuiActionRunner.execute(() -> categoryController.allCategory());

		// Select the category and click "Delete"
		window.list().selectItem(0);
		window.button(JButtonMatcher.withText("Delete Selected")).click();

		// Wait and verify that the category is deleted from the repository
		await().atMost(5, TimeUnit.SECONDS)
				.untilAsserted(() -> assertThat(categoryRepository.findById(category.getId())).isNull());
	}

	// Test updating a category through the UI
	@Test
	public void testUpdateCategory() {
		// Create a category in the repository
		Category category = new Category("bills", "description");
		categoryRepository.save(category);

		// Populate the view with the category
		GuiActionRunner.execute(() -> categoryController.allCategory());

		// Select the category, update its description, and click "Update"
		window.list().selectItem(0);
		window.button(JButtonMatcher.withText("Update Selected")).click();
		Category updatedCategory = new Category(category.getId(), "bills", "changes");
		window.textBox("descriptionTextBox").setText(updatedCategory.getDescription());
		window.button(JButtonMatcher.withText("Update Category")).click();

		// Wait and verify that the category is updated in the repository
		await().atMost(5, TimeUnit.SECONDS).untilAsserted(
				() -> assertThat(categoryRepository.findById(category.getId())).isEqualTo(updatedCategory));
	}

	// Helper method to set values in the category fields
	private void setFieldValues(String name, String description) {
		window.textBox("nameTextBox").enterText(name);
		window.textBox("descriptionTextBox").enterText(description);
	}
}
