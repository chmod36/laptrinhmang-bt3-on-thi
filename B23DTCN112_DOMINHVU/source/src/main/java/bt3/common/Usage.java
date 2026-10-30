package bt3.common;

/** Công dụng của thuốc: mã, tên, mô tả. */
public record Usage(String id, String name, String description) {

    public String toRow() {
        return Response.joinRow(id, name, description);
    }

    public static Usage fromRow(String[] fields) {
        return new Usage(fields[0], fields[1], fields[2]);
    }
}
