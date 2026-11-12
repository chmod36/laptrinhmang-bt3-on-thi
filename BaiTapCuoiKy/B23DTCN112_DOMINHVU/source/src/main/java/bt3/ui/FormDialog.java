package bt3.ui;

import bt3.network.RequestException;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public final class FormDialog {
    private final Component parent;
    private final JDialog dialog;
    private final JPanel fields = new JPanel(new GridBagLayout());
    private final JLabel error = Ui.label(" ", Ui.BODY, Ui.DANGER);
    private int row;

    public interface Reader<T> {
        T read() throws RequestException;
    }

    public FormDialog(Component parent, String title) {
        this.parent = parent;
        dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), title, ModalityType.APPLICATION_MODAL);
    }

    public void field(String label, JComponent input) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(Ui.GAP / 2, 0, Ui.GAP / 2, Ui.GAP);
        c.anchor = GridBagConstraints.WEST;
        c.gridy = row++;
        fields.add(Ui.label(label, Ui.BODY_BOLD, Ui.PRIMARY), c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        input.setFont(Ui.BODY);
        fields.add(input, c);
    }

    public <T> Optional<T> open(Reader<T> reader) {
        AtomicReference<T> result = new AtomicReference<>();
        JButton save = Ui.button("Lưu", Ui.PRIMARY, () -> {
            try {
                result.set(reader.read());
                dialog.dispose();
            } catch (RequestException e) {
                error.setText(Ui.wrap(e.getMessage()));
                dialog.pack();
            }
        });
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, Ui.GAP, 0));
        buttons.add(save);
        buttons.add(Ui.button("Hủy", null, dialog::dispose));
        JPanel south = new JPanel(new BorderLayout(0, Ui.GAP));
        south.add(error, BorderLayout.NORTH);
        south.add(buttons, BorderLayout.SOUTH);
        JPanel content = new JPanel(new BorderLayout(0, Ui.GAP));
        content.setBorder(Ui.padding(Ui.PADDING));
        content.add(fields, BorderLayout.CENTER);
        content.add(south, BorderLayout.SOUTH);
        dialog.setContentPane(content);
        dialog.getRootPane().setDefaultButton(save);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
        return Optional.ofNullable(result.get());
    }
}
