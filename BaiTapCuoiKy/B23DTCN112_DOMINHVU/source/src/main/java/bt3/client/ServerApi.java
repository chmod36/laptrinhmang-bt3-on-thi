package bt3.client;

import bt3.common.DeleteLog;
import bt3.common.Medicine;
import bt3.common.Protocol;
import bt3.common.Protocol.Command;
import bt3.common.Response;
import bt3.common.Usage;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;

final class ServerApi {
    private final InetSocketAddress server = new InetSocketAddress(Protocol.HOST, Protocol.PORT);

    String address() {
        return Protocol.HOST + ":" + Protocol.PORT;
    }

    Response send(Command command, String... args) throws IOException {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(Protocol.TIMEOUT_MS);
            byte[] out = Protocol.request(command, args).getBytes(StandardCharsets.UTF_8);
            socket.send(new DatagramPacket(out, out.length, server));
            byte[] buffer = new byte[Protocol.BUFFER_SIZE];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            return Response.parse(new String(packet.getData(), packet.getOffset(), packet.getLength(),
                    StandardCharsets.UTF_8));
        }
    }

    List<Medicine> medicines(String keyword, String usageId) throws IOException {
        return list(Medicine::fromRow, Command.MED_LIST, keyword, usageId);
    }

    List<Medicine> trash() throws IOException {
        return list(Medicine::fromRow, Command.TRASH_LIST);
    }

    List<Usage> usages() throws IOException {
        return list(Usage::fromRow, Command.USE_LIST);
    }

    List<Medicine> usageDependents(String usageId) throws IOException {
        return list(Medicine::fromRow, Command.USE_CHECK, usageId);
    }

    List<DeleteLog> logs() throws IOException {
        return list(DeleteLog::fromRow, Command.LOG_LIST);
    }

    private <T> List<T> list(Function<String[], T> fromRow, Command command, String... args) throws IOException {
        Response response = send(command, args);
        if (!response.ok()) {
            throw new IOException(response.body());
        }
        return response.rows(fromRow);
    }
}
