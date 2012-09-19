package net.stoerr.dwimdao;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.sql.DataSource;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Yields an implementation for a DAO interface that tries to guess what to do
 * according to the method names.
 */
public class DwimDao<T> implements InvocationHandler {

	private final Class<T> daoClass;
	private final NamedParameterJdbcOperations jdbc;

	public DwimDao(Class<T> daoClass, DataSource datasource) {
		this.daoClass = daoClass;
		jdbc = new NamedParameterJdbcTemplate(datasource);
	}

	/**
	 * Creates an Do-What-I-Mean implementation for the given Dao class.
	 * 
	 * @param daoClass
	 * @param datasource
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T make(Class<T> daoClass, DataSource datasource) {
		DwimDao<T> handler = new DwimDao<T>(daoClass, datasource);
		return (T) Proxy.newProxyInstance(daoClass.getClassLoader(),
				new Class[] { daoClass }, handler);
	}

	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		throw new UnsupportedOperationException();
	}

}
