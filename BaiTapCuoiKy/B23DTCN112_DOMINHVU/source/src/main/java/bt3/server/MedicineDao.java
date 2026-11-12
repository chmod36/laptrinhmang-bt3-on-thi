package bt3.server;

import bt3.common.Medicine;
import bt3.common.Protocol;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class MedicineDao {
    private static final String SELECT = "SELECT t.ma_thuoc, t.ten_thuoc, t.gia, t.ma_cd, c.ten_cd, t.dang_bao_che,"
            + " t.ngay_xoa FROM thuoc t JOIN cong_dung c ON c.ma_cd = t.ma_cd ";

    private MedicineDao() {
    }

    static List<Medicine> search(Connection c, String keyword, String usageId) throws SQLException {
        String pattern = "%" + keyword.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
        return list(c, SELECT + "WHERE t.ngay_xoa IS NULL AND (t.ma_thuoc LIKE ? OR t.ten_thuoc LIKE ?)"
                + " AND (? = '' OR t.ma_cd = ?) ORDER BY t.ma_thuoc", pattern, pattern, usageId, usageId);
    }

    static List<Medicine> trash(Connection c) throws SQLException {
        return list(c, SELECT + "WHERE t.ngay_xoa IS NOT NULL ORDER BY t.ngay_xoa DESC, t.ma_thuoc");
    }

    static List<Medicine> byUsage(Connection c, String usageId) throws SQLException {
        return list(c, SELECT + "WHERE t.ma_cd = ? ORDER BY t.ngay_xoa IS NOT NULL, t.ma_thuoc", usageId);
    }

    static Optional<Medicine> find(Connection c, String id) throws SQLException {
        return list(c, SELECT + "WHERE t.ma_thuoc = ? FOR UPDATE", id).stream().findFirst();
    }

    static void insert(Connection c, Medicine m) throws SQLException {
        Sql.update(c, "INSERT INTO thuoc (ten_thuoc, gia, ma_cd, dang_bao_che, ma_thuoc) VALUES (?, ?, ?, ?, ?)",
                m.name(), m.price(), m.usageId(), m.form(), m.id());
    }

    static void update(Connection c, Medicine m) throws SQLException {
        Sql.update(c, "UPDATE thuoc SET ten_thuoc = ?, gia = ?, ma_cd = ?, dang_bao_che = ? WHERE ma_thuoc = ?",
                m.name(), m.price(), m.usageId(), m.form(), m.id());
    }

    static void moveToTrash(Connection c, String id) throws SQLException {
        Sql.update(c, "UPDATE thuoc SET ngay_xoa = NOW() WHERE ma_thuoc = ?", id);
    }

    static void restore(Connection c, String id) throws SQLException {
        Sql.update(c, "UPDATE thuoc SET ngay_xoa = NULL WHERE ma_thuoc = ?", id);
    }

    static void purge(Connection c, String id) throws SQLException {
        Sql.update(c, "DELETE FROM thuoc WHERE ma_thuoc = ?", id);
    }

    static int changeUsage(Connection c, String fromUsage, String toUsage) throws SQLException {
        return Sql.update(c, "UPDATE thuoc SET ma_cd = ? WHERE ma_cd = ?", toUsage, fromUsage);
    }

    static int purgeByUsage(Connection c, String usageId) throws SQLException {
        return Sql.update(c, "DELETE FROM thuoc WHERE ma_cd = ?", usageId);
    }

    private static List<Medicine> list(Connection c, String sql, Object... params) throws SQLException {
        List<Medicine> result = new ArrayList<>();
        try (PreparedStatement statement = Sql.prepare(c, sql, params); ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                Timestamp deletedAt = rs.getTimestamp(7);
                result.add(new Medicine(rs.getString(1), rs.getString(2), rs.getLong(3), rs.getString(4),
                        rs.getString(5), rs.getString(6),
                        deletedAt == null ? "" : deletedAt.toLocalDateTime().format(Protocol.TIME)));
            }
        }
        return result;
    }
}
