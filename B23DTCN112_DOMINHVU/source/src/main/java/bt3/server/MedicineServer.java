package bt3.server;

import bt3.common.Medicine;
import bt3.common.Protocol;
import bt3.common.Response;
import bt3.common.Usage;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Server UDP quản lý thuốc theo công dụng. Dữ liệu lưu trong bộ nhớ, khởi động lại thì nạp lại dữ liệu mẫu. */
public class MedicineServer {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final Map<String, Medicine> medicines = new LinkedHashMap<>();
    private final Map<String, Usage> usages = new LinkedHashMap<>();

    public MedicineServer() {
        List.of(
                new Usage("CD01", "Giảm đau", "Giảm các cơn đau nhẹ và vừa"),
                new Usage("CD02", "Hạ sốt", "Hạ thân nhiệt khi bị sốt"),
                new Usage("CD03", "Bổ sung vitamin", "Bổ sung vitamin, tăng sức đề kháng"),
                new Usage("CD04", "Sát khuẩn", "Sát khuẩn vết thương ngoài da"),
                new Usage("CD05", "Kháng histamin", "Giảm triệu chứng dị ứng"))
                .forEach(usage -> usages.put(usage.id(), usage));
        List.of(
                new Medicine("T001", "Paracetamol 500mg", 15000, "CD02", "Viên nén"),
                new Medicine("T002", "Vitamin C 500mg", 25000, "CD03", "Viên sủi"),
                new Medicine("T003", "Povidone iodine 10%", 32000, "CD04", "Dung dịch"),
                new Medicine("T004", "Ibuprofen 200mg", 28000, "CD01", "Viên nang"),
                new Medicine("T005", "Efferalgan 150mg", 41000, "CD02", "Thuốc bột"))
                .forEach(medicine -> medicines.put(medicine.id(), medicine));
    }

    public synchronized Response handle(String request) {
        String[] parts = request.split(Protocol.SEPARATOR, -1);
        String id = parts.length > 1 ? parts[1].trim().toUpperCase(Locale.ROOT) : "";
        return switch (parts[0]) {
            case Protocol.LIST_MEDICINES -> Response.success(rows(medicines.values(), Medicine::toRow));
            case Protocol.LIST_USAGES -> Response.success(rows(usages.values(), Usage::toRow));
            case Protocol.DELETE_MEDICINE -> deleteMedicine(id);
            case Protocol.DELETE_USAGE -> deleteUsage(id);
            default -> Response.error("Lệnh không hợp lệ: " + parts[0]);
        };
    }

    private Response deleteMedicine(String id) {
        if (id.isEmpty()) {
            return Response.error("Chưa có mã thuốc cần xóa");
        }
        Medicine removed = medicines.remove(id);
        if (removed == null) {
            return Response.error("Không tìm thấy thuốc mã " + id + " (có thể đã bị xóa trước đó)");
        }
        return Response.success("Đã xóa thuốc " + removed.name() + " (" + id + ")");
    }

    // Công dụng còn được thuốc tham chiếu thì không xóa, tránh thuốc trỏ tới công dụng không tồn tại.
    private Response deleteUsage(String id) {
        if (id.isEmpty()) {
            return Response.error("Chưa có mã công dụng cần xóa");
        }
        Usage usage = usages.get(id);
        if (usage == null) {
            return Response.error("Không tìm thấy công dụng mã " + id + " (có thể đã bị xóa trước đó)");
        }
        List<String> users = medicines.values().stream()
                .filter(medicine -> medicine.usageId().equals(id))
                .map(Medicine::id)
                .toList();
        if (!users.isEmpty()) {
            return Response.error("Không thể xóa công dụng " + usage.name() + " (" + id + "): đang được "
                    + users.size() + " thuốc sử dụng (" + String.join(", ", users) + "). Hãy xóa các thuốc này trước.");
        }
        usages.remove(id);
        return Response.success("Đã xóa công dụng " + usage.name() + " (" + id + ")");
    }

    private static <T> String rows(Collection<T> items, Function<T, String> toRow) {
        return items.stream().map(toRow).collect(Collectors.joining(Protocol.ROW_SEPARATOR));
    }

    public static void main(String[] args) throws IOException {
        MedicineServer server = new MedicineServer();
        try (DatagramSocket socket = new DatagramSocket(Protocol.PORT)) {
            System.out.println("Server UDP quản lý thuốc đang lắng nghe tại cổng " + Protocol.PORT);
            byte[] buffer = new byte[Protocol.BUFFER_SIZE];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String request = new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
                Response response = server.handle(request);
                byte[] out = response.format().getBytes(StandardCharsets.UTF_8);
                socket.send(new DatagramPacket(out, out.length, packet.getSocketAddress()));
                System.out.printf("[%s] %s  %s  ->  %s%n", LocalTime.now().format(TIME), packet.getSocketAddress(),
                        request.replace(Protocol.SEPARATOR, " "), summary(request, response));
            }
        }
    }

    private static String summary(String request, Response response) {
        boolean isList = request.equals(Protocol.LIST_MEDICINES) || request.equals(Protocol.LIST_USAGES);
        if (isList && response.ok()) {
            return Protocol.OK + "  " + response.rows().size() + " bản ghi";
        }
        return (response.ok() ? Protocol.OK : Protocol.ERR) + "  " + response.body();
    }
}
