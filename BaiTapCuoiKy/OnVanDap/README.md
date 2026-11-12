# Ôn vấn đáp BT3 – Quản lý thuốc theo công dụng (UDP)

Bộ tài liệu này dành cho người **chưa biết gì về Java và lập trình mạng**. Học hết theo thứ tự thì đủ sức tự đi thi vấn đáp: giới thiệu bài, chạy demo, giải thích luồng, trả lời câu hỏi và sửa code ngay tại chỗ.

## Học theo thứ tự này

| Buổi | Đọc / xem | Thời gian | Học xong phải làm được |
|---|---|---|---|
| 1 | [01 – Câu chuyện của bài](01_cau_chuyen_bai_toan.md) | 30 phút | Kể lại bài bằng lời thường trong 1 phút |
| 2 | 🎬 `video/01_huong_dan_chay_code.mp4` + [02 – Hướng dẫn chạy](02_huong_dan_chay.md) | 30 phút | Tự chạy Server, Client và làm đủ 6 thao tác demo |
| 3 | [03 – Java tối thiểu](03_java_toi_thieu.md) | 1,5 giờ | Chỉ vào một hàm bất kỳ, nói được nó làm gì |
| 4 | 🎬 `video/02_giai_thich_chi_tiet.mp4` + [04 – Luồng chạy chi tiết](04_luong_chay_chi_tiet.md) | 1,5 giờ | Vẽ lại sơ đồ luồng xóa thuốc ra giấy, không nhìn tài liệu |
| 5 | [05 – Sửa code khi thi](05_sua_code_khi_thi.md) | 1,5 giờ | Tự làm lại 3 bài sửa code mà không nhìn đáp án |
| 6 | [06 – Câu hỏi vấn đáp](06_cau_hoi_van_dap.md) | 1 giờ | Nhờ người hỏi bất kỳ 10 câu, trả lời trôi chảy |
| 7 | [09 – Sổ công thức Swing](09_so_cong_thuc_swing.md) | 2 giờ | Nghe thầy nói một yêu cầu giao diện, biết ngay dùng món nào và dán vào đâu |
| 8 | [10 – Đề thi thử](10_de_thi_thu.md) | 1–2 giờ | Làm đề 1–7 trong 40 phút không nhìn sổ |
| Tra cứu | [07 – Từ điển](07_tu_dien.md) | khi cần | Gặp từ tiếng Anh lạ thì tra ở đây |

Tổng khoảng 10 giờ, chia làm 4 buổi tối.

**Cho người sẽ dạy lại:** trước khi dạy, làm [08 – Tự viết lại bài từ con số 0](08_tu_viet_code_tu_dau.md) (khoảng 4 giờ). Tự gõ một bản mini qua 5 bước, bước nào cũng chạy được, rồi đặt cạnh bài thật để so.

## Ba video

| Video | Dài | Nội dung |
|---|---|---|
| `video/01_huong_dan_chay_code.mp4` | 1,5 phút | Quay màn hình thật: biên dịch, chạy Server, chạy Client, xóa thành công, xóa công dụng bị từ chối, tắt Server rồi Thử lại |
| `video/02_giai_thich_chi_tiet.mp4` | 7,5 phút | Dành cho người mất gốc: đi từ con số 0 (Client/Server, mạng, gói tin, IP, port, TCP và UDP, timeout), rồi theo chân một lần bấm Xóa, 3 dòng code mỗi bên, và 4 câu thầy hay hỏi |
| `video/03_sua_code_theo_yeu_cau.mp4` | 4 phút | Cho người mất gốc và lười: thầy bảo sửa code thì làm sao, không cần giỏi Java, chỉ cần tra sổ, Ctrl+F dòng mốc, dán, chạy lại. Làm mẫu sắp xếp bảng (1 dòng) và thêm cột Giá giảm 10% |

Video có giọng đọc và phụ đề tiếng Việt in sẵn trong hình. Xem video trước để nắm ý chính, rồi đọc các file `.md` để hiểu từng dòng code.

## Bài nằm ở đâu

```
Mon2/
├── B23DTCN112_DOMINHVU/            ← bài đã nộp (đừng sửa trong này)
│   ├── B23DTCN112-Đỗ Minh Vũ.pdf   ← báo cáo
│   └── source/                     ← code Java, mở thư mục này bằng NetBeans
│       ├── HUONG_DAN_CHAY.txt
│       └── src/main/java/bt3/
│           ├── common/   Protocol, Medicine, Usage, Response
│           ├── server/   MedicineServer
│           └── client/   MedicineClient, DeletePanel, ResultLogPanel, UdpClient, Ui
└── OnVanDap/                       ← bộ tài liệu này
```

Mọi đường dẫn code trong tài liệu tính từ thư mục `source/src/main/java/`. Ví dụ `bt3/client/DeletePanel.java:82` nghĩa là file `DeletePanel.java`, **dòng 82**. Trong NetBeans bấm `Ctrl+G` rồi gõ số dòng để nhảy tới.

## Luyện sửa code ở đâu

**Không sửa trực tiếp trong `B23DTCN112_DOMINHVU/source`**, vì đó là bài đã nộp. Muốn tập thì copy thư mục `source` ra chỗ khác (ví dụ `Desktop/tap_sua`), mở bản copy bằng NetBeans rồi sửa thoải mái.

## Ba điều phải nhớ nếu chỉ nhớ được ba điều

1. **Client gửi một dòng chữ, Server trả một dòng chữ.** Ví dụ gửi `DEL_MED T001`, nhận lại `OK Đã xóa thuốc…` hoặc `ERR lý do`.
2. **UDP không đảm bảo tin tới nơi**, nên Client chỉ chờ tối đa 5 giây, quá thì báo lỗi.
3. **Công dụng còn thuốc đang dùng thì không xóa được.** Phải xóa các thuốc đó trước.
