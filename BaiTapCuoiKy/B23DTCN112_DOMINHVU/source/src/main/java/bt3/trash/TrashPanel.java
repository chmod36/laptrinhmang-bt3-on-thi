package bt3.trash;

import bt3.medicine.Medicine;
import bt3.network.Protocol;
import bt3.network.Protocol.Command;
import bt3.network.Response;
import bt3.ui.MedicineClient;
import bt3.ui.TablePanel;
import bt3.ui.Ui;
import java.util.ArrayList;
import java.util.List;

public final class TrashPanel extends TablePanel {

    public TrashPanel(MedicineClient app) {
        super(app, "THÙNG RÁC  –  thuốc đã xóa: khôi phục lại hoặc xóa vĩnh viễn khỏi cơ sở dữ liệu", "thuốc",
                new String[] {"Mã thuốc", "Tên thuốc", "Giá (VNĐ)", "Công dụng", "Thời điểm xóa"}, true);
        setWidths(90, 250, 110, 230, 170);
        addButton("Khôi phục", Ui.SUCCESS, true, this::restore);
        addButton("Xóa vĩnh viễn", Ui.DANGER, true, this::purge);
        addButton("Dọn sạch thùng rác", Ui.DANGER, false, this::empty);
    }

    public void show(List<Medicine> list) {
        List<Object[]> rows = new ArrayList<>();
        for (Medicine m : list) {
            rows.add(new Object[] {m.id(), m.name(), Ui.MONEY.format(m.price()), m.usageId() + " – " + m.usageName(),
                m.deletedAt()});
        }
        setRows(rows);
    }

    private void restore() {
        List<String> ids = selected(0);
        app.send("Khôi phục " + ids.size() + " thuốc", Command.TRASH_RESTORE,
                response -> app.showBatch("Kết quả khôi phục", response, null), Protocol.joinIds(ids));
    }

    private void purge() {
        List<String> ids = selected(0);
        String message = "Xóa VĨNH VIỄN " + ids.size() + " thuốc sau khỏi cơ sở dữ liệu?\n"
                + Ui.preview(selectedLabels()) + "\n\nThao tác này không thể hoàn tác.";
        if (app.confirm("Xác nhận xóa vĩnh viễn", message)) {
            app.send("Xóa vĩnh viễn " + ids.size() + " thuốc", Command.TRASH_PURGE,
                    response -> app.showBatch("Kết quả xóa vĩnh viễn", response, null), Protocol.joinIds(ids));
        }
    }

    private void empty() {
        if (table.getRowCount() == 0) {
            app.showMessage("Dọn sạch thùng rác", Response.error("Thùng rác đang trống"));
            return;
        }
        if (app.confirm("Xác nhận dọn sạch thùng rác", "Xóa vĩnh viễn toàn bộ " + table.getRowCount()
                + " thuốc trong thùng rác?\nThao tác này không thể hoàn tác.")) {
            app.send("Dọn sạch thùng rác", Command.TRASH_EMPTY,
                    response -> app.showBatch("Kết quả dọn thùng rác", response, null));
        }
    }
}
