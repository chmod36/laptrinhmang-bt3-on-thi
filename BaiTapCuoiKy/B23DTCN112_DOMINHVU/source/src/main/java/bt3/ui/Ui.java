package bt3.ui;

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

public final class Ui {
    public static final String STUDENT_NAME = "Đỗ Minh Vũ";
    public static final String STUDENT_ID = "B23DTCN112";
    public static final String STUDENT_CLASS = "D23TXCN04-B";
    public static final String COURSE = "INT1433 – Lập trình mạng";

    public static final Color PRIMARY = new Color(0x1F3864);
    public static final Color HEADER_BG = new Color(0xE9EFF7);
    public static final Color MUTED = new Color(0x4A5568);
    public static final Color SUCCESS = new Color(0x1E7B34);
    public static final Color SUCCESS_BG = new Color(0xE6F4EA);
    public static final Color DANGER = new Color(0xB42318);
    public static final Color DANGER_BG = new Color(0xFDECEA);
    public static final Color WARNING = new Color(0x9A5B00);
    public static final Color SELECTION_BG = new Color(0xCFDDF0);

    public static final Font TITLE = new Font(Font.SANS_SERIF, Font.BOLD, 21);
    public static final Font HEADING = new Font(Font.SANS_SERIF, Font.BOLD, 17);
    public static final Font BODY = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
    public static final Font BODY_BOLD = BODY.deriveFont(Font.BOLD);

    public static final int GAP = 10;
    public static final int PADDING = 14;
    public static final int ROW_HEIGHT = 30;
    public static final int FIELD_COLUMNS = 26;
    public static final int MESSAGE_WIDTH = 460;
    public static final int PREVIEW_LIMIT = 8;
    public static final int SEARCH_DELAY_MS = 300;
    public static final int SEARCH_MIN_CHARS = 2;
    public static final Dimension WINDOW_SIZE = new Dimension(1200, 740);
    public static final Dimension DIALOG_TABLE_SIZE = new Dimension(560, 170);

    public static final NumberFormat MONEY = NumberFormat.getIntegerInstance(Locale.forLanguageTag("vi-VN"));
    public static final String[] MEDICINE_FORMS = {"Viên nén", "Viên nang", "Viên sủi", "Thuốc bột", "Siro", "Dung dịch",
        "Thuốc mỡ"};

    private Ui() {
    }

    public static Border padding(int size) {
        return BorderFactory.createEmptyBorder(size, size, size, size);
    }

    public static JLabel label(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    public static JButton button(String text, Color color, Runnable action) {
        JButton button = new JButton(text);
        button.setFont(color == null ? BODY : BODY_BOLD);
        if (color != null) {
            button.setForeground(color);
        }
        button.addActionListener(e -> action.run());
        return button;
    }

    public static String wrap(String text) {
        String html = text.replace("&", "&amp;").replace("<", "&lt;").replace("\n", "<br>");
        return "<html><body style='width:" + MESSAGE_WIDTH + "px'>" + html + "</body></html>";
    }

    public static String preview(List<String> lines) {
        List<String> shown = lines.stream().limit(PREVIEW_LIMIT).map(line -> "  • " + line).toList();
        String more = lines.size() > PREVIEW_LIMIT ? "\n  … và " + (lines.size() - PREVIEW_LIMIT) + " mục khác" : "";
        return String.join("\n", shown) + more;
    }

    public static void styleTable(JTable table) {
        table.setFont(BODY);
        table.setRowHeight(ROW_HEIGHT);
        table.setSelectionBackground(SELECTION_BG);
        table.setSelectionForeground(Color.BLACK);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setFont(BODY_BOLD);
        table.getTableHeader().setReorderingAllowed(false);
    }

    public static void localizeDialogs() {
        UIManager.put("OptionPane.yesButtonText", "Đồng ý");
        UIManager.put("OptionPane.noButtonText", "Không");
        UIManager.put("OptionPane.okButtonText", "Đóng");
        UIManager.put("OptionPane.cancelButtonText", "Hủy");
        UIManager.put("OptionPane.messageFont", BODY);
        UIManager.put("OptionPane.buttonFont", BODY_BOLD);
    }
}
