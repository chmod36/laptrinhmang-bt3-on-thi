package bt3.trash;

import bt3.network.Protocol;

public record BatchItem(String id, String name, boolean ok, String message) {

    public String toRow() {
        return Protocol.joinRow(id, name, ok ? 1 : 0, message);
    }

    public static BatchItem fromRow(String[] f) {
        return new BatchItem(f[0], f[1], "1".equals(f[2]), f[3]);
    }
}
