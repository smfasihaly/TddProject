package com.tdd.expensetracker.repository.mysql;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.tdd.expensetracker.model.Expense;
import com.tdd.expensetracker.repository.ExpenseRepository;

public class ExpenseMysqlRepository implements ExpenseRepository {

	private SessionFactory sessionFactory;
	private static final Logger LOGGER = LogManager.getLogger(ExpenseMysqlRepository.class);

	// Constructor to initialize the repository with a session factory
	public ExpenseMysqlRepository(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	// Retrieves all Expense records from the database
	@Override
	public List<Expense> findAll() {
		Session session = sessionFactory.openSession();
		try {
			return session.createQuery("from Expense", Expense.class).list();
		} finally {
			session.close();
		}
	}

	// Finds an Expense by its unique ID from the database
	@Override
	public Expense findById(String id) {
		Session session = sessionFactory.openSession();
		try {
			return session.get(Expense.class, id);
		} finally {
			session.close();
		}
	}

	// Saves a new Expense to the database
	@Override
	public void save(Expense expense) {
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		try {
			session.save(expense);
			transaction.commit();
		} catch (Exception e) {
			transaction.rollback();
			LOGGER.error("Failed to save expense", e);
			throw new HibernateException("Could not save expense.", e);
		} finally {
			session.close();
		}
	}

	// Deletes an existing Expense from the database
	@Override
	public void delete(Expense expense) {
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		try {
			session.delete(expense);
			transaction.commit();
		} catch (Exception e) {
			transaction.rollback();
			LOGGER.error("Failed to delete expense", e);
			throw new HibernateException("Could not delete expense.", e);
		} finally {
			session.close();
		}
	}

	// Updates an existing Expense in the database
	@Override
	public void update(Expense updatedExpense) {
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();
		try {
			session.update(updatedExpense);
			transaction.commit();
		} catch (Exception e) {
			transaction.rollback();
			LOGGER.error("Failed to update expense", e);
			throw new HibernateException("Could not update expense.", e);
		} finally {
			session.close();
		}
	}
}
