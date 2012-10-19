package net.stoerr.dwimdao.test;

import java.util.Collection;

import net.stoerr.dwimdao.DwimSQL;

/**
 * An interface for a (very) simple data access object for {@link User}. 
 */
public interface UserDao {

	User findById(Long id);

	Collection<User> findByFirstName(String firstName);

	Collection<User> findByFirstNameAndSecondName(String firstName, String secondName);
	
	@DwimSQL("select * from user where secondname like ?")
	Collection<User> findBySecondNameLike(String secondnamepattern);
}
