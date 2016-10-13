package mail.not.tp.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by viacheslav on 12.10.16.
 */

public class TExecuter {
    public static <T> T execQuery(Connection connection, String query, TResultHandler<T> handler) throws SQLException {
        final T value;
        try (Statement statement = connection.createStatement()) {
            statement.execute(query);
            try (ResultSet result = statement.getResultSet()) {
                value = handler.handle(result);
            }
        }

        return value;
    }

    public static void execQuery(Connection connection, String query) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(query);
        }
    }
}
