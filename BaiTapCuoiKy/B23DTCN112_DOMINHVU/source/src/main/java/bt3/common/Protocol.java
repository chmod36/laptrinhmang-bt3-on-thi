package bt3.common;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public final class Protocol {
    public static final String HOST = "127.0.0.1";
    public static final int PORT = 9876;
    public static final int TIMEOUT_MS = 5000;
    public static final int BUFFER_SIZE = 65507;
    public static final int LOG_LIMIT = 150;

    public static final String OK = "OK";
    public static final String ERR = "ERR";
    public static final String FIELD = "\t";
    public static final String ROW = "\n";
    public static final String ID_LIST = ",";
    public static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

    public enum Command {
        MED_LIST, MED_ADD, MED_UPDATE, MED_DELETE,
        TRASH_LIST, TRASH_RESTORE, TRASH_PURGE, TRASH_EMPTY,
        USE_LIST, USE_ADD, USE_UPDATE, USE_CHECK, USE_DELETE,
        LOG_LIST
    }

    public enum UsageDeleteMode {
        ONLY, MOVE, CASCADE
    }

    private Protocol() {
    }

    public static String request(Command command, String... args) {
        StringBuilder text = new StringBuilder(command.name());
        for (String arg : args) {
            text.append(FIELD).append(clean(arg));
        }
        return text.toString();
    }

    public static String clean(Object value) {
        return value == null ? "" : value.toString().replaceAll("[\\t\\r\\n]+", " ").trim();
    }

    public static String joinRow(Object... fields) {
        return String.join(FIELD, Arrays.stream(fields).map(Protocol::clean).toList());
    }

    public static String joinIds(List<String> ids) {
        return String.join(ID_LIST, ids);
    }
}
