package bt3.client;

import bt3.common.Medicine;
import bt3.common.Protocol;
import bt3.common.Response;
import bt3.common.Usage;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Gửi một yêu cầu UDP tới Server và chờ phản hồi (tối đa {@link Protocol#TIMEOUT_MS} ms). */
final class UdpClient {
    private final InetSocketAddress server = new InetSocketAddress(Protocol.HOST, Protocol.PORT);

    Response send(String command, String id) throws IOException {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(Protocol.TIMEOUT_MS);
            byte[] out = Protocol.request(command, id).getBytes(StandardCharsets.UTF_8);
            socket.send(new DatagramPacket(out, out.length, server));
            byte[] buffer = new byte[Protocol.BUFFER_SIZE];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            return Response.parse(new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8));
        }
    }

    List<Medicine> listMedicines() throws IOException {
        return list(Protocol.LIST_MEDICINES).stream().map(Medicine::fromRow).toList();
    }

    List<Usage> listUsages() throws IOException {
        return list(Protocol.LIST_USAGES).stream().map(Usage::fromRow).toList();
    }

    private List<String[]> list(String command) throws IOException {
        Response response = send(command, null);
        if (!response.ok()) {
            throw new IOException(response.body());
        }
        return response.rows();
    }
}
