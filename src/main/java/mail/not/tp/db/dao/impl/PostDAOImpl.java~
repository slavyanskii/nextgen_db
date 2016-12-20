package mail.not.tp.db.dao.impl;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import mail.not.tp.db.dao.PostDAO;
import mail.not.tp.models.Post;
import mail.not.tp.models.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Arrays;

/**
 * Created by viacheslav on 14.10.16.
 */

@Repository
public class PostDAOImpl implements PostDAO {

    private final DataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    @Autowired
    public PostDAOImpl(DataSource dataSource) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public int status() {
        try {
            final String query = "SELECT COUNT(*) FROM Post;";
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
            this.jdbcTemplate.execute("TRUNCATE TABLE post;");
            this.jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1;");
        } catch (DataAccessException e) {
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

        final Post post;
        try {
            post = new Post(object);
        } catch (Exception e) {
            return new Response(Response.Codes.INCORRECT_QUERY);
        }

        try (Connection connection = dataSource.getConnection()) {
            final String query = "INSERT INTO post (date, thread, message, user, forum, parent, isApproved, isHighlighted, isEdited, isSpam, isDeleted) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                preparedStatement.setString(1, post.getDate());
                preparedStatement.setInt(2, (Integer) post.getThread());
                preparedStatement.setString(3, post.getMessage());
                preparedStatement.setString(4, (String) post.getUser());
                preparedStatement.setString(5, (String) post.getForum());
                preparedStatement.setObject(6, post.getParent());
                preparedStatement.setBoolean(7, post.getIsApproved());
                preparedStatement.setBoolean(8, post.getIsHighlighted());
                preparedStatement.setBoolean(9, post.getIsEdited());
                preparedStatement.setBoolean(10, post.getIsSpam());
                preparedStatement.setBoolean(11, post.getIsDeleted());
                preparedStatement.execute();
                try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        post.setId(resultSet.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new Response(Response.Codes.UNKNOWN_ERROR);
        }

        return new Response(post);
    }


    @Override
    public Response details(int postId, String[] related) {
        final Post postModel;

        try (Connection connection = dataSource.getConnection()) {
            final String query = "SELECT * FROM post WHERE id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, postId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        postModel = new Post(resultSet);
                    } else {
                        return new Response(Response.Codes.NOT_FOUND);
                    }
                }
            }
            if (related != null) {
                if (Arrays.asList(related).contains("user")) {
                    postModel.setUser(new UserDAOImpl(dataSource).details((String) postModel.getUser()).getResponse());
                }
                if (Arrays.asList(related).contains("forum")) {
                    postModel.setForum(new ForumDAOImpl(dataSource).details((String) postModel.getForum(), null).getResponse());
                }
                if (Arrays.asList(related).contains("thread")) {
                    postModel.setThread(new ThreadDAOImpl(dataSource).details((Integer) postModel.getThread(), null).getResponse());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new Response(Response.Codes.UNKNOWN_ERROR);
        }

        return new Response(postModel);
    }

    @Override
    public Response listForumPosts(String forum, String since, Integer limit, String order) {
        return new ForumDAOImpl(dataSource).listPosts(forum, since, limit, order, null);
    }

    @Override
    public Response listThreadPosts(int threadId, String since, Integer limit, String order) {
        return null;
    }

    @Override
    public Response remove(String jsonString) {
        final JsonObject object;
        try {
            object = new JsonParser().parse(jsonString).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            return new Response(Response.Codes.INVALID_QUERY);
        }

        try (Connection connection = dataSource.getConnection()) {
            final String query = "UPDATE post SET isDeleted = 1 WHERE id = ?;";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, object.get("post").getAsInt());
                preparedStatement.execute();
            }
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
            return new Response(Response.Codes.INCORRECT_QUERY);
        }

        return new Response(new Gson().fromJson(object, Object.class));
    }

    @Override
    public Response restore(String jsonString) {
        final JsonObject object;
        try {
            object = new JsonParser().parse(jsonString).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            return new Response(Response.Codes.INVALID_QUERY);
        }

        try (Connection connection = dataSource.getConnection()) {
            final String query = "UPDATE post SET isDeleted = 0 WHERE id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, object.get("post").getAsInt());
                preparedStatement.execute();
            }
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
            return new Response(Response.Codes.INCORRECT_QUERY);
        }

        return new Response(new Gson().fromJson(object, Object.class));
    }

    @Override
    public Response update(String jsonString) {
        final JsonObject object;
        try {
            object = new JsonParser().parse(jsonString).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            return new Response(Response.Codes.INVALID_QUERY);
        }

        try (Connection connection = dataSource.getConnection()) {
            final String query = "UPDATE post SET message = ? WHERE id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, object.get("message").getAsString());
                preparedStatement.setInt(2, object.get("post").getAsInt());
                preparedStatement.execute();
            }
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
            return new Response(Response.Codes.INCORRECT_QUERY);
        }

        return details(object.get("post").getAsInt(), null);
    }

    @Override
    public Response vote(String jsonString) {
        final JsonObject object;
        try {
            object = new JsonParser().parse(jsonString).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            return new Response(Response.Codes.INVALID_QUERY);
        }

        final String likeQuery = "UPDATE post SET likes = likes + 1 WHERE id = ?";
        final String dislikeQuery = "UPDATE post SET dislikes = dislikes + 1 WHERE id = ?";

        final String query = object.get("vote").getAsInt() > 0 ? likeQuery : dislikeQuery;

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, object.get("post").getAsInt());
                preparedStatement.execute();
            }
        } catch (SQLException | NullPointerException e) {
            e.printStackTrace();
            return new Response(Response.Codes.INCORRECT_QUERY);
        }

        return details(object.get("post").getAsInt(), null);
    }
}
