package bt3.server;

import bt3.common.Protocol;
import bt3.common.Response;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MedicineServer {
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static void main(String[] args) throws IOException {
        Database db = Database.fromConfig();
        try {
            db.init();
        } catch (SQLException e) {
            System.err.println("Không kết nối được MySQL tại " + db.address() + ": " + e.getMessage());
            System.err.println("Hãy bật MySQL trong XAMPP (hoặc Laragon) rồi chạy lại Server.");
            return;
        }
        RequestHandler handler = new RequestHandler(db);
        try (DatagramSocket socket = new DatagramSocket(Protocol.PORT)) {
            System.out.println("Đã kết nối MySQL: " + db.address());
            System.out.println("Server UDP quản lý thuốc đang lắng nghe tại cổng " + Protocol.PORT);
            byte[] buffer = new byte[Protocol.BUFFER_SIZE];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String request = new String(packet.getData(), packet.getOffset(), packet.getLength(),
                        StandardCharsets.UTF_8);
                String client = packet.getSocketAddress().toString().replaceFirst("^/", "");
                Response response = handler.handle(request, client);
                byte[] out = fit(response).format().getBytes(StandardCharsets.UTF_8);
                socket.send(new DatagramPacket(out, out.length, packet.getSocketAddress()));
                System.out.printf("[%s] %s  %s  ->  %s%n", LocalTime.now().format(CLOCK), client,
                        request.replace(Protocol.FIELD, " "), summary(response));
            }
        }
    }

    private static Response fit(Response response) {
        if (response.format().getBytes(StandardCharsets.UTF_8).length <= Protocol.BUFFER_SIZE) {
            return response;
        }
        return Response.error("Dữ liệu trả về vượt quá kích thước một gói tin UDP");
    }

    private static String summary(Response response) {
        String status = response.ok() ? Protocol.OK : Protocol.ERR;
        int rows = response.rows().size();
        return rows > 1 ? status + "  " + rows + " dòng" : status + "  " + response.body().replace(Protocol.FIELD, " | ");
    }
}
