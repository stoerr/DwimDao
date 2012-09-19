package net.stoerr.dwimdao.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

import java.sql.Connection;
import java.sql.SQLException;

import net.stoerr.dwimdao.DwimDao;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
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
	}

	/** Closes the database by closing the last ensureStayOpenConnection. */
	@After
	public void tearDownDatabase() throws SQLException {
		ensureStayOpenConnection.close();
	}

	@Test
	public void testCRUD() {
		final User user = new User();
		user.setFirstName("first");
		user.setSecondName("second");
		user.setId(17L);
		dao.save(user);
		assertEquals(user, dao.findById(user.getId()));
		assertNull(dao.findById(42L));
		assertEquals(user, dao.findByFirstName(user.getFirstName()));
		assertNull(dao.findByFirstName("nix"));
		assertEquals(
				user,
				dao.findByFirstNameAndSecondName(user.getFirstName(),
						user.getSecondName()));
		assertNull(dao.findByFirstNameAndSecondName(user.getFirstName(), "nix"));
		assertNull(dao
				.findByFirstNameAndSecondName("nix", user.getSecondName()));
		dao.delete(user);
		assertNull(dao.findById(user.getId()));
	}

}
