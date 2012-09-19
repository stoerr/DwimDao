package net.stoerr.dwimdao.test;

import java.util.Collection;

public interface UserDao {

	User findById(Long id);

	Collection<User> findByFirstName(String firstName);

	Collection<User> findByFirstNameAndSecondName(String firstName, String secondName);
}
