package net.stoerr.dwimdao.test;

import java.util.Collection;

public interface UserDao {

	void save(User user);

	void delete(User user);

	User findById(Long id);

	Collection<User> findByFirstName(String firstName);

	Collection<User> findByFirstNameAndSecondName(String firstName, String secondName);
}
