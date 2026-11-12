package bt3.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

abstract class TablePanel extends JPanel {
    protected final MedicineClient app;
    protected final DefaultTableModel model;
    protected final JTable table;
    private final String objectName;
    private final JPanel top = new JPanel(new BorderLayout(Ui.GAP, Ui.GAP));
    private final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, Ui.GAP, 0));
    private final JLabel info = Ui.label(" ", Ui.BODY, Ui.MUTED);
    private final List<JButton> allButtons = new ArrayList<>();
    private final List<JButton> selectionButtons = new ArrayList<>();
    private boolean busy;

    TablePanel(MedicineClient app, String heading, String objectName, String[] columns, boolean multiSelect) {
        super(new BorderLayout(Ui.GAP, Ui.GAP));
        this.app = app;
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
        table.setSelectionMode(multiSelect ? ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
                : ListSelectionModel.SINGLE_SELECTION);
        table.getSelectionModel().addListSelectionListener(e -> updateState());
        top.add(Ui.label(heading, Ui.HEADING, Ui.PRIMARY), BorderLayout.NORTH);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        JPanel footer = new JPanel(new BorderLayout(Ui.GAP, 0));
        footer.add(info, BorderLayout.WEST);
        footer.add(buttons, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);
        addButton("Tải lại", null, false, app::refreshAll);
    }

    protected void setFilters(JComponent filters) {
        top.add(filters, BorderLayout.CENTER);
    }

    protected JButton addButton(String text, Color color, boolean needsSelection, Runnable action) {
        JButton button = Ui.button(text, color, action);
        buttons.add(button);
        allButtons.add(button);
        if (needsSelection) {
            selectionButtons.add(button);
        }
        updateState();
        return button;
    }

    protected void setWidths(int... widths) {
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    void setRows(List<Object[]> rows) {
        model.setRowCount(0);
        rows.forEach(model::addRow);
        updateState();
    }

    void setBusy(boolean busy) {
        this.busy = busy;
        updateState();
    }

    protected List<String> selected(int column) {
        List<String> values = new ArrayList<>();
        for (int row : table.getSelectedRows()) {
            values.add(cell(row, column));
        }
        return values;
    }

    protected List<String> selectedLabels() {
        List<String> labels = new ArrayList<>();
        for (int row : table.getSelectedRows()) {
            labels.add(cell(row, 0) + " – " + cell(row, 1));
        }
        return labels;
    }

    protected String cell(int viewRow, int column) {
        return String.valueOf(model.getValueAt(table.convertRowIndexToModel(viewRow), column));
    }

    protected String infoText(int total, int chosen) {
        if (total == 0) {
            return "Không có " + objectName + " nào";
        }
        if (chosen == 0) {
            return "Có " + total + " " + objectName + " – chưa chọn dòng nào";
        }
        return "Đang chọn " + chosen + " " + objectName + ": " + String.join(", ", selected(0));
    }

    protected void updateState() {
        boolean chosen = table.getSelectedRowCount() > 0;
        allButtons.forEach(button -> button.setEnabled(!busy));
        selectionButtons.forEach(button -> button.setEnabled(chosen && !busy));
        info.setText(infoText(table.getRowCount(), table.getSelectedRowCount()));
    }
}
