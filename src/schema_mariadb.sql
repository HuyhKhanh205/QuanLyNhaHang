-- =====================================================
-- Schema MariaDB cho StarGuardian Restaurant
-- Lưu ý: TrangThaiBan dùng tên enum: TRONG, DANG_PHUC_VU, DA_DAT_TRUOC
--        VaiTro dùng tên enum: NHANVIEN, QUANLY
-- =====================================================

DROP DATABASE IF EXISTS StarGuardianDB;

CREATE DATABASE StarGuardianDB
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE StarGuardianDB;

-- =====================================================
-- Lookup tables (enum-like)
-- =====================================================

CREATE TABLE VaiTro (
    tenVaiTro VARCHAR(20) PRIMARY KEY
);
INSERT INTO VaiTro (tenVaiTro) VALUES ('NHANVIEN'), ('QUANLY');

CREATE TABLE HangThanhVien (
    tenHang VARCHAR(20) PRIMARY KEY
);
INSERT INTO HangThanhVien (tenHang) VALUES ('NONE'), ('MEMBER'), ('BRONZE'), ('SILVER'), ('GOLD'), ('DIAMOND');

-- =====================================================
-- Core tables
-- =====================================================

CREATE TABLE TaiKhoan (
    tenTK VARCHAR(50) PRIMARY KEY,
    matKhau VARCHAR(255) NOT NULL,
    trangThai TINYINT(1) NOT NULL DEFAULT 1
);

CREATE TABLE CaLam (
    maCa VARCHAR(20) PRIMARY KEY,
    tenCa VARCHAR(50) NOT NULL,
    gioBatDau TIME NOT NULL,
    gioKetThuc TIME NOT NULL
);

CREATE TABLE DanhMucMon (
    maDM VARCHAR(10) PRIMARY KEY,
    tenDM VARCHAR(100) NOT NULL,
    moTa VARCHAR(255),
    maNV VARCHAR(20) NULL
);

CREATE TABLE Ban (
    maBan VARCHAR(10) PRIMARY KEY,
    tenBan VARCHAR(50) NOT NULL,
    soGhe INT NOT NULL,
    trangThai VARCHAR(20) NOT NULL DEFAULT 'TRONG',
    gioMoBan DATETIME NULL,
    khuVuc VARCHAR(50) NOT NULL
);

CREATE TABLE KhuyenMai (
    maKM VARCHAR(20) PRIMARY KEY,
    tenKM VARCHAR(100) NOT NULL,
    moTa VARCHAR(255),
    ngayBatDau DATE NOT NULL,
    ngayKetThuc DATE NOT NULL,
    loaiGiam VARCHAR(50) NOT NULL,
    giaTriGiam DECIMAL(18, 2) NOT NULL,
    trangThai VARCHAR(50) NOT NULL,
    dieuKienApDung DECIMAL(18, 2) NULL DEFAULT 0,
    soLuongGioiHan INT DEFAULT NULL,
    soLuotDaDung INT NOT NULL DEFAULT 0
);

CREATE TABLE KhachHang (
    maKH VARCHAR(20) PRIMARY KEY,
    tenKH VARCHAR(100) NOT NULL,
    gioiTinh VARCHAR(10) NOT NULL,
    sdt VARCHAR(15) UNIQUE,
    hangThanhVien VARCHAR(20) NOT NULL DEFAULT 'NONE',
    tongChiTieu DECIMAL(18, 2) NOT NULL DEFAULT 0,
    ngaySinh DATE NULL,
    diaChi VARCHAR(255) NULL,
    ngayThamGia DATE NOT NULL DEFAULT (CURDATE()),
    email VARCHAR(100) NULL,
    CONSTRAINT FK_KhachHang_HangTV FOREIGN KEY (hangThanhVien) REFERENCES HangThanhVien (tenHang)
);

CREATE UNIQUE INDEX UQ_KhachHang_Email ON KhachHang (email);

-- =====================================================
-- Employee tables
-- =====================================================

CREATE TABLE NhanVien (
    maNV VARCHAR(20) PRIMARY KEY,
    hoTen VARCHAR(100) NOT NULL,
    ngaySinh DATE,
    gioiTinh VARCHAR(10) NOT NULL,
    sdt VARCHAR(15) UNIQUE NOT NULL,
    diaChi VARCHAR(255),
    ngayVaoLam DATE NOT NULL,
    luong DECIMAL(18, 2) NOT NULL,
    tenTK VARCHAR(50) NOT NULL UNIQUE,
    vaiTro VARCHAR(20) NOT NULL,
    email VARCHAR(100) UNIQUE,
    CONSTRAINT FK_NhanVien_TaiKhoan FOREIGN KEY (tenTK) REFERENCES TaiKhoan (tenTK),
    CONSTRAINT FK_NhanVien_VaiTro FOREIGN KEY (vaiTro) REFERENCES VaiTro (tenVaiTro)
);

ALTER TABLE DanhMucMon
    ADD CONSTRAINT FK_DanhMucMon_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien (maNV);

CREATE TABLE PhanCongCa (
    maNV VARCHAR(20) NOT NULL,
    maCa VARCHAR(20) NOT NULL,
    ngayLam DATE NOT NULL,
    PRIMARY KEY (maNV, maCa, ngayLam),
    CONSTRAINT FK_PhanCong_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien (maNV),
    CONSTRAINT FK_PhanCong_CaLam FOREIGN KEY (maCa) REFERENCES CaLam (maCa)
);

-- =====================================================
-- Order tables
-- =====================================================

CREATE TABLE MonAn (
    maMonAn VARCHAR(20) PRIMARY KEY,
    tenMon VARCHAR(100) NOT NULL,
    moTa VARCHAR(500),
    donGia DECIMAL(18, 2) NOT NULL,
    donViTinh VARCHAR(20) NOT NULL,
    trangThai VARCHAR(50) NOT NULL,
    hinhAnh VARCHAR(255),
    maDM VARCHAR(10) NOT NULL,
    CONSTRAINT FK_MonAn_DanhMucMon FOREIGN KEY (maDM) REFERENCES DanhMucMon (maDM)
);

CREATE TABLE DonDatMon (
    maDon VARCHAR(20) PRIMARY KEY,
    ngayKhoiTao DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    thoiGianDen DATETIME NULL,
    trangThai VARCHAR(50) NOT NULL DEFAULT 'Chưa thanh toán',
    maNV VARCHAR(20) NOT NULL,
    maKH VARCHAR(20) NULL,
    maBan VARCHAR(10) NULL,
    ghiChu TEXT NULL,
    CONSTRAINT FK_DonDatMon_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien (maNV),
    CONSTRAINT FK_DonDatMon_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang (maKH),
    CONSTRAINT FK_DonDatMon_Ban FOREIGN KEY (maBan) REFERENCES Ban (maBan)
);

CREATE TABLE ChiTietHoaDon (
    maDon VARCHAR(20) NOT NULL,
    maMonAn VARCHAR(20) NOT NULL,
    soLuong INT NOT NULL,
    donGia DECIMAL(18, 2) NOT NULL,
    PRIMARY KEY (maDon, maMonAn),
    CONSTRAINT FK_ChiTiet_DonDatMon FOREIGN KEY (maDon) REFERENCES DonDatMon (maDon),
    CONSTRAINT FK_ChiTiet_MonAn FOREIGN KEY (maMonAn) REFERENCES MonAn (maMonAn)
);

-- =====================================================
-- Invoice & Shift tables
-- =====================================================

CREATE TABLE HoaDon (
    maHD VARCHAR(20) PRIMARY KEY,
    ngayLap DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tongTien DECIMAL(18, 2) NOT NULL,
    trangThai VARCHAR(50) NOT NULL,
    hinhThucThanhToan VARCHAR(50),
    tienKhachDua DECIMAL(18, 2),
    giamGia DECIMAL(18, 2) NULL DEFAULT 0,
    tenBan VARCHAR(100),
    maNV VARCHAR(20) NOT NULL,
    maKM VARCHAR(20) NULL,
    maDon VARCHAR(20) NOT NULL UNIQUE,
    CONSTRAINT FK_HoaDon_KhuyenMai FOREIGN KEY (maKM) REFERENCES KhuyenMai (maKM),
    CONSTRAINT FK_HoaDon_DonDatMon FOREIGN KEY (maDon) REFERENCES DonDatMon (maDon)
);

CREATE TABLE LichSuSuDungKM (
    maLichSu INT AUTO_INCREMENT PRIMARY KEY,
    maKH VARCHAR(20) NOT NULL,
    maKM VARCHAR(20) NOT NULL,
    ngaySuDung DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT FK_LichSu_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang (maKH),
    CONSTRAINT FK_LichSu_KhuyenMai FOREIGN KEY (maKM) REFERENCES KhuyenMai (maKM)
);

CREATE TABLE GiaoCa (
    maGiaoCa INT AUTO_INCREMENT PRIMARY KEY,
    maNV VARCHAR(20),
    thoiGianBatDau DATETIME NOT NULL,
    thoiGianKetThuc DATETIME NULL,
    tienDauCa DECIMAL(18, 0) NOT NULL,
    tienCuoiCa DECIMAL(18, 0) NULL,
    tienHeThongTinh DECIMAL(18, 0) NULL,
    chenhLech DECIMAL(18, 0) NULL,
    ghiChu VARCHAR(255),
    FOREIGN KEY (maNV) REFERENCES NhanVien (maNV)
);
