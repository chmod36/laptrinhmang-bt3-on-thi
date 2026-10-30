# 03 – Java tối thiểu để đọc được bài

Không học Java từ đầu. Chỉ học **đúng những thứ có trong bài**, mỗi thứ gắn với một ví dụ đời thường và một đoạn code thật.

Cách đọc: xem ví dụ đời thường trước, rồi nhìn code, rồi đọc phần "đọc từng dòng".

---

## 1. Class và Object: cái khuôn và cái bánh

**Class** /klɑːs/ "clát" (nghĩa đen: loại, lớp) là **cái khuôn bánh**. **Object** /ˈɒb.dʒɪkt/ "óp-dếch" (đồ vật) là **cái bánh** đúc ra từ khuôn. Một khuôn đúc được nhiều bánh, mỗi bánh nhân khác nhau.

Trong bài: `Medicine` là khuôn "thuốc". Paracetamol và Vitamin C là hai cái bánh đúc từ khuôn đó.

Mỗi file `.java` trong bài chứa đúng một class cùng tên với file.

## 2. Record: tờ phiếu in sẵn ô

**Record** /ˈrek.ɔːd/ "ré-cợt" (bản ghi) là loại class **chỉ để chứa dữ liệu**. Giống tờ phiếu khám bệnh in sẵn các ô: họ tên, tuổi, địa chỉ. Điền vào là xong, không có việc gì phức tạp.

`bt3/common/Medicine.java:4`
```java
public record Medicine(String id, String name, long price, String usageId, String form) {
```
Đọc từng phần:
- `public` = công khai, file khác dùng được.
- `record Medicine` = phiếu tên là Medicine (thuốc).
- Trong ngoặc là **5 ô** của phiếu:
  - `String id` = ô mã thuốc, kiểu chữ (`String` "xờ-trinh" = chuỗi ký tự).
  - `String name` = ô tên thuốc.
  - `long price` = ô giá, kiểu số nguyên lớn (`long` = dài).
  - `String usageId` = ô mã công dụng (`usage` = công dụng, `Id` = mã).
  - `String form` = ô dạng bào chế (`form` = hình dạng).

Điền một phiếu (đúc một object):
```java
new Medicine("T001", "Paracetamol 500mg", 15000, "CD02", "Viên nén")
```
`new` = tạo mới. Đọc lại một ô: `thuoc.name()` cho ra `"Paracetamol 500mg"`.

Vì sao dùng record: Java **tự viết sẵn** các hàm đọc ô, so sánh, in ra. Nhờ vậy cả file chỉ có 13 dòng.

## 3. Biến và kiểu dữ liệu: cái hộp có dán nhãn

**Biến** là một cái hộp có tên, chứa một giá trị. Kiểu dữ liệu là **loại hộp**: hộp đựng chữ không đựng số được.

| Kiểu | Đựng gì | Ví dụ trong bài |
|---|---|---|
| `String` | chữ | `"T001"`, `"Đã xóa thuốc…"` |
| `int` | số nguyên | `9876` (cổng), `5000` (thời gian chờ) |
| `long` | số nguyên lớn | `15000` (giá) |
| `boolean` | đúng/sai (`true`/`false`) | `ok` trong `Response` (thành công hay không) |
| `String[]` | **mảng**: một dãy hộp chữ đánh số 0, 1, 2… | `parts` trong `handle` |

## 4. Hằng số: bảng nội quy không được sửa

`bt3/common/Protocol.java:8-15`
```java
public static final String HOST = "127.0.0.1";
public static final int PORT = 9876;
public static final int TIMEOUT_MS = 5000;
public static final int BUFFER_SIZE = 8192;

public static final String LIST_MEDICINES = "LIST_MED";
public static final String LIST_USAGES = "LIST_USE";
public static final String DELETE_MEDICINE = "DEL_MED";
```
- `static` = thuộc về **bảng nội quy chung**, không cần đúc object mới dùng được. Gọi thẳng `Protocol.PORT`.
- `final` = **không được sửa** khi chương trình đang chạy.
- Viết HOA toàn bộ là quy ước đặt tên cho hằng số.
- `TIMEOUT_MS = 5000`: MS là mili giây, 5000 ms = 5 giây.

Vì sao gom vào một file: muốn đổi cổng thì sửa **một chỗ**, cả Client lẫn Server đều đổi theo.

## 5. Method: một việc mà đồ vật làm được

**Method** /ˈmeθ.əd/ "mé-thợt" (tiếng Việt hay gọi là **hàm**) là một **công thức làm việc**: nhận nguyên liệu vào, làm, trả thành phẩm ra. Giống máy xay sinh tố: cho trái cây vào, bấm nút, ra ly sinh tố.

`bt3/server/MedicineServer.java:57-66`
```java
private Response deleteMedicine(String id) {
    if (id.isEmpty()) {
        return Response.error("Chưa có mã thuốc cần xóa");
    }
    Medicine removed = medicines.remove(id);
    if (removed == null) {
        return Response.error("Không tìm thấy thuốc mã " + id + " (có thể đã bị xóa trước đó)");
    }
    return Response.success("Đã xóa thuốc " + removed.name() + " (" + id + ")");
}
```
Đọc từng dòng:
- Dòng 1: hàm tên `deleteMedicine` (delete = xóa, medicine = thuốc). Nguyên liệu vào là `String id` (một mã). Thành phẩm ra là một `Response` (câu trả lời). `private` = chỉ dùng trong file này.
- Dòng 2: **nếu** (`if`) mã rỗng (`isEmpty` = là trống)…
- Dòng 3: …thì **trả về** (`return`) câu trả lời lỗi "Chưa có mã thuốc cần xóa". `return` trả kết quả và **kết thúc hàm ngay**, giống thu ngân nói "hết hàng" thì khách đi luôn, không làm tiếp.
- Dòng 5: xóa mã đó khỏi sổ thuốc, giữ lại thuốc vừa xóa trong hộp `removed` (= đã gỡ ra).
- Dòng 6: nếu `removed` là `null` (rỗng, không có gì), nghĩa là sổ không có mã này…
- Dòng 7: …trả lỗi "Không tìm thấy". Dấu `+` dùng để **nối chữ**.
- Dòng 9: tới được đây là xóa thành công, trả "Đã xóa thuốc Paracetamol 500mg (T001)".

**Mẹo đọc mọi hàm trong bài:** tìm các dòng `return`. Mỗi `return` là một kết cục có thể xảy ra. Hàm trên có 3 kết cục: mã rỗng, không tìm thấy, thành công.

## 6. List và Map: hàng người xếp hàng và cuốn danh bạ

**List** /lɪst/ "lít" (danh sách) giống **hàng người xếp hàng**: có thứ tự, người thứ nhất, thứ hai…

**Map** /mæp/ "máp" (nghĩa đen: bản đồ) giống **cuốn danh bạ điện thoại**: tra **tên** ra **số**. Trong bài: tra **mã thuốc** ra **thông tin thuốc**.

`bt3/server/MedicineServer.java:25-26`
```java
private final Map<String, Medicine> medicines = new LinkedHashMap<>();
private final Map<String, Usage> usages = new LinkedHashMap<>();
```
- `Map<String, Medicine>` = danh bạ tra mã (`String`) ra thuốc (`Medicine`). Phần trong `< >` nói danh bạ này chứa loại gì.
- `medicines` = sổ thuốc, `usages` = sổ công dụng.
- `LinkedHashMap` = loại danh bạ **nhớ thứ tự ghi vào**. Giống cuốn sổ ghi theo ngày, mở ra vẫn đúng thứ tự. Nhờ vậy bảng trên màn hình luôn là T001, T002, T003…
- `new LinkedHashMap<>()` = mua một cuốn danh bạ mới, còn trống.

Các việc với danh bạ:

| Viết | Nghĩa | Đời thường |
|---|---|---|
| `medicines.put("T001", thuoc)` | thêm | ghi tên mới vào danh bạ |
| `medicines.get("T001")` | tra | tra tên ra số; không có thì được `null` |
| `medicines.remove("T001")` | xóa | gạch tên khỏi danh bạ, trả lại dòng vừa gạch |
| `medicines.containsKey("T001")` | có chưa? | danh bạ có tên này không |
| `medicines.values()` | lấy tất cả | đọc hết mọi dòng |

Vì sao dùng Map mà không dùng List: tra theo mã **nhanh và gọn**. Dùng List thì phải đi từ đầu hàng, hỏi từng người "có phải T001 không?".

## 7. if / else và toán tử ba ngôi

`if (điều kiện) { … }` = **nếu** đúng thì làm.

Có một cách viết ngắn hay gặp trong bài:
```java
String id = parts.length > 1 ? parts[1].trim().toUpperCase(Locale.ROOT) : "";
```
Mẫu: `điều_kiện ? A : B` = **nếu đúng lấy A, sai lấy B**. Đọc dòng trên là: "nếu mảng `parts` có hơn 1 ô thì lấy ô thứ hai, bỏ khoảng trắng hai đầu (`trim` = cắt tỉa), đổi thành chữ HOA (`toUpperCase`); không thì lấy chữ rỗng".

Vì có đổi chữ hoa nên gửi `t001` vẫn xóa được `T001`.

## 8. switch: tổng đài "bấm phím 1, bấm phím 2"

Gọi tổng đài ngân hàng: "Bấm 1 để tra số dư, bấm 2 để khóa thẻ, bấm phím khác để gặp nhân viên". `switch` làm đúng như vậy.

`bt3/server/MedicineServer.java:48-54`
```java
return switch (parts[0]) {
    case Protocol.LIST_MEDICINES -> Response.success(rows(medicines.values(), Medicine::toRow));
    case Protocol.LIST_USAGES -> Response.success(rows(usages.values(), Usage::toRow));
    case Protocol.DELETE_MEDICINE -> deleteMedicine(id);
    case Protocol.DELETE_USAGE -> deleteUsage(id);
    default -> Response.error("Lệnh không hợp lệ: " + parts[0]);
};
```
- `switch (parts[0])` = xét tên lệnh (ô đầu tiên của gói tin).
- `case X ->` = nếu là X thì làm việc sau dấu mũi tên.
- `default` = không khớp phím nào → "lệnh không hợp lệ". Nhờ có nhánh này, gửi lệnh bậy Server cũng không sập.

**Thêm chức năng mới cho Server = thêm một dòng `case` ở đây.** Đây là chỗ hay bị bắt sửa nhất (xem file 05).

## 9. Lambda: tờ giấy dặn việc

**Lambda** /ˈlæm.də/ "lam-đa" (chữ cái Hy Lạp λ) là **một việc viết gọn, không cần đặt tên**, để đưa cho người khác làm sau. Giống để lại tờ giấy dặn "khi khách tới thì đưa họ gói hàng này".

Dạng: `đầu_vào -> việc_làm`.

`bt3/client/MedicineClient.java:49-51`
```java
medicinePanel = new DeletePanel(MEDICINE,
        new String[] {"Mã thuốc", "Tên thuốc", "Giá (VNĐ)", "Công dụng", "Dạng"},
        (id, name) -> delete(Protocol.DELETE_MEDICINE, MEDICINE, id), this::reload);
```
- Tạo màn tab 1 và đưa cho nó **tờ giấy dặn**: "khi người dùng xác nhận xóa dòng có mã `id`, tên `name`, thì gọi `delete` với lệnh `DELETE_MEDICINE`".
- `this::reload` cũng là một tờ giấy dặn, viết gọn hơn nữa: "khi bấm Tải lại thì gọi hàm `reload` của tôi".

Màn tab 2 nhận tờ giấy khác: gửi lệnh `DELETE_USAGE`. **Cùng một khuôn màn hình, khác tờ giấy dặn**, nên hai tab dùng chung `DeletePanel`.

## 10. Stream: dây chuyền nhà máy

`stream` (dòng chảy) giống **băng chuyền**: hàng đi qua từng trạm, mỗi trạm làm một việc.

`bt3/server/MedicineServer.java:77-80`
```java
List<String> users = medicines.values().stream()
        .filter(medicine -> medicine.usageId().equals(id))
        .map(Medicine::id)
        .toList();
```
- `medicines.values()` = đổ hết thuốc trong sổ ra.
- `.stream()` = đặt lên băng chuyền.
- `.filter(...)` = trạm **lọc**: chỉ giữ thuốc có mã công dụng bằng `id` (`equals` = bằng nhau). Giống trạm loại trái cây hỏng.
- `.map(Medicine::id)` = trạm **biến đổi**: mỗi thuốc chỉ giữ lại cái mã. Giống trạm gọt vỏ.
- `.toList()` = cuối băng chuyền, gom vào thùng thành danh sách.

Kết quả với `id = "CD02"`: `[T001, T005]`, là các thuốc đang dùng Hạ sốt.

## 11. try / catch: còi báo cháy và người cứu hỏa

**Exception** /ɪkˈsep.ʃən/ "ích-xép-sần" (ngoại lệ) là **tiếng còi báo có sự cố**. Ví dụ chờ 5 giây không thấy gói tin trả lời, Java hú còi `SocketTimeoutException`.

- `try { … }` = "thử làm việc này".
- `catch (Loại_lỗi e) { … }` = "nếu còi kêu đúng loại này thì xử lý thế này". Không bắt thì chương trình sập.

Trong bài còn có `try (DatagramSocket socket = new DatagramSocket()) { … }` (`UdpClient.java:19`). Dạng có ngoặc tròn sau `try` nghĩa là **mượn đồ, dùng xong tự trả**. Giống mượn ô ở cửa hàng: ra khỏi cửa là ô tự quay về giá, không lo quên.

## 12. Swing: bộ đồ chơi lắp ráp giao diện

**Swing** /swɪŋ/ "xuynh" là thư viện có sẵn của Java để vẽ cửa sổ. Giống bộ Lego: có sẵn miếng cửa sổ, miếng nút, miếng bảng, lắp lại với nhau.

| Miếng Lego | Trên màn hình | Trong bài |
|---|---|---|
| `JFrame` "giây-frêm" | cả cửa sổ | `MedicineClient extends JFrame`, nghĩa là MedicineClient **là một** cửa sổ (`extends` = mở rộng từ) |
| `JPanel` | một tấm bảng để gắn miếng khác lên | `DeletePanel`, `ResultLogPanel` |
| `JTabbedPane` | các tab | `MedicineClient.java:56-58` |
| `JTable` + `DefaultTableModel` | bảng; model là **dữ liệu đằng sau** bảng | `DeletePanel.java` |
| `JButton` | nút bấm | "Xóa thuốc đã chọn", "Tải lại danh sách" |
| `addActionListener(e -> …)` | **gắn việc cần làm khi bấm nút** | `DeletePanel.java` |
| `JLabel` | một dòng chữ | tiêu đề, MSSV, dòng trạng thái |
| `JOptionPane` | hộp thoại bật lên | xác nhận xóa, báo kết quả |
| `BorderLayout` | cách xếp: Bắc, Nam, Đông, Tây, Giữa | đầu trang ở `NORTH`, tab ở `CENTER`, trạng thái ở `SOUTH` |

## 13. Thread và SwingWorker: thu ngân không bỏ quầy

**Thread** /θred/ "thred" (sợi chỉ) là **một dòng công việc**. Một chương trình có thể có nhiều thread chạy song song, giống nhiều nhân viên cùng làm.

Cửa sổ Swing có **đúng một nhân viên lo giao diện**: vẽ màn hình và nhận click. Ví như **cô thu ngân duy nhất** của cửa hàng. Nếu bắt cô ấy tự chạy ra bưu điện đứng chờ thư 5 giây, cả cửa hàng đứng hình: khách bấm gì cũng không ai phản hồi, cửa sổ "đơ" (Not Responding).

**SwingWorker** = thuê **một người chạy việc**. Thu ngân đưa việc "gửi thư và chờ trả lời" cho người này, rồi tiếp tục đứng quầy. Người chạy việc về thì báo lại, thu ngân cập nhật bảng.

`bt3/client/MedicineClient.java:172-180`
```java
new SwingWorker<T, Void>() {
    @Override
    protected T doInBackground() throws Exception {
        return task.call();
    }

    @Override
    protected void done() {
        medicinePanel.setBusy(false);
```
- `doInBackground` (làm ở phía sau) = việc của người chạy việc: gửi gói UDP và chờ.
- `done` (xong) = lúc người chạy việc quay về. Phần này chạy trên thread giao diện, nên được phép sửa bảng và hiện hộp thoại.
- `setBusy(false)` = mở khóa lại các nút. Trong lúc chờ, nút bị khóa để không bấm xóa hai lần.
- `@Override` = "viết đè lên công thức có sẵn". Chưa cần quan tâm.

## 14. synchronized: phòng thay đồ một người

`bt3/server/MedicineServer.java:45`
```java
public synchronized Response handle(String request) {
```
**synchronized** /ˈsɪŋ.krə.naɪzd/ "xinh-crơ-naizd" (được đồng bộ) giống **phòng thay đồ chỉ một người vào một lúc**, người sau đứng chờ. Nó đảm bảo hai yêu cầu không cùng lúc sửa cuốn sổ.

Server hiện tại xử lý từng gói một nên chưa thật sự cần. Đây là **phòng xa**: sau này cho Server xử lý nhiều yêu cầu song song thì sổ vẫn không bị hỏng.

## 15. Những thứ chưa cần quan tâm

Nhìn thấy thì bỏ qua. Nếu bị hỏi thì một câu là đủ:

| Thứ | Một câu trả lời |
|---|---|
| `import …` đầu file | Khai báo các công cụ có sẵn của Java mà file này dùng |
| `package bt3.client;` | Cho biết file nằm ở thư mục nào |
| `@Override` | Viết lại một hàm có sẵn của lớp cha |
| `<T>` | Kiểu dữ liệu tổng quát, dùng được cho nhiều loại kết quả |
| `Ui.java` | Bảng màu, cỡ chữ, khoảng cách của giao diện |
| `ResultRenderer` trong `ResultLogPanel` | Tô màu xanh/đỏ và tự xuống dòng cho bảng kết quả |
| `pom.xml` | File cấu hình để NetBeans/Maven mở và chạy project |

## Tự kiểm tra

1. `Map` giống vật gì ngoài đời? `medicines.remove("T001")` trả về gì nếu không có T001?
2. Nếu không dùng SwingWorker mà gửi UDP thẳng khi bấm nút, chuyện gì xảy ra lúc Server tắt?
3. Vì sao tab 1 và tab 2 dùng chung được `DeletePanel`?

**Bài tập:** mở `bt3/server/MedicineServer.java`, tìm hàm `deleteUsage` (dòng 69). Đếm có mấy dòng `return` và nói mỗi `return` là kết cục gì.

<details><summary>Đáp án</summary>

1. Cuốn danh bạ, tra mã ra thuốc. Không có thì trả `null`.
2. Cửa sổ đơ hoàn toàn trong 5 giây, bấm gì cũng không được, vì thread giao diện đang đứng chờ.
3. Hai màn giống hệt nhau, chỉ khác tên cột và lệnh gửi. Phần khác nhau được truyền vào bằng lambda.

Bài tập: 4 `return`: mã rỗng, không tìm thấy công dụng, còn thuốc đang dùng, xóa thành công.

</details>
