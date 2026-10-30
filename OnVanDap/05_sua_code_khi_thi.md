# 05 – Sửa code khi thi

Thầy hay bảo "em thêm cho thầy chức năng…" hoặc "sửa chỗ này thành…". Đừng hoảng. Gần như mọi yêu cầu đều theo **một khuôn 3 bước**.

**Mọi bài dưới đây đã được chạy thử thật** trên một bản copy của bài: biên dịch được, chạy ra đúng kết quả ghi ở cuối mỗi bài. Luyện trên bản copy, đừng sửa trong thư mục nộp bài.

---

## Khuôn 3 bước: thêm một món mới vào thực đơn

Ví dụ đời thường: quán muốn bán thêm món "Trà đào".
1. **Ghi tên món vào thực đơn chung** để cả khách lẫn bếp biết món đó tên gì → `Protocol.java`.
2. **Dạy bếp cách làm món** → thêm một dòng `case` và một hàm trong `MedicineServer.java`.
3. **Cho khách gọi món** → Client gửi lệnh mới bằng `client.send(...)` trong `MedicineClient.java`.

```
① bt3/common/Protocol.java        public static final String TEN_LENH = "CHU_GUI_DI";
② bt3/server/MedicineServer.java  trong switch của handle:  case Protocol.TEN_LENH -> hamXuLy(id);
                                  và viết hàm:  private Response hamXuLy(String id) { … return …; }
③ bt3/client/MedicineClient.java  client.send(Protocol.TEN_LENH, du_lieu)
```

**Sửa xong luôn làm 3 việc:**
1. **Tắt Server cũ** (nút ■ đỏ trong NetBeans). Server cũ vẫn chạy code cũ, và còn giữ cửa 9876.
2. Chạy lại Server.
3. Chạy lại Client.

Khi thi, **nói khuôn 3 bước ra miệng trước khi gõ**. Thầy thấy bạn hiểu cấu trúc, dù gõ chậm cũng được điểm.

---

## Bài 1 (dễ, 1 dòng) – Đổi cổng, đổi thời gian chờ, đổi địa chỉ Server

`bt3/common/Protocol.java:8-10`
```java
public static final String HOST = "127.0.0.1";
public static final int PORT = 9876;
public static final int TIMEOUT_MS = 5000;
```
| Thầy bảo | Sửa |
|---|---|
| "Đổi cổng thành 5000" | `PORT = 5000` |
| "Cho Client chờ 3 giây thôi" | `TIMEOUT_MS = 3000` |
| "Chạy Client và Server trên 2 máy" | `HOST` = IP của máy chạy Server, ví dụ `"192.168.1.5"`. Hai máy cùng mạng LAN, tường lửa cho phép UDP cổng 9876. Server không cần sửa vì đã nghe trên mọi địa chỉ của máy |

**Câu thầy sẽ hỏi tiếp:** "Sao sửa một chỗ mà cả hai bên đều đổi?" → Vì Client và Server **dùng chung** file `Protocol` trong thư mục `common`.

Kiểm tra: đổi `TIMEOUT_MS = 2000`, tắt Server, bấm Tải lại danh sách → lỗi hiện sau 2 giây.

---

## Bài 2 (dễ) – Sửa giao diện

| Thầy bảo | Sửa ở đâu |
|---|---|
| Đổi tên tab | `MedicineClient.java:56-58`, chữ trong `addTab("…")` |
| Đổi tiêu đề lớn | `MedicineClient.java`, hàm `header()`, dòng `label("HỆ THỐNG QUẢN LÝ THUỐC…")` |
| Đổi họ tên / MSSV hiện trên cửa sổ | `Ui.java`, `STUDENT_NAME`, `STUDENT_ID` |
| Đổi màu chữ tiêu đề | `Ui.java`, `PRIMARY = new Color(0x1F3864)`. Số sau `0x` là mã màu như trong Paint/Photoshop |
| Chữ to hơn | `Ui.java`, `BODY = new Font(…, 14)`, đổi 14 thành 16 |
| Cửa sổ to hơn | `Ui.java`, `WINDOW_SIZE = new Dimension(1120, 700)` (rộng, cao) |
| Đổi chữ nút "Có, xóa" | `Ui.java`, `localizeDialogs()` |
| Bỏ hộp xác nhận | `DeletePanel.java`, hàm `confirmDelete`: xóa phần `showConfirmDialog` và `if`, gọi thẳng `onDelete.accept(id, name);` |
| Thêm dữ liệu mẫu | `MedicineServer.java`, hàm khởi tạo (dòng 28–43), thêm một dòng `new Medicine("T006", "…", 20000, "CD05", "…"),` vào danh sách (nhớ dấu phẩy giữa các dòng) |

---

## Bài 3 (trung bình) – Tìm thuốc theo công dụng

① `Protocol.java`, thêm dưới dòng `DELETE_USAGE`:
```java
public static final String FIND_BY_USAGE = "FIND_MED";
```
② `MedicineServer.java`, trong `switch` thêm một dòng **trước** `default`:
```java
case Protocol.FIND_BY_USAGE -> Response.success(rows(
        medicines.values().stream().filter(m -> m.usageId().equals(id)).toList(), Medicine::toRow));
```
Đọc là: lấy mọi thuốc, lọc những thuốc có mã công dụng bằng `id`, rồi dùng hàm `rows` có sẵn biến danh sách thành chữ để gửi về. **Tận dụng code có sẵn**, không viết lại.

③ Client gọi `client.send(Protocol.FIND_BY_USAGE, "CD02")` rồi đọc `response.rows()`.

**Kết quả đã chạy:** gửi `FIND_MED cd05` (chữ thường vẫn được, vì `handle` đổi sang chữ hoa) → `OK T006|Loratadin 10mg|20000|CD05|Viên nén`.

---

## Bài 4 (trung bình) – Xóa công dụng kèm luôn các thuốc đang dùng nó

Ví dụ đời thường: giải thể lớp thì cho các bạn trong lớp nghỉ học luôn (xóa dây chuyền, tiếng Anh là **cascade** /kæˈskeɪd/ "ca-xkây" = thác nước đổ tầng tầng).

① `Protocol.java`:
```java
public static final String DELETE_USAGE_CASCADE = "DEL_USE_ALL";
```
② `MedicineServer.java`, thêm `case` trước `default`:
```java
case Protocol.DELETE_USAGE_CASCADE -> deleteUsageCascade(id);
```
và thêm hàm mới ngay dưới hàm `deleteUsage`:
```java
private Response deleteUsageCascade(String id) {
    Usage usage = usages.remove(id);
    if (usage == null) {
        return Response.error("Không tìm thấy công dụng mã " + id);
    }
    int before = medicines.size();
    medicines.values().removeIf(m -> m.usageId().equals(id));
    return Response.success("Đã xóa công dụng " + usage.name() + " và " + (before - medicines.size()) + " thuốc");
}
```
- `usages.remove(id)`: gạch công dụng, giữ lại cái vừa gạch. `null` là không có.
- `before`: đếm số thuốc **trước** khi xóa (`size` = kích cỡ).
- `removeIf(...)`: xóa mọi thuốc **thỏa điều kiện** (`if` = nếu).
- Số thuốc bị xóa = trước − sau.

③ `MedicineClient.java:54`, đổi `Protocol.DELETE_USAGE` thành `Protocol.DELETE_USAGE_CASCADE`. Nút "Xóa công dụng đã chọn" ở tab 2 giờ xóa dây chuyền.

**Kết quả đã chạy:** xóa CD02 (lúc đó còn T005) → `OK Đã xóa công dụng Hạ sốt và 1 thuốc`.

**Câu thầy sẽ hỏi tiếp:** "Làm vậy có rủi ro gì?" → Xóa nhầm mất cả loạt thuốc. Nên sửa luôn câu hỏi xác nhận cho rõ "sẽ xóa cả các thuốc đang dùng".

---

## Bài 5 (khó hơn) – Thêm thuốc, có nút trên giao diện

### Phía Server

① `Protocol.java`:
```java
public static final String ADD_MEDICINE = "ADD_MED";
```
② `MedicineServer.java`, thêm `case`:
```java
case Protocol.ADD_MEDICINE -> addMedicine(parts.length > 1 ? parts[1] : "");
```
**Chú ý:** dùng `parts[1]` gốc, **không** dùng `id`. `id` đã bị đổi sang chữ HOA, tên thuốc sẽ thành "LORATADIN 10MG".

Thêm hàm:
```java
private Response addMedicine(String row) {
    String[] f = row.split("\\|", -1);
    if (f.length != 5) {
        return Response.error("Dữ liệu thuốc phải có 5 trường");
    }
    Medicine medicine;
    try {
        medicine = Medicine.fromRow(f);
    } catch (NumberFormatException e) {
        return Response.error("Giá thuốc phải là số");
    }
    if (medicines.containsKey(medicine.id())) {
        return Response.error("Mã thuốc " + medicine.id() + " đã tồn tại");
    }
    if (!usages.containsKey(medicine.usageId())) {
        return Response.error("Không có công dụng mã " + medicine.usageId());
    }
    medicines.put(medicine.id(), medicine);
    return Response.success("Đã thêm thuốc " + medicine.name() + " (" + medicine.id() + ")");
}
```
Đọc từng đoạn:
- `split("\\|", -1)`: cắt chuỗi theo dấu `|`. Trong `split`, dấu `|` có nghĩa đặc biệt ("hoặc"), nên phải viết `\\|` để Java hiểu là ký tự `|` bình thường.
- Không đủ 5 ô → báo lỗi.
- `try … catch`: giá gõ chữ (ví dụ `abc`) thì `Long.parseLong` (trong `fromRow`) hú còi `NumberFormatException`. Bắt còi và trả lời lịch sự, thay vì để Server sập.
- `containsKey` (= có chứa mã): trùng mã → lỗi.
- Công dụng phải có thật. Cùng tinh thần với quy tắc xóa: không để thuốc trỏ tới công dụng ma.
- Hợp lệ hết thì `put` vào sổ.

### Phía Client – thêm nút "Thêm thuốc" ở tab 1

③a `DeletePanel.java`: biến khung nút thành **thuộc tính** của class để thêm nút từ bên ngoài được.
- Thêm dòng này cạnh các thuộc tính `private final …` ở đầu class:
  ```java
  private final JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, Ui.GAP, 0));
  ```
- **Xóa** dòng `JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, Ui.GAP, 0));` trong hàm khởi tạo (không xóa thì Java báo trùng tên).
- Thêm hàm:
  ```java
  void addAction(String text, Runnable action) {
      JButton button = new JButton(text);
      button.setFont(Ui.BODY);
      button.addActionListener(e -> action.run());
      buttons.add(button, 0);
  }
  ```
  Đọc là: tạo nút có chữ `text`, bấm vào thì chạy `action`, gắn vào **đầu** hàng nút (vị trí 0).

③b `MedicineClient.java`:
- Trong hàm khởi tạo, thêm ngay trước dòng `tabs.setFont(Ui.BODY_BOLD);`:
  ```java
  medicinePanel.addAction("Thêm thuốc", this::addMedicine);
  ```
- Thêm hàm:
  ```java
  private void addMedicine() {
      String row = JOptionPane.showInputDialog(this, "Nhập theo mẫu: mã|tên|giá|mã công dụng|dạng",
              "T006|Loratadin 10mg|20000|CD05|Viên nén");
      if (row == null || row.isBlank()) {
          return;
      }
      runInBackground(() -> client.send(Protocol.ADD_MEDICINE, row.trim()), response -> {
          JOptionPane.showMessageDialog(this, response.body());
          reload();
      }, error -> JOptionPane.showMessageDialog(this, "Lỗi kết nối: " + reason(error)));
  }
  ```
  - `showInputDialog` = hộp cho gõ chữ, có sẵn dòng mẫu.
  - Bấm Hủy thì được `null`. Gõ trống thì `isBlank`. Cả hai trường hợp thì thôi, không gửi.
  - Gửi đi bằng người chạy việc (`runInBackground`), giống hệt cách hàm `delete` làm. Có trả lời thì hiện thông báo và tải lại bảng.

**Lưu ý khi demo:** hộp nhập hiện 2 nút **"Đóng"** và **"Hủy"**. "Đóng" chính là nút OK, vì `Ui.localizeDialogs()` đổi chữ nút OK thành "Đóng" cho các hộp thông báo. Muốn chữ hợp hơn thì sửa `OptionPane.okButtonText` trong `Ui.java` thành "Đồng ý".

**Kết quả đã chạy:**
- Bấm "Thêm thuốc" → bấm "Đóng" (OK) → hộp "Đã thêm thuốc Loratadin 10mg (T006)". Bảng có 6 dòng, dòng T006 ghi "CD05 – Kháng histamin". Server ghi log `ADD_MED T006|Loratadin 10mg|20000|CD05|Viên nén -> OK`.
- Giá `abc` → `ERR Giá thuốc phải là số`.
- Thêm trùng T006 → `ERR Mã thuốc T006 đã tồn tại`.

---

## Bài 6 (hỏi miệng, ít khi bắt gõ) – Các hướng mở rộng

| Thầy hỏi | Trả lời |
|---|---|
| "Gói tin mất thì làm sao cho chắc?" | Bọc `send` trong vòng lặp thử lại tối đa 3 lần. Lần nào bị `SocketTimeoutException` thì gửi lại |
| "Muốn Server xử lý nhiều Client song song?" | Mỗi gói nhận được giao cho một thread (ví dụ `ExecutorService`) xử lý. Lúc đó `synchronized` ở `handle` mới thật sự cần, để hai thread không cùng sửa sổ |
| "Đổi sang TCP thì sửa gì?" | Server dùng `ServerSocket` + `accept()`, Client dùng `Socket`, đọc ghi qua luồng (stream) thay cho `DatagramPacket`. Phần `handle` giữ nguyên, vì xử lý nghiệp vụ không phụ thuộc cách gửi tin |
| "Lưu dữ liệu lâu dài?" | Ghi hai Map ra file mỗi lần sửa và đọc lại khi khởi động, hoặc dùng cơ sở dữ liệu qua JDBC |
| "Xóa nhiều dòng cùng lúc?" | Bảng cho chọn nhiều dòng (`MULTIPLE_INTERVAL_SELECTION` trong `Ui.styleTable`), gửi mã nối bằng dấu phẩy, Server cắt ra và xóa từng mã |

Câu trả lời "phần `handle` giữ nguyên" rất ăn điểm: nó cho thấy bài **tách phần gửi tin ra khỏi phần nghiệp vụ**.

---

## Đọc lỗi khi biên dịch và khi chạy

| Lỗi | Nghĩa | Sửa |
|---|---|---|
| `';' expected` | Thiếu dấu chấm phẩy cuối câu | Thêm `;` ở dòng báo lỗi, hoặc dòng ngay trên |
| `cannot find symbol … symbol: variable ADD_MEDICINE` | Gọi một tên không có | Gõ sai tên, hoặc quên bước ① thêm hằng vào `Protocol` |
| `missing return statement` | Có nhánh của hàm không trả kết quả | Thêm `return …;` cho nhánh đó |
| `reached end of file while parsing` | Thiếu dấu `}` | Đếm lại ngoặc. NetBeans tô sáng cặp ngoặc khi đặt con trỏ cạnh ngoặc |
| `variable buttons is already defined` | Khai báo trùng tên (Bài 5) | Xóa dòng khai báo cũ trong hàm khởi tạo |
| `duplicate case label` | Hai `case` cùng một lệnh | Đặt chữ lệnh mới khác các lệnh đã có |
| `BindException: Address already in use` (lúc chạy) | Server cũ chưa tắt | Bấm ■ đỏ tắt Server cũ, chạy lại |
| Client báo "Lệnh không hợp lệ: …" | Server đang chạy là **bản cũ**, chưa có `case` mới | Tắt Server, chạy lại bản mới |

NetBeans gạch đỏ ngay dòng lỗi. Rê chuột vào để đọc thông báo, rồi tra bảng trên.

## Tự kiểm tra

1. Nói khuôn 3 bước khi thêm một lệnh mới.
2. Ở Bài 5, vì sao dùng `parts[1]` mà không dùng `id`?
3. Sửa xong code, chạy Client thì nhận "Lệnh không hợp lệ". Vì sao?

**Bài tập:** copy thư mục `source` ra chỗ khác, tự làm lại Bài 3 và Bài 4 **không nhìn tài liệu**. Chạy và kiểm tra kết quả giống phần "Kết quả đã chạy".

<details><summary>Đáp án</summary>

1. Thêm tên lệnh vào `Protocol` → thêm `case` và hàm xử lý ở `MedicineServer` → Client gọi `client.send`.
2. `id` đã bị đổi sang chữ HOA, tên thuốc sẽ bị viết hoa hết.
3. Server đang chạy vẫn là bản cũ chưa có `case` mới. Tắt Server rồi chạy lại.

</details>
