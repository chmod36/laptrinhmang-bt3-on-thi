package bt3.medicine;

import bt3.network.Protocol;
import bt3.network.Protocol.Command;
import bt3.ui.MedicineClient;
import bt3.ui.TablePanel;
import bt3.ui.Ui;
import bt3.usage.Usage;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public final class MedicinePanel extends TablePanel {
    private static final String ALL_USAGES = "Tất cả công dụng";

    private final JTextField search = new JTextField(Ui.FIELD_COLUMNS);
    private final JComboBox<String> usageFilter = new JComboBox<>(new String[] {ALL_USAGES});
    private final Timer searchDelay = new Timer(Ui.SEARCH_DELAY_MS, e -> app.searchMedicines());
    private List<Medicine> shown = List.of();
    private List<Usage> usages = List.of();
    private boolean fillingUsages;

    public MedicinePanel(MedicineClient app) {
        super(app, "QUẢN LÝ THUỐC  –  tìm kiếm, thêm, sửa và xóa (chọn được nhiều dòng)", "thuốc",
                new String[] {"Mã thuốc", "Tên thuốc", "Giá (VNĐ)", "Công dụng", "Dạng bào chế"}, true);
        setWidths(90, 260, 110, 230, 140);
        addButton("Thêm thuốc", Ui.PRIMARY, false, () -> openForm(null, true));
        addButton("Sửa", Ui.PRIMARY, true, this::edit);
        addButton("Xóa thuốc đã chọn", Ui.DANGER, true, this::delete);
        setFilters(filters());
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRowCount() == 1) {
                    edit();
                }
            }
        });
    }

    private JPanel filters() {
        searchDelay.setRepeats(false);
        search.setFont(Ui.BODY);
        search.setToolTipText("Nhập ít nhất " + Ui.SEARCH_MIN_CHARS + " ký tự của mã hoặc tên thuốc");
        search.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                scheduleSearch();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                scheduleSearch();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                scheduleSearch();
            }
        });
        usageFilter.setFont(Ui.BODY);
        usageFilter.addActionListener(e -> {
            if (!fillingUsages) {
                app.searchMedicines();
            }
        });
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, Ui.GAP, 0));
        filters.add(Ui.label("Tìm theo mã hoặc tên:", Ui.BODY, Ui.MUTED));
        filters.add(search);
        filters.add(Ui.label("Công dụng:", Ui.BODY, Ui.MUTED));
        filters.add(usageFilter);
        return filters;
    }

    private void scheduleSearch() {
        int length = search.getText().trim().length();
        if (length == 0 || length >= Ui.SEARCH_MIN_CHARS) {
            searchDelay.restart();
        }
    }

    public String keyword() {
        String text = Protocol.clean(search.getText());
        return text.length() >= Ui.SEARCH_MIN_CHARS ? text : "";
    }

    public String usageFilter() {
        int index = usageFilter.getSelectedIndex();
        return index <= 0 ? "" : usages.get(index - 1).id();
    }

    public void showUsages(List<Usage> list) {
        String current = usageFilter();
        fillingUsages = true;
        usages = list;
        usageFilter.removeAllItems();
        usageFilter.addItem(ALL_USAGES);
        list.forEach(usage -> usageFilter.addItem(usage.label()));
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).id().equals(current)) {
                usageFilter.setSelectedIndex(i + 1);
            }
        }
        fillingUsages = false;
    }

    public void show(List<Medicine> list) {
        shown = list;
        List<Object[]> rows = new ArrayList<>();
        for (Medicine m : list) {
            rows.add(new Object[] {m.id(), m.name(), Ui.MONEY.format(m.price()), m.usageId() + " – " + m.usageName(),
                m.form()});
        }
        setRows(rows);
    }

    private void edit() {
        String id = selected(0).get(0);
        shown.stream().filter(m -> m.id().equals(id)).findFirst().ifPresent(m -> openForm(m, false));
    }

    private void openForm(Medicine initial, boolean isNew) {
        Optional<Medicine> result = MedicineForm.open(this, initial, app.usages(), isNew);
        if (result.isEmpty()) {
            return;
        }
        Medicine m = result.get();
        app.send(isNew ? "Thêm thuốc " + m.id() : "Cập nhật thuốc " + m.id(),
                isNew ? Command.MED_ADD : Command.MED_UPDATE, response -> {
                    app.showMessage(isNew ? "Thêm thuốc" : "Sửa thuốc", response);
                    if (!response.ok()) {
                        openForm(m, isNew);
                    }
                }, m.id(), m.name(), String.valueOf(m.price()), m.usageId(), m.form());
    }

    private void delete() {
        List<String> ids = selected(0);
        String message = "Chuyển " + ids.size() + " thuốc sau vào Thùng rác?\n" + Ui.preview(selectedLabels())
                + "\n\nThuốc trong Thùng rác không còn hiển thị ở danh sách bán, có thể khôi phục lại.";
        if (app.confirm("Xác nhận xóa thuốc", message)) {
            sendDelete(ids);
        }
    }

    private void sendDelete(List<String> ids) {
        app.send("Xóa " + ids.size() + " thuốc", Command.MED_DELETE, response -> {
            List<String> undo = app.showBatch("Kết quả xóa thuốc", response, "Hoàn tác (khôi phục)");
            if (!undo.isEmpty()) {
                app.send("Hoàn tác xóa " + undo.size() + " thuốc", Command.TRASH_RESTORE,
                        restored -> app.showBatch("Kết quả hoàn tác", restored, null), Protocol.joinIds(undo));
            }
        }, Protocol.joinIds(ids));
    }
}
