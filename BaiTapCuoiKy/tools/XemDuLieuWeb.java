import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class XemDuLieuWeb {
    private static final int PORT = 8090;
    private static final int REFRESH_SECONDS = 3;
    private static final String[][] TABLES = {
        {"cong_dung", "SELECT * FROM cong_dung ORDER BY ma_cd"},
        {"thuoc", "SELECT * FROM thuoc ORDER BY ngay_xoa IS NOT NULL, ma_thuoc"},
        {"nhat_ky_xoa", "SELECT * FROM nhat_ky_xoa ORDER BY id DESC"}
    };
    private static final String STYLE = """
            body{font-family:sans-serif;margin:24px;background:#f5f7fb;color:#1d2433}
            h1{color:#1f3a68}h2{margin-top:28px;color:#1f3a68}
            table{border-collapse:collapse;background:#fff;font-size:14px}
            th,td{border:1px solid #cfd6e4;padding:6px 10px;text-align:left}
            th{background:#1f3a68;color:#fff}tr.xoa td{background:#fde8e8}
            .null{color:#98a2b3;font-style:italic}.note{color:#5b6475}
            """;

    private static Properties config;

    public static void main(String[] args) throws IOException {
        config = new Properties();
        try (FileReader in = new FileReader(args[0], StandardCharsets.UTF_8)) {
            config.load(in);
        }
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", XemDuLieuWeb::handle);
        server.start();
        System.out.println("Trang xem dữ liệu: http://localhost:" + PORT + " (Ctrl+C để tắt)");
    }

    private static void handle(HttpExchange exchange) throws IOException {
        byte[] body = page().getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        exchange.sendResponseHeaders(200, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }

    private static String page() {
        StringBuilder html = new StringBuilder("<!doctype html><html lang='vi'><head><meta charset='utf-8'>")
                .append("<meta http-equiv='refresh' content='").append(REFRESH_SECONDS).append("'>")
                .append("<title>CSDL quanlythuoc</title><style>").append(STYLE).append("</style></head><body>")
                .append("<h1>Cơ sở dữ liệu quanlythuoc</h1><p class='note'>Tự tải lại sau mỗi ")
                .append(REFRESH_SECONDS).append(" giây. Dòng tô đỏ là thuốc đang nằm trong thùng rác.</p>");
        try (Connection connection = DriverManager.getConnection(config.getProperty("db.url"),
                config.getProperty("db.user"), config.getProperty("db.password", ""))) {
            for (String[] table : TABLES) {
                appendTable(html, connection, table[0], table[1]);
            }
        } catch (SQLException e) {
            html.append("<p>Không kết nối được MySQL: ").append(escape(e.getMessage())).append("</p>");
        }
        return html.append("</body></html>").toString();
    }

    private static void appendTable(StringBuilder html, Connection connection, String name, String sql)
            throws SQLException {
        try (Statement statement = connection.createStatement(); ResultSet rows = statement.executeQuery(sql)) {
            ResultSetMetaData meta = rows.getMetaData();
            StringBuilder body = new StringBuilder();
            int count = 0;
            while (rows.next()) {
                count++;
                boolean inTrash = "thuoc".equals(name) && rows.getObject("ngay_xoa") != null;
                body.append(inTrash ? "<tr class='xoa'>" : "<tr>");
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    Object value = rows.getObject(i);
                    body.append(value == null ? "<td class='null'>NULL</td>" : "<td>" + escape(value) + "</td>");
                }
                body.append("</tr>");
            }
            html.append("<h2>").append(name).append(" (").append(count).append(" dòng)</h2><table><tr>");
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                html.append("<th>").append(meta.getColumnLabel(i)).append("</th>");
            }
            html.append("</tr>").append(body).append("</table>");
        }
    }

    private static String escape(Object value) {
        return value.toString().replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
