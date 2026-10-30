package bt3.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.Border;

/** Màu, phông chữ, khoảng cách và định dạng dùng chung cho giao diện Client. */
final class Ui {
    static final String STUDENT_NAME = "Đỗ Minh Vũ";
    static final String STUDENT_ID = "B23DTCN112";

    static final Color PRIMARY = new Color(0x1F3864);
    static final Color HEADER_BG = new Color(0xE9EFF7);
    static final Color MUTED = new Color(0x4A5568);
    static final Color SUCCESS = new Color(0x1E7B34);
    static final Color SUCCESS_BG = new Color(0xE6F4EA);
    static final Color DANGER = new Color(0xB42318);
    static final Color DANGER_BG = new Color(0xFDECEA);
    static final Color SELECTION_BG = new Color(0xCFDDF0);

    static final Font TITLE = new Font(Font.SANS_SERIF, Font.BOLD, 21);
    static final Font HEADING = new Font(Font.SANS_SERIF, Font.BOLD, 17);
    static final Font BODY = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    static final Font BODY_BOLD = BODY.deriveFont(Font.BOLD);

    static final int GAP = 10;
    static final int PADDING = 14;
    static final int ROW_HEIGHT = 30;
    static final Dimension WINDOW_SIZE = new Dimension(1120, 700);

    static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");
    static final NumberFormat MONEY = NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"));

    private Ui() {
    }

    static Border padding(int size) {
        return BorderFactory.createEmptyBorder(size, size, size, size);
    }

    static void styleTable(JTable table) {
        table.setFont(BODY);
        table.setRowHeight(ROW_HEIGHT);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setSelectionBackground(SELECTION_BG);
        table.setSelectionForeground(Color.BLACK);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setFont(BODY_BOLD);
        table.getTableHeader().setReorderingAllowed(false);
    }

    /** Nút trong hộp thoại JOptionPane hiển thị tiếng Việt. */
    static void localizeDialogs() {
        UIManager.put("OptionPane.yesButtonText", "Có, xóa");
        UIManager.put("OptionPane.noButtonText", "Không");
        UIManager.put("OptionPane.okButtonText", "Đóng");
        UIManager.put("OptionPane.cancelButtonText", "Hủy");
        UIManager.put("OptionPane.messageFont", BODY);
        UIManager.put("OptionPane.buttonFont", BODY_BOLD);
    }
}
