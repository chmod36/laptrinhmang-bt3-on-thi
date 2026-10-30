# 06 – Câu hỏi vấn đáp hay gặp

Câu trả lời viết ngắn để **học ý**. Khi nói thì dùng lời của mình, vừa nói vừa mở đúng file chỉ cho thầy.

---

## A. Về bài làm

**1. Em giới thiệu bài đi.**
Nói bài 1 phút ở file 01, mục 9. Rồi chạy demo 6 thao tác ở file 02, mục 5.

**2. Phần cá nhân của em gồm chức năng gì? Ở đâu trên giao diện?**
Xóa thuốc (tab 1), xóa công dụng (tab 2), hiển thị kết quả xóa (tab 3). MSSV và họ tên ở góc phải phần đầu và trên thanh tiêu đề cửa sổ.

**3. Chương trình có mấy phần? Chạy cái nào trước?**
Hai chương trình riêng: Server (`MedicineServer`) và Client (`MedicineClient`). Server chạy trước để mở cửa 9876 chờ sẵn.

**4. Dữ liệu lưu ở đâu? Tắt Server thì sao?**
Trong bộ nhớ, ở hai `LinkedHashMap` (`MedicineServer.java:25-26`). Tắt là mất; bật lại nạp lại 5 thuốc và 5 công dụng mẫu. Muốn lưu lâu dài thì ghi file hoặc dùng cơ sở dữ liệu.

**5. Vì sao chia ba gói `common`, `server`, `client`?**
`common` chứa thứ **cả hai bên dùng chung**: quy ước lệnh, khuôn dữ liệu. Sửa một chỗ thì hai bên cùng đổi, không lệch nhau. `server` và `client` là hai chương trình riêng.

## B. Về mạng và UDP

**6. UDP khác TCP thế nào?**
UDP không nối máy trước, gửi từng gói độc lập, không đảm bảo tới nơi hay đúng thứ tự, nhưng nhanh và nhẹ. Giống gửi bưu thiếp. TCP phải nối máy trước (bắt tay 3 bước), đảm bảo tới nơi, đúng thứ tự. Giống gọi điện.

**7. Sao bài dùng UDP?**
Đề yêu cầu. Hơn nữa mỗi yêu cầu ở đây chỉ là một tin ngắn và một câu trả lời ngắn, rất hợp với UDP.

**8. Gói tin bị mất thì sao?**
Client đặt `setSoTimeout(5000)` (`UdpClient.java:20`). Sau 5 giây không có trả lời thì `receive` ném `SocketTimeoutException`. Client báo lỗi kết nối, ghi một dòng đỏ ở tab 3, giữ nguyên dữ liệu đang hiện. Bài không tự gửi lại; muốn thì bọc `send` trong vòng lặp thử 3 lần.

**9. Server biết gửi trả lời về đâu?**
Đọc địa chỉ người gửi từ chính gói vừa nhận: `packet.getSocketAddress()` (`MedicineServer.java:104`). Vì UDP không giữ đường dây nối, mỗi gói tự mang địa chỉ người gửi, giống địa chỉ ghi sau phong bì.

**10. Vì sao Server mở cửa cố định còn Client thì không?**
Client phải biết gõ cửa nào nên Server cố định ở 9876. Client để máy tự chọn cửa trống (`new DatagramSocket()` không có số). Server biết cửa đó nhờ địa chỉ trong gói tin.

**11. `127.0.0.1` là gì?**
Địa chỉ "chính máy này" (localhost). Client và Server chạy cùng máy. Hai máy khác nhau thì đổi `HOST` thành IP máy Server.

**12. Giao thức hai bên thống nhất thế nào?**
File `Protocol.java`:
- Yêu cầu: `LỆNH`, hoặc `LỆNH⇥mã`.
- Trả lời: `OK⇥nội dung` hoặc `ERR⇥lý do`.
- Danh sách: mỗi bản ghi một dòng, các ô cách nhau bằng `|`.

4 lệnh: `LIST_MED`, `LIST_USE`, `DEL_MED`, `DEL_USE`.

**13. Sao dùng TAB và `|` mà không dùng dấu phẩy?**
Tên và mô tả có thể chứa dấu phẩy. TAB và `|` gần như không có trong dữ liệu, cắt ra không nhầm.

**14. Sao phải dùng UTF-8?**
Mạng chỉ chở byte. Tiếng Việt có dấu cần một bảng mã chung để hai bên đổi chữ ↔ byte giống nhau. Lệch bảng mã thì chữ bị lỗi.

**15. Buffer 8192 byte, gói lớn hơn thì sao?**
Phần thừa bị cắt mất. Dữ liệu bài nhỏ nên đủ. Dữ liệu lớn thì tăng `BUFFER_SIZE` (một gói UDP tối đa khoảng 64 KB) hoặc chia thành nhiều gói.

**16. Gửi lệnh sai thì Server làm gì?**
Nhánh `default` trong `switch` trả `ERR Lệnh không hợp lệ`. Server không sập.

## C. Về nghiệp vụ

**17. Sao không xóa được công dụng?**
Còn thuốc đang dùng nó. Xóa thì thuốc trỏ tới công dụng không tồn tại (toàn vẹn tham chiếu, giống khóa ngoại). Phải xóa các thuốc đó trước. Code ở `MedicineServer.java:77-83`.

**18. Hai Client cùng xóa T001 thì sao?**
Ai tới trước xóa được. Người sau nhận `ERR Không tìm thấy thuốc mã T001 (có thể đã bị xóa trước đó)`.

**19. Gõ mã chữ thường `t001` có xóa được không?**
Được. `handle` đổi mã sang chữ hoa (`toUpperCase`) và bỏ khoảng trắng (`trim`) trước khi tra.

**20. Cột "Số thuốc đang dùng" ở tab 2 lấy từ đâu?**
Client tự đếm từ danh sách thuốc vừa nhận (`MedicineClient.java:108-109`). Server không gửi con số này.

## D. Về Java và giao diện

**21. SwingWorker để làm gì?**
Gửi và chờ UDP chạy ở thread khác, để thread giao diện không bị chặn và cửa sổ không đơ. Xong việc, `done()` chạy lại trên thread giao diện để cập nhật bảng.

**22. Chống bấm xóa hai lần thế nào?**
Trong lúc đang gửi, `setBusy(true)` khóa nút Xóa và nút Tải lại (`MedicineClient.java:170`, `DeletePanel.java:78`).

**23. Vì sao tab 1 và tab 2 dùng chung `DeletePanel`?**
Hai màn giống nhau, chỉ khác tên cột và lệnh gửi. Phần khác nhau được truyền vào bằng lambda (`MedicineClient.java:49-54`), nên không phải viết lặp code.

**24. `synchronized` ở `handle` để làm gì?**
Mỗi lúc chỉ một yêu cầu được sửa dữ liệu. Hiện Server xử lý tuần tự nên chưa bắt buộc. Đây là phòng xa khi sau này xử lý song song nhiều thread.

**25. `record` là gì? Sao dùng?**
Class chỉ chứa dữ liệu. Java tự sinh hàm khởi tạo, hàm đọc giá trị, `equals`, `toString`, nên viết ngắn. Có từ Java 16, vì vậy cần JDK 17 trở lên.

**26. Tab "Kết quả xóa" lấy dữ liệu từ đâu?**
Từ phản hồi của Server sau mỗi lần xóa, kể cả lỗi mạng. Lưu ở Client, không gửi lệnh riêng. Nút "Xóa nhật ký" chỉ xóa bảng ở Client.

**27. Vì sao xóa thành công thì tải lại danh sách, lỗi mạng thì không?**
Thành công thì bảng phải khớp với sổ ở Server. Lỗi mạng thì tải lại cũng lỗi, nên giữ nguyên dữ liệu đang hiện cho người dùng xem.

**28. `try (DatagramSocket socket = …)` khác `try` thường chỗ nào?**
Dạng có ngoặc tròn tự đóng socket khi dùng xong, kể cả khi có lỗi. Không bị rò rỉ cổng mạng.

---

## Thi thử

Nhờ một người cầm tài liệu này đóng vai thầy, làm theo thứ tự:
1. "Em giới thiệu bài." → bài nói 1 phút.
2. "Chạy cho thầy xem." → 6 thao tác demo.
3. Thầy chỉ ngẫu nhiên vào một hàm: "Hàm này làm gì?" → tìm các dòng `return`, nói từng kết cục.
4. Hỏi ngẫu nhiên 8 câu trong danh sách trên.
5. "Thêm cho thầy chức năng…" → chọn một bài ở file 05, làm trong 10 phút.

Lặp lại tới khi qua được cả 5 phần mà không mở tài liệu.

## Mẹo trong phòng thi

- **Trả lời theo luồng.** Bị hỏi khó thì nói "Khi bấm xóa thì Client… rồi Server…", vừa nói vừa mở đúng file. Thầy chấm hiểu luồng hơn thuộc chữ.
- **Bị bắt sửa:** nói khuôn 3 bước ra trước, rồi mới gõ. Gõ xong nhớ **tắt Server cũ** rồi chạy lại.
- **Mở file nhanh:** NetBeans `Ctrl+Shift+O` gõ tên file; `Ctrl+G` gõ số dòng; `Ctrl+F` tìm chữ (ví dụ `DEL_MED`).
- **Không biết thì nói thật** phần mình chưa biết, rồi nói phần gần nhất mình biết. Đừng bịa.
- **Thao tác demo trước khi thầy hỏi** sẽ dẫn dắt câu hỏi về phần mình đã chuẩn bị.
