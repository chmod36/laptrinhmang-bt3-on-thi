package bt3.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public final class Sql {

    private Sql() {
    }

    public static int update(Connection c, String sql, Object... params) throws SQLException {
        try (PreparedStatement statement = prepare(c, sql, params)) {
            return statement.executeUpdate();
        }
    }

    public static PreparedStatement prepare(Connection c, String sql, Object... params) throws SQLException {
        PreparedStatement statement = c.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
        return statement;
    }
}
