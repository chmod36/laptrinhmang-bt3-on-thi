package bt3.client;

import bt3.common.Usage;
import bt3.common.Validation;
import java.awt.Component;
import java.util.Optional;
import javax.swing.JTextField;

final class UsageForm {

    private UsageForm() {
    }

    static Optional<Usage> open(Component parent, Usage initial, boolean isNew) {
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
