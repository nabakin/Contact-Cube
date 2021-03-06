package smallproject.dao;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindFields;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import smallproject.model.Session;
import smallproject.model.User;

import java.util.Optional;

/**
 * @author Matthew
 */
public interface SessionDao {

    @SqlUpdate("CREATE TABLE IF NOT EXISTS sessions (" +
            "`userId` int(10) PRIMARY KEY," +
            "`ip` varchar(40) NOT NULL," +
            "`token` char(64) NOT NULL)")
    void createTable();

    @SqlUpdate("TRUNCATE TABLE sessions")
    void truncateTable();

    @SqlQuery("SELECT * FROM sessions WHERE userId = ? AND ip = ?")
    @RegisterBeanMapper(Session.class)
    Session _getSession(final long userId, final String ip);

    @SqlUpdate("INSERT INTO sessions (userId, ip, token) VALUES (:userId, :ip, :token) " +
            "ON DUPLICATE KEY UPDATE ip = :ip, token = :token")
    void _insertSession(@BindFields final Session session);

    @SqlQuery("SELECT userId FROM sessions WHERE ip = ? AND token = ?")
    Optional<Long> userIdForSession(final String ip, final String token);

    @SqlUpdate("DELETE FROM sessions WHERE userId = ? AND token = ?")
    void logout(final long userId, final String token);

    /**
     * Creates a session for the provided user
     *
     * @param user the user to create the session for, the userId is part of the token
     * @param ip   the IP of the user creating the session
     * @return a {@link Session} object if no issues are encountered
     * <tt>null</tt> if there is some issue
     */
    default Session create(final User user, final String ip) {

        // make sure this is a real user
        final long userId = user.id;
        if (userId == -1) return null;

        // lookup session table
        Session session = _getSession(userId, ip);
        // if a session already exists for this user... just return it
        if (session != null) return session;


        final HashFunction function = Hashing.sha256();
        final String token = function.newHasher()
                .putLong(userId)
                .putUnencodedChars(ip)
                .hash()
                .toString();

        // no session was found for the userId and ip, so create a new one
        session = new Session();
        session.setIp(ip);
        session.setToken(token);
        session.setUserId(userId);
        _insertSession(session);
        return session;

    }

}
