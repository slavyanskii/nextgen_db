package mail.not.tp.db.dao.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import mail.not.tp.db.TExecuter;
import mail.not.tp.db.dao.UserDAO;
import mail.not.tp.models.Response;
import mail.not.tp.models.User;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by viacheslav on 13.10.16.
 */

@Repository
public class UserDAOImpl implements UserDAO {

    private final DataSource dataSource;

    public UserDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public int status() {
        try {
            try (Connection connection = dataSource.getConnection()) {
                final String query = "SELECT COUNT(*) FROM user;";
                return TExecuter.execQuery(connection, query, resultSet -> {
                    resultSet.next();
                    return resultSet.getInt(1);
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void clear() {
        try {
            try (Connection connection = dataSource.getConnection()) {
                TExecuter.execQuery(connection, "SET FOREIGN_KEY_CHECKS = 0;");
                TExecuter.execQuery(connection, "TRUNCATE TABLE user;");
                TExecuter.execQuery(connection, "SET FOREIGN_KEY_CHECKS = 1;");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Response create(String jsonString) {
        final JsonObject object;
        try {
            object = new JsonParser().parse(jsonString).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            return new Response(Response.Codes.INVALID_QUERY);
        }

        final User user;
        try {
            user = new User(object);
        } catch (Exception e) {
            return new Response(Response.Codes.INCORRECT_QUERY);
        }

        try (Connection connection = dataSource.getConnection()) {
            final String query = "INSERT INTO user (username, about, name, email, isAnonymous) VALUES (?,?,?,?,?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, user.getUsername());
                preparedStatement.setString(2, user.getAbout());
                preparedStatement.setString(3, user.getName());
                preparedStatement.setString(4, user.getEmail());
                preparedStatement.setBoolean(5, user.getIsAnonymous());
                preparedStatement.execute();
                try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        user.setId(resultSet.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
//            if (e.getErrorCode() == MYSQL_DUPLICATE_PK) {
//                return new Response(Response.Codes.USER_ALREDY_EXIST);
//            } else {
//                return new Response(Response.Codes.UNKNOWN_ERROR);
//            }
        }

        return new Response(user);
    }

    @Override
    public Response details(String email) {

        final User user;

        try (Connection connection = dataSource.getConnection()) {
            final String query = "SELECT * FROM user WHERE email = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, email);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        user = new User(resultSet);
                    } else {
                        return new Response(Response.Codes.NOT_FOUND);
                    }
                }
            }

            user.setFollowers(getFollowers(connection, email));
            user.setFollowing(getFollowing(connection, email));
            user.setSubscriptions(getSubscriptions(connection, email));
        } catch (SQLException e) {
            e.printStackTrace();
            return new Response(Response.Codes.UNKNOWN_ERROR);
        }

        return new Response(user);
    }

    @Override
    public Response follow(String jsonString) {
        return null;
    }

    @Override
    public Response unfollow(String jsonString) {
        return null;
    }

    @Override
    public Response listFollowers(String email, Integer limit, String order, Integer sinceId) {
        return null;
    }

    @Override
    public Response listFollowing(String email, Integer limit, String order, Integer sinceId) {
        return null;
    }

    @Override
    public Response listPosts(String email, Integer limit, String order, String since) {
        return null;
    }

    @Override
    public Response updateProfile(String jsonString) {
        return null;
    }

    public List<String> getFollowers(Connection connection, String email) throws SQLException {

        final List<String> array = new ArrayList<>();

        final String query = "SELECT Followers FROM User_followers WHERE User = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, email);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    array.add(resultSet.getString("Followers"));
                }
            }
        }
        return array;
    }

    public List<String> getFollowing(Connection connection, String email) throws SQLException {

        final List<String> array = new ArrayList<>();

        final String query = "SELECT User FROM User_followers WHERE Followers = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, email);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    array.add(resultSet.getString("User"));
                }
            }
        }
        return array;
    }

    public List<Integer> getSubscriptions(Connection connection, String email) throws SQLException {

        final List<Integer> array = new ArrayList<>();

        final String query = "SELECT thread_id FROM Thread_followers WHERE follower_email = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, email);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    array.add(resultSet.getInt("thread_id"));
                }
            }
        }
        return array;
    }
}
