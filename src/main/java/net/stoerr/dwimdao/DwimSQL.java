package net.stoerr.dwimdao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * In DAO interfaces used with {@link DwimDao} this annotation can specify the
 * actual SQL to be executed for the annotated method. This can be used if the
 * query exceeds the SQL guessing functionality of {@link DwimDao}.
 * 
 * @author hps
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DwimSQL {

	/** The actual SQL statement that should be executed for the query. */
	String value();
}
