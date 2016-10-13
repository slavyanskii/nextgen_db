package mail.not.tp.db;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by viacheslav on 12.10.16.
 */
public interface TResultHandler<T> {
    T handle(ResultSet resultSet) throws SQLException;
}
