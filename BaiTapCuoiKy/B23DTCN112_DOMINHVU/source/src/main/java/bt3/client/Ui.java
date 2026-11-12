package bt3.client;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;

final class Ui {
    static final String STUDENT_NAME = "Đỗ Minh Vũ";
    static final String STUDENT_ID = "B23DTCN112";
    static final String STUDENT_CLASS = "D23TXCN04-B";
    static final String COURSE = "INT1433 – Lập trình mạng";

    static final Color PRIMARY = new Color(0x1F3864);
    static final Color HEADER_BG = new Color(0xE9EFF7);
    static final Color MUTED = new Color(0x4A5568);
    static final Color SUCCESS = new Color(0x1E7B34);
    static final Color SUCCESS_BG = new Color(0xE6F4EA);
    static final Color DANGER = new Color(0xB42318);
    static final Color DANGER_BG = new Color(0xFDECEA);
    static final Color WARNING = new Color(0x9A5B00);
    static final Color SELECTION_BG = new Color(0xCFDDF0);

    static final Font TITLE = new Font(Font.SANS_SERIF, Font.BOLD, 21);
    static final Font HEADING = new Font(Font.SANS_SERIF, Font.BOLD, 17);
    static final Font BODY = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    static final Font BODY_BOLD = BODY.deriveFont(Font.BOLD);

    static final int GAP = 10;
    static final int PADDING = 14;
    static final int ROW_HEIGHT = 30;
    static final int FIELD_COLUMNS = 26;
    static final int MESSAGE_WIDTH = 460;
    static final int PREVIEW_LIMIT = 8;
    static final int SEARCH_DELAY_MS = 300;
    static final int SEARCH_MIN_CHARS = 2;
    static final Dimension WINDOW_SIZE = new Dimension(1200, 740);
    static final Dimension DIALOG_TABLE_SIZE = new Dimension(560, 170);

    static final NumberFormat MONEY = NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"));
    static final String[] MEDICINE_FORMS = {"Viên nén", "Viên nang", "Viên sủi", "Thuốc bột", "Siro", "Dung dịch",
        "Thuốc mỡ"};

    private Ui() {
    }

    static Border padding(int size) {
        return BorderFactory.createEmptyBorder(size, size, size, size);
    }

    static JLabel label(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    static JButton button(String text, Color color, Runnable action) {
        JButton button = new JButton(text);
        button.setFont(color == null ? BODY : BODY_BOLD);
        if (color != null) {
            button.setForeground(color);
        }
        button.addActionListener(e -> action.run());
        return button;
    }

    static String wrap(String text) {
        String html = text.replace("&", "&amp;").replace("<", "&lt;").replace("\n", "<br>");
        return "<html><body style='width:" + MESSAGE_WIDTH + "px'>" + html + "</body></html>";
    }

    static String preview(List<String> lines) {
        List<String> shown = lines.stream().limit(PREVIEW_LIMIT).map(line -> "  • " + line).toList();
        String more = lines.size() > PREVIEW_LIMIT ? "\n  … và " + (lines.size() - PREVIEW_LIMIT) + " mục khác" : "";
        return String.join("\n", shown) + more;
    }

    static void styleTable(JTable table) {
        table.setFont(BODY);
        table.setRowHeight(ROW_HEIGHT);
        table.setSelectionBackground(SELECTION_BG);
        table.setSelectionForeground(Color.BLACK);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setFont(BODY_BOLD);
        table.getTableHeader().setReorderingAllowed(false);
    }

    static void localizeDialogs() {
        UIManager.put("OptionPane.yesButtonText", "Đồng ý");
        UIManager.put("OptionPane.noButtonText", "Không");
        UIManager.put("OptionPane.okButtonText", "Đóng");
        UIManager.put("OptionPane.cancelButtonText", "Hủy");
        UIManager.put("OptionPane.messageFont", BODY);
        UIManager.put("OptionPane.buttonFont", BODY_BOLD);
    }
}
