CREATE TABLE IF NOT EXISTS cong_dung (
    ma_cd VARCHAR(10) NOT NULL,
    ten_cd VARCHAR(100) NOT NULL,
    mo_ta VARCHAR(255) NOT NULL DEFAULT '',
    PRIMARY KEY (ma_cd)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS thuoc (
    ma_thuoc VARCHAR(10) NOT NULL,
    ten_thuoc VARCHAR(100) NOT NULL,
    gia BIGINT NOT NULL,
    ma_cd VARCHAR(10) NOT NULL,
    dang_bao_che VARCHAR(50) NOT NULL,
    ngay_xoa DATETIME NULL DEFAULT NULL,
    PRIMARY KEY (ma_thuoc),
    KEY idx_thuoc_cong_dung (ma_cd),
    CONSTRAINT fk_thuoc_cong_dung FOREIGN KEY (ma_cd) REFERENCES cong_dung (ma_cd)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS nhat_ky_xoa (
    id INT NOT NULL AUTO_INCREMENT,
    thoi_gian DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    hanh_dong VARCHAR(40) NOT NULL,
    doi_tuong VARCHAR(20) NOT NULL,
    ma_doi_tuong VARCHAR(10) NOT NULL,
    ten_doi_tuong VARCHAR(100) NOT NULL DEFAULT '',
    thanh_cong TINYINT(1) NOT NULL,
    chi_tiet VARCHAR(500) NOT NULL,
    dia_chi_client VARCHAR(60) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
