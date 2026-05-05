-- =====================================================
-- Seed data cho StarGuardianDB
-- Password "admin123" → hash: hashed_-969161597
-- =====================================================

USE StarGuardianDB;

-- =====================================================
-- TaiKhoan
-- =====================================================
INSERT IGNORE INTO TaiKhoan (tenTK, matKhau, trangThai) VALUES
  ('admin', 'hashed_-969161597', 1),
  ('nv01',  'hashed_-969161597', 1);

-- =====================================================
-- CaLam
-- =====================================================
INSERT IGNORE INTO CaLam (maCa, tenCa, gioBatDau, gioKetThuc) VALUES
  ('CA001', 'Ca Sáng', '07:00:00', '14:00:00'),
  ('CA002', 'Ca Tối',  '14:00:00', '22:00:00');

-- =====================================================
-- NhanVien (phải insert TaiKhoan trước)
-- =====================================================
INSERT IGNORE INTO NhanVien (maNV, hoTen, ngaySinh, gioiTinh, sdt, diaChi, ngayVaoLam, luong, tenTK, vaiTro, email) VALUES
  ('NV02001', 'Nguyễn Văn Admin', '1990-01-01', 'Nam',  '0901234567', 'TP. HCM', '2023-01-01', 15000000, 'admin', 'QUANLY',   'admin@starguardian.com'),
  ('NV01002', 'Trần Thị Nhân Viên', '1995-05-15', 'Nữ', '0912345678', 'TP. HCM', '2023-06-01',  8000000, 'nv01',  'NHANVIEN', 'nv01@starguardian.com');

-- =====================================================
-- DanhMucMon
-- =====================================================
INSERT IGNORE INTO DanhMucMon (maDM, tenDM, moTa, maNV) VALUES
  ('DM001', 'Món chính',  'Các món ăn chính', 'NV02001'),
  ('DM002', 'Đồ uống',    'Nước ngọt, trà, cà phê', 'NV02001'),
  ('DM003', 'Món tráng miệng', 'Bánh, kem', 'NV02001');

-- =====================================================
-- Ban
-- =====================================================
INSERT IGNORE INTO Ban (maBan, tenBan, soGhe, trangThai, gioMoBan, khuVuc) VALUES
  ('BAN01', 'Bàn 01', 4, 'TRONG', NULL, 'Tầng trệt'),
  ('BAN02', 'Bàn 02', 4, 'TRONG', NULL, 'Tầng trệt'),
  ('BAN03', 'Bàn 03', 6, 'TRONG', NULL, 'Tầng trệt'),
  ('BAN04', 'Bàn 04', 2, 'TRONG', NULL, 'Tầng 2'),
  ('BAN05', 'Bàn 05', 4, 'TRONG', NULL, 'Tầng 2'),
  ('BAN06', 'Bàn 06', 6, 'TRONG', NULL, 'Tầng 2');

-- =====================================================
-- MonAn
-- =====================================================
INSERT IGNORE INTO MonAn (maMonAn, tenMon, moTa, donGia, donViTinh, trangThai, hinhAnh, maDM) VALUES
  ('MON001', 'Cơm sườn nướng',    'Cơm trắng + sườn nướng than hoa',  75000, 'Phần', 'Còn hàng', NULL, 'DM001'),
  ('MON002', 'Bún bò Huế',        'Bún bò cay đặc trưng Huế',         65000, 'Tô',   'Còn hàng', NULL, 'DM001'),
  ('MON003', 'Phở bò tái chín',   'Phở bò truyền thống',              60000, 'Tô',   'Còn hàng', NULL, 'DM001'),
  ('MON004', 'Gà chiên giòn',     'Đùi gà chiên giòn',                80000, 'Phần', 'Còn hàng', NULL, 'DM001'),
  ('MON005', 'Cà phê đen',        'Cà phê phin truyền thống',         25000, 'Ly',   'Còn hàng', NULL, 'DM002'),
  ('MON006', 'Cà phê sữa đá',     'Cà phê phin + sữa đặc + đá',      30000, 'Ly',   'Còn hàng', NULL, 'DM002'),
  ('MON007', 'Nước cam ép',        'Cam tươi ép',                      35000, 'Ly',   'Còn hàng', NULL, 'DM002'),
  ('MON008', 'Trà đào cam sả',    'Trà đào tươi mát',                 40000, 'Ly',   'Còn hàng', NULL, 'DM002'),
  ('MON009', 'Chè khúc bạch',     'Chè khúc bạch hạnh nhân',         45000, 'Ly',   'Còn hàng', NULL, 'DM003'),
  ('MON010', 'Kem dừa',           'Kem dừa tươi',                     35000, 'Phần', 'Còn hàng', NULL, 'DM003');