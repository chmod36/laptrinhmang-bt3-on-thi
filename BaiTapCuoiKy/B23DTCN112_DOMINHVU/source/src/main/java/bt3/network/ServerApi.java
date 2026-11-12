package bt3.network;

import bt3.network.Protocol.Command;
import bt3.history.DeleteLog;
import bt3.medicine.Medicine;
import bt3.usage.Usage;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.function.Function;

public final class ServerApi {
    private final InetSocketAddress server = new InetSocketAddress(Protocol.HOST, Protocol.PORT);

    public String address() {
        return Protocol.HOST + ":" + Protocol.PORT;
    }

    public Response send(Command command, String... args) throws IOException {
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

    public List<Medicine> medicines(String keyword, String usageId) throws IOException {
        return list(Medicine::fromRow, Command.MED_LIST, keyword, usageId);
    }

    public List<Medicine> trash() throws IOException {
        return list(Medicine::fromRow, Command.TRASH_LIST);
    }

    public List<Usage> usages() throws IOException {
        return list(Usage::fromRow, Command.USE_LIST);
    }

    public List<Medicine> usageDependents(String usageId) throws IOException {
        return list(Medicine::fromRow, Command.USE_CHECK, usageId);
    }

    public List<DeleteLog> logs() throws IOException {
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
