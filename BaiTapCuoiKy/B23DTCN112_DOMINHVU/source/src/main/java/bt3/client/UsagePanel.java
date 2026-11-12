package bt3.client;

import bt3.common.Medicine;
import bt3.common.Protocol.Command;
import bt3.common.Protocol.UsageDeleteMode;
import bt3.common.Usage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class UsagePanel extends TablePanel {
    private List<Usage> shown = List.of();

    UsagePanel(MedicineClient app) {
        super(app, "QUẢN LÝ CÔNG DỤNG  –  công dụng còn thuốc sử dụng sẽ có tùy chọn xử lý khi xóa", "công dụng",
                new String[] {"Mã công dụng", "Tên công dụng", "Mô tả", "Thuốc đang bán", "Thuốc trong thùng rác"},
                false);
        setWidths(110, 180, 330, 120, 160);
        addButton("Thêm công dụng", Ui.PRIMARY, false, () -> openForm(null, true));
        addButton("Sửa", Ui.PRIMARY, true, () -> selectedUsage().ifPresent(u -> openForm(u, false)));
        addButton("Xóa công dụng đã chọn", Ui.DANGER, true, this::delete);
    }

    void show(List<Usage> list) {
        shown = list;
        List<Object[]> rows = new ArrayList<>();
        for (Usage u : list) {
            rows.add(new Object[] {u.id(), u.name(), u.description(), u.activeCount(), u.trashCount()});
        }
        setRows(rows);
    }

    private Optional<Usage> selectedUsage() {
        String id = selected(0).isEmpty() ? "" : selected(0).get(0);
        return shown.stream().filter(u -> u.id().equals(id)).findFirst();
    }

    private void openForm(Usage initial, boolean isNew) {
        Optional<Usage> result = UsageForm.open(this, initial, isNew);
        if (result.isEmpty()) {
            return;
        }
        Usage u = result.get();
        app.send(isNew ? "Thêm công dụng " + u.id() : "Cập nhật công dụng " + u.id(),
                isNew ? Command.USE_ADD : Command.USE_UPDATE, response -> {
                    app.showMessage(isNew ? "Thêm công dụng" : "Sửa công dụng", response);
                    if (!response.ok()) {
                        openForm(u, isNew);
                    }
                }, u.id(), u.name(), u.description());
    }

    private void delete() {
        selectedUsage().ifPresent(usage -> app.call("Kiểm tra thuốc đang dùng công dụng " + usage.id(),
                () -> app.api.usageDependents(usage.id()), dependents -> decide(usage, dependents)));
    }

    private void decide(Usage usage, List<Medicine> dependents) {
        if (dependents.isEmpty()) {
            if (app.confirm("Xác nhận xóa công dụng", "Xóa công dụng \"" + usage.name() + "\" (mã " + usage.id()
                    + ")?\nKhông có thuốc nào đang dùng công dụng này.")) {
                sendDelete(usage, UsageDeleteMode.ONLY, "");
            }
            return;
        }
        List<Usage> others = app.usages().stream().filter(u -> !u.id().equals(usage.id())).toList();
        UsageDeleteDialog.ask(this, usage, dependents, others).ifPresent(choice -> {
            boolean cascade = choice.mode() == UsageDeleteMode.CASCADE;
            if (!cascade || app.confirm("Xác nhận xóa vĩnh viễn", "Xóa vĩnh viễn công dụng " + usage.id() + " và "
                    + dependents.size() + " thuốc đang dùng nó?\nThao tác này không thể hoàn tác.")) {
                sendDelete(usage, choice.mode(), choice.target());
            }
        });
    }

    private void sendDelete(Usage usage, UsageDeleteMode mode, String target) {
        app.send("Xóa công dụng " + usage.id(), Command.USE_DELETE,
                response -> app.showMessage("Kết quả xóa công dụng", response), usage.id(), mode.name(), target);
    }
}
