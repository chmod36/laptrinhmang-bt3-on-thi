package bt3.medicine;

import bt3.network.Protocol;

public record Medicine(String id, String name, long price, String usageId, String usageName, String form,
        String deletedAt) {

    public static Medicine of(String id, String name, long price, String usageId, String form) {
        return new Medicine(id, name, price, usageId, "", form, "");
    }

    public boolean inTrash() {
        return !deletedAt.isEmpty();
    }

    public String toRow() {
        return Protocol.joinRow(id, name, price, usageId, usageName, form, deletedAt);
    }

    public static Medicine fromRow(String[] f) {
        return new Medicine(f[0], f[1], Long.parseLong(f[2]), f[3], f[4], f[5], f[6]);
    }
}
