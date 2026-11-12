package bt3.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

final class Sql {

    private Sql() {
    }

    static int update(Connection c, String sql, Object... params) throws SQLException {
        try (PreparedStatement statement = prepare(c, sql, params)) {
            return statement.executeUpdate();
        }
    }

    static PreparedStatement prepare(Connection c, String sql, Object... params) throws SQLException {
        PreparedStatement statement = c.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
        return statement;
    }
}
