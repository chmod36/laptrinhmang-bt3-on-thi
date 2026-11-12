package bt3.database;

import bt3.network.RequestException;
import bt3.usage.UsageDao;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Properties;

public final class Database {
    private static final String CONFIG = "/db.properties";
    private static final String SCHEMA = "/sql/schema.sql";
    private static final String SAMPLE_DATA = "/sql/sample-data.sql";

    private final String url;
    private final String user;
    private final String password;

    public interface Work<T> {
        T run(Connection connection) throws SQLException, RequestException;
    }

    private Database(Properties config) {
        url = config.getProperty("db.url");
        user = config.getProperty("db.user");
        password = config.getProperty("db.password", "");
    }

    public static Database fromConfig() throws IOException {
        Properties config = new Properties();
        try (InputStream in = resource(CONFIG)) {
            config.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
        return new Database(config);
    }

    public String address() {
        return url.split("\\?")[0];
    }

    public void init() throws SQLException, IOException {
        try (Connection connection = connect()) {
            runScript(connection, SCHEMA);
            if (UsageDao.count(connection) == 0) {
                runScript(connection, SAMPLE_DATA);
            }
        }
    }

    public <T> T query(Work<T> work) throws SQLException, RequestException {
        try (Connection connection = connect()) {
            return work.run(connection);
        }
    }

    public <T> T inTransaction(Work<T> work) throws SQLException, RequestException {
        try (Connection connection = connect()) {
            connection.setAutoCommit(false);
            try {
                T result = work.run(connection);
                connection.commit();
                return result;
            } catch (SQLException | RequestException | RuntimeException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    private static void runScript(Connection connection, String name) throws SQLException, IOException {
        String script;
        try (InputStream in = resource(name)) {
            script = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }
        try (Statement statement = connection.createStatement()) {
            for (String sql : Arrays.stream(script.split(";")).map(String::trim).filter(s -> !s.isEmpty()).toList()) {
                statement.execute(sql);
            }
        }
    }

    private static InputStream resource(String name) throws IOException {
        InputStream in = Database.class.getResourceAsStream(name);
        if (in == null) {
            throw new IOException("Không tìm thấy file " + name);
        }
        return in;
    }
}
