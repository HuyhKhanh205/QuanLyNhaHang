# PLAN — Vấn đề tồn tại ảnh hưởng đọc CSDL theo tính năng

> Kết quả audit toàn bộ DAO + GUI sau migration JPA/Hibernate/MariaDB.  
> Chỉ liệt kê vấn đề **thực sự** làm sai dữ liệu hoặc crash — không thêm scope.

---

## Tổng quan kết quả audit

| Hạng mục | Kết quả |
|----------|---------|
| SQL Server syntax còn sót trong DAO | ✅ Không có |
| EntityManager leak (không close) | ✅ Không có |
| Import `Map` thiếu | ✅ Đầy đủ |
| Enum TrangThaiBan trong native SQL | ✅ Dùng TRONG/DANG_PHUC_VU/DA_DAT_TRUOC đúng |


| DonDatMon/HoaDon trangThai | ✅ Vẫn dùng tiếng Việt đúng (STRING field) |
| Column index mapping DAO → Entity | ✅ Khớp |

**Vấn đề thực sự tìm thấy: 3**

---

## VẤN ĐỀ 1 — BanDAO.ghepBanLienKet() hardcode maNV không tồn tại
**Mức độ: 🔴 CRITICAL — crash khi ghép bàn**

### Hiện trạng
`src/dao/BanDAO.java` dòng 164 và 220:
```java
// Dòng 164: tạo đơn cho bàn đích (khi chưa có đơn)
"VALUES(?, NOW(), NOW(), 'NV01102', ?, 'Chưa thanh toán')"

// Dòng 220: tạo đơn dummy LINKED cho bàn nguồn
"VALUES(?, NOW(), NOW(), 'NV01102', ?, 'Chưa thanh toán', ?)"
```

`maNV = 'NV01102'` được hardcode từ code cũ (SQL Server era). Trong MariaDB với seed data mới, các nhân viên có mã `NV02001` và `NV01002` — **không có NV01102**.

### Tác động
- `ghepBanLienKet()` throw **FK constraint violation** → tính năng ghép bàn crash hoàn toàn.
- Lỗi xảy ra tại: `GhepBanDialog` → `BanDAO.ghepBanLienKet()`.

### Fix
Thay `'NV01102'` bằng maNV của nhân viên đang đăng nhập. Cần truyền `maNVDangNhap` vào `ghepBanLienKet()`:

```java
// Chữ ký method mới
public boolean ghepBanLienKet(List<Ban> listBanNguon, Ban banDich, String maNVDangNhap)

// Trong SQL thay 'NV01102' bằng tham số
"VALUES(?, NOW(), NOW(), ?, ?, 'Chưa thanh toán')"
.setParameter(1, maDonDich).setParameter(2, maNVDangNhap).setParameter(3, banDich.getMaBan())
```

**Verify:** Ghép bàn không throw exception, DonDatMon được tạo với đúng maNV.

---

## VẤN ĐỀ 2 — ManHinhGoiMonGUI không nhận ORDER events từ Socket
**Mức độ: 🟠 HIGH — màn gọi món không sync real-time**

### Hiện trạng
`src/gui/ManHinhGoiMonGUI.java` dòng 332–337:
```java
private void dangKySocketEvents() {
    SocketClient client = SocketManager.getClient();
    if (client == null) return;
    client.subscribe(SocketEvent.MENU_UPDATED, msg ->     // ← chỉ subscribe 1 event
            SwingUtilities.invokeLater(this::loadDataFromDB));
}
```

Thiếu subscription cho `ORDER_CREATED` và `ORDER_UPDATED`.

### Tác động
- Máy 1 đang mở bàn BAN01, máy 2 gọi thêm món cho BAN01.
- `activeHoaDon` trên máy 1 **không tự cập nhật** — danh sách món cũ, tổng tiền sai.
- Khi in hóa đơn từ máy 1, sẽ **thiếu món do máy 2 gọi**.

### Fix
Thêm 2 subscription vào `dangKySocketEvents()`:

```java
private void dangKySocketEvents() {
    SocketClient client = SocketManager.getClient();
    if (client == null) return;
    client.subscribe(SocketEvent.MENU_UPDATED, msg ->
            SwingUtilities.invokeLater(this::loadDataFromDB));
    // Thêm mới:
    client.subscribe(SocketEvent.ORDER_CREATED, msg ->
            SwingUtilities.invokeLater(this::reloadActiveOrder));
    client.subscribe(SocketEvent.ORDER_UPDATED, msg ->
            SwingUtilities.invokeLater(this::reloadActiveOrder));
}

// Method mới — chỉ reload order nếu event cùng bàn đang hiển thị
private void reloadActiveOrder() {
    if (banHienTai == null || activeHoaDon == null) return;
    List<ChiTietHoaDon> dsChiTiet = chiTietDAO.getChiTietTheoMaDon(activeHoaDon.getMaDon());
    activeHoaDon.setDsChiTiet(dsChiTiet);
    activeHoaDon.tinhLaiTongTienTuChiTiet();
    // Cập nhật bảng chi tiết và billPanel
    modelChiTietHoaDon.setRowCount(0);
    if (dsChiTiet != null) {
        for (ChiTietHoaDon ct : dsChiTiet) {
            modelChiTietHoaDon.addRow(new Object[]{ct.getTenMon(), ct.getSoluong(),
                    ct.getDongia(), ct.getThanhtien()});
        }
    }
    billPanel.updateBill(activeHoaDon);
}
```

**Verify:** Máy 2 thêm món → màn gọi món máy 1 tự cập nhật danh sách và tổng tiền.

---

## VẤN ĐỀ 3 — getHoaDonChuaThanhToan() trả tenBan = null
**Mức độ: 🟡 THẤP — có fallback, không crash**

### Hiện trạng
`src/dao/HoaDonDAO.java` dòng 132–133:
```java
"SELECT hd.maHD, ..., hd.maDon, NULL, ddm.maKH " // ← NULL thay cho tenBan
```

`rowToHoaDon()` đọc `r[10]` là `tenBan` → luôn nhận `null`.

### Tác động
- `ManHinhGoiMonGUI`: **Không ảnh hưởng** — BillPanel dùng `banHienTai.getTenBan()`, không đọc `HoaDon.tenBan`.
- `HoaDonGUI` (lịch sử hóa đơn): **Không ảnh hưởng** — dùng `getHoaDonByPage()` (có JOIN với Ban, `tenBan` đúng). Khi hiện chi tiết, có fallback query DB:
  ```java
  if (hoaDon.getTenBan() != null) tenBan = hoaDon.getTenBan();
  else tenBan = banDAO.getTenBanByMa(...);  // fallback hoạt động
  ```

### Fix (cải thiện, không bắt buộc)
Thêm JOIN vào query `getHoaDonChuaThanhToan()` để lấy `tenBan` thực thay vì NULL:
```java
"SELECT hd.maHD, hd.ngayLap, hd.tongTien, hd.trangThai, hd.hinhThucThanhToan, " +
"hd.tienKhachDua, hd.giamGia, hd.maNV, hd.maKM, hd.maDon, b.tenBan, ddm.maKH " +
"FROM HoaDon hd JOIN DonDatMon ddm ON hd.maDon = ddm.maDon " +
"LEFT JOIN Ban b ON ddm.maBan = b.maBan " +           // ← thêm JOIN này
"WHERE ddm.maBan = ? AND hd.trangThai = 'Chưa thanh toán'"
```

**Verify:** `activeHoaDon.getTenBan()` trả về tên bàn thực.

---

## Thứ tự fix

```
1. BanDAO.ghepBanLienKet() — maNVDangNhap    🔴 Fix ngay (crash)
2. ManHinhGoiMonGUI — ORDER subscriptions     🟠 Fix tiếp (UX real-time)
3. getHoaDonChuaThanhToan — tenBan NULL       🟡 Fix sau hoặc skip (có fallback)
```

---

## Tính năng KHÔNG bị ảnh hưởng (đã xác minh)

| Tính năng | Lý do OK |
|-----------|---------|
| Đăng nhập | TaiKhoanDAO xử lý TINYINT(1) đúng (Boolean + Number) |
| Danh sách bàn | BanDAO.getAllBan() dùng JPA entity mapping, trangThai enum tự map |
| Gọi món (đơn lẻ) | DonDatMonDAO, ChiTietHoaDonDAO column mapping đúng |
| Thanh toán | HoaDonDAO.thanhToanHoaDon() dùng MariaDB LOCATE, query enum đúng |
| Lịch sử hóa đơn | getHoaDonByPage() JOIN Ban, tenBan đúng |
| Nhân viên CRUD | NhanVienDAO + TaiKhoanDAO hoạt động |
| Khách hàng CRUD | KhachHangDAO enum HangThanhVien tự map |
| Danh mục + Món ăn | DanhMucMonDAO, MonAnDAO JPA persist/merge đúng |
| Lịch làm việc | PhanCongDAO native query dùng CURDATE/CURTIME |
| Giao ca | GiaoCaDAO TIMESTAMPDIFF, DATE_ADD MariaDB đúng |
| Dashboard | DashboardQuanLyGUI getTableStatusCounts() đã fix key map |
