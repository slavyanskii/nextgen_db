package mail.not.tp.db.dao.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import mail.not.tp.db.dao.UserDAO;
import mail.not.tp.models.Post;
import mail.not.tp.models.Response;
import mail.not.tp.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
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
    private JdbcTemplate jdbcTemplate;
    private static final int MYSQL_DUPLICATE_PK = 1062;


    @Autowired
    public UserDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public int status() {
        try {
            final String query = "SELECT COUNT(*) FROM User;";
            return this.jdbcTemplate.queryForObject(query, Integer.class);
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void clear() {
        try {
            this.jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0;");
            this.jdbcTemplate.execute("TRUNCATE TABLE User;");
            this.jdbcTemplate.execute("TRUNCATE TABLE User_followers;");
            this.jdbcTemplate.execute("TRUNCATE TABLE User_subscribes;");
            this.jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1;");
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Response create(String jsonString) { //todo DONE
        final JsonObject object;
        try {
            object = new JsonParser().parse(jsonString).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            return new Response(Response.Codes.INVALID_QUERY);
        }
        final User user;
        try (Connection connection = dataSource.getConnection()) {
            user = new User(object);

            String query = "INSERT INTO " + "User" + " (username, about, name, email, isAnonymous) VALUES (?,?,?,?,?)";

            try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, user.getUsername());
                stmt.setString(2, user.getAbout());
                stmt.setString(3, user.getName());
                stmt.setString(4, user.getEmail());
                stmt.setBoolean(5, user.getIsAnonymous());

                stmt.executeUpdate();

                try (ResultSet resultSet = stmt.getGeneratedKeys()) {
                    resultSet.next();
                    user.setId(resultSet.getInt(1));
                }
            } catch (SQLException e) {
                e.printStackTrace();
                if (e.getErrorCode() == MYSQL_DUPLICATE_PK) {
                    return new Response(Response.Codes.USER_ALREDY_EXIST);
                } else {
                    return new Response(Response.Codes.UNKNOWN_ERROR);
                }
            }
        } catch (Exception e) {
            return new Response(Response.Codes.INVALID_QUERY);
        }
        return new Response(user);
    }

    @Override
    public Response details(String email) { //todo DONE
        final User user;
        try (Connection connection = dataSource.getConnection()) {
            final String query = "SELECT U.*, group_concat(distinct JUF.followee_email) as following, group_concat(distinct JUF1.user_email) as followers, group_concat(distinct JUS.thread_id) as subscribes\n" +
                    "FROM User U \n" +
                    "LEFT JOIN User_followers JUF ON U.email = JUF.user_email\n" +
                    "LEFT JOIN User_followers JUF1 ON U.email = JUF1.followee_email\n" +
                    "LEFT JOIN User_subscribes JUS ON U.email= JUS.user_email\n" +
                    "WHERE U.email = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, email);
                try (ResultSet resultSet = stmt.executeQuery()) {
                    resultSet.next();

                    user = new User(resultSet);

                } catch (SQLException e) {
                    return new Response(Response.Codes.NOT_FOUND);
                }
            }
        } catch (SQLException e) {
            return new Response(Response.Codes.INVALID_QUERY);
        }

        return new Response(user);
    }

    @Override
    public Response follow(String jsonString) {
        final String follower;
        try (Connection connection = dataSource.getConnection())  {
            final JsonObject object = new JsonParser().parse(jsonString).getAsJsonObject();
            follower = object.get("follower").getAsString();
            final String followee = object.get("followee").getAsString();

            try {
                final String query = "INSERT INTO User_followers (user_email, followee_email) VALUES (?,?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, follower);
                    preparedStatement.setString(2, followee);
                    preparedStatement.execute();
                }
            } catch (SQLException | NullPointerException e) {
                e.printStackTrace();
                return new Response(Response.Codes.INCORRECT_QUERY);
            }
        } catch (Exception e) {
            return new Response(Response.Codes.INVALID_QUERY);
        }
        return new Response(details(follower).getObject());
    }

    @Override
    public Response unfollow(String jsonString) {
        JsonObject object;
        try {
            object = new JsonParser().parse(jsonString).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            return new Response(Response.Codes.INVALID_QUERY);
        }

        try {
            try (Connection connection = dataSource.getConnection()) {
                String query = "DELETE FROM user_followers WHERE user = ? AND followers = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, object.get("followee").getAsString());
                    preparedStatement.setString(2, object.get("follower").getAsString());
                    preparedStatement.execute();
                }
            }
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
            return new Response(Response.Codes.INCORRECT_QUERY);
        }

        return details(object.get("user").getAsString());
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
        final JsonObject object;
        try {
            object = new JsonParser().parse(jsonString).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            return new Response(Response.Codes.INVALID_QUERY);
        }

        try (Connection connection = dataSource.getConnection()) {
            final String query = "UPDATE user SET about = ?, name = ? WHERE email = ?;";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, object.get("about").getAsString());
                preparedStatement.setString(2, object.get("name").getAsString());
                preparedStatement.setString(3, object.get("user").getAsString());
                preparedStatement.execute();
            }
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
            return new Response(Response.Codes.INCORRECT_QUERY);
        }

        return new Response(details(object.get("user").getAsString()));
    }

    public List<String> getFollowers(Connection connection, String email) throws SQLException {

        final List<String> array = new ArrayList<>();

        final String query = "SELECT followers FROM user_followers WHERE user = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, email);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    array.add(resultSet.getString("followers"));
                }
            }
        }
        return array;
    }

    public List<String> getFollowing(Connection connection, String email) throws SQLException {

        final List<String> array = new ArrayList<>();

        final String query = "SELECT user FROM user_followers WHERE followers = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, email);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    array.add(resultSet.getString("user"));
                }
            }
        }
        return array;
    }

    public List<Integer> getSubscriptions(Connection connection, String email) throws SQLException {

        final List<Integer> array = new ArrayList<>();

        final String query = "SELECT thread_id FROM thread_followers WHERE follower_email = ?";
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
