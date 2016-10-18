package mail.not.tp.db.dao.impl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import mail.not.tp.db.dao.ForumDAO;
import mail.not.tp.models.*;
import mail.not.tp.models.Thread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by viacheslav on 13.10.16.
 */

@Repository
public class ForumDAOImpl implements ForumDAO {

    private final DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private static final int MYSQL_DUPLICATE_PK = 1062;

    @Autowired
    public ForumDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public int status() {
        try {
            final String query = "SELECT COUNT(*) FROM Forum;";
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
            this.jdbcTemplate.execute("TRUNCATE TABLE Forum;");
            this.jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1;");
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Response create(String jsonString) { // TODO: 13.10.16 to be done
        final Forum forum;
        try (Connection connection = dataSource.getConnection()){
            forum = new Forum(new JsonParser().parse(jsonString).getAsJsonObject());

            final String query = "INSERT INTO "+ "Post" + "(name, short_name, user_email) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, forum.getName());
                stmt.setString(2, forum.getShort_name());
                stmt.setString(3, forum.getUser().toString());

                stmt.executeUpdate();

                try (ResultSet resultSet = stmt.getGeneratedKeys()) {
                    resultSet.next();
                    forum.setId(resultSet.getInt(1));
                }
            } catch (SQLException e) {
                return new Response(Response.Codes.UNKNOWN_ERROR);
            }
        } catch (Exception e) {
            return new Response(Response.Codes.INVALID_QUERY);
        }
        return new Response(forum);
    }

    @Override
    public Response details(String forum, String[] related) {
        Forum forumModel;

        if (related != null && (Arrays.asList(related).contains("forum") || Arrays.asList(related).contains("thread"))) {
            return new Response(Response.Codes.INCORRECT_QUERY);
        }

        try (Connection connection = dataSource.getConnection()) {
            String query = "SELECT * FROM forum WHERE short_name = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, forum);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        forumModel = new Forum(resultSet);
                    } else {
                        return new Response(Response.Codes.NOT_FOUND);
                    }
                }
            }
            if (related != null) {
                if (Arrays.asList(related).contains("user")) {
                    forumModel.setUser(new UserDAOImpl(dataSource).details((String) forumModel.getUser()).getResponse());
                } else {
                    return new Response(Response.Codes.INCORRECT_QUERY);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new Response(Response.Codes.UNKNOWN_ERROR);
        }

        return new Response(forumModel);
    }

    @Override
    public Response listPosts(String forum, String since, Integer limit, String order, String[] related) {
        List<Post> array = new ArrayList<>();

        order = order == null ? "desc" : order;

        final StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT * FROM post ");
        queryBuilder.append("WHERE forum = ? ");
        if (since != null) {
            queryBuilder.append("AND date >= ? ");
        }
        queryBuilder.append("ORDER BY date ");
        switch (order) {
            case "asc":
                queryBuilder.append("ASC");
                break;
            case "desc":
                queryBuilder.append("DESC");
                break;
            default:
                return new Response(Response.Codes.INCORRECT_QUERY);
        }
        if (limit != null) {
            queryBuilder.append(" LIMIT ?");
        }
        queryBuilder.append(';');

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(queryBuilder.toString())) {
                int parameterIndex = 0;
                preparedStatement.setString(++parameterIndex, forum);
                if (since != null) {
                    preparedStatement.setString(++parameterIndex, since);
                }
                if (limit != null) {
                    preparedStatement.setInt(++parameterIndex, limit);
                }
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        Post post = new Post(resultSet);
                        if (related != null) {
                            if (Arrays.asList(related).contains("user")) {
                                post.setUser(new UserDAOImpl(dataSource).details((String) post.getUser()).getResponse());
                            }
                            if (Arrays.asList(related).contains("forum")) {
                                post.setForum(details((String) post.getForum(), null).getResponse());
                            }
                            if (Arrays.asList(related).contains("thread")) {
                                post.setThread(new ThreadDAOImpl(dataSource).details((Integer) post.getThread(), null).getResponse());
                            }
                        }
                        array.add(post);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new Response(Response.Codes.INCORRECT_QUERY);
        }

        return new Response(array);
    }

    @Override
    public Response listThreads(String forum, String since, Integer limit, String order, String[] related) {
        List<Thread> array = new ArrayList<>();

        if (related != null && Arrays.asList(related).contains("thread")) {
            return new Response(Response.Codes.INCORRECT_QUERY);
        }

        order = order == null ? "desc" : order;

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT * FROM thread ");
        queryBuilder.append("WHERE forum = ? ");
        if (since != null) {
            queryBuilder.append("AND date >= ? ");
        }
        queryBuilder.append("ORDER BY date ");
        switch (order) {
            case "asc":
                queryBuilder.append("ASC");
                break;
            case "desc":
                queryBuilder.append("DESC");
                break;
            default:
                return new Response(Response.Codes.INCORRECT_QUERY);
        }
        if (limit != null) {
            queryBuilder.append(" LIMIT ?");
        }
        queryBuilder.append(';');

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(queryBuilder.toString())) {
                int parameterIndex = 0;
                preparedStatement.setString(++parameterIndex, forum);
                if (since != null) {
                    preparedStatement.setString(++parameterIndex, since);
                }
                if (limit != null) {
                    preparedStatement.setInt(++parameterIndex, limit);
                }
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    while (resultSet.next()) {
                        Thread thread = new Thread(resultSet);
                        if (related != null) {
                            if (Arrays.asList(related).contains("user")) {
                                thread.setUser(new UserDAOImpl(dataSource).details((String) thread.getUser()).getResponse());
                            }
                            if (Arrays.asList(related).contains("forum")) {
                                thread.setForum(details((String) thread.getForum(), null).getResponse());
                            }
                        }
                        array.add(thread);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new Response(Response.Codes.INCORRECT_QUERY);
        }

        return new Response(array);
    }

    @Override
    public Response listUsers(String forum, Integer sinceId, Integer limit, String order) {
        return null;
    }


}
