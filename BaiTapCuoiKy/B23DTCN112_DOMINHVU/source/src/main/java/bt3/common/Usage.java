package bt3.common;

public record Usage(String id, String name, String description, int activeCount, int trashCount) {

    public static Usage of(String id, String name, String description) {
        return new Usage(id, name, description, 0, 0);
    }

    public String label() {
        return id + " – " + name;
    }

    public String toRow() {
        return Protocol.joinRow(id, name, description, activeCount, trashCount);
    }

    public static Usage fromRow(String[] f) {
        return new Usage(f[0], f[1], f[2], Integer.parseInt(f[3]), Integer.parseInt(f[4]));
    }
}
