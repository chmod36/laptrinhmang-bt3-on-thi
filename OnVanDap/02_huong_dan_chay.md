# 02 – Hướng dẫn chạy bài

🎬 Xem trước: `video/01_huong_dan_chay_code.mp4` (1,5 phút). Video quay các bước chạy và thao tác demo 1, 2, 5, 6 ở mục 5.

## 1. Cần gì

- **JDK 17 trở lên**. JDK /ˌdʒeɪ.diːˈkeɪ/ "giây-đi-cây" là Java Development Kit, bộ đồ nghề để dịch và chạy Java. Kiểm tra bằng cách mở cửa sổ dòng lệnh và gõ `java -version`. Thấy số 17, 21, 25… là được.
- **NetBeans** (thầy hay dùng) **hoặc** chỉ cần cửa sổ dòng lệnh.

Vì sao phải từ 17: code dùng `record` (Buổi 03). Java chỉ có `record` từ bản 16.

## 2. Cách A – Chạy bằng NetBeans

1. `File` → `Open Project` → chọn thư mục **`source`** (thư mục có file `pom.xml`). NetBeans nhận ra đây là một project Maven.
2. Ở khung Projects, mở `Source Packages` → `bt3.server` → chuột phải vào `MedicineServer.java` → **Run File** (hoặc `Shift+F6`).
3. Khung Output phía dưới hiện:
   ```
   Server UDP quản lý thuốc đang lắng nghe tại cổng 9876
   ```
   **Đừng tắt** Server. Nó phải chạy suốt.
4. Mở `bt3.client` → chuột phải `MedicineClient.java` → **Run File**. Cửa sổ chương trình hiện lên.
5. Muốn dừng Server: bấm nút **■ màu đỏ** cạnh khung Output.

## 3. Cách B – Chạy bằng dòng lệnh

Mở cửa sổ dòng lệnh (Windows: `cmd`; Linux/macOS: Terminal), đi vào thư mục `source`:

```
cd đường_dẫn_tới/B23DTCN112_DOMINHVU/source
```

**Bước 1 – Biên dịch (compile).** Dịch code người viết thành dạng máy chạy được, đặt vào thư mục `out`:
```
javac -encoding UTF-8 -d out src/main/java/bt3/common/*.java src/main/java/bt3/server/*.java src/main/java/bt3/client/*.java
```
- `javac` = Java compiler, cái máy dịch.
- `-encoding UTF-8` = đọc file theo bảng mã UTF-8, để tiếng Việt có dấu không bị lỗi.
- `-d out` = để kết quả vào thư mục `out`.
- Không in ra gì là **thành công**.

**Bước 2 – Chạy Server:**
```
java -cp out bt3.server.MedicineServer
```
`-cp out` = classpath, nghĩa là "tìm code đã dịch trong thư mục `out`".

**Bước 3 – Mở cửa sổ dòng lệnh thứ hai**, cũng `cd` vào `source`, chạy Client:
```
java -cp out bt3.client.MedicineClient
```

Ví như mở **hai quầy riêng**: cửa sổ đầu là quầy chính (Server), cửa sổ hai là máy nhân viên (Client).

## 4. Nhìn cửa sổ chương trình

```
┌────────────────────────────────────────────────────────────────────────────┐
│ HỆ THỐNG QUẢN LÝ THUỐC THEO CÔNG DỤNG          Sinh viên thực hiện: Đỗ Minh Vũ │
│ Ứng dụng Client – Server qua giao thức UDP • Server 127.0.0.1:9876   MSSV: B23DTCN112 │
├──────────────┬─────────────────┬───────────────┐                            │
│ 1. Xóa thuốc │ 2. Xóa công dụng │ 3. Kết quả xóa │                            │
├──────────────┴─────────────────┴───────────────┴───────────────────────────┤
│  bảng danh sách                                                            │
│  ...                                                                       │
│  Đang chọn: T001 – Paracetamol 500mg      [Tải lại danh sách] [Xóa thuốc đã chọn] │
├────────────────────────────────────────────────────────────────────────────┤
│ Đã nhận từ Server 127.0.0.1:9876: 5 thuốc, 5 công dụng     ← dòng trạng thái │
└────────────────────────────────────────────────────────────────────────────┘
```

- **Phần đầu**: tên hệ thống, địa chỉ Server, họ tên và MSSV (đề bắt buộc có).
- **3 tab**: đúng 3 chức năng của phần cá nhân.
- **Dòng trạng thái** dưới cùng: luôn cho biết đang làm gì, xong chưa, có lỗi không.

## 5. Sáu thao tác demo (phải tự làm một lần, đi thi làm lại đúng như vậy)

Dữ liệu ban đầu:

| Thuốc | Công dụng |
|---|---|
| T001 Paracetamol 500mg | CD02 Hạ sốt |
| T002 Vitamin C 500mg | CD03 Bổ sung vitamin |
| T003 Povidone iodine 10% | CD04 Sát khuẩn |
| T004 Ibuprofen 200mg | CD01 Giảm đau |
| T005 Efferalgan 150mg | CD02 Hạ sốt |

CD05 Kháng histamin chưa có thuốc nào dùng.

| # | Làm gì | Thấy gì | Giải thích khi thầy hỏi |
|---|---|---|---|
| 1 | Tab 1, bấm dòng **T001**, bấm **Xóa thuốc đã chọn**, chọn **Có, xóa** | Hộp "Kết quả xóa: THÀNH CÔNG", T001 biến mất, tự chuyển sang tab 3 có một dòng xanh | Client gửi `DEL_MED T001`, Server xóa và trả `OK` |
| 2 | Tab 2, chọn **CD02 Hạ sốt**, xóa | Hộp "THẤT BẠI": đang được 1 thuốc sử dụng (T005) | Quy tắc: công dụng còn thuốc dùng thì không xóa |
| 3 | Tab 1, xóa **T005** | Thành công | Dọn thuốc đang dùng CD02 |
| 4 | Tab 2, xóa lại **CD02** | Lần này thành công; cột "Số thuốc đang dùng" trước đó đã về 0 | Không còn thuốc nào dùng nên xóa được |
| 5 | Mở tab **3. Kết quả xóa** | 4 dòng: 3 xanh, 1 đỏ. Dưới cùng: Tổng 4, Thành công 3, Thất bại 1 | Mỗi lần xóa đều ghi lại, kể cả thất bại |
| 6 | **Tắt Server**, bấm **Tải lại danh sách** | Chờ đúng 5 giây → hộp "Lỗi kết nối Server" có nút **Thử lại**. Bật lại Server, bấm Thử lại → danh sách hiện lại **đủ 5 thuốc** | Timeout 5 giây vì UDP không báo mất gói. Dữ liệu về như ban đầu vì Server lưu trong bộ nhớ |

Sau mỗi thao tác, nhìn cửa sổ Server. Mỗi gói tin đến là một dòng log:
```
[20:15:03] /127.0.0.1:53211  DEL_MED T001  ->  OK  Đã xóa thuốc Paracetamol 500mg (T001)
```
Đọc là: lúc 20:15:03, máy `127.0.0.1` gửi từ cửa `53211` lệnh `DEL_MED T001`, Server trả `OK`. Cửa `53211` do máy tự chọn cho Client, mỗi lần chạy một số khác.

## 6. Lỗi hay gặp khi chạy

| Hiện tượng | Nguyên nhân | Cách xử lý |
|---|---|---|
| `BindException: Address already in use` khi chạy Server | Đã có một Server khác đang giữ cửa 9876 (thường là lần chạy trước chưa tắt) | Tắt Server cũ (nút ■ đỏ trong NetBeans, hoặc `Ctrl+C` ở cửa sổ dòng lệnh) rồi chạy lại |
| Client báo "Server không phản hồi sau 5 giây" | Chưa bật Server, hoặc Server đã tắt | Bật Server rồi bấm **Thử lại** |
| `javac: command not found` / `'javac' is not recognized` | Chưa cài JDK hoặc chưa thêm vào PATH | Cài JDK 17+, mở lại cửa sổ dòng lệnh |
| Chữ tiếng Việt thành ký tự lạ | Biên dịch thiếu `-encoding UTF-8` | Biên dịch lại đúng lệnh ở Bước 1 |
| Sửa code xong chạy vẫn như cũ | Server cũ vẫn đang chạy code cũ | Tắt Server cũ, chạy lại cả Server và Client |
| `UnsupportedClassVersionError` | Java dùng để chạy cũ hơn Java dùng để dịch | Dùng JDK 17+ cho cả hai |

## Tự kiểm tra

1. Lệnh nào để biên dịch? Không in ra gì nghĩa là gì?
2. Chạy Server hai lần cùng lúc thì bị lỗi gì, vì sao?
3. Ở thao tác 6, vì sao lỗi hiện ra sau đúng 5 giây chứ không ngay lập tức?

<details><summary>Đáp án</summary>

1. `javac -encoding UTF-8 -d out …`. Không in ra gì là biên dịch thành công.
2. `BindException: Address already in use`. Mỗi cửa (port) chỉ một chương trình được giữ, Server đầu đã giữ 9876.
3. Vì UDP không báo "gói tin bị mất". Client cứ chờ, tới 5 giây (`TIMEOUT_MS`) mới bỏ cuộc và báo lỗi.

</details>
