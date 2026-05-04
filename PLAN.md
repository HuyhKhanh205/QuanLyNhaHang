# PLAN.md — Các bước tiếp theo sau migration JPA/Hibernate/MariaDB/Socket

> Tuân thủ SKILL.md: mỗi bước có tiêu chí xác minh cụ thể, không thêm scope ngoài yêu cầu.

---

## Trạng thái hiện tại

Code đã được migrate và push lên GitHub. **Chưa chạy được** vì:
1. MariaDB chưa được setup
2. Maven dependencies chưa được download vào IntelliJ
3. Có **1 bug nghiêm trọng** trong Dashboard sẽ gây lỗi silent ngay khi login

---

## Bug cần sửa trước khi chạy

### BUG-1 (Critical) — DashboardQuanLyGUI dùng key sai
**File:** `src/gui/DashboardQuanLyGUI.java` — dòng 478–480

```java
// HIỆN TẠI (sai — key cũ bằng tiếng Việt)
lblBanTrong.setText(...tables.getOrDefault("Trống", 0));
lblBanPhucVu.setText(...tables.getOrDefault("Đang có khách", 0));
tables.getOrDefault("Đã đặt trước", 0)
```

`BanDAO.getTableStatusCounts()` giờ trả về key enum: `"TRONG"`, `"DANG_PHUC_VU"`, `"DA_DAT_TRUOC"`.
Dashboard sẽ luôn hiển thị `0` cho mọi trạng thái bàn.

**Fix:** Sửa `BanDAO.getTableStatusCounts()` trả về key tiếng Việt (ít thay đổi hơn):

```java
// BanDAO.java — getTableStatusCounts()
counts.put("Trống", 0);
counts.put("Đang có khách", 0);
counts.put("Đã đặt trước", 0);
// ...
for (Object[] row : rows) {
    String tt = row[0].toString();  // enum name từ DB
    String key = switch (tt) {
        case "DANG_PHUC_VU" -> "Đang có khách";
        case "DA_DAT_TRUOC"  -> "Đã đặt trước";
        default              -> "Trống";
    };
    counts.put(key, ((Long) row[1]).intValue());
}
```
**Verify:** Dashboard Manager hiển thị số bàn đúng sau khi login.

---

### BUG-2 (Minor) — schema_mariadb.sql unique index hint có thể fail
**File:** `src/schema_mariadb.sql` — phần tạo index cho `KhachHang.email`

```sql
-- HIỆN TẠI (có thể fail trên một số bản MariaDB)
CREATE UNIQUE INDEX UQ_KhachHang_Email ON KhachHang (email)
    ALGORITHM = INPLACE LOCK = NONE;
```

**Fix:** Xóa hint `ALGORITHM = INPLACE LOCK = NONE`, giữ nguyên `CREATE UNIQUE INDEX`.
MariaDB cho phép nhiều NULL trong UNIQUE index, nên không cần `WHERE email IS NOT NULL`.

**Verify:** Script chạy không lỗi từ đầu đến cuối.

---

### BUG-3 (Minor) — PhanCongDAO dead code
**File:** `src/dao/PhanCongDAO.java` — `themPhanCong()`

```java
// 2 dòng này thừa, không dùng
PhanCong pc = new PhanCong();
pc.getId();
```

**Fix:** Xóa 2 dòng đó.
**Verify:** Code compile OK, không side-effect.

---

## Thứ tự thực hiện

```
Bước 1: Sửa 3 bug ở trên
Bước 2: Setup môi trường (MariaDB + Maven)
Bước 3: Chạy app và test từng chức năng
Bước 4: Tạo data seed MariaDB
```

---

## Bước 2 — Setup môi trường

### 2a. Cài MariaDB & tạo database
```bash
# Đăng nhập MariaDB
mysql -u root -p

# Chạy schema
source f:/QuanLyDatBan/src/schema_mariadb.sql
```
**Verify:** `SHOW TABLES;` hiện đủ 15 bảng.

### 2b. Tạo file data_mariadb.sql (seed data)
Cần file seed data dùng **tên enum mới** (không phải tiếng Việt cho trangThai/vaiTro):

```sql
-- Ví dụ accounts
INSERT INTO TaiKhoan(tenTK, matKhau, trangThai) VALUES
  ('admin', 'hashed_-1294775406', 1),   -- password: admin123
  ('nv01',  'hashed_-1294775406', 1);

INSERT INTO NhanVien(maNV, hoTen, ngaySinh, gioiTinh, sdt, ngayVaoLam, luong, tenTK, vaiTro, email)
VALUES
  ('NV02001', 'Nguyễn Văn A', '1990-01-01', 'Nam', '0901234567', '2023-01-01', 15000000, 'admin', 'QUANLY',  'admin@star.com'),
  ('NV01002', 'Trần Thị B',   '1995-05-15', 'Nữ', '0912345678', '2023-06-01', 8000000,  'nv01',  'NHANVIEN', 'nv01@star.com');

-- Bàn dùng enum name (không phải tiếng Việt)
INSERT INTO Ban(maBan, tenBan, soGhe, trangThai, khuVuc) VALUES
  ('BAN01', 'Bàn 1', 4, 'TRONG', 'Tầng trệt'),
  ('BAN02', 'Bàn 2', 4, 'TRONG', 'Tầng trệt');
```
**Verify:** App login được với tài khoản `admin`.

### 2c. Reimport Maven trong IntelliJ
`File → Reload All Maven Projects` → chờ download xong (~2-5 phút).

**Verify:** IDE errors "jakarta cannot be resolved" biến mất.

### 2d. Compile kiểm tra
```bash
mvn compile
```
**Verify:** `BUILD SUCCESS`, 0 errors.

---

## Bước 3 — Test từng chức năng

Thực hiện theo thứ tự từ core outward:

| # | Test case | Verify |
|---|-----------|--------|
| 1 | Đăng nhập với tài khoản admin | Vào được MainGUI |
| 2 | Dashboard Quản lý hiển thị | Số bàn, doanh thu load được (không crash) |
| 3 | Danh sách bàn hiển thị | Các bàn từ DB hiện ra đúng |
| 4 | Tạo đơn đặt món (BAN01) | `DonDatMon` insert vào DB, bàn chuyển sang `DANG_PHUC_VU` |
| 5 | Gọi thêm món | `ChiTietHoaDon` insert, tổng tiền tính đúng |
| 6 | Thanh toán | `HoaDon` cập nhật `Đã thanh toán`, bàn về `TRONG` |
| 7 | Quản lý nhân viên (CRUD) | Thêm/sửa NV và TK cùng lúc hoạt động |
| 8 | Lịch làm việc | PhanCong hiển thị, thêm/xóa phân công |
| 9 | Giao ca | Bắt đầu ca, kết thúc ca, số tiền tính đúng |
| 10 | Socket sync | Mở 2 instance, đổi trạng thái bàn ở máy 1 → máy 2 tự refresh |

---

## Bước 4 — Rủi ro kỹ thuật cần theo dõi

Các điểm có thể gây runtime error nhưng chưa chắc:

| Rủi ro | Lý do | Cách kiểm tra |
|--------|-------|---------------|
| `LocalTime` ↔ MariaDB `TIME` | Hibernate 6 hỗ trợ, nhưng cần xác nhận | `CaLamDAO.getAllCaLam()` trả về đúng giờ |
| `float` → `DECIMAL(18,2)` | Có thể mất precision trên số lớn | Kiểm tra tổng tiền hóa đơn >1 triệu |
| `@EmbeddedId` PhanCong | PhanCongId Serializable, nhưng cần Hibernate accept | Test thêm phân công |
| `hbm2ddl.auto=validate` | Nếu schema không khớp entity → crash khi start | Chạy app, xem log lỗi |

---

## Không làm (scope ngoài plan)

- Refactor GUI code (không liên quan đến migration)
- Thêm feature mới
- Migrate data từ SQL Server sang MariaDB (chỉ dùng seed data mới)
- Tối ưu performance (HikariCP, cache)
