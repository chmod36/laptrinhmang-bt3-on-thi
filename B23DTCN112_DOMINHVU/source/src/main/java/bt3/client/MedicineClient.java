package bt3.client;

import bt3.common.Medicine;
import bt3.common.Protocol;
import bt3.common.Response;
import bt3.common.Usage;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/** Cửa sổ chính của Client: xóa thuốc, xóa công dụng và hiển thị kết quả xóa nhận từ Server UDP. */
public class MedicineClient extends JFrame {
    private static final String MEDICINE = "thuốc";
    private static final String USAGE = "công dụng";
    private static final String SERVER = Protocol.HOST + ":" + Protocol.PORT;

    private final UdpClient client = new UdpClient();
    private final JTabbedPane tabs = new JTabbedPane();
    private final DeletePanel medicinePanel;
    private final DeletePanel usagePanel;
    private final ResultLogPanel resultPanel = new ResultLogPanel();
    private final JLabel status = new JLabel();

    private record Catalog(List<Usage> usages, List<Medicine> medicines) {
    }

    public MedicineClient() {
        super("Quản lý thuốc theo công dụng (UDP) – " + Ui.STUDENT_ID + " – " + Ui.STUDENT_NAME);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(Ui.WINDOW_SIZE);
        setLocationRelativeTo(null);
        medicinePanel = new DeletePanel(MEDICINE,
                new String[] {"Mã thuốc", "Tên thuốc", "Giá (VNĐ)", "Công dụng", "Dạng"},
                (id, name) -> delete(Protocol.DELETE_MEDICINE, MEDICINE, id), this::reload);
        usagePanel = new DeletePanel(USAGE,
                new String[] {"Mã công dụng", "Tên công dụng", "Mô tả", "Số thuốc đang dùng"},
                (id, name) -> delete(Protocol.DELETE_USAGE, USAGE, id), this::reload);
        tabs.setFont(Ui.BODY_BOLD);
        tabs.addTab("1. Xóa thuốc", medicinePanel);
        tabs.addTab("2. Xóa công dụng", usagePanel);
        tabs.addTab("3. Kết quả xóa", resultPanel);
        status.setFont(Ui.BODY);
        status.setBorder(Ui.padding(Ui.GAP / 2));
        add(header(), BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        reload();
    }

    private JPanel header() {
        JLabel title = label("HỆ THỐNG QUẢN LÝ THUỐC THEO CÔNG DỤNG", Ui.TITLE, Ui.PRIMARY);
        JLabel subtitle = label("Ứng dụng Client – Server qua giao thức UDP  •  Server " + SERVER, Ui.BODY, Ui.MUTED);
        JLabel name = label("Sinh viên thực hiện: " + Ui.STUDENT_NAME, Ui.BODY_BOLD, Ui.PRIMARY);
        JLabel id = label("MSSV: " + Ui.STUDENT_ID, Ui.BODY_BOLD, Ui.PRIMARY);
        name.setHorizontalAlignment(SwingConstants.RIGHT);
        id.setHorizontalAlignment(SwingConstants.RIGHT);

        JPanel titles = new JPanel(new GridLayout(2, 1));
        titles.setOpaque(false);
        titles.add(title);
        titles.add(subtitle);
        JPanel student = new JPanel(new GridLayout(2, 1));
        student.setOpaque(false);
        student.add(name);
        student.add(id);

        JPanel header = new JPanel(new BorderLayout(Ui.GAP, 0));
        header.setBackground(Ui.HEADER_BG);
        header.setBorder(Ui.padding(Ui.PADDING));
        header.add(titles, BorderLayout.CENTER);
        header.add(student, BorderLayout.EAST);
        return header;
    }

    private static JLabel label(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(color);
        return label;
    }

    private void reload() {
        setStatus("Đang tải danh sách từ Server " + SERVER + "…", Ui.MUTED);
        runInBackground(() -> new Catalog(client.listUsages(), client.listMedicines()), this::showCatalog,
                this::showReloadError);
    }

    private void showCatalog(Catalog catalog) {
        Map<String, Usage> usageById = catalog.usages().stream()
                .collect(Collectors.toMap(Usage::id, Function.identity()));
        Map<String, Long> medicineCount = catalog.medicines().stream()
                .collect(Collectors.groupingBy(Medicine::usageId, Collectors.counting()));
        medicinePanel.setRows(catalog.medicines().stream()
                .map(m -> new Object[] {m.id(), m.name(), Ui.MONEY.format(m.price()), usageLabel(m, usageById), m.form()})
                .toList());
        usagePanel.setRows(catalog.usages().stream()
                .map(u -> new Object[] {u.id(), u.name(), u.description(), medicineCount.getOrDefault(u.id(), 0L)})
                .toList());
        setStatus("Đã nhận từ Server " + SERVER + ": " + catalog.medicines().size() + " thuốc, "
                + catalog.usages().size() + " công dụng", Ui.SUCCESS);
    }

    private static String usageLabel(Medicine medicine, Map<String, Usage> usageById) {
        Usage usage = usageById.get(medicine.usageId());
        return usage == null ? medicine.usageId() : medicine.usageId() + " – " + usage.name();
    }

    // Lỗi tải danh sách không xóa dữ liệu đang hiển thị; người dùng chạy Server rồi chọn Thử lại.
    private void showReloadError(Exception error) {
        setStatus("Không kết nối được Server " + SERVER + ". " + reason(error), Ui.DANGER);
        Object[] options = {"Thử lại", "Đóng"};
        int choice = JOptionPane.showOptionDialog(this,
                "Không tải được danh sách từ Server " + SERVER + ".\n" + reason(error)
                        + "\nHãy chạy MedicineServer rồi nhấn Thử lại.",
                "Lỗi kết nối Server", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
        if (choice == 0) {
            reload();
        }
    }

    private void delete(String command, String objectName, String id) {
        setStatus("Đang gửi yêu cầu xóa " + objectName + " " + id + " tới Server " + SERVER + "…", Ui.MUTED);
        runInBackground(() -> client.send(command, id),
                response -> showDeleteResult(objectName, id, response, true),
                error -> showDeleteResult(objectName, id, Response.error("Lỗi kết nối: " + reason(error)), false));
    }

    private void showDeleteResult(String objectName, String id, Response response, boolean fromServer) {
        resultPanel.add(objectName, id, response.ok(), response.body());
        setStatus((response.ok() ? "Xóa thành công: " : "Xóa thất bại: ") + response.body(),
                response.ok() ? Ui.SUCCESS : Ui.DANGER);
        JOptionPane.showMessageDialog(this, response.body(),
                response.ok() ? "Kết quả xóa: THÀNH CÔNG" : "Kết quả xóa: THẤT BẠI",
                response.ok() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
        tabs.setSelectedComponent(resultPanel);
        if (fromServer) {
            reload();
        }
    }

    private static String reason(Exception error) {
        return error instanceof SocketTimeoutException
                ? "Server không phản hồi sau " + Protocol.TIMEOUT_MS / 1000 + " giây."
                : String.valueOf(error.getMessage());
    }

    private void setStatus(String text, Color color) {
        status.setText(text);
        status.setForeground(color);
    }

    private <T> void runInBackground(Callable<T> task, Consumer<T> onSuccess, Consumer<Exception> onFailure) {
        medicinePanel.setBusy(true);
        usagePanel.setBusy(true);
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return task.call();
            }

            @Override
            protected void done() {
                medicinePanel.setBusy(false);
                usagePanel.setBusy(false);
                try {
                    onSuccess.accept(get());
                } catch (ExecutionException e) {
                    onFailure.accept(e.getCause() instanceof Exception cause ? cause : e);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Ui.localizeDialogs();
            new MedicineClient().setVisible(true);
        });
    }
}
