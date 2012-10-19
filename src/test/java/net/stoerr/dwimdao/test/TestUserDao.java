package net.stoerr.dwimdao.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.TreeSet;

import net.stoerr.dwimdao.DwimDao;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.test.jdbc.SimpleJdbcTestUtils;

/**
 * Verifies that the Do-What-I-Mean Implementation of the UserDao does what it
 * should.
 */
public class TestUserDao {

	private UserDao dao;

	private JdbcDataSource datasource;
	private Connection ensureStayOpenConnection;
	private JdbcTemplate jdbc;

	/**
	 * Initializes the database and opens one ensureStayOpenConnection such that
	 * it stays open until the test is finished.
	 */
	@Before
	public void setupDatabase() throws SQLException {
		this.datasource = new JdbcDataSource();
		datasource.setURL("jdbc:h2:mem:" + getClass().getName());
		datasource.setUser("sa");
		datasource.setPassword("sa");
		ensureStayOpenConnection = datasource.getConnection();
		Resource createScript = new ClassPathResource("/user.sql");
		SimpleJdbcTestUtils.executeSqlScript(
				new SimpleJdbcTemplate(datasource), createScript, false);
		dao = DwimDao.make(UserDao.class, datasource);
		jdbc = new JdbcTemplate(datasource);
	}

	/** Closes the database by closing the last ensureStayOpenConnection. */
	@After
	public void tearDownDatabase() throws SQLException {
		ensureStayOpenConnection.close();
	}

	private User createAndInsertUser(Long id, String firstName,
			String secondName) {
		final User user = new User();
		user.setFirstName(firstName);
		user.setSecondName(secondName);
		user.setId(id);
		jdbc.update(
				"insert into user (id, firstname, secondname) values (?,?,?)",
				user.getId(), user.getFirstName(), user.getSecondName());
		return user;
	}

	/**
	 * Tests the finders defined in {@link UserDao}.
	 */
	@Test
	public void testFinders() {
		User user = createAndInsertUser(17L, "first", "second");
		assertEquals(user, dao.findById(user.getId()));
		assertNull(dao.findById(42L));
		assertEquals(user, dao.findByFirstName(user.getFirstName()).iterator()
				.next());
		assertTrue(dao.findByFirstName("nix").isEmpty());
		assertEquals(
				user,
				dao.findByFirstNameAndSecondName(user.getFirstName(),
						user.getSecondName()).iterator().next());
		assertTrue(dao.findByFirstNameAndSecondName(user.getFirstName(), "nix")
				.isEmpty());
		assertTrue(dao
				.findByFirstNameAndSecondName("nix", user.getSecondName())
				.isEmpty());

		jdbc.update("delete from user where id=?", user.getId());
		assertNull(dao.findById(user.getId()));
	}

	@Test
	public void testDwimSQLAnnotation() {
		createAndInsertUser(17L, "first", "second");
		createAndInsertUser(18L, "first2", "second2");
		createAndInsertUser(19L, "first3", "other");
		Collection<User> secondnames = dao.findBySecondNameLike("%second%");
		assertEquals(2, secondnames.size());
	}

}
