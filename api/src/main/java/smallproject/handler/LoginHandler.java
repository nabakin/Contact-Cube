package smallproject.handler;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import smallproject.dao.SessionDao;
import smallproject.dao.UserDao;
import smallproject.model.Session;
import smallproject.model.User;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author Matthew
 */
public class LoginHandler extends AbstractHandler {

    public LoginHandler(final Jdbi dbi) {
        super(dbi);
        dbi.useExtension(UserDao.class, UserDao::createTable);
    }

    /**
     * Handle POST requests to the registration handler
     *
     * @param req      the servlet request
     * @param response the response
     * @throws ServletException there was an issue with the input
     * @throws IOException      there was an issue with the connection
     */
    protected void doPost(HttpServletRequest req, HttpServletResponse response) throws ServletException, IOException {

        // get the IP that the request came from
        final String ip = this.getIpAddress(req);

        final JsonElement element = new JsonParser().parse(getPayload(req.getReader()));
        if (!element.isJsonObject()) {
            badRequest(response, "payload is not a valid JSON object!");
            return;
        }

        final JsonObject json = element.getAsJsonObject();
        if (!json.has("email")) {
            badRequest(response, "payload is missing email!");
            return;
        }
        if (!json.has("password")) {
            badRequest(response, "payload is missing password hash!");
            return;
        }

        final String email = json.get("email").getAsString();
        final String password = json.get("password").getAsString();
        if (email.isEmpty()) {
            badRequest(response, "email may not be empty!");
            return;
        }
        if (password.isEmpty()) {
            badRequest(response, "password may not be empty!");
            return;
        }

        try {
            try (final Handle h = dbi.open()) {
                // attach the handle to the userdao
                final UserDao userDao = h.attach(UserDao.class);
                final User authenticatedUser = userDao.login(email, password);
                // if authenticatedUser is null, no match for email/password
                if (authenticatedUser == null) {
                    error(response, HttpServletResponse.SC_FORBIDDEN, "invalid email/password");
                    return;
                }
                // some match was found, lookup session
                final SessionDao sessionDao = h.attach(SessionDao.class);
                final Session session = sessionDao.create(authenticatedUser, ip);
                if (session == null) {
                    badRequest(response, "unable to create session");
                    return;
                }
                sendSession(response, session);
            }
        } catch (final Exception e) {
            e.printStackTrace();
            error(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }

    }

}
