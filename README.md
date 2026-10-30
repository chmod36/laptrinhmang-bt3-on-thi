# Lập trình mạng – BT3: Quản lý thuốc theo công dụng (UDP)

- `B23DTCN112_DOMINHVU/source/` – code Java (mở bằng NetBeans hoặc IntelliJ, cần JDK 17+). Cách chạy: `source/HUONG_DAN_CHAY.txt`.
- `OnVanDap/` – tài liệu và 3 video ôn vấn đáp. Lộ trình đầy đủ (khoảng 10 giờ) ở `OnVanDap/README.md`.

## Ôn cấp tốc (khoảng 3 giờ)

| Bước | Làm gì | Thời gian | Xong khi |
|---|---|---|---|
| 1 | Xem `OnVanDap/video/01_huong_dan_chay_code.mp4`, rồi tự chạy Server + Client theo `HUONG_DAN_CHAY.txt` | 20 phút | Tự xóa được 1 thuốc, thấy lỗi khi xóa công dụng còn thuốc |
| 2 | Xem `OnVanDap/video/02_giai_thich_chi_tiet.mp4` | 10 phút | Nói được: Client gửi một dòng chữ qua UDP, Server trả một dòng chữ |
| 3 | Đọc `OnVanDap/01_cau_chuyen_bai_toan.md` mục 9, tập nói bài 1 phút thành tiếng | 15 phút | Nói trơn không nhìn giấy |
| 4 | Đọc `OnVanDap/06_cau_hoi_van_dap.md`, che đáp án tự trả lời | 45 phút | Trả lời được 10 câu bất kỳ |
| 5 | Xem `OnVanDap/video/03_sua_code_theo_yeu_cau.mp4`, mở `09_so_cong_thuc_swing.md` | 30 phút | Biết món nào dán vào đâu |
| 6 | Làm 2–3 đề trong `OnVanDap/10_de_thi_thu.md` trên **bản copy** của `source` | 60 phút | Sửa xong, chạy lại được, không nhìn đáp án |

Ba điều phải nhớ:
1. Client gửi một dòng chữ (`DEL_MED T001`), Server trả `OK …` hoặc `ERR lý do`.
2. UDP không đảm bảo tin tới nơi, nên Client chờ tối đa 5 giây rồi báo lỗi.
3. Công dụng còn thuốc đang dùng thì không xóa được.

Luyện sửa code: copy thư mục `source` ra chỗ khác rồi sửa, giữ nguyên bản gốc.
