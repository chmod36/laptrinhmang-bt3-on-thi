package bt3.trash;

import bt3.database.Database;
import bt3.history.DeleteLog;
import bt3.history.DeleteLogDao;
import bt3.medicine.CatalogService;
import bt3.medicine.Medicine;
import bt3.medicine.MedicineDao;
import bt3.network.Protocol.UsageDeleteMode;
import bt3.network.RequestException;
import bt3.network.Response;
import bt3.network.Validation;
import bt3.usage.Usage;
import bt3.usage.UsageDao;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public final class DeleteService {
    private static final String MEDICINE = "Thuốc";
    private static final String USAGE = "Công dụng";

    private static final Step TRASH = new Step("Chuyển vào thùng rác", m -> !m.inTrash(),
            "Thuốc đã nằm trong thùng rác", MedicineDao::moveToTrash, "Đã chuyển vào thùng rác, có thể khôi phục");
    private static final Step RESTORE = new Step("Khôi phục", Medicine::inTrash,
            "Thuốc không nằm trong thùng rác", MedicineDao::restore, "Đã khôi phục về danh sách thuốc");
    private static final Step PURGE = new Step("Xóa vĩnh viễn", Medicine::inTrash,
            "Chỉ xóa vĩnh viễn được thuốc đã nằm trong thùng rác", MedicineDao::purge,
            "Đã xóa vĩnh viễn khỏi cơ sở dữ liệu");

    private final Database db;

    private record Step(String action, Predicate<Medicine> allowed, String rejected, Change change, String done) {
    }

    private interface Change {
        void apply(Connection c, String id) throws SQLException;
    }

    public DeleteService(Database db) {
        this.db = db;
    }

    public Response trash(List<String> ids, String client) throws SQLException, RequestException {
        return batch(ids, TRASH, client);
    }

    public Response restore(List<String> ids, String client) throws SQLException, RequestException {
        return batch(ids, RESTORE, client);
    }

    public Response purge(List<String> ids, String client) throws SQLException, RequestException {
        return batch(ids, PURGE, client);
    }

    public Response emptyTrash(String client) throws SQLException, RequestException {
        List<String> ids = db.query(MedicineDao::trash).stream().map(Medicine::id).toList();
        if (ids.isEmpty()) {
            return Response.error("Thùng rác đang trống, không có thuốc nào để xóa");
        }
        return batch(ids, PURGE, client);
    }

    public Response usageDependents(String usageId) throws SQLException, RequestException {
        return Response.ofRows(db.query(c -> {
            CatalogService.requireUsage(c, usageId);
            return MedicineDao.byUsage(c, usageId);
        }), Medicine::toRow);
    }

    public Response deleteUsage(String id, UsageDeleteMode mode, String target, String client)
            throws SQLException, RequestException {
        String name = db.query(c -> UsageDao.find(c, id).map(Usage::name).orElse(""));
        String action = switch (mode) {
            case ONLY -> "Xóa công dụng";
            case MOVE -> "Xóa công dụng, chuyển thuốc";
            case CASCADE -> "Xóa công dụng và thuốc";
        };
        try {
            String message = db.inTransaction(c -> removeUsage(c, id, mode, target));
            log(new BatchItem(id, name, true, message), action, USAGE, client);
            return Response.success(message);
        } catch (RequestException e) {
            log(new BatchItem(id, name, false, e.getMessage()), action, USAGE, client);
            throw e;
        }
    }

    private String removeUsage(Connection c, String id, UsageDeleteMode mode, String target)
            throws SQLException, RequestException {
        Usage usage = CatalogService.requireUsage(c, id);
        List<Medicine> users = MedicineDao.byUsage(c, id);
        String extra = switch (mode) {
            case ONLY -> refuseIfUsed(usage, users);
            case MOVE -> moveMedicines(c, usage, users, target);
            case CASCADE -> users.isEmpty() ? "" : ", xóa vĩnh viễn " + MedicineDao.purgeByUsage(c, id) + " thuốc ("
                    + ids(users) + ")";
        };
        UsageDao.delete(c, id);
        return "Đã xóa công dụng " + usage.name() + " (" + id + ")" + extra;
    }

    private static String refuseIfUsed(Usage usage, List<Medicine> users) throws RequestException {
        if (!users.isEmpty()) {
            throw new RequestException("Không thể xóa công dụng " + usage.name() + " (" + usage.id() + "): đang được "
                    + users.size() + " thuốc sử dụng (" + ids(users) + ")");
        }
        return "";
    }

    private static String moveMedicines(Connection c, Usage usage, List<Medicine> users, String target)
            throws SQLException, RequestException {
        String targetId = Validation.id(target, "công dụng nhận thuốc");
        if (targetId.equals(usage.id())) {
            throw new RequestException("Công dụng nhận thuốc phải khác công dụng đang xóa");
        }
        Usage destination = CatalogService.requireUsage(c, targetId);
        int moved = MedicineDao.changeUsage(c, usage.id(), targetId);
        return users.isEmpty() ? "" : ", chuyển " + moved + " thuốc (" + ids(users) + ") sang công dụng "
                + destination.name() + " (" + targetId + ")";
    }

    private Response batch(List<String> ids, Step step, String client) throws SQLException, RequestException {
        List<BatchItem> items = new ArrayList<>();
        for (String id : ids) {
            BatchItem item = db.inTransaction(c -> apply(c, id, step));
            log(item, step.action(), MEDICINE, client);
            items.add(item);
        }
        return new Response(items.stream().anyMatch(BatchItem::ok), Response.ofRows(items, BatchItem::toRow).body());
    }

    private static BatchItem apply(Connection c, String id, Step step) throws SQLException {
        Optional<Medicine> found = MedicineDao.find(c, id);
        if (found.isEmpty()) {
            return new BatchItem(id, "", false, "Không tìm thấy thuốc mã này (có thể đã bị xóa vĩnh viễn)");
        }
        Medicine medicine = found.get();
        if (!step.allowed().test(medicine)) {
            return new BatchItem(id, medicine.name(), false, step.rejected());
        }
        step.change().apply(c, id);
        return new BatchItem(id, medicine.name(), true, step.done());
    }

    private void log(BatchItem item, String action, String objectType, String client)
            throws SQLException, RequestException {
        DeleteLog log = new DeleteLog(0, "", action, objectType, item.id(), item.name(), item.ok(), item.message(),
                client);
        db.query(c -> {
            DeleteLogDao.add(c, log);
            return null;
        });
    }

    private static String ids(List<Medicine> medicines) {
        return String.join(", ", medicines.stream().map(Medicine::id).toList());
    }
}
