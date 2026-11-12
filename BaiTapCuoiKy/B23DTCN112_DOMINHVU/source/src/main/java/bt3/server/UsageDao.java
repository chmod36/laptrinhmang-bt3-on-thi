package bt3.server;

import bt3.common.Usage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class UsageDao {
    private static final String SELECT = "SELECT c.ma_cd, c.ten_cd, c.mo_ta,"
            + " COALESCE(SUM(t.ma_thuoc IS NOT NULL AND t.ngay_xoa IS NULL), 0),"
            + " COALESCE(SUM(t.ngay_xoa IS NOT NULL), 0)"
            + " FROM cong_dung c LEFT JOIN thuoc t ON t.ma_cd = c.ma_cd ";
    private static final String GROUP = " GROUP BY c.ma_cd, c.ten_cd, c.mo_ta ORDER BY c.ma_cd";

    private UsageDao() {
    }

    static List<Usage> all(Connection c) throws SQLException {
        return list(c, SELECT + GROUP);
    }

    static Optional<Usage> find(Connection c, String id) throws SQLException {
        return list(c, SELECT + "WHERE c.ma_cd = ?" + GROUP, id).stream().findFirst();
    }

    static int count(Connection c) throws SQLException {
        try (PreparedStatement statement = c.prepareStatement("SELECT COUNT(*) FROM cong_dung");
                ResultSet rs = statement.executeQuery()) {
            rs.next();
            return rs.getInt(1);
        }
    }

    static void insert(Connection c, Usage u) throws SQLException {
        Sql.update(c, "INSERT INTO cong_dung (ten_cd, mo_ta, ma_cd) VALUES (?, ?, ?)",
                u.name(), u.description(), u.id());
    }

    static void update(Connection c, Usage u) throws SQLException {
        Sql.update(c, "UPDATE cong_dung SET ten_cd = ?, mo_ta = ? WHERE ma_cd = ?",
                u.name(), u.description(), u.id());
    }

    static void delete(Connection c, String id) throws SQLException {
        Sql.update(c, "DELETE FROM cong_dung WHERE ma_cd = ?", id);
    }

    private static List<Usage> list(Connection c, String sql, Object... params) throws SQLException {
        List<Usage> result = new ArrayList<>();
        try (PreparedStatement statement = Sql.prepare(c, sql, params);
                ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                result.add(new Usage(rs.getString(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getInt(5)));
            }
        }
        return result;
    }
}
