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
	@SuppressWarnings("resource")
	@ClassRule
	public static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>(DockerImageName.parse("mysql:8.0.28"))
			.withDatabaseName("test").withUsername("test").withPassword("test");

	private static SessionFactory sessionFactory;

	private static StandardServiceRegistry registry;

	private CategoryMySqlRepository categoryMySqlRepository;

	@BeforeClass
	public static void setupContainer() {
		mysqlContainer.start();
		registry = new StandardServiceRegistryBuilder().configure("hibernate-IT.cfg.xml")
				.applySetting("hibernate.connection.url", mysqlContainer.getJdbcUrl())
				.applySetting("hibernate.connection.username", mysqlContainer.getUsername())
				.applySetting("hibernate.connection.password", mysqlContainer.getPassword()).build();

	}

	@AfterClass
	public static void shutdownServer() {

		StandardServiceRegistryBuilder.destroy(registry);
		if (sessionFactory != null) {
			sessionFactory.close();
		}
		mysqlContainer.stop();
	}

	@Before
	public void setup() {

		MetadataSources metadataSources = new MetadataSources(registry);
		sessionFactory = metadataSources.buildMetadata().buildSessionFactory();
		categoryMySqlRepository = new CategoryMySqlRepository(sessionFactory);
	}

	@Test
	public void testFindAll() {
		
		Category category = new Category( "name1", "description1");
		Category category2 = new Category( "name2", "description2");

		String id1 = addTestCategoryToDatabase(category);
		String id2 = addTestCategoryToDatabase(category2);

		Assertions.assertThat(categoryMySqlRepository.findAll()).containsExactlyInAnyOrder(new Category[] {
				new Category(id1, "name1", "description1"), new Category(id2, "name2", "description2") });
	}

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

	@Test
	public void testSave() {

		Category category = new Category("name2", "description2");

		categoryMySqlRepository.save(category);

		Assertions.assertThat(readAllCategoryFromDatabase()).containsExactly(new Category[] { category });
	}

	@Test
	public void testDelete() {

		Category category = new Category( "name2", "description2");
		String id = addTestCategoryToDatabase(category);

		Category categoryToDelete = new Category(id, "name2", "description2");
		categoryMySqlRepository.delete(categoryToDelete);
		
		Assertions.assertThat(readAllCategoryFromDatabase()).isEmpty();
	}

	@Test
	public void testUpdate() {

		Category category = new Category("name2", "description2");

		String expenseToUpdate = addTestCategoryToDatabase(category);

		Category updateCategory = new Category(expenseToUpdate, "nameUpdate2", "updated Desc");
		categoryMySqlRepository.update(updateCategory);

		Assertions.assertThat(readAllCategoryFromDatabase()).containsExactly(new Category[] { updateCategory });
	}

	private List<Category> readAllCategoryFromDatabase() {

		Session session = sessionFactory.openSession();
		Transaction tx = session.beginTransaction();
		
		List<Category> result = session.createQuery("from Category", Category.class).list();
		
		tx.commit();
		session.close();

		return result;

	}


	private String addTestCategoryToDatabase(Category category) {
		
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		session.save(category);

		session.getTransaction().commit();
		session.close();
		
		return category.getId();
	}
}
