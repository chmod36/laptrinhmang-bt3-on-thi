# 04 – Luồng chạy chi tiết

🎬 Xem trước: `video/02_giai_thich_chi_tiet.mp4` (3,5 phút) để nắm ý chính, rồi đọc file này để hiểu từng dòng.

"Luồng" là **thứ tự các việc xảy ra** từ lúc người dùng bấm nút tới lúc màn hình đổi. Thầy chấm vấn đáp chủ yếu xem bạn có hiểu luồng không. Bài có 5 luồng:

1. Server khởi động và chờ
2. Client mở lên và tải danh sách
3. **Xóa thuốc** (quan trọng nhất, phải vẽ lại được)
4. Xóa công dụng (giống luồng 3, thêm một quy tắc)
5. Hiển thị kết quả xóa, và khi mất kết nối

---

## Luồng 1 – Server khởi động và chờ

Ví dụ đời thường: nhân viên quầy chính mở cửa, ngồi vào phòng 9876, đặt cuốn sổ lên bàn rồi **ngồi chờ thư**. Có thư thì đọc, làm, viết thư trả lời, rồi lại ngồi chờ. Cứ thế cả ngày.

**Bước 1 – Viết sẵn dữ liệu mẫu vào sổ.** `bt3/server/MedicineServer.java:28-43`
```java
public MedicineServer() {
    List.of(
            new Usage("CD01", "Giảm đau", "Giảm các cơn đau nhẹ và vừa"),
            ...
            .forEach(usage -> usages.put(usage.id(), usage));
```
Hàm trùng tên với class (`MedicineServer()`) gọi là **hàm khởi tạo** (constructor), chạy một lần lúc tạo Server. Nó ghi 5 công dụng và 5 thuốc vào hai cuốn sổ. `forEach` = với từng cái. `put` = ghi vào sổ.

**Bước 2 – Mở cửa và chờ thư mãi mãi.** `bt3/server/MedicineServer.java:94-104`
```java
MedicineServer server = new MedicineServer();
try (DatagramSocket socket = new DatagramSocket(Protocol.PORT)) {
    System.out.println("Server UDP quản lý thuốc đang lắng nghe tại cổng " + Protocol.PORT);
    byte[] buffer = new byte[Protocol.BUFFER_SIZE];
    while (true) {
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        String request = new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
        Response response = server.handle(request);
        byte[] out = response.format().getBytes(StandardCharsets.UTF_8);
        socket.send(new DatagramPacket(out, out.length, packet.getSocketAddress()));
```
Đọc từng dòng:
1. Tạo Server, sổ đã có dữ liệu mẫu.
2. `DatagramSocket(9876)` = lắp một **hòm thư** (socket "xóc-kít" = ổ cắm) ở cửa 9876. Cửa này đã có chương trình khác giữ thì báo `BindException`.
3. In ra dòng "đang lắng nghe…" để biết Server đã sẵn sàng.
4. Chuẩn bị một **thùng chứa thư** (buffer "bắp-phơ" = vùng đệm) 8192 byte. Thư dài hơn bị cắt.
5. `while (true)` = **lặp mãi mãi**. Đây là vòng "ngồi chờ thư cả ngày".
6. Chuẩn bị một phong bì trống (`DatagramPacket`).
7. `receive` = **đứng chờ** tới khi có thư. Dòng này đứng yên cho tới khi có gói tin bay tới.
8. Thư tới dưới dạng byte, đổi lại thành chữ theo bảng mã UTF-8.
9. `handle` = **đọc lệnh và làm** (xem Luồng 3, bước 6).
10. Đổi câu trả lời thành byte.
11. Gửi trả về `packet.getSocketAddress()`, tức **địa chỉ người gửi ghi sau phong bì**. UDP không giữ đường dây nối như gọi điện, nên Server phải xem thư từ đâu tới để gửi trả đúng chỗ.

Sau đó Server in một dòng log, rồi quay lại dòng 7 chờ thư tiếp.

---

## Luồng 2 – Client mở lên và tải danh sách

**Bước 1 – Dựng cửa sổ.** `bt3/client/MedicineClient.java:44-65` (hàm khởi tạo):
- Đặt tiêu đề cửa sổ có MSSV và họ tên.
- Tạo màn tab 1 (`medicinePanel`) và tab 2 (`usagePanel`) từ cùng khuôn `DeletePanel`, mỗi màn một tờ giấy dặn việc riêng (dòng 49–54).
- Gắn 3 tab (dòng 56–58), phần đầu (`header()`) ở trên, dòng trạng thái ở dưới.
- Cuối cùng gọi `reload()` để tải danh sách ngay.

**Bước 2 – Tải danh sách.** `bt3/client/MedicineClient.java:99-103`
```java
private void reload() {
    setStatus("Đang tải danh sách từ Server " + SERVER + "…", Ui.MUTED);
    runInBackground(() -> new Catalog(client.listUsages(), client.listMedicines()), this::showCatalog,
            this::showReloadError);
}
```
- Dòng trạng thái đổi thành "Đang tải…".
- Giao cho người chạy việc (`runInBackground`) gửi **2 gói tin**: `LIST_USE` rồi `LIST_MED`. Gói kết quả vào một `Catalog` (catalog = danh mục).
- Xong thì gọi `showCatalog` (hiện danh mục). Lỗi thì gọi `showReloadError` (hiện lỗi tải).

**Bước 3 – Đổ dữ liệu vào bảng.** `bt3/client/MedicineClient.java:105-118`
- Server gửi về các dòng `T001|Paracetamol 500mg|15000|CD02|Viên nén`. `Response.rows()` cắt theo dấu xuống dòng, rồi cắt mỗi dòng theo `|`. `Medicine.fromRow` điền từng ô vào phiếu thuốc.
- Cột "Công dụng" ở tab 1 hiện "CD02 – Hạ sốt": Client tự tra tên công dụng từ danh sách công dụng vừa nhận.
- Cột "Số thuốc đang dùng" ở tab 2 do **Client tự đếm** (`groupingBy … counting` = gom nhóm theo công dụng rồi đếm). Server không gửi con số này.
- Dòng trạng thái: "Đã nhận từ Server …: 5 thuốc, 5 công dụng" màu xanh.

---

## Luồng 3 – Xóa thuốc (vẽ lại được sơ đồ này là qua 70%)

```
NGƯỜI DÙNG        CLIENT                                            SERVER
 bấm dòng  ─▶ ① updateSelection: bật nút Xóa
 bấm Xóa   ─▶ ② confirmDelete: hiện hộp "Bạn có chắc…?"
 "Có, xóa" ─▶ ③ delete: khóa nút, giao việc cho người chạy việc
              ④ UdpClient.send ── gói "DEL_MED⇥T001" ──────▶ ⑤ receive: nhận, đổi byte → chữ
                 (đứng chờ tối đa 5 giây)                      ⑥ handle: cắt lệnh, switch
                                                               ⑦ deleteMedicine: gạch T001 khỏi sổ
              ⑨ nhận gói, Response.parse ◀─ "OK⇥Đã xóa…" ──── ⑧ format, gửi về địa chỉ người gửi
              ⑩ showDeleteResult: ghi tab 3, hiện hộp kết quả
              ⑪ reload: gửi LIST_USE, LIST_MED, bảng cập nhật
```

Ví dụ đời thường cho cả luồng: bạn gọi món ở quán (①② chọn món, người phục vụ hỏi lại "chốt món này nhé?"), người phục vụ ghi phiếu mang vào bếp (③④), bếp đọc phiếu, làm món (⑤⑥⑦), đưa ra kèm lời nhắn (⑧⑨), người phục vụ báo bạn và ghi vào hóa đơn (⑩), rồi cập nhật lại thực đơn còn món gì (⑪).

### ① Bấm vào một dòng → nút Xóa sáng lên

`bt3/client/DeletePanel.java:76-80`
```java
private void updateSelection() {
    int row = table.getSelectedRow();
    deleteButton.setEnabled(row >= 0 && !busy);
    selection.setText(row < 0 ? "Chưa chọn " + objectName + " nào" : "Đang chọn: " + cell(row, 0) + " – " + cell(row, 1));
}
```
- `getSelectedRow` = dòng đang chọn. Chưa chọn thì được `-1`.
- Nút Xóa chỉ bật (`setEnabled` = cho phép) khi **đã chọn dòng** (`row >= 0`) **và** (`&&`) **không bận** (`!busy`, dấu `!` = không).
- Dòng chữ bên trái hiện "Đang chọn: T001 – Paracetamol 500mg".

Hàm này chạy mỗi khi đổi dòng chọn (gắn ở `DeletePanel.java:37`).

### ② Bấm Xóa → hộp xác nhận

`bt3/client/DeletePanel.java:82-96`
```java
int row = table.getSelectedRow();
if (row < 0) {
    return;
}
String id = cell(row, 0);
String name = cell(row, 1);
String message = "Bạn có chắc muốn xóa " + objectName + " \"" + name + "\" (mã " + id + ")?\n" + …;
int answer = JOptionPane.showConfirmDialog(this, message, "Xác nhận xóa " + objectName, …);
if (answer == JOptionPane.YES_OPTION) {
    onDelete.accept(id, name);
}
```
- Lấy mã ở cột 0 và tên ở cột 1 của dòng đang chọn.
- Hiện hộp hỏi "Bạn có chắc…?" với 2 nút "Có, xóa" và "Không". Hai chữ tiếng Việt này được đặt ở `Ui.localizeDialogs()`.
- Chỉ khi chọn **Có** mới thực hiện tờ giấy dặn việc `onDelete`. Tab 1 thì tờ giấy là "gửi lệnh `DELETE_MEDICINE`".

Vì sao phải xác nhận: xóa là việc **không quay lại được**, hỏi lại để tránh bấm nhầm.

### ③ Giao việc cho người chạy việc

`bt3/client/MedicineClient.java:138-143`
```java
private void delete(String command, String objectName, String id) {
    setStatus("Đang gửi yêu cầu xóa " + objectName + " " + id + " tới Server " + SERVER + "…", Ui.MUTED);
    runInBackground(() -> client.send(command, id),
            response -> showDeleteResult(objectName, id, response, true),
            error -> showDeleteResult(objectName, id, Response.error("Lỗi kết nối: " + reason(error)), false));
}
```
`runInBackground` nhận **3 tờ giấy dặn**:
1. Việc chính: `client.send(command, id)`, tức gửi gói tin.
2. Khi có trả lời: `showDeleteResult(…, true)`, hiện kết quả từ Server.
3. Khi lỗi (hết 5 giây…): tự tạo một câu trả lời lỗi "Lỗi kết nối: …" rồi cũng hiện ra, kèm `false` = "không phải Server trả lời".

Bên trong `runInBackground` (dòng 169–191): khóa các nút (`setBusy(true)`), dùng **SwingWorker** chạy việc chính ở thread khác, xong thì mở khóa và gọi tờ giấy 2 hoặc 3. Lý do xem file 03, mục 13: thu ngân không bỏ quầy.

### ④ Người đưa thư gửi gói UDP và chờ

`bt3/client/UdpClient.java:18-27`
```java
Response send(String command, String id) throws IOException {
    try (DatagramSocket socket = new DatagramSocket()) {
        socket.setSoTimeout(Protocol.TIMEOUT_MS);
        byte[] out = Protocol.request(command, id).getBytes(StandardCharsets.UTF_8);
        socket.send(new DatagramPacket(out, out.length, server));
        byte[] buffer = new byte[Protocol.BUFFER_SIZE];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        return Response.parse(new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8));
    }
```
| Dòng | Code | Đời thường |
|---|---|---|
| 2 | `new DatagramSocket()` | Mua một hòm thư tạm. Không ghi số cửa, máy tự chọn cửa trống (ví dụ 53211). `try (…)` = dùng xong tự trả hòm thư |
| 3 | `setSoTimeout(5000)` | Đặt **đồng hồ hẹn giờ 5 giây** cho việc chờ thư |
| 4 | `Protocol.request(…)` | Viết thư: `"DEL_MED" + TAB + "T001"`. `getBytes(UTF_8)` đổi chữ thành byte vì dây mạng chỉ chở byte |
| 5 | `socket.send(…)` | Bỏ phong bì có địa chỉ `127.0.0.1:9876` vào hòm thư |
| 6–7 | `buffer`, `packet` | Chuẩn bị phong bì trống 8192 byte để nhận thư về |
| 8 | `socket.receive(packet)` | **Đứng chờ**. Có thư thì đi tiếp. Hết 5 giây chưa có thì hú còi `SocketTimeoutException` |
| 9 | `Response.parse(…)` | Đổi byte về chữ, tách `"OK"` hoặc `"ERR"` và nội dung thành một `Response` |

`throws IOException` = "hàm này có thể hú còi lỗi mạng, ai gọi thì tự lo xử lý". Người lo xử lý là `runInBackground`, gọi tờ giấy số 3.

### ⑤–⑧ Server nhận, làm, trả lời

Xem lại vòng lặp ở Luồng 1. Phần "làm" nằm trong `handle` → `switch` → `deleteMedicine` (đã đọc từng dòng ở file 03, mục 5 và 8):
- Mã rỗng → `ERR Chưa có mã thuốc cần xóa`.
- `medicines.remove("T001")` trả `null` → `ERR Không tìm thấy thuốc mã T001 (có thể đã bị xóa trước đó)`.
- Còn lại → `OK Đã xóa thuốc Paracetamol 500mg (T001)`.

`Response.format()` (`bt3/common/Response.java:25`) viết câu trả lời thành chữ: `"OK" + TAB + nội dung`.

### ⑨–⑪ Client hiện kết quả

`bt3/client/MedicineClient.java:145-156`
```java
private void showDeleteResult(String objectName, String id, Response response, boolean fromServer) {
    resultPanel.add(objectName, id, response.ok(), response.body());
    setStatus((response.ok() ? "Xóa thành công: " : "Xóa thất bại: ") + response.body(), …);
    JOptionPane.showMessageDialog(this, response.body(),
            response.ok() ? "Kết quả xóa: THÀNH CÔNG" : "Kết quả xóa: THẤT BẠI", …);
    tabs.setSelectedComponent(resultPanel);
    if (fromServer) {
        reload();
    }
}
```
1. Ghi một dòng vào tab 3 (`resultPanel.add`).
2. Đổi dòng trạng thái: xanh nếu thành công, đỏ nếu thất bại.
3. Hiện hộp thông báo.
4. Chuyển sang tab 3.
5. Nếu là trả lời thật từ Server (`fromServer`) thì **tải lại danh sách**, để bảng khớp với sổ ở Server. Nếu là lỗi mạng thì **không tải lại**: tải lại cũng lỗi, mà dữ liệu đang hiện vẫn được giữ cho người dùng xem.

---

## Luồng 4 – Xóa công dụng

Giống y luồng 3, chỉ khác:
- Tab 2 có tờ giấy dặn gửi lệnh `DEL_USE` (`MedicineClient.java:52-54`).
- Server chạy hàm `deleteUsage` có thêm **một quy tắc**.

`bt3/server/MedicineServer.java:69-87`
```java
Usage usage = usages.get(id);
if (usage == null) {
    return Response.error("Không tìm thấy công dụng mã " + id + " (có thể đã bị xóa trước đó)");
}
List<String> users = medicines.values().stream()
        .filter(medicine -> medicine.usageId().equals(id))
        .map(Medicine::id)
        .toList();
if (!users.isEmpty()) {
    return Response.error("Không thể xóa công dụng " + … + "Hãy xóa các thuốc này trước.");
}
usages.remove(id);
return Response.success("Đã xóa công dụng " + usage.name() + " (" + id + ")");
```
Các bước:
1. **Tra** công dụng (`get`), chưa xóa vội. Không có thì báo không tìm thấy.
2. Cho cả sổ thuốc chạy qua băng chuyền, **lọc** ra mã các thuốc đang dùng công dụng này. Tên biến `users` = những người đang dùng.
3. Danh sách **không rỗng** (`!users.isEmpty()`) → từ chối, kèm luôn mã các thuốc để người dùng biết phải xóa thuốc nào.
4. Rỗng → mới gạch khỏi sổ (`remove`) và báo thành công.

**Vì sao tra trước rồi mới xóa** (khác với xóa thuốc dùng `remove` ngay): phải kiểm tra quy tắc trước. Lỡ xóa rồi mới phát hiện còn thuốc dùng thì không gỡ lại được.

Ví dụ đời thường: trường muốn xóa lớp. Trước hết mở danh sách lớp xem có lớp đó không, rồi xem còn sinh viên nào thuộc lớp không. Còn thì không xóa, và nói rõ "còn bạn A, bạn B".

---

## Luồng 5 – Hiển thị kết quả xóa, và khi mất kết nối

### Tab 3 ghi lại như thế nào

`bt3/client/ResultLogPanel.java:61-70`
```java
void add(String objectName, String id, boolean success, String detail) {
    if (success) {
        successCount++;
    } else {
        failureCount++;
    }
    model.addRow(new Object[] {model.getRowCount() + 1, LocalDateTime.now().format(Ui.TIME), objectName, id,
            success ? SUCCESS : FAILURE, detail});
    updateSummary();
}
```
- Cộng 1 vào bộ đếm thành công hoặc thất bại (`++` = cộng thêm 1).
- Thêm một dòng vào bảng: STT, thời gian hiện tại, đối tượng (thuốc/công dụng), mã, "Thành công"/"Thất bại", chi tiết lời nhắn của Server.
- Cập nhật dòng tổng kết: "Tổng số yêu cầu: 4 | Thành công: 3 | Thất bại: 1".

Tô màu xanh/đỏ: lớp `ResultRenderer` trong cùng file nhìn cột "Kết quả" để chọn màu nền. Ví như cô giáo chấm bài, đúng tô xanh, sai tô đỏ.

Nút **Xóa nhật ký** chỉ xóa bảng ở Client. **Không** gửi gì tới Server, không ảnh hưởng dữ liệu thuốc.

Tab 3 **không gửi lệnh riêng nào**. Nó chỉ ghi lại những gì đã nhận được ở luồng 3 và 4. Đây là cách làm chức năng "Hiển thị kết quả xóa".

### Khi Server tắt

1. Người dùng bấm xóa → Client gửi gói tin → **không ai nhận**. UDP không báo lại gì.
2. Client đứng chờ ở `socket.receive`. Đủ 5 giây thì Java hú còi `SocketTimeoutException`.
3. `runInBackground` bắt được còi và gọi tờ giấy số 3.
4. Hàm `reason` (`MedicineClient.java:158-162`) đổi còi thành câu dễ hiểu: "Server không phản hồi sau 5 giây."
5. Tab 3 thêm một dòng đỏ "Lỗi kết nối: …". Bảng **giữ nguyên** dữ liệu cũ.

Nếu là **tải danh sách** bị lỗi, `showReloadError` (`MedicineClient.java:126-136`) hiện hộp có nút **Thử lại** và **Đóng**. Bật Server lên rồi bấm Thử lại là tải lại được.

---

## Tóm tắt một hơi

Server ngồi ở cửa 9876 chờ thư mãi mãi. Client mở lên gửi `LIST_USE`, `LIST_MED` để lấy danh sách. Người dùng chọn dòng, bấm xóa, xác nhận. Client nhờ người chạy việc gửi `DEL_MED⇥mã` hoặc `DEL_USE⇥mã` rồi chờ tối đa 5 giây. Server tra sổ, kiểm tra quy tắc, xóa, gửi `OK` hoặc `ERR` về đúng địa chỉ người gửi. Client ghi vào tab 3, hiện hộp thoại và tải lại danh sách.

## Tự kiểm tra

1. Server biết gửi trả lời về đâu? Dòng code nào?
2. Xóa thuốc dùng `remove` ngay, còn xóa công dụng lại `get` trước. Vì sao?
3. Sau khi xóa lỗi do mất mạng, vì sao Client không tải lại danh sách?

**Bài tập:** vẽ sơ đồ Luồng 3 ra giấy, không nhìn. Sau đó mở đúng 4 file theo thứ tự: `DeletePanel` → `MedicineClient` → `UdpClient` → `MedicineServer`, chỉ vào từng bước ① tới ⑪.

<details><summary>Đáp án</summary>

1. Đọc địa chỉ người gửi trong chính gói vừa nhận: `packet.getSocketAddress()`, `MedicineServer.java:104`.
2. Công dụng phải kiểm tra quy tắc "còn thuốc dùng không" **trước khi** xóa. Xóa rồi mới phát hiện thì không gỡ lại được.
3. Tải lại cũng sẽ lỗi (Server vẫn tắt), và giữ nguyên dữ liệu đang hiện để người dùng vẫn xem được.

</details>
