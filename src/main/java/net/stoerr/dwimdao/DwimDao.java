package net.stoerr.dwimdao;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.Collection;

import javax.sql.DataSource;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;

/**
 * Yields an implementation for a DAO interface that tries to guess what to do
 * according to the method names. This is just an exploration how this might
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

	public DwimDao(DataSource datasource) {
		jdbc = new JdbcTemplate(datasource);
	}

	/**
	 * Creates an Do-What-I-Mean implementation for the given Dao class.
	 * 
	 * @param daoClass
	 * @param datasource
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <DAOITF> DAOITF make(Class<DAOITF> daoClass,
			DataSource datasource) {
		DwimDao handler = new DwimDao(datasource);
		return (DAOITF) Proxy.newProxyInstance(daoClass.getClassLoader(),
				new Class[] { daoClass }, handler);
	}

	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		if (method.getName().startsWith("findBy")) {
			return doFind(method, args);
		}
		throw new IllegalArgumentException("We don't understand method "
				+ method);
	}

	private Object doFind(Method method, Object[] args) {
		Type returnType = method.getGenericReturnType();
		String argdescr = method.getName().substring("findBy".length());
		if (returnType instanceof Class) {
			Class<?> beanClass = (Class<?>) returnType;
			String sql = createSql(argdescr, beanClass);
			ParameterizedBeanPropertyRowMapper<?> rowmapper = ParameterizedBeanPropertyRowMapper
					.newInstance(beanClass);
			try {
				return jdbc.queryForObject(sql, args, rowmapper);
			} catch (EmptyResultDataAccessException e) {
				return null;
			}
		} else if (returnType instanceof ParameterizedType) {
			ParameterizedType returnPType = (ParameterizedType) returnType;
			Type rawType = returnPType.getRawType();
			Type[] typeargs = returnPType.getActualTypeArguments();
			if (Collection.class.equals(rawType)) {
				Class<?> beanClass = (Class<?>) typeargs[0];
				String sql = createSql(argdescr, beanClass);
				ParameterizedBeanPropertyRowMapper<?> rowMapper = ParameterizedBeanPropertyRowMapper
						.newInstance(beanClass);
				return jdbc.query(sql, rowMapper, args);
			}
		}
		throw new IllegalArgumentException("We don't understand return type "
				+ returnType);
	}

	private String createSql(String argdescr, Class<?> beanClass) {
		String[] argnames = argdescr.split("And");
		StringBuilder buf = new StringBuilder("select * from ");
		buf.append(beanClass.getSimpleName()).append(" where 1=1");
		for (String arg : argnames) {
			buf.append(" and ").append(arg).append(" = ?");
		}
		return buf.toString();
	}

}
