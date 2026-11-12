# ÔN CẤP TỐC – thi tối nay (khoảng 2 tiếng)

> Các file 01–10 trong thư mục này viết cho **bản cũ** (lưu trong RAM, 3 tab). Bài nộp hiện tại đã nâng cấp: **có MySQL, 4 tab, có Thùng rác, Hoàn tác, Lịch sử xóa**. Ôn theo file này trước; file cũ chỉ dùng để hiểu thêm phần UDP (vẫn đúng).

Lộ trình: Phần 1 (15 phút) → Phần 2 chạy demo (20 phút) → Phần 3 đọc code (40 phút) → Phần 4 học thuộc câu hỏi (45 phút).

---

## Phần 1 – Hiểu từ con số 0 (15 phút)

| Từ | Nghĩa đời thường |
|---|---|
| **Chương trình** | Một cái app chạy trên máy. Bài này có 2 app. |
| **Server** | "Quầy chính" của nhà thuốc: giữ sổ sách (database), ngồi chờ người hỏi. Chạy trước, chạy suốt. |
| **Client** | "Máy của nhân viên": có giao diện (cửa sổ, bảng, nút). Bấm nút thì gửi câu hỏi tới Server. |
| **Gói tin (packet)** | Một lá thư gửi qua mạng. |
| **IP** | Số nhà của máy. `127.0.0.1` = "chính máy này". |
| **Port (cổng)** | Số phòng trong nhà. Server ở phòng **9876**. |
| **Giao thức** | Luật hai bên nói chuyện. |
| **TCP** | Gọi điện thoại: nối máy trước, nói gì chắc chắn tới, đúng thứ tự. |
| **UDP** | Bỏ thư/bưu thiếp: không nối máy, gửi là đi, **có thể mất mà không ai báo**. Nhanh, nhẹ. **Bài này dùng UDP.** |
| **Timeout** | Hẹn giờ chờ thư trả lời. Bài này chờ **5 giây**, quá thì báo lỗi. |
| **MySQL / database** | Cuốn sổ lưu dữ liệu lâu dài. Tắt máy vẫn còn. |
| **Bảng (table)** | Một trang trong sổ: `cong_dung`, `thuoc`, `nhat_ky_xoa`. |
| **Khóa ngoại (FK)** | Thuốc phải trỏ tới 1 công dụng có thật. Công dụng còn thuốc thì DB không cho xóa. |
| **Transaction** | "Làm hết hoặc không làm gì": lỗi giữa chừng thì quay lại như cũ (rollback). |

**Câu chuyện của bài trong 1 phút (học thuộc):**
> Em làm hệ thống quản lý thuốc theo công dụng, kiểu Client – Server giao tiếp bằng **UDP**, dữ liệu lưu **MySQL**. Phần cá nhân của em là **chức năng xóa**. Client gửi một dòng lệnh dạng chữ, ví dụ `MED_DELETE T001`, Server đọc lệnh, làm việc với MySQL rồi trả `OK …` hoặc `ERR lý do`. Xóa thuốc thì **chuyển vào thùng rác** để còn khôi phục, muốn mất hẳn thì xóa vĩnh viễn trong thùng rác. Xóa công dụng mà còn thuốc đang dùng thì phải chọn: **chuyển thuốc sang công dụng khác**, hoặc **xóa luôn cả thuốc**. Mọi lần xóa, thành công hay thất bại, đều được **ghi vào lịch sử xóa**. Vì UDP có thể mất gói nên Client chờ tối đa **5 giây**, quá thì báo lỗi và có nút **Thử lại**.

---

## Phần 2 – Chạy bài trên Kubuntu (20 phút)

Bài cần 2 thứ: **Java** (để chạy code) và **MySQL** (để lưu dữ liệu). Code không cần Docker.

**Laptop ở nhà (Kubuntu, chưa có gì), làm một lần duy nhất:**
```bash
git clone -b on-cap-toc https://github.com/quzkhanh/laptrinhmang-bt3-on-thi.git
cd laptrinhmang-bt3-on-thi/BaiTapCuoiKy
./chay_bai.sh caidat
```
Lệnh `caidat` hỏi mật khẩu máy (sudo), rồi cài **Java 21** và **MariaDB** (một bản MySQL miễn phí, dùng y như MySQL), đặt tài khoản `root` mật khẩu trống đúng như code cần. Cần có mạng, mất khoảng 3–5 phút.

### Cách nhanh (1 lệnh)

Mở **Konsole**, gõ:
```bash
cd laptrinhmang-bt3-on-thi/BaiTapCuoiKy   # máy công ty: cd ~/Job_Do_An/LapTrinhMang/BaiTapCuoiKy
./chay_bai.sh
```
Script tự làm 4 việc: bật MySQL → biên dịch → mở cửa sổ Client → chạy Server ngay trong Konsole đó. (Máy có Docker thì script dùng MySQL trong Docker, không có thì dùng MariaDB đã cài.)

- Konsole hiện `Server UDP quản lý thuốc đang lắng nghe tại cổng 9876` là chạy rồi. **Đừng tắt Konsole này.**
- Muốn mở thêm 1 Client (để chứng minh nhiều Client cùng lúc): mở Konsole thứ 2, gõ `./chay_bai.sh client`.
- Tắt Server: bấm `Ctrl+C` trong Konsole đầu.
- Làm loạn dữ liệu, muốn về dữ liệu mẫu: `./chay_bai.sh reset` rồi `./chay_bai.sh`.

### Cách thủ công (thầy hỏi "chạy thế nào" thì nói cách này)

1. Bật MySQL. Trên Windows mở **XAMPP → Start MySQL**. Trên Linux là MariaDB, đã tự chạy sau khi cài. Code chỉ cần một MySQL ở `localhost:3306`, tài khoản `root`, mật khẩu trống (sửa ở `src/main/resources/db.properties`).
2. Biên dịch: `javac -encoding UTF-8 -d out …*.java` (dịch code thành dạng máy chạy được).
3. Chạy Server: `java -cp out:mysql-connector-j.jar bt3.server.MedicineServer`
4. Chạy Client ở cửa sổ khác: `java -cp out bt3.client.MedicineClient`

Trên NetBeans/IntelliJ: mở thư mục `source` (có `pom.xml`) → chuột phải `MedicineServer.java` → Run File, rồi `MedicineClient.java` → Run File. **Luôn chạy Server trước.**

### Kịch bản demo (làm thử 1 lần trước khi thi, đi thi làm y hệt)

| # | Làm gì | Thấy gì | Nói gì |
|---|---|---|---|
| 1 | Tab **1. Thuốc**: bấm T001, bấm **Xóa thuốc đã chọn** → Có | Hộp kết quả THÀNH CÔNG, có nút **Hoàn tác** | Client gửi `MED_DELETE T001`. Server không xóa hẳn mà ghi `ngay_xoa = NOW()` → vào thùng rác |
| 2 | Giữ **Ctrl** bấm T002, T003 → Xóa | Kết quả từng thuốc | Xóa nhiều: gửi `MED_DELETE T002,T003`, mỗi thuốc xử lý riêng |
| 3 | Tab **3. Thùng rác** → Khôi phục T001 | T001 quay lại tab 1 | `TRASH_RESTORE`: đặt `ngay_xoa = NULL` |
| 4 | Thùng rác → Xóa vĩnh viễn 1 thuốc | Mất hẳn | `TRASH_PURGE`: chạy `DELETE FROM thuoc` thật |
| 5 | Tab **2. Công dụng**: xóa **CD06 Tiêu hóa** | Xóa luôn (không thuốc nào dùng) | Chế độ `ONLY` |
| 6 | Xóa **CD02 Hạ sốt** | Hộp hỏi: chuyển thuốc sang công dụng khác hay xóa cả thuốc | Client hỏi Server trước bằng `USE_CHECK CD02` xem thuốc nào đang dùng |
| 7 | Chọn **chuyển sang CD01** | Thành công, thuốc T001/T005/T008 giờ là Giảm đau | Chế độ `MOVE`, chạy trong **transaction** |
| 8 | Tab **4. Lịch sử xóa** | Mọi lần xóa, có cả thất bại, có IP:port của Client | Bảng `nhat_ky_xoa` |
| 9 | **Ctrl+C tắt Server**, bấm **Tải lại** | Chờ 5 giây → báo lỗi có nút **Thử lại**. Bật lại Server → Thử lại → có dữ liệu | UDP không báo mất gói, nên phải có timeout 5 giây. Dữ liệu vẫn còn vì nằm trong MySQL |

Nhìn Konsole Server: mỗi yêu cầu là 1 dòng log, ví dụ
`[16:28:54] 127.0.0.1:49587  MED_DELETE T001  ->  OK  T001 | Paracetamol 500mg | 1 | Đã chuyển vào thùng rác…`
= giờ, địa chỉ Client (IP:port, port do máy tự chọn), lệnh, kết quả.

---

## Phần 3 – Đọc code: chỉ cần nhớ 6 chỗ (40 phút)

Code ở `B23DTCN112_DOMINHVU/source/src/main/java/bt3/`. Chia 3 gói:
- `common/` – đồ dùng chung cả 2 bên (luật giao tiếp, kiểu dữ liệu, kiểm tra đầu vào).
- `server/` – quầy chính: nhận gói tin, gọi MySQL.
- `client/` – giao diện Swing.

### ① Luật giao tiếp – `common/Protocol.java`
```java
public static final int PORT = 9876;        // Server ở phòng 9876
public static final int TIMEOUT_MS = 5000;   // Client chờ tối đa 5 giây
public static final String FIELD = "\t";     // các phần trong 1 lệnh cách nhau bằng dấu Tab
```
Lệnh là một dòng chữ: `TÊN_LỆNH <Tab> tham số 1 <Tab> tham số 2…`. Danh sách lệnh nằm trong `enum Command` (MED_DELETE, TRASH_RESTORE, USE_DELETE, LOG_LIST…).
Trả lời: `OK <Tab> nội dung` hoặc `ERR <Tab> lý do` (`common/Response.java`).

### ② Client gửi và chờ – `client/ServerApi.java:24-35` (QUAN TRỌNG NHẤT)
```java
DatagramSocket socket = new DatagramSocket();          // mở 1 "hòm thư" UDP, port tự chọn
socket.setSoTimeout(Protocol.TIMEOUT_MS);              // chờ thư trả lời tối đa 5 giây
socket.send(new DatagramPacket(out, out.length, server)); // gửi lá thư tới 127.0.0.1:9876
socket.receive(packet);                                // đứng chờ thư trả lời
```
Quá 5 giây không có thư → Java ném `SocketTimeoutException` → giao diện báo lỗi + nút Thử lại.

### ③ Server nhận và trả – `server/MedicineServer.java:25-38`
```java
DatagramSocket socket = new DatagramSocket(Protocol.PORT); // giữ phòng 9876
socket.receive(packet);                                    // chờ thư tới (vòng lặp while(true))
Response response = handler.handle(request, client);       // đọc lệnh, làm việc
socket.send(new DatagramPacket(out, out.length, packet.getSocketAddress())); // trả lời đúng địa chỉ người gửi
```
`packet.getSocketAddress()` = địa chỉ người gửi ghi trên phong bì, nhờ đó Server biết trả về đâu.

### ④ Chia lệnh – `server/RequestHandler.java:43-60`
Một `switch` lớn: lệnh `MED_DELETE` → `deletes.trash(...)`, `USE_DELETE` → `deletes.deleteUsage(...)`… Lệnh lạ → `ERR Lệnh không hợp lệ`.
Hàm `handle` có chữ `synchronized` = mỗi lúc chỉ xử lý 1 yêu cầu, tránh 2 Client sửa cùng lúc.

### ⑤ Logic xóa – `server/DeleteService.java`
- Xóa thuốc = `UPDATE thuoc SET ngay_xoa = NOW()` (`server/MedicineDao.java:49`) → **xóa mềm** (soft delete), còn khôi phục được.
- Khôi phục = `SET ngay_xoa = NULL`. Xóa vĩnh viễn = `DELETE FROM thuoc` (chỉ cho phép khi đã ở thùng rác).
- Xóa công dụng có 3 chế độ (`DeleteService.java:91-100`):
  - `ONLY`: còn thuốc dùng thì **từ chối** ("đang được 3 thuốc sử dụng").
  - `MOVE`: `UPDATE thuoc SET ma_cd = <công dụng mới>` rồi xóa công dụng.
  - `CASCADE`: xóa vĩnh viễn hết thuốc của công dụng đó rồi xóa công dụng.
- Tất cả chạy trong `db.inTransaction(...)` (`server/Database.java:61-73`): lỗi giữa chừng thì `rollback`, không bị "chuyển được nửa số thuốc".
- Mỗi lần xóa gọi `log(...)` → ghi bảng `nhat_ky_xoa`.

### ⑥ Giao diện không bị đơ – `client/MedicineClient.java:118-135`
Gửi mạng phải chờ tới 5 giây. Nếu chờ ngay trên luồng giao diện thì cửa sổ **đơ cứng**. Nên dùng `SwingWorker`: `doInBackground()` gửi/chờ ở luồng phụ, `done()` quay về cập nhật giao diện.

---

## Phần 4 – Câu thầy hay hỏi (45 phút, học thuộc)

**1. Vì sao dùng UDP mà không dùng TCP?** Đề yêu cầu UDP. Mỗi thao tác chỉ là 1 câu hỏi – 1 câu trả lời ngắn, không cần giữ kết nối, UDP nhẹ và nhanh.

**2. UDP mất gói thì sao?** UDP không báo mất. Client đặt `setSoTimeout(5000)`: chờ 5 giây không thấy trả lời thì báo "Server không phản hồi" và cho bấm **Thử lại**.

**3. Server biết trả lời cho Client nào?** Mỗi gói tin nhận được có địa chỉ người gửi (`packet.getSocketAddress()`), Server gửi trả về đúng địa chỉ đó.

**4. Port của Client là bao nhiêu?** Không cố định. `new DatagramSocket()` không truyền số → hệ điều hành tự chọn port trống (thấy trong log, mỗi lần một số).

**5. Chạy 2 Server cùng lúc?** Lỗi `BindException: Address already in use`, vì cổng 9876 chỉ 1 chương trình giữ được.

**6. Nhiều Client cùng lúc được không?** Được. Server chỉ có 1 socket, nhận lần lượt từng gói; `handle` có `synchronized` để không xử lý chồng nhau.

**7. Gói tin UDP tối đa bao nhiêu?** 65507 byte dữ liệu (`BUFFER_SIZE`). Nếu trả về to hơn, Server trả lỗi "vượt quá kích thước một gói tin UDP" (`MedicineServer.java:45-50`).

**8. Vì sao tiếng Việt không lỗi?** Đổi chữ ↔ byte bằng `StandardCharsets.UTF_8` ở cả 2 bên; DB dùng `utf8mb4`.

**9. Xóa thuốc có mất hẳn không?** Không, là xóa mềm: chỉ ghi `ngay_xoa`. Danh sách chỉ hiện thuốc `ngay_xoa IS NULL`. Muốn mất hẳn thì vào Thùng rác xóa vĩnh viễn.

**10. Vì sao xóa công dụng bị từ chối dù thuốc đã ở thùng rác?** Thuốc trong thùng rác **vẫn còn trong bảng** và vẫn trỏ tới công dụng (khóa ngoại `ON DELETE RESTRICT`). Phải chuyển hoặc xóa vĩnh viễn nó trước.

**11. Transaction là gì, dùng ở đâu?** "Làm hết hoặc không gì cả". Dùng khi xóa công dụng kiểu MOVE/CASCADE: nhiều câu SQL, lỗi giữa chừng thì `rollback`.

**12. Chống SQL injection thế nào?** Dùng `PreparedStatement` với dấu `?` (`server/Sql.java`), không cộng chuỗi người dùng nhập vào câu SQL. Mã cũng được kiểm tra chỉ gồm chữ, số, `-`, `_` (`common/Validation.java`).

**13. Kiểm tra dữ liệu ở đâu?** Cả 2 phía: Client kiểm tra để báo nhanh, Server kiểm tra lại (`Validation`) vì không tin dữ liệu gửi qua mạng.

**14. Server tắt thì dữ liệu có mất không?** Không, dữ liệu nằm trong MySQL. Server lần đầu chạy tự tạo database `quanlythuoc`, 3 bảng và dữ liệu mẫu (`Database.java:46-53`).

**15. Đổi cổng / đổi IP Server ở đâu?** `common/Protocol.java`: `PORT`, `HOST`. Sửa xong chạy lại cả Server và Client. Chạy 2 máy khác nhau thì đổi `HOST` thành IP máy Server.

**16. Đổi thời gian chờ?** `TIMEOUT_MS` trong `Protocol.java`.

---

## Nếu thầy bắt sửa code tại chỗ

Khuôn chung: **tìm chỗ → sửa → tắt Server cũ (Ctrl+C) → chạy lại `./chay_bai.sh`**.
- Đổi cổng thành 9999 → `Protocol.java` dòng `PORT`.
- Đổi timeout 3 giây → `TIMEOUT_MS = 3000`.
- Thêm chữ/nhãn trên giao diện → xem `09_so_cong_thuc_swing.md`.
- Thêm 1 lệnh mới: thêm tên vào `enum Command` → thêm 1 dòng `case` trong `RequestHandler.dispatch` → Client gọi `app.send(..., Command.TEN_MOI, ...)`.

Tập sửa trên **bản copy**, không sửa trong `B23DTCN112_DOMINHVU/` (bài đã nộp).
