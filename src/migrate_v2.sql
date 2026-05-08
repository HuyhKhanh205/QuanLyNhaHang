-- =====================================================
-- Migration v2: Màn hình bếp
-- Chạy 1 lần trên DB đã tồn tại (StarGuardianDB)
-- =====================================================

USE StarGuardianDB;

-- 1. Thêm các cột mới vào ChiTietHoaDon
ALTER TABLE ChiTietHoaDon
    ADD COLUMN IF NOT EXISTS trangThaiMon VARCHAR(20) NOT NULL DEFAULT 'Chờ',
    ADD COLUMN IF NOT EXISTS soLuongDaXacNhan INT NOT NULL DEFAULT 0;

-- 2. Thêm tài khoản bếp để test
INSERT IGNORE INTO TaiKhoan (tenTK, matKhau, trangThai) VALUES
  ('bep01', 'hashed_-969161597', 1);

INSERT IGNORE INTO NhanVien (maNV, hoTen, ngaySinh, gioiTinh, sdt, diaChi, ngayVaoLam, luong, tenTK, vaiTro, email) VALUES
  ('NV01003', 'Lê Văn Bếp', '1998-03-20', 'Nam', '0923456789', 'TP. HCM', '2023-06-01', 7000000, 'bep01', 'BEP', 'bep01@starguardian.com');

-- Kiểm tra kết quả
SELECT 'ChiTietHoaDon columns:' AS info;
SHOW COLUMNS FROM ChiTietHoaDon LIKE 'trangThaiMon';

SELECT 'Accounts:' AS info;
SELECT nv.maNV, nv.hoTen, nv.vaiTro, tk.tenTK
FROM NhanVien nv JOIN TaiKhoan tk ON nv.tenTK = tk.tenTK
ORDER BY nv.vaiTro, nv.maNV;
