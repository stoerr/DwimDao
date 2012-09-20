DwimDao
=======

Proof of concept how to "auto-implement" a DAO defined as a interface (DwimDao = "Do What I Mean Data Access Object").
The idea is that one just declares the needed methods on a DAO interface, but never needs to implement them:
the DwimDao engine guesses from the method declarations what SQL statement is needed. 
 
This is meant to be vaguely similar to Rails ActiveRecord (http://api.rubyonrails.org/classes/ActiveRecord/Base.html), but in Java.
Currently, this is just a proof of concept. The DAO interface can contain methods named like findBy<First>[And<Field>]* that can return
the stored value object or a collection of them. For example:

public interface UserDao {
	User findById(Long id);
	Collection<User> findByFirstName(String firstName);
	Collection<User> findByFirstNameAndSecondName(String firstName, String secondName);
}

You create a DAO like this:

UserDao dao = DwimDao.make(UserDao.class, datasource);

and the finder methods on the UserDao interface can be used without ever being explicitly implemented.

Ideas for extension
-------------------

To make this really usable one would need to implement the following things.
- Implement "save", "delete", "deleteBy" methods
- What to do about update methods?
- Provide Annotations for:
* perhaps the table name at class level
* an explicitly provided SQL statement at method level
* perhaps a special row mapper class at class level (currently ParameterizedBeanPropertyRowMapper is used)
- Integration with spring: a way to specify / inject the datasource and / or a JdbcTemplate.

Probably I should study ActiveRecord and the Groovy equivalent to find ideas. :-)
