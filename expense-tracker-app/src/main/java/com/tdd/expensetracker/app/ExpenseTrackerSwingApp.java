package com.tdd.expensetracker.app;

import java.awt.EventQueue;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

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

/**
 * Hello world!
 *
 */
public class ExpenseTrackerSwingApp {

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			 			e1.printStackTrace();
		}
		EventQueue.invokeLater(() -> {
			try {

				StandardServiceRegistry registry = new StandardServiceRegistryBuilder().configure().build();
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
				//categoryView.setVisible(false);

			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}
}
