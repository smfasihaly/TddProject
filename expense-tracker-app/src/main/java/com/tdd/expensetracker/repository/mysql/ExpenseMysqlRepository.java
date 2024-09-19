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
		Session session = null;
		Transaction transaction = null;
		try {
			session = sessionFactory.openSession();
			transaction = session.beginTransaction();

			List<Expense> expenses = session.createQuery("from Expense", Expense.class).list();

			transaction.commit();
			return expenses;
		} catch (Exception e) {
			if (transaction != null) {
				transaction.rollback();
			}
			LOGGER.error("Failed to retrieve all expenses", e);
			throw new HibernateException("Could not retrieve expenses.", e);
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}

	// Finds an Expense by its unique ID from the database
	@Override
	public Expense findById(String id) {
		Session session = null;
		Transaction transaction = null;
		try {
			session = sessionFactory.openSession();
			transaction = session.beginTransaction();

			Expense expense = session.get(Expense.class, id);

			transaction.commit();
			return expense;
		} catch (Exception e) {
			if (transaction != null) {
				transaction.rollback();
			}
			LOGGER.error("Failed to find expense by id", e);
			throw new HibernateException("Could not find expense by id.", e);
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}

	// Saves a new Expense to the database
	@Override
	public void save(Expense expense) {
		Session session = null;
		Transaction transaction = null;
		try {
			session = sessionFactory.openSession();
			transaction = session.beginTransaction();

			session.save(expense);

			transaction.commit();
		} catch (Exception e) {
			if (transaction != null) {
				transaction.rollback();
			}
			LOGGER.error("Failed to save expense", e);
			throw new HibernateException("Could not save expense.", e);
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}

	// Deletes an existing Expense from the database
	@Override
	public void delete(Expense expense) {
		Session session = null;
		Transaction transaction = null;
		try {
			session = sessionFactory.openSession();
			transaction = session.beginTransaction();

			session.delete(expense);

			transaction.commit();
		} catch (Exception e) {
			if (transaction != null) {
				transaction.rollback();
			}
			LOGGER.error("Failed to delete expense", e);
			throw new HibernateException("Could not delete expense.", e);
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}

	// Updates an existing Expense in the database
	@Override
	public void update(Expense updatedExpense) {
		Session session = null;
		Transaction transaction = null;
		try {
			session = sessionFactory.openSession();
			transaction = session.beginTransaction();

			session.update(updatedExpense);

			transaction.commit();
		} catch (Exception e) {
			if (transaction != null) {
				transaction.rollback();
			}
			LOGGER.error("Failed to update expense", e);
			throw new HibernateException("Could not update expense.", e);
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}
}
