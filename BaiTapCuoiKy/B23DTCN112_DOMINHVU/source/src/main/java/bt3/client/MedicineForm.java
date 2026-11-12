package bt3.client;

import bt3.common.Medicine;
import bt3.common.RequestException;
import bt3.common.Usage;
import bt3.common.Validation;
import java.awt.Component;
import java.util.List;
import java.util.Optional;
import javax.swing.JComboBox;
import javax.swing.JTextField;

final class MedicineForm {

    private MedicineForm() {
    }

    static Optional<Medicine> open(Component parent, Medicine initial, List<Usage> usages, boolean isNew) {
        Medicine m = initial == null ? Medicine.of("", "", 0, "", Ui.MEDICINE_FORMS[0]) : initial;
        JTextField id = new JTextField(m.id(), Ui.FIELD_COLUMNS);
        JTextField name = new JTextField(m.name(), Ui.FIELD_COLUMNS);
        JTextField price = new JTextField(initial == null ? "" : String.valueOf(m.price()), Ui.FIELD_COLUMNS);
        JComboBox<String> usage = new JComboBox<>(usages.stream().map(Usage::label).toArray(String[]::new));
        JComboBox<String> form = new JComboBox<>(Ui.MEDICINE_FORMS);
        form.setEditable(true);
        form.setSelectedItem(m.form());
        for (int i = 0; i < usages.size(); i++) {
            if (usages.get(i).id().equals(m.usageId())) {
                usage.setSelectedIndex(i);
            }
        }
        id.setEditable(isNew);
        FormDialog dialog = new FormDialog(parent, isNew ? "Thêm thuốc mới" : "Sửa thông tin thuốc " + m.id());
        dialog.field("Mã thuốc", id);
        dialog.field("Tên thuốc", name);
        dialog.field("Giá (VNĐ)", price);
        dialog.field("Công dụng", usage);
        dialog.field("Dạng bào chế", form);
        return dialog.open(() -> {
            if (usage.getSelectedIndex() < 0) {
                throw new RequestException("Chưa có công dụng nào, hãy thêm công dụng trước");
            }
            return Validation.medicine(id.getText(), name.getText(), price.getText(),
                    usages.get(usage.getSelectedIndex()).id(), String.valueOf(form.getEditor().getItem()));
        });
    }
}
