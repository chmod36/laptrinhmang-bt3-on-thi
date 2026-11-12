package bt3.usage;

import bt3.network.Validation;
import bt3.ui.FormDialog;
import bt3.ui.Ui;
import java.awt.Component;
import java.util.Optional;
import javax.swing.JTextField;

public final class UsageForm {

    private UsageForm() {
    }

    public static Optional<Usage> open(Component parent, Usage initial, boolean isNew) {
        Usage u = initial == null ? Usage.of("", "", "") : initial;
        JTextField id = new JTextField(u.id(), Ui.FIELD_COLUMNS);
        JTextField name = new JTextField(u.name(), Ui.FIELD_COLUMNS);
        JTextField description = new JTextField(u.description(), Ui.FIELD_COLUMNS);
        id.setEditable(isNew);
        FormDialog dialog = new FormDialog(parent, isNew ? "Thêm công dụng mới" : "Sửa công dụng " + u.id());
        dialog.field("Mã công dụng", id);
        dialog.field("Tên công dụng", name);
        dialog.field("Mô tả", description);
        return dialog.open(() -> Validation.usage(id.getText(), name.getText(), description.getText()));
    }
}
