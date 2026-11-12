package bt3.server;

import bt3.common.Protocol;
import bt3.common.Protocol.Command;
import bt3.common.Protocol.UsageDeleteMode;
import bt3.common.RequestException;
import bt3.common.Response;
import bt3.common.Validation;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

final class RequestHandler {
    private final CatalogService catalog;
    private final DeleteService deletes;

    RequestHandler(Database db) {
        catalog = new CatalogService(db);
        deletes = new DeleteService(db);
    }

    synchronized Response handle(String request, String client) {
        List<String> parts = Arrays.asList(request.split(Protocol.FIELD, -1));
        Command command;
        try {
            command = Command.valueOf(parts.get(0));
        } catch (IllegalArgumentException e) {
            return Response.error("Lệnh không hợp lệ: " + parts.get(0));
        }
        try {
            return dispatch(command, i -> i < parts.size() ? parts.get(i) : "", client);
        } catch (RequestException e) {
            return Response.error(e.getMessage());
        } catch (SQLException e) {
            return Response.error("Lỗi cơ sở dữ liệu: " + e.getMessage());
        }
    }

    private interface Args {
        String get(int index);
    }

    private Response dispatch(Command command, Args a, String client) throws SQLException, RequestException {
        return switch (command) {
            case MED_LIST -> catalog.medicines(a.get(1), a.get(2));
            case MED_ADD -> catalog.addMedicine(Validation.medicine(a.get(1), a.get(2), a.get(3), a.get(4), a.get(5)));
            case MED_UPDATE -> catalog.updateMedicine(
                    Validation.medicine(a.get(1), a.get(2), a.get(3), a.get(4), a.get(5)));
            case MED_DELETE -> deletes.trash(Validation.ids(a.get(1), "thuốc"), client);
            case TRASH_LIST -> catalog.trash();
            case TRASH_RESTORE -> deletes.restore(Validation.ids(a.get(1), "thuốc"), client);
            case TRASH_PURGE -> deletes.purge(Validation.ids(a.get(1), "thuốc"), client);
            case TRASH_EMPTY -> deletes.emptyTrash(client);
            case USE_LIST -> catalog.usages();
            case USE_ADD -> catalog.addUsage(Validation.usage(a.get(1), a.get(2), a.get(3)));
            case USE_UPDATE -> catalog.updateUsage(Validation.usage(a.get(1), a.get(2), a.get(3)));
            case USE_CHECK -> deletes.usageDependents(Validation.id(a.get(1), "công dụng"));
            case USE_DELETE -> deletes.deleteUsage(Validation.id(a.get(1), "công dụng"), mode(a.get(2)), a.get(3),
                    client);
            case LOG_LIST -> catalog.logs();
        };
    }

    private static UsageDeleteMode mode(String text) throws RequestException {
        try {
            return UsageDeleteMode.valueOf(text);
        } catch (IllegalArgumentException e) {
            throw new RequestException("Cách xóa công dụng không hợp lệ: " + text);
        }
    }
}
