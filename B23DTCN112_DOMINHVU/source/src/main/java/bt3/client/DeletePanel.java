package bt3.client;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import java.util.function.BiConsumer;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/** Màn hình xóa một loại đối tượng: bảng danh sách, nút tải lại và nút xóa dòng đang chọn (có hộp xác nhận). */
final class DeletePanel extends JPanel {
    private final String objectName;
    private final DefaultTableModel model;
    private final JTable table;
    private final JButton reloadButton = new JButton("Tải lại danh sách");
    private final JButton deleteButton;
    private final JLabel selection = new JLabel(" ");
    private boolean busy;

    DeletePanel(String objectName, String[] columns, BiConsumer<String, String> onDelete, Runnable onReload) {
        super(new BorderLayout(Ui.GAP, Ui.GAP));
        this.objectName = objectName;
        setBorder(Ui.padding(Ui.PADDING));
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        Ui.styleTable(table);
        table.getSelectionModel().addListSelectionListener(e -> updateSelection());

        JLabel title = new JLabel("XÓA " + objectName.toUpperCase() + "  –  chọn một dòng trong bảng rồi nhấn Xóa");
        title.setFont(Ui.HEADING);
        title.setForeground(Ui.PRIMARY);
        add(title, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        deleteButton = new JButton("Xóa " + objectName + " đã chọn");
        deleteButton.setFont(Ui.BODY_BOLD);
        deleteButton.setForeground(Ui.DANGER);
        deleteButton.addActionListener(e -> confirmDelete(onDelete));
        reloadButton.setFont(Ui.BODY);
        reloadButton.addActionListener(e -> onReload.run());
        selection.setFont(Ui.BODY);
        selection.setForeground(Ui.MUTED);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, Ui.GAP, 0));
        buttons.add(reloadButton);
        buttons.add(deleteButton);
        JPanel footer = new JPanel(new BorderLayout());
        footer.add(selection, BorderLayout.WEST);
        footer.add(buttons, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);
        updateSelection();
    }

    void setRows(List<Object[]> rows) {
        model.setRowCount(0);
        rows.forEach(model::addRow);
        updateSelection();
    }

    void setBusy(boolean busy) {
        this.busy = busy;
        reloadButton.setEnabled(!busy);
        updateSelection();
    }

    private void updateSelection() {
        int row = table.getSelectedRow();
        deleteButton.setEnabled(row >= 0 && !busy);
        selection.setText(row < 0 ? "Chưa chọn " + objectName + " nào" : "Đang chọn: " + cell(row, 0) + " – " + cell(row, 1));
    }

    private void confirmDelete(BiConsumer<String, String> onDelete) {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        String id = cell(row, 0);
        String name = cell(row, 1);
        String message = "Bạn có chắc muốn xóa " + objectName + " \"" + name + "\" (mã " + id + ")?\n"
                + "Yêu cầu sẽ được gửi tới Server qua UDP.";
        int answer = JOptionPane.showConfirmDialog(this, message, "Xác nhận xóa " + objectName,
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (answer == JOptionPane.YES_OPTION) {
            onDelete.accept(id, name);
        }
    }

    private String cell(int viewRow, int column) {
        return model.getValueAt(table.convertRowIndexToModel(viewRow), column).toString();
    }
}
