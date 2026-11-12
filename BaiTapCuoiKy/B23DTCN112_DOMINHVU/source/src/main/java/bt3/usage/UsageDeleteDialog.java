package bt3.usage;

import bt3.medicine.Medicine;
import bt3.network.Protocol.UsageDeleteMode;
import bt3.ui.Ui;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

public final class UsageDeleteDialog {

    public record Choice(UsageDeleteMode mode, String target) {
    }

    private UsageDeleteDialog() {
    }

    public static Optional<Choice> ask(Component parent, Usage usage, List<Medicine> dependents, List<Usage> others) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), "Xóa công dụng " + usage.id()
                + " đang có thuốc sử dụng", ModalityType.APPLICATION_MODAL);
        JRadioButton move = new JRadioButton("Chuyển các thuốc này sang công dụng khác rồi xóa công dụng:");
        JComboBox<String> target = new JComboBox<>(others.stream().map(Usage::label).toArray(String[]::new));
        JRadioButton cascade = new JRadioButton("Xóa vĩnh viễn cả " + dependents.size()
                + " thuốc cùng với công dụng (không khôi phục được)");
        ButtonGroup group = new ButtonGroup();
        group.add(move);
        group.add(cascade);
        move.setEnabled(!others.isEmpty());
        target.setEnabled(!others.isEmpty());
        (others.isEmpty() ? cascade : move).setSelected(true);
        AtomicReference<Choice> result = new AtomicReference<>();
        Runnable submit = () -> {
            result.set(move.isSelected()
                    ? new Choice(UsageDeleteMode.MOVE, others.get(target.getSelectedIndex()).id())
                    : new Choice(UsageDeleteMode.CASCADE, ""));
            dialog.dispose();
        };
        JPanel content = new JPanel(new BorderLayout(0, Ui.GAP));
        content.setBorder(Ui.padding(Ui.PADDING));
        content.add(Ui.label(Ui.wrap("Công dụng \"" + usage.name() + "\" (" + usage.id() + ") đang được "
                + dependents.size() + " thuốc sử dụng. Nếu xóa thẳng, các thuốc này sẽ trỏ tới công dụng không còn "
                + "tồn tại nên Server sẽ từ chối. Hãy chọn cách xử lý các thuốc:"), Ui.BODY, Ui.MUTED),
                BorderLayout.NORTH);
        content.add(dependentTable(dependents), BorderLayout.CENTER);
        content.add(options(move, target, cascade, submit, dialog), BorderLayout.SOUTH);
        dialog.setContentPane(content);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return Optional.ofNullable(result.get());
    }

    private static JScrollPane dependentTable(List<Medicine> dependents) {
        Object[][] rows = dependents.stream().map(m -> new Object[] {m.id(), m.name(),
            m.inTrash() ? "Trong thùng rác" : "Đang bán"}).toArray(Object[][]::new);
        JTable table = new JTable(rows, new String[] {"Mã thuốc", "Tên thuốc", "Trạng thái"});
        table.setEnabled(false);
        Ui.styleTable(table);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(Ui.DIALOG_TABLE_SIZE);
        return scroll;
    }

    private static JPanel options(JRadioButton move, JComboBox<String> target, JRadioButton cascade, Runnable submit,
            JDialog dialog) {
        move.setFont(Ui.BODY);
        cascade.setFont(Ui.BODY);
        target.setFont(Ui.BODY);
        JPanel moveRow = new JPanel(new FlowLayout(FlowLayout.LEFT, Ui.GAP, 0));
        moveRow.add(move);
        moveRow.add(target);
        JPanel choices = new JPanel(new GridLayout(2, 1, 0, Ui.GAP / 2));
        choices.add(moveRow);
        choices.add(cascade);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, Ui.GAP, 0));
        buttons.add(Ui.button("Thực hiện xóa", Ui.DANGER, submit));
        buttons.add(Ui.button("Hủy", null, dialog::dispose));
        JPanel panel = new JPanel(new BorderLayout(0, Ui.GAP));
        panel.add(choices, BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }
}
