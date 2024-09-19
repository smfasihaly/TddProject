package com.tdd.expensetracker.repository.mysql;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.tdd.expensetracker.model.Category;
import com.tdd.expensetracker.repository.CategoryRepository;

public class CategoryMySqlRepository implements CategoryRepository {

	private SessionFactory sessionFactory;
	private static final Logger LOGGER = LogManager.getLogger(CategoryMySqlRepository.class);

	// Constructor to initialize CategoryMySqlRepository with the session factory
	public CategoryMySqlRepository(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	// Retrieves all Category records from the database
	@Override
	public List<Category> findAll() {
		Session session = sessionFactory.openSession();

		try {
			return session.createQuery("from Category", Category.class).list();
		} finally {
			session.close();
		}
	}

	// Finds a Category by its unique ID from the database
	@Override
	public Category findById(String id) {
		Session session = sessionFactory.openSession();

		try {
			return session.get(Category.class, id);
		} finally {
			session.close();
		}
	}

	// Saves a new Category to the database
	@Override
	public void save(Category category) {
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();

		try {
			session.save(category);
			transaction.commit();
		} catch (Exception e) {
			transaction.rollback();
			LOGGER.error("Failed to save category", e);
			throw new HibernateException("Could not save category.", e);
		} finally {
			session.close();
		}
	}

	// Deletes an existing Category from the database
	@Override
	public void delete(Category category) {
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();

		try {
			session.delete(category);
			transaction.commit();
		} catch (Exception e) {
			transaction.rollback();
			LOGGER.error("Failed to delete category", e);
			throw new HibernateException("Could not delete category.", e);
		} finally {
			session.close();
		}
	}

	// Updates an existing Category in the database
	@Override
	public void update(Category updatedCategory) {
		Session session = sessionFactory.openSession();
		Transaction transaction = session.beginTransaction();

		try {
			session.update(updatedCategory);
			transaction.commit();
		} catch (Exception e) {
			transaction.rollback();
			LOGGER.error("Failed to update category", e);
			throw new HibernateException("Could not update category.", e);
		} finally {
			session.close();
		}
	}

	// Finds a Category by its name from the database
	@Override
	public Category findByName(String name) {
		Session session = sessionFactory.openSession();

		try {
			return session.createQuery("from Category where name = :name", Category.class).setParameter("name", name)
					.uniqueResult();
		} finally {
			session.close();
		}
	}
}