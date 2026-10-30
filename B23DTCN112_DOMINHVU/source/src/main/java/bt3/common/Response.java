package bt3.common;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/** Phản hồi của Server: thành công/thất bại kèm nội dung (thông báo hoặc danh sách bản ghi). */
public record Response(boolean ok, String body) {
    private static final Pattern FIELDS = Pattern.compile(Pattern.quote(Protocol.FIELD_SEPARATOR));

    public static Response success(String body) {
        return new Response(true, body);
    }

    public static Response error(String message) {
        return new Response(false, message);
    }

    public static Response parse(String raw) {
        String[] parts = raw.split(Protocol.SEPARATOR, 2);
        String body = parts.length > 1 ? parts[1] : "";
        return new Response(Protocol.OK.equals(parts[0]), body);
    }

    public String format() {
        return (ok ? Protocol.OK : Protocol.ERR) + Protocol.SEPARATOR + body;
    }

    public List<String[]> rows() {
        if (body.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(body.split(Protocol.ROW_SEPARATOR))
                .map(line -> FIELDS.split(line, -1))
                .toList();
    }

    public static String joinRow(Object... fields) {
        return String.join(Protocol.FIELD_SEPARATOR, Arrays.stream(fields).map(String::valueOf).toList());
    }
}
