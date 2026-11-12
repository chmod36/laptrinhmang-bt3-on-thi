package bt3.server;

import bt3.common.DeleteLog;
import bt3.common.Protocol;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

final class DeleteLogDao {

    private DeleteLogDao() {
    }

    static void add(Connection c, DeleteLog log) throws SQLException {
        Sql.update(c, "INSERT INTO nhat_ky_xoa (hanh_dong, doi_tuong, ma_doi_tuong, ten_doi_tuong, thanh_cong,"
                + " chi_tiet, dia_chi_client) VALUES (?, ?, ?, ?, ?, ?, ?)", log.action(), log.objectType(),
                log.objectId(), log.objectName(), log.success(), log.detail(), log.client());
    }

    static List<DeleteLog> latest(Connection c, int limit) throws SQLException {
        List<DeleteLog> result = new ArrayList<>();
        String sql = "SELECT id, thoi_gian, hanh_dong, doi_tuong, ma_doi_tuong, ten_doi_tuong, thanh_cong, chi_tiet,"
                + " dia_chi_client FROM nhat_ky_xoa ORDER BY id DESC LIMIT ?";
        try (PreparedStatement statement = Sql.prepare(c, sql, limit);
                ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                result.add(new DeleteLog(rs.getLong(1), rs.getTimestamp(2).toLocalDateTime().format(Protocol.TIME),
                        rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6), rs.getBoolean(7),
                        rs.getString(8), rs.getString(9)));
            }
        }
        return result;
    }
}
