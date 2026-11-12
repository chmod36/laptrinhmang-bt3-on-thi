package bt3.history;

import bt3.network.Protocol;

public record DeleteLog(long id, String time, String action, String objectType, String objectId, String objectName,
        boolean success, String detail, String client) {

    public String toRow() {
        return Protocol.joinRow(id, time, action, objectType, objectId, objectName, success ? 1 : 0, detail, client);
    }

    public static DeleteLog fromRow(String[] f) {
        return new DeleteLog(Long.parseLong(f[0]), f[1], f[2], f[3], f[4], f[5], "1".equals(f[6]), f[7], f[8]);
    }
}
