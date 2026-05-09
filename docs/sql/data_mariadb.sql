-- =====================================================
-- Dữ liệu mẫu StarGuardianDB  (clean seed)
-- Tất cả mật khẩu: admin123  → hashed_-969161597
-- Bàn: Tầng trệt (6 bàn) + Tầng 1 (5 bàn)
-- =====================================================

USE StarGuardianDB;

-- =====================================================
-- TaiKhoan
-- =====================================================
INSERT IGNORE INTO TaiKhoan (tenTK, matKhau, trangThai) VALUES
  ('admin',  'hashed_-969161597', 1),
  ('nv01',   'hashed_-969161597', 1),
  ('nv02',   'hashed_-969161597', 1),
  ('bep01',  'hashed_-969161597', 1);

-- =====================================================
-- CaLam
-- =====================================================
INSERT IGNORE INTO CaLam (maCa, tenCa, gioBatDau, gioKetThuc) VALUES
  ('CA001', 'Ca Sáng',  '07:00:00', '14:00:00'),
  ('CA002', 'Ca Tối',   '14:00:00', '22:00:00');

-- =====================================================
-- NhanVien
-- =====================================================
INSERT IGNORE INTO NhanVien (maNV, hoTen, ngaySinh, gioiTinh, sdt, diaChi, ngayVaoLam, luong, tenTK, vaiTro, email) VALUES
  ('NV001', 'Nguyễn Văn Quản',  '1988-04-12', 'Nam', '0901111111', 'Q.1, TP. HCM',  '2022-01-01', 15000000, 'admin', 'QUANLY',   'admin@starguardian.vn'),
  ('NV002', 'Trần Thị Lan',     '1998-07-25', 'Nữ',  '0912222222', 'Q.3, TP. HCM',  '2023-03-01',  8000000, 'nv01',  'NHANVIEN', 'lan.tran@starguardian.vn'),
  ('NV003', 'Lê Văn Hùng',      '1997-11-08', 'Nam', '0923333333', 'Q.10, TP. HCM', '2023-03-01',  8000000, 'nv02',  'NHANVIEN', 'hung.le@starguardian.vn'),
  ('NV004', 'Phạm Thị Bếp',     '1995-02-14', 'Nữ',  '0934444444', 'Q.5, TP. HCM',  '2023-01-15',  9000000, 'bep01', 'BEP',      'bep@starguardian.vn');

-- =====================================================
-- DanhMucMon
-- =====================================================
INSERT IGNORE INTO DanhMucMon (maDM, tenDM, moTa, maNV) VALUES
  ('DM001', 'Món chính',       'Các món ăn chính theo thực đơn',          'NV001'),
  ('DM002', 'Khai vị',         'Món khai vị và ăn nhẹ',                   'NV001'),
  ('DM003', 'Đồ uống',         'Nước ngọt, trà, cà phê, rượu vang',       'NV001'),
  ('DM004', 'Tráng miệng',     'Kem, bánh, chè và các món ngọt',          'NV001');

-- =====================================================
-- Ban  (Tầng trệt: 6 bàn | Tầng 1: 5 bàn)
-- =====================================================
INSERT IGNORE INTO Ban (maBan, tenBan, soGhe, trangThai, gioMoBan, khuVuc) VALUES
  -- Tầng trệt
  ('BAN01', 'Bàn T.01', 2, 'TRONG', NULL, 'Tầng trệt'),
  ('BAN02', 'Bàn T.02', 4, 'TRONG', NULL, 'Tầng trệt'),
  ('BAN03', 'Bàn T.03', 4, 'TRONG', NULL, 'Tầng trệt'),
  ('BAN04', 'Bàn T.04', 4, 'TRONG', NULL, 'Tầng trệt'),
  ('BAN05', 'Bàn T.05', 6, 'TRONG', NULL, 'Tầng trệt'),
  ('BAN06', 'Bàn T.06', 8, 'TRONG', NULL, 'Tầng trệt'),
  -- Tầng 1
  ('BAN07', 'Bàn 1.01', 4, 'TRONG', NULL, 'Tầng 1'),
  ('BAN08', 'Bàn 1.02', 4, 'TRONG', NULL, 'Tầng 1'),
  ('BAN09', 'Bàn 1.03', 4, 'TRONG', NULL, 'Tầng 1'),
  ('BAN10', 'Bàn 1.04', 6, 'TRONG', NULL, 'Tầng 1'),
  ('BAN11', 'Bàn 1.05', 8, 'TRONG', NULL, 'Tầng 1');

-- =====================================================
-- MonAn  (ảnh khớp với resources/img/MonAn/)
-- =====================================================
INSERT IGNORE INTO MonAn (maMonAn, tenMon, moTa, donGia, donViTinh, trangThai, hinhAnh, maDM) VALUES
  -- Món chính
  ('MON001', 'Sườn nướng BBQ',      'Sườn heo nướng sốt BBQ đặc trưng',         95000,  'Phần', 'Còn', 'bbqRibs.jpg',    'DM001'),
  ('MON002', 'Bò bít tết',          'Thăn bò áp chảo bơ tỏi, kèm khoai tây',   120000, 'Phần', 'Còn', 'beefsteak.jpg',  'DM001'),
  ('MON003', 'Burger bò phô mai',   'Burger 2 tầng thịt bò + phô mai cheddar',  85000,  'Phần', 'Còn', 'burger.jpg',     'DM001'),
  ('MON004', 'Cơm chiên dương châu','Cơm chiên hải sản + trứng + rau củ',        65000,  'Phần', 'Còn', 'comChien.jpg',   'DM001'),
  ('MON005', 'Mì ý sốt bò bằm',    'Spaghetti sốt bolognese bò xay',            80000,  'Phần', 'Còn', 'spaghetti.jpg',  'DM001'),
  -- Khai vị
  ('MON006', 'Khoai tây chiên',     'Khoai tây cắt que giòn, sốt mayo',          45000,  'Phần', 'Còn', 'frenchFries.jpg','DM002'),
  ('MON007', 'Bánh mì tỏi',         'Bánh mì nướng bơ tỏi thơm',                35000,  'Phần', 'Còn', 'garlicBread.jpg','DM002'),
  ('MON008', 'Nấm xào bơ tỏi',      'Nấm trộn các loại, xào bơ tỏi nguyên củ', 50000,  'Phần', 'Còn', 'namXao.jpg',     'DM002'),
  ('MON009', 'Salad Caesar',         'Salad romaine + crouton + sốt Caesar',     55000,  'Phần', 'Còn', 'saladCaesar.jpg','DM002'),
  ('MON010', 'Súp kem bí đỏ',       'Súp kem bí đỏ nướng, topping kem tươi',   45000,  'Tô',   'Còn', 'supBiDo.jpg',    'DM002'),
  -- Đồ uống
  ('MON011', 'Espresso',            'Cà phê espresso rang đậm',                  35000,  'Ly',   'Còn', 'espresso.jpg',   'DM003'),
  ('MON012', 'Soda chanh dây',      'Soda tươi chanh dây nhiệt đới',             40000,  'Ly',   'Còn', 'sodaBlue.jpg',   'DM003'),
  ('MON013', 'Trà đào cam sả',      'Trà đào tươi, cam + sả tự nhiên',          45000,  'Ly',   'Còn', 'traDao.jpg',     'DM003'),
  ('MON014', 'Trà lài mật ong',     'Trà lài hoa tươi + mật ong nguyên chất',   40000,  'Ly',   'Còn', 'traLai.jpg',     'DM003'),
  ('MON015', 'Nước lọc',            'Nước suối đóng chai',                       15000,  'Chai', 'Còn', 'water.jpg',      'DM003'),
  ('MON016', 'Vang Chile',          'Rượu vang đỏ Chile – chai 750ml',          180000, 'Chai', 'Còn', 'vangChile.jpg',  'DM003'),
  ('MON017', 'Vang Pháp',           'Rượu vang đỏ Bordeaux – chai 750ml',       250000, 'Chai', 'Còn', 'vangPhap.jpg',   'DM003'),
  -- Tráng miệng
  ('MON018', 'Kem dừa tươi',        'Kem dừa tươi dừa xiêm + đậu xanh',         35000,  'Phần', 'Còn', NULL, 'DM004'),
  ('MON019', 'Chè khúc bạch',       'Chè khúc bạch hạnh nhân, thạch rau câu',   45000,  'Ly',   'Còn', NULL, 'DM004'),
  ('MON020', 'Bánh tiramisu',       'Bánh tiramisu cà phê Ý kiểu truyền thống', 65000,  'Phần', 'Còn', NULL, 'DM004');

-- =====================================================
-- KhachHang  (phủ đủ 5 hạng thành viên)
-- MEMBER : 0 – 5,000,000   | BRONZE : 5,000,001 – 10,000,000
-- SILVER : 10,000,001 – 25,000,000 | GOLD : 25,000,001 – 50,000,000
-- DIAMOND: > 50,000,000
-- =====================================================
INSERT IGNORE INTO KhachHang (maKH, tenKH, gioiTinh, sdt, hangThanhVien, tongChiTieu, ngaySinh, diaChi, ngayThamGia, email) VALUES
  ('KH001', 'Nguyễn Thị Lan',   'Nữ',  '0981111111', 'MEMBER',  3200000,  '1992-03-15', 'Q.Bình Thạnh, TP. HCM', '2025-01-10', 'lan.nguyen@gmail.com'),
  ('KH002', 'Trần Văn Minh',    'Nam', '0982222222', 'BRONZE',  6800000,  '1988-07-22', 'Q.Gò Vấp, TP. HCM',     '2024-08-05', 'minh.tran@gmail.com'),
  ('KH003', 'Lê Thị Hoa',       'Nữ',  '0983333333', 'SILVER',  12500000, '1995-11-08', 'Q.Phú Nhuận, TP. HCM',  '2024-03-20', 'hoa.le@gmail.com'),
  ('KH004', 'Phạm Quốc Đức',    'Nam', '0984444444', 'GOLD',    28000000, '1985-04-30', 'Q.1, TP. HCM',           '2023-06-15', 'duc.pham@gmail.com'),
  ('KH005', 'Hoàng Thị Mai',    'Nữ',  '0985555555', 'DIAMOND', 55000000, '1980-09-12', 'Q.3, TP. HCM',           '2023-01-02', 'mai.hoang@gmail.com');

-- =====================================================
-- KhuyenMai
-- =====================================================
INSERT IGNORE INTO KhuyenMai (maKM, tenKM, moTa, ngayBatDau, ngayKetThuc, loaiGiam, giaTriGiam, trangThai, dieuKienApDung, soLuongGioiHan, soLuotDaDung) VALUES
  ('KM001', 'Giảm 10% toàn menu',     'Áp dụng mọi hóa đơn, không giới hạn',              '2026-05-01', '2026-05-31', 'Giảm theo phần trăm', 10,    'Đang áp dụng', 0,      NULL, 0),
  ('KM002', 'Ưu đãi 50k',             'Giảm 50.000đ khi hóa đơn từ 300.000đ',             '2026-05-01', '2026-05-31', 'Giảm giá số tiền',    50000, 'Đang áp dụng', 300000, 100,  12),
  ('KM003', 'Khai trương – 20%',       'Khuyến mãi khai trương tháng 01/2025 (đã hết hạn)', '2025-01-01', '2025-01-31', 'Giảm theo phần trăm', 20,    'Ngưng áp dụng', 0,     NULL, 87);

-- =====================================================
-- PhanCongCa  (tuần hiện tại + vài ngày trước)
-- =====================================================
INSERT IGNORE INTO PhanCongCa (maNV, maCa, ngayLam) VALUES
  -- Tuần trước
  ('NV002', 'CA001', '2026-05-04'), ('NV003', 'CA002', '2026-05-04'),
  ('NV003', 'CA001', '2026-05-05'), ('NV002', 'CA002', '2026-05-05'),
  ('NV002', 'CA001', '2026-05-06'), ('NV003', 'CA002', '2026-05-06'),
  ('NV003', 'CA001', '2026-05-07'), ('NV002', 'CA002', '2026-05-07'),
  -- Hôm qua
  ('NV002', 'CA001', '2026-05-08'), ('NV003', 'CA002', '2026-05-08'),
  ('NV004', 'CA001', '2026-05-08'), ('NV004', 'CA002', '2026-05-08'),
  -- Hôm nay
  ('NV002', 'CA001', '2026-05-09'), ('NV003', 'CA002', '2026-05-09'),
  ('NV004', 'CA001', '2026-05-09'), ('NV004', 'CA002', '2026-05-09'),
  -- Ngày mai
  ('NV003', 'CA001', '2026-05-10'), ('NV002', 'CA002', '2026-05-10');

-- =====================================================
-- DonDatMon  (3 đơn hoàn thành ngày hôm qua)
-- =====================================================
INSERT IGNORE INTO DonDatMon (maDon, ngayKhoiTao, thoiGianDen, trangThai, maNV, maKH, maBan, ghiChu) VALUES
  ('DON001', '2026-05-08 12:10:00', NULL, 'Đã thanh toán', 'NV002', 'KH001', 'BAN02', NULL),
  ('DON002', '2026-05-08 18:30:00', NULL, 'Đã thanh toán', 'NV003', 'KH003', 'BAN05', 'Sinh nhật khách hàng'),
  ('DON003', '2026-05-08 19:45:00', NULL, 'Đã thanh toán', 'NV002', NULL,    'BAN07', NULL);

-- =====================================================
-- ChiTietHoaDon
-- =====================================================
INSERT IGNORE INTO ChiTietHoaDon (maDon, maMonAn, soLuong, donGia, trangThaiMon, soLuongDaXacNhan) VALUES
  -- DON001: tổng = 120k + 90k = 210,000
  ('DON001', 'MON002', 1, 120000, 'Đã lên', 1),   -- Bò bít tết x1
  ('DON001', 'MON013', 2,  45000, 'Đã lên', 2),   -- Trà đào x2
  -- DON002: tổng = 190k + 85k + 120k = 395,000  → áp KM001 -10% → giảm 39,500
  ('DON002', 'MON001', 2,  95000, 'Đã lên', 2),   -- Sườn BBQ x2
  ('DON002', 'MON003', 1,  85000, 'Đã lên', 1),   -- Burger x1
  ('DON002', 'MON012', 3,  40000, 'Đã lên', 3),   -- Soda chanh dây x3
  -- DON003: tổng = 130k + 30k = 160,000
  ('DON003', 'MON004', 2,  65000, 'Đã lên', 2),   -- Cơm chiên x2
  ('DON003', 'MON015', 2,  15000, 'Đã lên', 2);   -- Nước lọc x2

-- =====================================================
-- HoaDon
-- =====================================================
INSERT IGNORE INTO HoaDon (maHD, ngayLap, tongTien, trangThai, hinhThucThanhToan, tienKhachDua, giamGia, tenBan, maNV, maKM, maDon) VALUES
  ('HD001', '2026-05-08 12:35:00', 210000, 'Đã thanh toán', 'Tiền mặt',     250000,     0, 'Bàn T.02', 'NV002', NULL,  'DON001'),
  ('HD002', '2026-05-08 19:05:00', 395000, 'Đã thanh toán', 'Chuyển khoản', 395000, 39500, 'Bàn T.05', 'NV003', 'KM001','DON002'),
  ('HD003', '2026-05-08 20:20:00', 160000, 'Đã thanh toán', 'Tiền mặt',     200000,     0, 'Bàn 1.01', 'NV002', NULL,  'DON003');

-- =====================================================
-- LichSuSuDungKM  (KH003 đã dùng KM001)
-- =====================================================
INSERT IGNORE INTO LichSuSuDungKM (maKH, maKM, ngaySuDung) VALUES
  ('KH003', 'KM001', '2026-05-08 19:05:00');

-- =====================================================
-- GiaoCa  (2 ca hôm qua đã đóng)
-- =====================================================
INSERT IGNORE INTO GiaoCa (maNV, thoiGianBatDau, thoiGianKetThuc, tienDauCa, tienCuoiCa, tienHeThongTinh, chenhLech, ghiChu) VALUES
  ('NV002', '2026-05-08 07:00:00', '2026-05-08 14:00:00', 500000, 870000, 870000,  0,    'Ca sáng bình thường'),
  ('NV003', '2026-05-08 14:00:00', '2026-05-08 22:00:00', 500000, 856000, 856000,  0,    'Ca tối bình thường');
