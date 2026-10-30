package bt3.common;

/** Thuốc: mã, tên, giá (VNĐ), mã công dụng, dạng bào chế. */
public record Medicine(String id, String name, long price, String usageId, String form) {

    public String toRow() {
        return Response.joinRow(id, name, price, usageId, form);
    }

    public static Medicine fromRow(String[] fields) {
        return new Medicine(fields[0], fields[1], Long.parseLong(fields[2]), fields[3], fields[4]);
    }
}
