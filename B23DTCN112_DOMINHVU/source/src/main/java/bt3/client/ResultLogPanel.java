package bt3.client;

import java.awt.BorderLayout;
import java.awt.Component;
import java.time.LocalDateTime;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

/** Màn hình hiển thị kết quả các lần xóa: mỗi lần một dòng, xanh là thành công, đỏ là thất bại. */
final class ResultLogPanel extends JPanel {
    private static final String SUCCESS = "Thành công";
    private static final String FAILURE = "Thất bại";
    private static final int RESULT_COLUMN = 4;
    private static final int CELL_PADDING = 5;
    private static final int[] COLUMN_WIDTHS = {50, 170, 110, 70, 110, 560};

    private final DefaultTableModel model = new DefaultTableModel(
            new String[] {"STT", "Thời gian", "Đối tượng", "Mã", "Kết quả", "Chi tiết phản hồi từ Server"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JLabel summary = new JLabel();
    private int successCount;
    private int failureCount;

    ResultLogPanel() {
        super(new BorderLayout(Ui.GAP, Ui.GAP));
        setBorder(Ui.padding(Ui.PADDING));
        JLabel title = new JLabel("KẾT QUẢ XÓA  –  phản hồi của Server cho từng yêu cầu xóa");
        title.setFont(Ui.HEADING);
        title.setForeground(Ui.PRIMARY);
        add(title, BorderLayout.NORTH);

        JTable table = new JTable(model);
        Ui.styleTable(table);
        table.setDefaultRenderer(Object.class, new ResultRenderer());
        for (int i = 0; i < COLUMN_WIDTHS.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(COLUMN_WIDTHS[i]);
        }
        add(new JScrollPane(table), BorderLayout.CENTER);

        summary.setFont(Ui.BODY_BOLD);
        JButton clear = new JButton("Xóa nhật ký");
        clear.setFont(Ui.BODY);
        clear.addActionListener(e -> clear());
        JPanel footer = new JPanel(new BorderLayout());
        footer.add(summary, BorderLayout.WEST);
        footer.add(clear, BorderLayout.EAST);
        add(footer, BorderLayout.SOUTH);
        updateSummary();
    }

    void add(String objectName, String id, boolean success, String detail) {
        if (success) {
            successCount++;
        } else {
            failureCount++;
        }
        model.addRow(new Object[] {model.getRowCount() + 1, LocalDateTime.now().format(Ui.TIME), objectName, id,
                success ? SUCCESS : FAILURE, detail});
        updateSummary();
    }

    private void clear() {
        model.setRowCount(0);
        successCount = 0;
        failureCount = 0;
        updateSummary();
    }

    private void updateSummary() {
        int total = successCount + failureCount;
        summary.setText(total == 0
                ? "Chưa có yêu cầu xóa nào. Hãy xóa thuốc hoặc công dụng ở tab 1, 2."
                : "Tổng số yêu cầu: " + total + "   |   Thành công: " + successCount + "   |   Thất bại: " + failureCount);
    }

    /** Tô màu theo kết quả và tự xuống dòng, tăng chiều cao dòng để không cắt mất thông báo dài. */
    private static final class ResultRenderer extends JTextArea implements TableCellRenderer {
        ResultRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setBorder(Ui.padding(CELL_PADDING));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean selected, boolean focused,
                int row, int column) {
            boolean success = SUCCESS.equals(table.getModel().getValueAt(table.convertRowIndexToModel(row), RESULT_COLUMN));
            setText(value == null ? "" : value.toString());
            setBackground(selected ? Ui.SELECTION_BG : success ? Ui.SUCCESS_BG : Ui.DANGER_BG);
            setForeground(column == RESULT_COLUMN ? (success ? Ui.SUCCESS : Ui.DANGER) : table.getForeground());
            setFont(column == RESULT_COLUMN ? Ui.BODY_BOLD : Ui.BODY);
            setSize(table.getColumnModel().getColumn(column).getWidth(), Short.MAX_VALUE);
            int height = Math.max(Ui.ROW_HEIGHT, getPreferredSize().height);
            if (column == table.getColumnCount() - 1 && table.getRowHeight(row) != height) {
                table.setRowHeight(row, height);
            }
            return this;
        }
    }
}
