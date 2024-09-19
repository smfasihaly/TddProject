package com.tdd.expensetracker.app;

import java.awt.EventQueue;
import java.util.concurrent.Callable;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import com.tdd.expensetracker.controller.CategoryController;
import com.tdd.expensetracker.controller.ExpenseController;
import com.tdd.expensetracker.repository.mysql.CategoryMySqlRepository;
import com.tdd.expensetracker.repository.mysql.ExpenseMysqlRepository;
import com.tdd.expensetracker.view.swing.CategorySwingView;
import com.tdd.expensetracker.view.swing.ExpenseSwingView;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

// Main class for the Expense Tracker Swing Application
@Command(mixinStandardHelpOptions = true)
public class ExpenseTrackerSwingApp implements Callable<Void> {

	/**
	 * The database URL to connect to the MySQL database. This is set via the
	 * command line option "--mysql-DB_URL".
	 */
	@Option(names = { "--mysql-DB_URL" }, description = "mysql DB_URL ")
	private String url = "jdbc:mysql://localhost:3307/expense_tracker";

	/**
	 * The username for the MySQL database. This is set via the command line option
	 * "--mysql-user".
	 */
	@Option(names = { "--mysql-user" }, description = "mysql user")
	private String user = "test";

	/**
	 * The password for the MySQL database. This is set via the command line option
	 * "--mysql-pass".
	 */
	@Option(names = { "--mysql-pass" }, description = "mysql pass")
	private String password = "test";

	// Logger for logging errors and information.
	private static final Logger LOGGER = LogManager.getLogger(ExpenseTrackerSwingApp.class);

	// Hibernate service registry for database configuration.
	private StandardServiceRegistry registry;

	public static void main(String[] args) {
		try {
			// Sets the look and feel of the UI to GTK, if available.
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			LOGGER.error("context", e);
		}
		// Initializes the application using picocli's CommandLine to parse command-line
		// arguments.
		new CommandLine(new ExpenseTrackerSwingApp()).execute(args);
	}

	// Method invoked when the application is called.
	@Override
	public Void call() throws Exception {
		// Runs the UI-related code on the EventQueue thread to ensure thread safety in
		// Swing.
		EventQueue.invokeLater(() -> {
			try {
				// Fetch the current environment property to determine which configuration to
				// load.
				String environment = System.getProperty("ENVIRONMENT");

				// If the environment is set to "testWithEclipes", load the testing
				// configuration.
				if ("testWithEclipes".equals(environment)) {
					registry = new StandardServiceRegistryBuilder().configure("hibernate-IT.cfg.xml")
							.applySetting("hibernate.connection.url", url)
							.applySetting("hibernate.connection.username", user)
							.applySetting("hibernate.hbm2ddl.auto", "validate")
							.applySetting("hibernate.connection.password", password).build();
				} else {
					// For other environments, load the default configuration.
					registry = new StandardServiceRegistryBuilder().configure().build();
				}

				// Sets up Hibernate's SessionFactory for managing database connections.
				MetadataSources metadataSources = new MetadataSources(registry);
				SessionFactory sessionFactory = metadataSources.buildMetadata().buildSessionFactory();

				// Initialize the Swing views for expenses and categories.
				ExpenseSwingView expenseView = new ExpenseSwingView();
				CategorySwingView categoryView = new CategorySwingView();

				// Initialize the repositories for interacting with the MySQL database.
				ExpenseMysqlRepository expenseRepository = new ExpenseMysqlRepository(sessionFactory);
				CategoryMySqlRepository categoryRepository = new CategoryMySqlRepository(sessionFactory);

				// Create controllers to handle user interactions and business logic.
				ExpenseController expenseController = new ExpenseController(expenseView, expenseRepository,
						categoryRepository);
				CategoryController categoryController = new CategoryController(categoryView, categoryRepository);

				// Link views with their respective controllers.
				expenseView.setExpenseController(expenseController);
				expenseView.setCategoryView(categoryView);

				// Load all expenses and make the view visible to the user.
				expenseController.allExpense();
				expenseView.setVisible(true);

				// Link category view to category controller and load all categories.
				categoryView.setCategoryController(categoryController);
				categoryController.allCategory();
				categoryView.setExpenseView(expenseView);

			} catch (Exception e) {
				// Logs any exceptions that occur during initialization.
				LOGGER.error("context", e);
			}
		});
		return null;
	}
}
