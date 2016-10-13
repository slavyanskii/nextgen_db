package mail.not.tp.db.dao.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import mail.not.tp.db.dao.ForumDAO;
import mail.not.tp.models.Forum;
import mail.not.tp.models.Response;
import mail.not.tp.db.TExecuter;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;

/**
 * Created by viacheslav on 13.10.16.
 */

@Repository
public class ForumDAOImpl implements ForumDAO {

    private final DataSource dataSource;

    public ForumDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public int status() {
        try (Connection connection = dataSource.getConnection()) {
            final String query = "SELECT COUNT(*) FROM forum;";
            return TExecuter.execQuery(connection, query, resultSet -> {
                resultSet.next();
                return resultSet.getInt(1);
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void clear() {
        try (Connection connection = dataSource.getConnection()) {
            TExecuter.execQuery(connection, "SET FOREIGN_KEY_CHECKS = 0;");
            TExecuter.execQuery(connection, "TRUNCATE TABLE forum;");
            TExecuter.execQuery(connection, "SET FOREIGN_KEY_CHECKS = 1;");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Response create(String jsonString) { // TODO: 13.10.16 to be done
        final JsonObject object;
        try {
            object = new JsonParser().parse(jsonString).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            return new Response(Response.Codes.INVALID_QUERY);
        }

        final Forum forum;
        try {
            forum = new Forum(object);
        } catch (Exception e) {
            return new Response(Response.Codes.INCORRECT_QUERY);
        }

        try (Connection connection = dataSource.getConnection()) {
            final String query = "INSERT INTO forum (name, short_name, user) VALUES (?,?,?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, forum.getName());
                preparedStatement.setString(2, forum.getShort_name());
                preparedStatement.setString(3, (String) forum.getUser());
                preparedStatement.execute();
                try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        forum.setId(resultSet.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
//            if (e.getErrorCode() == MYSQL_DUPLICATE_PK) {
//                return details(object.get("short_name").getAsString(), null);
//            } else {
//                return new Response(Response.Codes.UNKNOWN_ERROR);
//            }
        }

        return new Response(forum);
    }

    @Override
    public Response details(String forum, String[] related) {
        return null;
    }

    @Override
    public Response listPosts(String forum, String since, Integer limit, String order, String[] related) {
        return null;
    }

    @Override
    public Response listThreads(String forum, String since, Integer limit, String order, String[] related) {
        return null;
    }

    @Override
    public Response listUsers(String forum, Integer sinceId, Integer limit, String order) {
        return null;
    }
}
