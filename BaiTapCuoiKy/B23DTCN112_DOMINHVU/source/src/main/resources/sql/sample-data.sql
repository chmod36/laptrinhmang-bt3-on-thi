INSERT INTO cong_dung (ma_cd, ten_cd, mo_ta) VALUES
    ('CD01', 'Giảm đau', 'Giảm các cơn đau nhẹ và vừa'),
    ('CD02', 'Hạ sốt', 'Hạ thân nhiệt khi bị sốt'),
    ('CD03', 'Bổ sung vitamin', 'Bổ sung vitamin, tăng sức đề kháng'),
    ('CD04', 'Sát khuẩn', 'Sát khuẩn vết thương ngoài da'),
    ('CD05', 'Kháng histamin', 'Giảm triệu chứng dị ứng'),
    ('CD06', 'Tiêu hóa', 'Hỗ trợ tiêu hóa, giảm đầy bụng');

INSERT INTO thuoc (ma_thuoc, ten_thuoc, gia, ma_cd, dang_bao_che) VALUES
    ('T001', 'Paracetamol 500mg', 15000, 'CD02', 'Viên nén'),
    ('T002', 'Vitamin C 500mg', 25000, 'CD03', 'Viên sủi'),
    ('T003', 'Povidone iodine 10%', 32000, 'CD04', 'Dung dịch'),
    ('T004', 'Ibuprofen 200mg', 28000, 'CD01', 'Viên nang'),
    ('T005', 'Efferalgan 150mg', 41000, 'CD02', 'Thuốc bột'),
    ('T006', 'Loratadin 10mg', 18000, 'CD05', 'Viên nén'),
    ('T007', 'Vitamin B1 250mg', 12000, 'CD03', 'Viên nén'),
    ('T008', 'Hapacol 250mg', 22000, 'CD02', 'Thuốc bột'),
    ('T009', 'Cồn 70 độ', 9000, 'CD04', 'Dung dịch'),
    ('T010', 'Panadol Extra', 35000, 'CD01', 'Viên nén');
