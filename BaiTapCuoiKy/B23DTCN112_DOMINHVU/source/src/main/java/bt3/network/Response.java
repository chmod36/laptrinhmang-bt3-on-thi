package bt3.network;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public record Response(boolean ok, String body) {

    public static Response success(String body) {
        return new Response(true, body);
    }

    public static Response error(String message) {
        return new Response(false, message);
    }

    public static <T> Response ofRows(Collection<T> items, Function<T, String> toRow) {
        return success(items.stream().map(toRow).collect(Collectors.joining(Protocol.ROW)));
    }

    public static Response parse(String raw) {
        String[] parts = raw.split(Protocol.FIELD, 2);
        String body = parts.length > 1 ? parts[1] : "";
        return new Response(Protocol.OK.equals(parts[0]), body);
    }

    public String format() {
        return (ok ? Protocol.OK : Protocol.ERR) + Protocol.FIELD + body;
    }

    public List<String[]> rows() {
        if (body.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(body.split(Protocol.ROW)).map(line -> line.split(Protocol.FIELD, -1)).toList();
    }

    public <T> List<T> rows(Function<String[], T> fromRow) {
        return rows().stream().map(fromRow).toList();
    }
}
