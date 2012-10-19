package net.stoerr.dwimdao;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;

/**
 * Yields an implementation for a DAO interface that tries to guess what to do
 * according to the method names. This is just a proof of concept how this might
 * work. Currently we support only beans where the table name is the class name
 * of the bean and the column names are the property names of the bean, and
 * finder methods that start with "findBy" and are followed by the column names
 * we search for separated by "And".
 * 
 * @param <DAOITF>
 *            the DAO interface we implement.
 */
public class DwimDao implements InvocationHandler {

	private final JdbcOperations jdbc;

	public DwimDao(final DataSource datasource) {
		jdbc = new JdbcTemplate(datasource);
	}

	/**
	 * Creates an Do-What-I-Mean implementation for the given DAO interface.
	 * 
	 * @param daoClass
	 *            class of an interface with finder methods, not null.
	 * @param datasource
	 *            the datasource to access, not null.
	 * @return an implementation for the dao
	 */
	@SuppressWarnings("unchecked")
	public static <DAO> DAO make(final Class<DAO> daoClass,
			final DataSource datasource) {
		final DwimDao handler = new DwimDao(datasource);
		return (DAO) Proxy.newProxyInstance(daoClass.getClassLoader(),
				new Class[] { daoClass }, handler);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
	 * java.lang.reflect.Method, java.lang.Object[])
	 */
	public Object invoke(final Object proxy, final Method method,
			final Object[] args) throws Throwable {
		if (method.isAnnotationPresent(DwimSQL.class))
			return doDwimSQL(method, args);
		if (method.getName().startsWith("findBy"))
			return doFind(method, args, null);
		throw new UnsupportedOperationException("We don't understand method "
				+ method);
	}

	/** Handles methods with {@link DwimSQL} annotation */
	private Object doDwimSQL(Method method, Object[] args) {
		String sql = method.getAnnotation(DwimSQL.class).value();
		return doFind(method, args, sql);
	}

	/** Executes the finder, depending on its name and return type. */
	private Object doFind(final Method method, final Object[] args, final String givensql) {
		final Type returnType = method.getGenericReturnType();
		if (returnType instanceof Class)
			return handleBeanClassReturnType((Class<?>) returnType, args,
					method.getName(), givensql);
		else if (returnType instanceof ParameterizedType)
			return handleParameterizedReturnType(
					(ParameterizedType) returnType, args, method.getName(), givensql);
		throw new UnsupportedOperationException("We don't understand return type "
				+ returnType);
	}
	
	private List<?> handleParameterizedReturnType(
			final ParameterizedType returnType, final Object[] args,
			final String methodname, final String givensql) {
		final Type rawType = returnType.getRawType();
		final Type[] typeargs = returnType.getActualTypeArguments();
		if (Collection.class.equals(rawType)) {
			final Class<?> beanClass = (Class<?>) typeargs[0];
			final String sql = (null != givensql) ? givensql : createFinderSql(methodname, beanClass);
			final ParameterizedBeanPropertyRowMapper<?> rowMapper = ParameterizedBeanPropertyRowMapper
					.newInstance(beanClass);
			return jdbc.query(sql, rowMapper, args);
		}
		throw new UnsupportedOperationException("We don't understand return type "
				+ returnType);
	}

	private Object handleBeanClassReturnType(final Class<?> beanClass,
			final Object[] args, final String methodname, String givensql) {
		try {
			final String sql = (null != givensql) ? givensql : createFinderSql(methodname, beanClass);
			final ParameterizedBeanPropertyRowMapper<?> rowmapper = ParameterizedBeanPropertyRowMapper
					.newInstance(beanClass);
			return jdbc.queryForObject(sql, args, rowmapper);
		} catch (final EmptyResultDataAccessException e) {
			// we handle not existent beans with null value instead of
			// exceptions.
			return null;
		}
	}

	/**
	 * Creates a select statement according to the names of arguments in
	 * argdescr, separated by And.
	 */
	private String createFinderSql(final String methodname,
			final Class<?> beanClass) {
		final String argdescr = methodname.substring("findBy".length());
		final String[] argnames = argdescr.split("And");
		final StringBuilder buf = new StringBuilder("select * from ");
		buf.append(beanClass.getSimpleName()).append(" where 1=1");
		for (final String arg : argnames)
			buf.append(" and ").append(arg).append(" = ?");
		return buf.toString();
	}

}
