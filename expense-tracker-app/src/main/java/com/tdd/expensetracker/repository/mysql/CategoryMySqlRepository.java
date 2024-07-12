package com.tdd.expensetracker.repository.mysql;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.tdd.expensetracker.model.Category;
import com.tdd.expensetracker.repository.CategoryRepository;

public class CategoryMySqlRepository implements CategoryRepository {

	private SessionFactory sessionFactory;

	public CategoryMySqlRepository(SessionFactory sessionFactory) {

		this.sessionFactory = sessionFactory;

	}

	@Override
	public List<Category> findAll() {
		
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		List<Category> categories= session.createQuery("from Category", Category.class).list();

		session.getTransaction().commit();
		session.close();

		return categories;
	}

	@Override
	public Category findById(String id) {

		Session session = sessionFactory.openSession();
		session.beginTransaction();

		Category category = session.get(Category.class, id);

		session.getTransaction().commit();
		session.close();

		return category;
	}

	@Override
	public void save(Category category) {
		
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		session.save(category);

		session.getTransaction().commit();
		session.close();

	}

	@Override
	public void delete(Category category) {
	
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		session.delete(category);

		session.getTransaction().commit();
		session.close();

	}

	@Override
	public void update(Category updatedCategory) {
		
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		// Update the expense in the database
		session.update(updatedCategory);
		session.getTransaction().commit();
		session.close();

	}

}
