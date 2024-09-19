package com.tdd.expensetracker.repository.mysql;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.tdd.expensetracker.model.Category;

public class CategoryMySqlRepositoryTest {

	private SessionFactory sessionFactory;
	private static StandardServiceRegistry registry;
	private CategoryMySqlRepository categoryMySqlRepository;

	@BeforeClass
	public static void setupServer() {
		// Setup Hibernate configuration for tests
		registry = new StandardServiceRegistryBuilder().configure("hibernate-test.cfg.xml").build();
	}

	@AfterClass
	public static void shutdownServer() {
		// Cleanup Hibernate resources after tests
		StandardServiceRegistryBuilder.destroy(registry);
	}

	@Before
	public void setup() {
		// Setup sessionFactory and repository before each test
		sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
		categoryMySqlRepository = new CategoryMySqlRepository(sessionFactory);
	}

	// Test for finding all categories when the database is empty
	@Test
	public void testFindAllWhenDatabaseIsEmpty() {
		assertThat(categoryMySqlRepository.findAll()).isEmpty();
	}

	// Test for finding all categories when the database contains data
	@Test
	public void testFindAllWhenDatabaseIsNotEmpty() {
		Category category = new Category("name1", "description1");
		Category category2 = new Category("name2", "description2");
		String id1 = addTestCategoryToDatabase(category);
		String id2 = addTestCategoryToDatabase(category2);
		Assertions.assertThat(categoryMySqlRepository.findAll()).containsExactly(new Category[] {
				new Category(id1, "name1", "description1"), new Category(id2, "name2", "description2") });
	}

	// Test for finding a category by ID when it is not found
	@Test
	public void testFindByIdNotFound() {
		Assertions.assertThat(categoryMySqlRepository.findById("1")).isNull();
	}

	// Test for finding a category by ID when it is found
	@Test
	public void testFindByIdFound() {
		Category category = new Category("name1", "description1");
		Category category2 = new Category("name2", "description2");
		String id1 = addTestCategoryToDatabase(category);
		addTestCategoryToDatabase(category2);
		String expected = new Category(id1, "name1", "description1").toString();
		String actual = categoryMySqlRepository.findById(id1).toString();
		Assertions.assertThat(actual).isEqualTo(expected);
	}

	// Test for finding a category by name when it is not found
	@Test
	public void testFindByNameNotFound() {
		Assertions.assertThat(categoryMySqlRepository.findByName("fasih")).isNull();
	}

	// Test for finding a category by name when it is found
	@Test
	public void testFindByNameFound() {
		Category category = new Category("name1", "description1");
		Category category2 = new Category("name2", "description2");
		String id1 = addTestCategoryToDatabase(category);
		addTestCategoryToDatabase(category2);
		String expected = new Category(id1, "name1", "description1").toString();
		String actual = categoryMySqlRepository.findByName("name1").toString();
		Assertions.assertThat(actual).isEqualTo(expected);
	}

	// Test for saving a new category in the database
	@Test
	public void testSave() {
		Category category = new Category("name2", "description2");
		categoryMySqlRepository.save(category);
		Assertions.assertThat(readAllCategoryFromDatabase()).containsExactly(new Category[] { category });
	}

	// Test for deleting a category from the database
	@Test
	public void testDelete() {
		Category category = new Category("name2", "description2");
		String id = addTestCategoryToDatabase(category);
		Category categoryToDelete = new Category(id, "name2", "description2");
		categoryMySqlRepository.delete(categoryToDelete);
		Assertions.assertThat(readAllCategoryFromDatabase()).isEmpty();
	}

	// Test for updating an existing category in the database
	@Test
	public void testUpdate() {
		Category category = new Category("name2", "description2");
		String expenseToUpdate = addTestCategoryToDatabase(category);
		Category updateCategory = new Category(expenseToUpdate, "nameUpdate2", "updated Desc");
		categoryMySqlRepository.update(updateCategory);
		Assertions.assertThat(readAllCategoryFromDatabase()).containsExactly(new Category[] { updateCategory });
	}

	// Utility method to read all categories from the database
	private List<Category> readAllCategoryFromDatabase() {
		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		List<Category> result = session.createQuery("from Category", Category.class).list();
		tx.commit();
		session.close();
		return result;
	}

	// Utility method to add a test category to the database
	private String addTestCategoryToDatabase(Category category) {
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		session.save(category);
		session.getTransaction().commit();
		session.close();
		return category.getId();
	}
}