package bt3.ui;

import bt3.history.DeleteLog;
import bt3.history.HistoryPanel;
import bt3.medicine.Medicine;
import bt3.medicine.MedicinePanel;
import bt3.network.Protocol;
import bt3.network.Protocol.Command;
import bt3.network.Response;
import bt3.network.ServerApi;
import bt3.trash.BatchItem;
import bt3.trash.TrashPanel;
import bt3.usage.Usage;
import bt3.usage.UsagePanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

public class MedicineClient extends JFrame {
    private static final int TRASH_TAB = 2;

    public final ServerApi api = new ServerApi();
    private final JTabbedPane tabs = new JTabbedPane();
    private final JLabel status = Ui.label(" ", Ui.BODY, Ui.MUTED);
    private final MedicinePanel medicines;
    private final UsagePanel usages;
    private final TrashPanel trash;
    private final HistoryPanel history;
    private final List<TablePanel> panels;
    private List<Usage> usageList = List.of();
    private int running;

    private record Snapshot(List<Usage> usages, List<Medicine> medicines, List<Medicine> trash,
            List<DeleteLog> logs) {
    }

    public MedicineClient() {
        super("Quản lý thuốc theo công dụng (UDP) – " + Ui.STUDENT_ID + " – " + Ui.STUDENT_NAME);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(Ui.WINDOW_SIZE);
        setLocationRelativeTo(null);
        medicines = new MedicinePanel(this);
        usages = new UsagePanel(this);
        trash = new TrashPanel(this);
        history = new HistoryPanel(this);
        panels = List.of(medicines, usages, trash, history);
        tabs.setFont(Ui.BODY_BOLD);
        tabs.addTab("1. Thuốc", medicines);
        tabs.addTab("2. Công dụng", usages);
        tabs.addTab("3. Thùng rác", trash);
        tabs.addTab("4. Lịch sử xóa", history);
        status.setBorder(Ui.padding(Ui.GAP / 2));
        add(header(), BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        refreshAll();
    }

    private JPanel header() {
        JPanel titles = new JPanel(new GridLayout(2, 1));
        titles.setOpaque(false);
        titles.add(Ui.label("HỆ THỐNG QUẢN LÝ THUỐC THEO CÔNG DỤNG", Ui.TITLE, Ui.PRIMARY));
        titles.add(Ui.label("Học phần " + Ui.COURSE + "  •  Client – Server UDP  •  Server " + api.address()
                + "  •  CSDL MySQL", Ui.BODY, Ui.MUTED));
        JPanel student = new JPanel(new GridLayout(2, 1));
        student.setOpaque(false);
        JLabel name = Ui.label("Sinh viên thực hiện: " + Ui.STUDENT_NAME, Ui.BODY_BOLD, Ui.PRIMARY);
        JLabel id = Ui.label("MSSV: " + Ui.STUDENT_ID + "  –  Lớp: " + Ui.STUDENT_CLASS, Ui.BODY_BOLD, Ui.PRIMARY);
        name.setHorizontalAlignment(SwingConstants.RIGHT);
        id.setHorizontalAlignment(SwingConstants.RIGHT);
        student.add(name);
        student.add(id);
        JPanel header = new JPanel(new BorderLayout(Ui.GAP, 0));
        header.setBackground(Ui.HEADER_BG);
        header.setBorder(Ui.padding(Ui.PADDING));
        header.add(titles, BorderLayout.CENTER);
        header.add(student, BorderLayout.EAST);
        return header;
    }

    public List<Usage> usages() {
        return usageList;
    }

    public void refreshAll() {
        call("Tải dữ liệu từ Server", () -> new Snapshot(api.usages(),
                api.medicines(medicines.keyword(), medicines.usageFilter()), api.trash(), api.logs()), this::show);
    }

    public void searchMedicines() {
        call("Tìm thuốc", () -> api.medicines(medicines.keyword(), medicines.usageFilter()), medicines::show);
    }

    private void show(Snapshot snapshot) {
        usageList = snapshot.usages();
        medicines.showUsages(usageList);
        medicines.show(snapshot.medicines());
        usages.show(usageList);
        trash.show(snapshot.trash());
        history.show(snapshot.logs());
        tabs.setTitleAt(TRASH_TAB, "3. Thùng rác (" + snapshot.trash().size() + ")");
        setStatus("Đã nhận từ Server " + api.address() + ": " + snapshot.medicines().size() + " thuốc, "
                + usageList.size() + " công dụng, " + snapshot.trash().size() + " thuốc trong thùng rác", Ui.SUCCESS);
    }

    public void send(String doing, Command command, Consumer<Response> onDone, String... args) {
        call(doing, () -> api.send(command, args), onDone);
    }

    public <T> void call(String doing, Callable<T> task, Consumer<T> onDone) {
        setStatus(doing + "…", Ui.MUTED);
        setBusy(1);
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return task.call();
            }

            @Override
            protected void done() {
                setBusy(-1);
                try {
                    T result = get();
                    setStatus(doing + ": đã nhận phản hồi từ Server", Ui.SUCCESS);
                    onDone.accept(result);
                } catch (ExecutionException e) {
                    showFailure(doing, e.getCause(), () -> call(doing, task, onDone));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }

    private void showFailure(String doing, Throwable error, Runnable retry) {
        String reason = error instanceof SocketTimeoutException
                ? "Server không phản hồi sau " + Protocol.TIMEOUT_MS / 1000 + " giây (UDP không đảm bảo gói tin tới nơi)."
                : String.valueOf(error.getMessage());
        setStatus("Lỗi khi " + doing.toLowerCase() + ": " + reason, Ui.DANGER);
        Object[] options = {"Thử lại", "Đóng"};
        int choice = JOptionPane.showOptionDialog(this, Ui.wrap("Không thực hiện được: " + doing + ".\n" + reason
                + "\nDữ liệu đang hiển thị được giữ nguyên. Hãy kiểm tra Server và MySQL (XAMPP) rồi nhấn Thử lại."),
                "Lỗi kết nối Server", JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
        if (choice == 0) {
            retry.run();
        }
    }

    public void showMessage(String title, Response response) {
        setStatus(response.body(), response.ok() ? Ui.SUCCESS : Ui.DANGER);
        JOptionPane.showMessageDialog(this, Ui.wrap(response.body()), title + (response.ok() ? ": THÀNH CÔNG"
                : ": THẤT BẠI"), response.ok() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE);
        refreshAll();
    }

    public List<String> showBatch(String title, Response response, String undoText) {
        if (!response.body().contains(Protocol.FIELD)) {
            showMessage(title, response);
            return List.of();
        }
        List<BatchItem> items = response.rows(BatchItem::fromRow);
        List<String> done = items.stream().filter(BatchItem::ok).map(BatchItem::id).toList();
        String text = "Thành công " + done.size() + "/" + items.size() + " thuốc:\n" + Ui.preview(items.stream()
                .map(i -> (i.ok() ? "Thành công" : "Thất bại") + " – " + i.id() + (i.name().isEmpty() ? "" : " "
                        + i.name()) + ": " + i.message()).toList());
        setStatus(title + ": thành công " + done.size() + "/" + items.size(), done.isEmpty() ? Ui.DANGER : Ui.SUCCESS);
        int type = done.isEmpty() ? JOptionPane.ERROR_MESSAGE : done.size() < items.size()
                ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE;
        refreshAll();
        if (undoText == null || done.isEmpty()) {
            JOptionPane.showMessageDialog(this, Ui.wrap(text), title, type);
            return List.of();
        }
        Object[] options = {"Đóng", undoText};
        int choice = JOptionPane.showOptionDialog(this, Ui.wrap(text), title, JOptionPane.DEFAULT_OPTION, type,
                null, options, options[0]);
        return choice == 1 ? done : List.of();
    }

    public boolean confirm(String title, String message) {
        return JOptionPane.showConfirmDialog(this, Ui.wrap(message), title, JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    private void setBusy(int change) {
        running += change;
        panels.forEach(panel -> panel.setBusy(running > 0));
    }

    private void setStatus(String text, Color color) {
        status.setText(text);
        status.setForeground(color);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Ui.localizeDialogs();
            new MedicineClient().setVisible(true);
        });
    }
}
