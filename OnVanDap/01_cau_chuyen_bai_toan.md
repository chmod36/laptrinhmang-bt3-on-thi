# 01 – Câu chuyện của bài (chưa cần đọc code)

## 1. Đề bài yêu cầu gì

Đề chung của lớp là "Quản lý thuốc theo công dụng" làm theo kiểu Client–Server qua UDP. Mỗi sinh viên làm một phần. **Phần cá nhân** của bài này gồm 3 chức năng:

1. **Xóa thuốc**
2. **Xóa công dụng**
3. **Hiển thị kết quả xóa**

Giao diện phải có **MSSV và họ tên** sinh viên. Báo cáo PDF có bảng 3 cột: STT | chức năng | ảnh giao diện.

## 2. Câu chuyện nhà thuốc

Tưởng tượng một **nhà thuốc** có một cuốn sổ lớn ghi:
- **Danh sách công dụng**: CD01 Giảm đau, CD02 Hạ sốt, CD03 Bổ sung vitamin, CD04 Sát khuẩn, CD05 Kháng histamin.
- **Danh sách thuốc**: mỗi thuốc ghi rõ thuộc công dụng nào. Ví dụ T001 Paracetamol thuộc CD02 Hạ sốt.

Cuốn sổ chỉ nằm ở **quầy chính**. Nhân viên ở **máy tính bàn ngoài** muốn xóa một thuốc thì không được tự lấy bút gạch sổ. Họ phải **gửi giấy nhắn** vào quầy: "Xóa giúp thuốc T001". Quầy chính xem sổ, xóa, rồi **gửi giấy nhắn trả lời**: "Đã xóa Paracetamol" hoặc "Không xóa được, vì…".

Ghép với bài:

| Đời thường | Trong bài | Tên tiếng Anh |
|---|---|---|
| Quầy chính giữ cuốn sổ | Chương trình `MedicineServer` | **Server** /ˈsɜː.vər/ "xơ-vơ" = người phục vụ |
| Nhân viên ở máy ngoài | Chương trình `MedicineClient` (có cửa sổ) | **Client** /ˈklaɪ.ənt/ "clai-ần" = khách hàng |
| Tờ giấy nhắn | Gói tin gửi qua mạng | **Packet** /ˈpæk.ɪt/ "pác-kít" = gói nhỏ |
| Quy định cách viết giấy nhắn | File `Protocol.java` | **Protocol** /ˈprəʊ.tə.kɒl/ "prâu-tơ-côn" = nghi thức |

Hai chương trình này **chạy riêng**. Server phải bật trước, giống quầy chính phải mở cửa thì nhân viên mới gửi giấy vào được.

## 3. UDP là gì: gửi bưu thiếp hay gọi điện?

Có hai cách phổ biến để gửi tin qua mạng:

| | **TCP** "ti-xi-pi" | **UDP** "diu-đi-pi" |
|---|---|---|
| Ví dụ đời thường | **Gọi điện thoại**: phải đổ chuông, bên kia nhấc máy rồi mới nói. Nói câu nào bên kia nghe câu đó, đúng thứ tự. | **Gửi bưu thiếp**: viết xong bỏ hòm thư là xong. Không biết tới chưa, mất thì cũng không ai báo. |
| Có nối máy trước không | Có | Không |
| Đảm bảo tới nơi, đúng thứ tự | Có | Không |
| Tốc độ | Chậm hơn chút | Nhanh, nhẹ |
| Dùng cho | Web, tải file, chat | Game online, gọi video, DNS, **bài này** |

Bài này dùng **UDP**, theo yêu cầu của đề. Vì gửi bưu thiếp không chắc tới nơi, Client làm một việc rất đời thường: **chờ tối đa 5 giây**. Không thấy trả lời thì báo "Server không phản hồi". Giống gửi tin nhắn hỏi đường: chờ một lúc không thấy ai trả lời thì thôi, đi hỏi người khác.

## 4. Địa chỉ và cửa: IP và Port

Muốn gửi bưu thiếp thì cần **địa chỉ nhà** và **số phòng**.

- **Địa chỉ IP** = địa chỉ tòa nhà. Bài dùng `127.0.0.1`, nghĩa là **chính máy này** (Client và Server chạy cùng một máy). Ai cũng gọi địa chỉ này là "localhost".
- **Port** /pɔːt/ "pót" = số phòng trong tòa nhà. Một máy có hàng chục nghìn "phòng". Server ngồi ở **phòng 9876**. Client phải biết số này mới gửi đúng chỗ.

## 5. Mười file trong bài, ví như các bộ phận của nhà thuốc

| Nhóm | File | Ví như | Vai trò |
|---|---|---|---|
| `common` (dùng chung) | `Protocol.java` | Bảng nội quy dán ở cả hai bên | Tên các lệnh, cổng 9876, thời gian chờ 5 giây, ký tự ngăn cách |
| | `Medicine.java` | Mẫu phiếu "thông tin thuốc" | Một thuốc gồm: mã, tên, giá, mã công dụng, dạng bào chế |
| | `Usage.java` | Mẫu phiếu "thông tin công dụng" | Một công dụng gồm: mã, tên, mô tả |
| | `Response.java` | Mẫu giấy trả lời | Thành công hay thất bại, kèm nội dung |
| `server` | `MedicineServer.java` | Quầy chính và cuốn sổ | Giữ dữ liệu, nhận lệnh, xóa, trả lời |
| `client` | `MedicineClient.java` | Người quản lý ca ở máy ngoài | Dựng cửa sổ chính, điều phối mọi việc |
| | `DeletePanel.java` | Một bàn làm việc có bảng và nút xóa | Màn tab 1 và tab 2 (dùng chung một khuôn) |
| | `ResultLogPanel.java` | Sổ nhật ký ở máy ngoài | Tab 3: ghi lại mọi lần xóa, xanh hay đỏ |
| | `UdpClient.java` | Người đưa thư | Gửi gói tin đi và đứng chờ thư trả lời |
| | `Ui.java` | Bộ màu sơn, cỡ chữ của cửa hàng | Màu, phông chữ, khoảng cách, họ tên, MSSV |

Lý do có thư mục `common`: cả hai bên **phải nói cùng một ngôn ngữ**. Nếu Client gọi lệnh là `DEL_MED` mà Server hiểu là `DELETE_MED` thì không ai hiểu ai. Đặt chung một chỗ thì sửa một lần, hai bên cùng đổi.

## 6. Giấy nhắn viết thế nào (giao thức)

Mỗi giấy nhắn là **một dòng chữ**. Ký hiệu `⇥` dưới đây là phím TAB.

| Client gửi | Nghĩa | Server trả |
|---|---|---|
| `LIST_MED` | Cho xem danh sách thuốc | `OK⇥` + mỗi thuốc một dòng, các ô cách nhau bằng `\|` |
| `LIST_USE` | Cho xem danh sách công dụng | `OK⇥` + mỗi công dụng một dòng |
| `DEL_MED⇥T001` | Xóa thuốc T001 | `OK⇥Đã xóa thuốc…` hoặc `ERR⇥Không tìm thấy…` |
| `DEL_USE⇥CD02` | Xóa công dụng CD02 | `OK⇥Đã xóa công dụng…` hoặc `ERR⇥lý do` |
| lệnh không có trong danh sách | | `ERR⇥Lệnh không hợp lệ: …` |

Ví dụ một dòng thuốc Server gửi về: `T001|Paracetamol 500mg|15000|CD02|Viên nén`. Giống một dòng trong file Excel, mỗi ô cách nhau bằng dấu `|`.

**Vì sao không dùng dấu phẩy?** Vì mô tả có thể chứa dấu phẩy ("Giảm đau, hạ sốt"), dễ cắt nhầm. Dấu TAB và `|` gần như không bao giờ có trong dữ liệu.

## 7. Một quy tắc nghiệp vụ duy nhất

> **Công dụng còn thuốc đang dùng thì không được xóa.**

Ví dụ đời thường: trường học không thể xóa lớp D23CN04 khi trong lớp vẫn còn sinh viên. Xóa lớp đi thì các sinh viên đó "thuộc lớp không tồn tại". Phải chuyển hoặc xóa hết sinh viên trước.

Trong bài: T005 Efferalgan thuộc CD02 Hạ sốt. Xóa CD02 trong khi T005 còn đó thì T005 trỏ vào công dụng không có thật. Vì vậy Server từ chối và nói rõ: "đang được 1 thuốc sử dụng (T005). Hãy xóa các thuốc này trước." Trong môn Cơ sở dữ liệu, cái này gọi là **toàn vẹn tham chiếu** (giống khóa ngoại – foreign key).

## 8. Dữ liệu lưu ở đâu

**Trong bộ nhớ (RAM) của Server**, không có file hay cơ sở dữ liệu. Giống viết trên bảng trắng: tắt Server là xóa bảng. Bật lại thì Server tự viết lại 5 thuốc và 5 công dụng mẫu ban đầu. Đề chỉ yêu cầu chức năng xóa và giao tiếp UDP nên làm như vậy là đủ. Nếu thầy hỏi "muốn lưu lâu dài thì sao?": ghi ra file hoặc dùng cơ sở dữ liệu (MySQL, SQL Server…).

## 9. Bài nói 1 phút (học thuộc ý, nói bằng lời của mình)

> "Em làm phần cá nhân gồm 3 chức năng: xóa thuốc, xóa công dụng và hiển thị kết quả xóa.
> Chương trình có hai phần chạy riêng. Server UDP giữ danh sách thuốc và công dụng trong bộ nhớ, lắng nghe ở cổng 9876. Client là giao diện Java Swing có 3 tab, phần đầu ghi MSSV và họ tên.
> Khi người dùng chọn một dòng và bấm xóa, Client hỏi xác nhận, rồi gửi một gói UDP gồm tên lệnh và mã cần xóa. Server xóa rồi trả về OK hoặc ERR kèm lý do. Client hiện kết quả bằng hộp thoại, ghi vào tab Kết quả xóa và tải lại danh sách.
> Có một quy tắc: công dụng còn thuốc đang dùng thì không xóa được. Vì UDP không đảm bảo gói tin tới nơi, Client chỉ chờ tối đa 5 giây, quá thì báo lỗi kết nối."

## Tự kiểm tra

1. Server và Client, cái nào phải chạy trước? Vì sao?
2. UDP giống gửi bưu thiếp hay gọi điện thoại? Vì thế Client phải làm gì?
3. Tại sao không xóa được công dụng CD02 ngay từ đầu?

<details><summary>Đáp án</summary>

1. Server chạy trước, vì nó phải "ngồi ở phòng 9876" chờ sẵn thì Client mới có chỗ gửi tin tới.
2. Giống bưu thiếp: gửi đi không biết tới chưa. Vì thế Client chỉ chờ tối đa 5 giây, quá thì báo lỗi.
3. Vì còn thuốc đang dùng CD02 (T001 và T005). Xóa đi thì các thuốc đó trỏ tới công dụng không tồn tại.

</details>
