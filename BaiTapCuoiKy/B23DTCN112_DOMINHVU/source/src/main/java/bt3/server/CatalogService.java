package bt3.server;

import bt3.common.DeleteLog;
import bt3.common.Medicine;
import bt3.common.Protocol;
import bt3.common.RequestException;
import bt3.common.Response;
import bt3.common.Usage;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Optional;

final class CatalogService {
    private final Database db;

    CatalogService(Database db) {
        this.db = db;
    }

    Response medicines(String keyword, String usageId) throws SQLException, RequestException {
        String usage = Protocol.clean(usageId).toUpperCase(Locale.ROOT);
        return Response.ofRows(db.query(c -> MedicineDao.search(c, Protocol.clean(keyword), usage)), Medicine::toRow);
    }

    Response trash() throws SQLException, RequestException {
        return Response.ofRows(db.query(MedicineDao::trash), Medicine::toRow);
    }

    Response usages() throws SQLException, RequestException {
        return Response.ofRows(db.query(UsageDao::all), Usage::toRow);
    }

    Response logs() throws SQLException, RequestException {
        return Response.ofRows(db.query(c -> DeleteLogDao.latest(c, Protocol.LOG_LIMIT)), DeleteLog::toRow);
    }

    Response addMedicine(Medicine medicine) throws SQLException, RequestException {
        return db.inTransaction(c -> {
            requireUsage(c, medicine.usageId());
            Optional<Medicine> existing = MedicineDao.find(c, medicine.id());
            if (existing.isPresent()) {
                throw new RequestException("Mã thuốc " + medicine.id() + " đã tồn tại" + (existing.get().inTrash()
                        ? " trong thùng rác. Hãy khôi phục thuốc đó thay vì thêm mới" : ""));
            }
            MedicineDao.insert(c, medicine);
            return Response.success("Đã thêm thuốc " + medicine.name() + " (" + medicine.id() + ")");
        });
    }

    Response updateMedicine(Medicine medicine) throws SQLException, RequestException {
        return db.inTransaction(c -> {
            Medicine current = MedicineDao.find(c, medicine.id()).orElseThrow(
                    () -> new RequestException("Không tìm thấy thuốc mã " + medicine.id()));
            if (current.inTrash()) {
                throw new RequestException("Thuốc " + medicine.id() + " đang nằm trong thùng rác, hãy khôi phục trước");
            }
            requireUsage(c, medicine.usageId());
            MedicineDao.update(c, medicine);
            return Response.success("Đã cập nhật thuốc " + medicine.name() + " (" + medicine.id() + ")");
        });
    }

    Response addUsage(Usage usage) throws SQLException, RequestException {
        return db.inTransaction(c -> {
            if (UsageDao.find(c, usage.id()).isPresent()) {
                throw new RequestException("Mã công dụng " + usage.id() + " đã tồn tại");
            }
            UsageDao.insert(c, usage);
            return Response.success("Đã thêm công dụng " + usage.name() + " (" + usage.id() + ")");
        });
    }

    Response updateUsage(Usage usage) throws SQLException, RequestException {
        return db.inTransaction(c -> {
            requireUsage(c, usage.id());
            UsageDao.update(c, usage);
            return Response.success("Đã cập nhật công dụng " + usage.name() + " (" + usage.id() + ")");
        });
    }

    static Usage requireUsage(Connection c, String id) throws SQLException, RequestException {
        return UsageDao.find(c, id).orElseThrow(() -> new RequestException("Không tìm thấy công dụng mã " + id
                + " (có thể đã bị xóa trước đó)"));
    }
}
