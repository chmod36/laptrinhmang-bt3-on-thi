package bt3.history;

import bt3.ui.MedicineClient;
import bt3.ui.TablePanel;
import bt3.ui.Ui;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

public final class HistoryPanel extends TablePanel {
    private static final String SUCCESS = "Thành công";
    private static final String FAILURE = "Thất bại";
    private static final String ALL = "Tất cả";
    private static final int ACTION_COLUMN = 2;
    private static final int RESULT_COLUMN = 6;
    private static final int CELL_PADDING = 5;

    private final JComboBox<String> resultFilter = new JComboBox<>(new String[] {ALL, SUCCESS, FAILURE});
    private final JComboBox<String> actionFilter = new JComboBox<>(new String[] {ALL});
    private final TableRowSorter<DefaultTableModel> sorter;
    private List<DeleteLog> logs = List.of();

    public HistoryPanel(MedicineClient app) {
        super(app, "LỊCH SỬ XÓA  –  mọi yêu cầu xóa, khôi phục được Server ghi vào bảng nhat_ky_xoa", "lượt",
                new String[] {"STT", "Thời gian", "Hành động", "Đối tượng", "Mã", "Tên", "Kết quả", "Chi tiết",
                    "Client"}, false);
        setWidths(40, 145, 175, 85, 55, 145, 90, 290, 125);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        table.setDefaultRenderer(Object.class, new ResultRenderer());
        resultFilter.setFont(Ui.BODY);
        actionFilter.setFont(Ui.BODY);
        resultFilter.addActionListener(e -> applyFilter());
        actionFilter.addActionListener(e -> applyFilter());
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, Ui.GAP, 0));
        filters.add(Ui.label("Kết quả:", Ui.BODY, Ui.MUTED));
        filters.add(resultFilter);
        filters.add(Ui.label("Hành động:", Ui.BODY, Ui.MUTED));
        filters.add(actionFilter);
        setFilters(filters);
    }

    public void show(List<DeleteLog> list) {
        logs = list;
        String action = String.valueOf(actionFilter.getSelectedItem());
        actionFilter.removeAllItems();
        actionFilter.addItem(ALL);
        list.stream().map(DeleteLog::action).distinct().sorted().forEach(actionFilter::addItem);
        actionFilter.setSelectedItem(action);
        List<Object[]> rows = new ArrayList<>();
        for (DeleteLog log : list) {
            rows.add(new Object[] {log.id(), log.time(), log.action(), log.objectType(), log.objectId(),
                log.objectName(), log.success() ? SUCCESS : FAILURE, log.detail(), log.client()});
        }
        setRows(rows);
        applyFilter();
    }

    private void applyFilter() {
        List<RowFilter<DefaultTableModel, Integer>> filters = new ArrayList<>();
        if (!ALL.equals(resultFilter.getSelectedItem())) {
            filters.add(RowFilter.regexFilter("^" + resultFilter.getSelectedItem() + "$", RESULT_COLUMN));
        }
        Object action = actionFilter.getSelectedItem();
        if (action != null && !ALL.equals(action)) {
            filters.add(RowFilter.regexFilter("^" + Pattern.quote(action.toString()) + "$",
                    ACTION_COLUMN));
        }
        sorter.setRowFilter(RowFilter.andFilter(filters));
        updateState();
    }

    @Override
    protected String infoText(int total, int chosen) {
        long success = logs == null ? 0 : logs.stream().filter(DeleteLog::success).count();
        int all = logs == null ? 0 : logs.size();
        return "Đang hiển thị " + total + "/" + all + " lượt   |   Thành công: " + success + "   |   Thất bại: "
                + (all - success);
    }

    private static final class ResultRenderer extends JTextArea implements TableCellRenderer {

        ResultRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setBorder(Ui.padding(CELL_PADDING));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused,
                int row, int column) {
            boolean success = SUCCESS.equals(table.getModel().getValueAt(table.convertRowIndexToModel(row),
                    RESULT_COLUMN));
            setText(value == null ? "" : value.toString());
            setBackground(selected ? Ui.SELECTION_BG : success ? Ui.SUCCESS_BG : Ui.DANGER_BG);
            setForeground(column == RESULT_COLUMN ? (success ? Ui.SUCCESS : Ui.DANGER) : table.getForeground());
            setFont(column == RESULT_COLUMN ? Ui.BODY_BOLD : Ui.BODY);
            setSize(table.getColumnModel().getColumn(column).getWidth(), Short.MAX_VALUE);
            int height = Math.max(Ui.ROW_HEIGHT, getPreferredSize().height);
            if (height > table.getRowHeight(row)) {
                table.setRowHeight(row, height);
            }
            return this;
        }
    }
}
