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

@Command(mixinStandardHelpOptions = true)
public class ExpenseTrackerSwingApp implements Callable<Void>{

	/** The url. */
	@Option(names = { "--mysql-DB_URL" }, description = "mysql DB_URL ")
	private String url = "jdbc:mysql://localhost:3307/expense_tracker";

	/** The user. */
	@Option(names = { "--mysql-user" }, description = "mysql user")
	private String user = "test";

	/** The password. */
	@Option(names = { "--mysql-pass" }, description = "mysql pass")
	private String password= "test";

	
	private static final Logger LOGGER = LogManager.getLogger(ExpenseTrackerSwingApp.class);
	private static StandardServiceRegistry registry;

	public static void main(String[] args) {
		
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			LOGGER.error("context", e);
		}
		new CommandLine(new ExpenseTrackerSwingApp()).execute(args);
		
	}

	@Override
	public Void call() throws Exception {
		EventQueue.invokeLater(() -> {
			try {
				 String environment = System.getProperty("ENVIRONMENT"); 
			    
				if ("testWithEclipes".equals(environment)) {
					registry = new StandardServiceRegistryBuilder().configure("hibernate-IT.cfg.xml")
							.applySetting("hibernate.connection.url", url)
							.applySetting("hibernate.connection.username", user)
							.applySetting("hibernate.hbm2ddl.auto", "validate")
							.applySetting("hibernate.connection.password", password).build();
				} else {
					registry = new StandardServiceRegistryBuilder().configure().build();
				}
				MetadataSources metadataSources = new MetadataSources(registry);

				SessionFactory sessionFactory = metadataSources.buildMetadata().buildSessionFactory();

				ExpenseSwingView expenseView = new ExpenseSwingView();
				CategorySwingView categoryView = new CategorySwingView();

				ExpenseMysqlRepository expenseRepository = new ExpenseMysqlRepository(sessionFactory);
				CategoryMySqlRepository categoryRepository = new CategoryMySqlRepository(sessionFactory);

				ExpenseController expenseController = new ExpenseController(expenseView, expenseRepository,
						categoryRepository);
				CategoryController categoryController = new CategoryController(categoryView, categoryRepository);

				expenseView.setExpenseController(expenseController);
				expenseView.setCategoryView(categoryView);

				expenseController.allExpense();
				expenseView.setVisible(true);

				categoryView.setCategoryController(categoryController);
				categoryController.allCategory();
				categoryView.setExpenseView(expenseView);

			} catch (Exception e) {
				LOGGER.error("context", e);
			}
		});
		return null;
	}
}
