package bt3.common;

/**
 * Quy ước gói tin UDP dùng chung cho Client và Server.
 * Yêu cầu: LỆNH [TAB mã]. Phản hồi: OK|ERR TAB nội dung; danh sách gồm các dòng, mỗi trường cách nhau bởi "|".
 */
public final class Protocol {
    public static final String HOST = "127.0.0.1";
    public static final int PORT = 9876;
    public static final int TIMEOUT_MS = 5000;
    public static final int BUFFER_SIZE = 8192;

    public static final String LIST_MEDICINES = "LIST_MED";
    public static final String LIST_USAGES = "LIST_USE";
    public static final String DELETE_MEDICINE = "DEL_MED";
    public static final String DELETE_USAGE = "DEL_USE";

    public static final String OK = "OK";
    public static final String ERR = "ERR";
    public static final String SEPARATOR = "\t";
    public static final String FIELD_SEPARATOR = "|";
    public static final String ROW_SEPARATOR = "\n";

    private Protocol() {
    }

    public static String request(String command, String id) {
        return id == null ? command : command + SEPARATOR + id;
    }
}
