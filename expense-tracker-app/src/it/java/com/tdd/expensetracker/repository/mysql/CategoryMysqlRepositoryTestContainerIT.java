package com.tdd.expensetracker.repository.mysql;

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
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.utility.DockerImageName;

import com.tdd.expensetracker.model.Category;

public class CategoryMysqlRepositoryTestContainerIT {

	// Using MySQLContainer from Testcontainers for integration testing
	@SuppressWarnings("resource")
	@ClassRule
	public static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>(DockerImageName.parse("mysql:8.0.28"))
			.withDatabaseName("test").withUsername("test").withPassword("test");

	private static SessionFactory sessionFactory;
	private static StandardServiceRegistry registry;
	private CategoryMySqlRepository categoryMySqlRepository;

	// Set up the MySQL container and Hibernate configuration
	@BeforeClass
	public static void setupContainer() {
		mysqlContainer.start();
		registry = new StandardServiceRegistryBuilder().configure("hibernate-IT.cfg.xml")
				.applySetting("hibernate.connection.url", mysqlContainer.getJdbcUrl())
				.applySetting("hibernate.connection.username", mysqlContainer.getUsername())
				.applySetting("hibernate.connection.password", mysqlContainer.getPassword()).build();
	}

	// Tear down the database and stop the container after tests
	@AfterClass
	public static void shutdownServer() {
		StandardServiceRegistryBuilder.destroy(registry);
		if (sessionFactory != null) {
			sessionFactory.close();
		}
		mysqlContainer.stop();
	}

	// Initialize Hibernate session factory and repository before each test
	@Before
	public void setup() {
		MetadataSources metadataSources = new MetadataSources(registry);
		sessionFactory = metadataSources.buildMetadata().buildSessionFactory();
		categoryMySqlRepository = new CategoryMySqlRepository(sessionFactory);
	}

	// Test that the repository retrieves all categories from the database
	@Test
	public void testFindAll() {
		Category category = new Category("name1", "description1");
		Category category2 = new Category("name2", "description2");
		String id1 = addTestCategoryToDatabase(category);
		String id2 = addTestCategoryToDatabase(category2);
		Assertions.assertThat(categoryMySqlRepository.findAll()).containsExactlyInAnyOrder(
				new Category(id1, "name1", "description1"), new Category(id2, "name2", "description2"));
	}

	// Test that the repository finds a category by its ID
	@Test
	public void testFindById() {
		Category category = new Category("name1", "description1");
		Category category2 = new Category("name2", "description2");
		String id1 = addTestCategoryToDatabase(category);
		addTestCategoryToDatabase(category2);
		String expected = new Category(id1, "name1", "description1").toString();
		String actual = categoryMySqlRepository.findById(id1).toString();
		Assertions.assertThat(actual).isEqualTo(expected);
	}

	// Test that the repository saves a new category to the database
	@Test
	public void testSave() {
		Category category = new Category("name2", "description2");
		categoryMySqlRepository.save(category);
		Assertions.assertThat(readAllCategoryFromDatabase()).containsExactly(category);
	}

	// Test that the repository deletes a category from the database
	@Test
	public void testDelete() {
		Category category = new Category("name2", "description2");
		String id = addTestCategoryToDatabase(category);
		Category categoryToDelete = new Category(id, "name2", "description2");
		categoryMySqlRepository.delete(categoryToDelete);
		Assertions.assertThat(readAllCategoryFromDatabase()).isEmpty();
	}

	// Test that the repository updates a category in the database
	@Test
	public void testUpdate() {
		Category category = new Category("name2", "description2");
		String id = addTestCategoryToDatabase(category);
		Category updatedCategory = new Category(id, "nameUpdate2", "updated Desc");
		categoryMySqlRepository.update(updatedCategory);
		Assertions.assertThat(readAllCategoryFromDatabase()).containsExactly(updatedCategory);
	}

	// Utility method to retrieve all categories from the database
	private List<Category> readAllCategoryFromDatabase() {
		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		List<Category> result = session.createQuery("from Category", Category.class).list();
		tx.commit();
		session.close();
		return result;
	}

	// Utility method to add a category to the database and return its ID
	private String addTestCategoryToDatabase(Category category) {
		Session session = sessionFactory.openSession();
		session.beginTransaction();
		session.save(category);
		session.getTransaction().commit();
		session.close();
		return category.getId();
	}
}
