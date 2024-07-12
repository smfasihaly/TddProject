package com.tdd.expensetracker.repository.mysql;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.tdd.expensetracker.model.Expense;
import com.tdd.expensetracker.repository.ExpenseRepository;

public class ExpenseMysqlRepository implements ExpenseRepository {

	private SessionFactory sessionFactory;

	public ExpenseMysqlRepository(SessionFactory sessionFactory) {

		this.sessionFactory = sessionFactory;

	}

	@Override
	public List<Expense> findAll() {

		Session session = sessionFactory.openSession();
		session.beginTransaction();

		List<Expense> expenses = session.createQuery("from Expense", Expense.class).list();

		session.getTransaction().commit();
		session.close();

		return expenses;
	}

	@Override
	public Expense findById(String id) {

		Session session = sessionFactory.openSession();
		session.beginTransaction();

		Expense expense = session.get(Expense.class, id);

		session.getTransaction().commit();
		session.close();

		return expense;
	}

	@Override
	public void save(Expense expense) {

		Session session = sessionFactory.openSession();
		session.beginTransaction();

		session.save(expense);

		session.getTransaction().commit();
		session.close();

	}

	@Override
	public void delete(Expense expense) {

		Session session = sessionFactory.openSession();
		session.beginTransaction();

		session.delete(expense);

		session.getTransaction().commit();
		session.close();

	}

	@Override
	public void update(Expense updatedExpense) {

		Session session = sessionFactory.openSession();
		session.beginTransaction();

		// Update the expense in the database
		session.update(updatedExpense);
		session.getTransaction().commit();
		session.close();

	}

}
