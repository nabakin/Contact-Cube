package smallproject.dao;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindFields;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import smallproject.model.User;

/**
 * @author Matthew
 */
public interface UserDao {

    @SqlUpdate(
            "CREATE TABLE IF NOT EXISTS users " +
                    "(" +
                    "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                    "email VARCHAR(255) NOT NULL," +
                    "password VARCHAR(255) NOT NULL," +
                    "firstname VARCHAR(255) NOT NULL," +
                    "lastname VARCHAR(255) NOT NULL" +
                    ")"
    )
    void createTable();

    @SqlUpdate("TRUNCATE users")
    void truncateTable();

    /**
     * Inserts the user into the database
     *
     * @param user the user to insert into the database
     * @return the created ID (auto-increment) of the user
     */
    @SqlUpdate("INSERT INTO users (email, password, firstname, lastname) values (:email, :password, :firstname, :lastname)")
    @GetGeneratedKeys("id")
    long insert(@BindFields final User user);

    @SqlQuery("SELECT * FROM users WHERE email = ?")
    @RegisterBeanMapper(User.class)
    User getUserByEmail(final String email);

}