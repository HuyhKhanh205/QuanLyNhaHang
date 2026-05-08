# Kế hoạch Kiểm thử — StarGuardian Restaurant Management

## Context

Dự án đã hoàn thành migration JPA + Socket + MariaDB và vừa qua audit fix lỗi. Mục tiêu kế hoạch này là kiểm thử **toàn diện** tất cả chức năng để đảm bảo hệ thống hoạt động đúng trước khi bàn giao/demo. Test được phân loại theo luồng nghiệp vụ thực tế, mỗi case có mức ưu tiên P1 (bắt buộc pass) / P2 (quan trọng) / P3 (nice-to-have).

---

## Module 1 — Xác thực & Phân quyền

| ID | Test Case | Input | Kết quả mong đợi | P |
|----|-----------|-------|-----------------|---|
| A01 | Đăng nhập đúng — QUANLY | user: `admin`, pass đúng | Vào MainGUI, menu hiện: Dashboard, Danh mục, Lịch làm việc, Khuyến mãi, Hóa đơn, Nhân viên, Màn hình bếp | P1 |
| A02 | Đăng nhập đúng — NHANVIEN | user: `nv01`, pass đúng | Vào MainGUI, menu hiện: Dashboard, Danh sách bàn, Thành viên, Lịch làm việc, Hóa đơn, Màn hình bếp | P1 |
| A03 | Đăng nhập đúng — BEP | user: `bep01`, pass đúng | Vào MainGUI chỉ có Màn hình bếp | P1 |
| A04 | Sai mật khẩu | pass sai | Dialog lỗi "Sai tên tài khoản hoặc mật khẩu" | P1 |
| A05 | Tài khoản bị khóa | tài khoản trangThai=0 | Dialog "Tài khoản bị tạm ngưng" | P1 |
| A06 | Nhấn Enter tại ô username | Gõ tên, Enter | Focus chuyển sang ô password | P2 |
| A07 | Nhấn Enter tại ô password | Gõ pass, Enter | Kích hoạt đăng nhập (giống click nút) | P2 |
| A08 | Đăng xuất khi chưa kết ca (NHANVIEN) | Click Đăng xuất | Cảnh báo "Chưa kết thúc ca", điều hướng về Dashboard | P1 |
| A09 | Đăng xuất bình thường | Click Đăng xuất → Xác nhận | Đóng MainGUI, quay về TaiKhoanGUI | P1 |
| A10 | Quên mật khẩu | Click "Quên mật khẩu?" | Mở ForgotPasswordDialog | P2 |

---

## Module 2 — Quản lý Bàn

| ID | Test Case | Input | Kết quả mong đợi | P |
|----|-----------|-------|-----------------|---|
| B01 | Hiển thị danh sách bàn | Vào tab Bàn | Tất cả bàn hiện đúng màu: xanh=Trống, đỏ=Đang phục vụ, vàng=Đặt trước | P1 |
| B02 | Lọc theo khu vực | Click tab "Tầng 1" | Chỉ hiện bàn thuộc Tầng 1 | P2 |
| B03 | Chọn bàn TRONG → xem chi tiết | Click bàn Trống | Panel phải hiện thông tin bàn, trạng thái Trống | P1 |
| B04 | Chọn bàn DANG_PHUC_VU | Click bàn đang phục vụ | Hiện thông tin khách: giờ vào, số điện thoại, tên, hạng thành viên | P1 |
| B05 | Nhập SĐT khách → auto-fill tên | Nhập 10 số hợp lệ | Tên khách và hạng TV tự điền từ DB | P2 |
| B06 | Nhập SĐT chưa có trong DB | SĐT mới | Ô tên để trống, không crash | P2 |
| B07 | Nhập mã khuyến mãi hợp lệ | Mã còn hiệu lực, đủ điều kiện | Tổng tiền giảm đúng % hoặc số tiền | P1 |
| B08 | Nhập mã khuyến mãi hết hạn | Mã hết hạn | Thông báo lỗi, không áp dụng | P1 |
| B09 | Double-click bàn → chuyển tab Gọi Món | Double-click bàn bất kỳ | Tự động chuyển sang tab "Gọi Món" với bàn đó được load | P2 |
| B10 | Auto-refresh bàn (60 giây) | Chờ 60 giây | Danh sách bàn tự làm mới | P3 |

---

## Module 3 — Gọi Món

| ID | Test Case | Input | Kết quả mong đợi | P |
|----|-----------|-------|-----------------|---|
| C01 | Vào tab Gọi Món chưa chọn bàn | Click tab Gọi Món | Hiện cảnh báo "Vui lòng chọn bàn trước" | P1 |
| C02 | Chọn bàn TRONG → vào Gọi Món | Chọn bàn Trống, click tab | Màn hình gọi món load, menu hiện đầy đủ, bàn chưa mở (lazy) | P1 |
| C03 | Thêm món đầu tiên (lazy open) | Click món bất kỳ | Bàn chuyển DANG_PHUC_VU, DonDatMon + HoaDon tạo trong DB, món xuất hiện trong bảng | P1 |
| C04 | Thêm món đã có → tăng SL | Click món đã trong bảng | Số lượng +1, thành tiền cập nhật, DB cập nhật | P1 |
| C05 | Chỉnh SL bằng spinner | Spinner lên/xuống | Thành tiền cập nhật ngay, DB ghi nhận | P1 |
| C06 | Xóa món (nút X) — còn món khác | Click X hàng | Hàng xóa khỏi bảng, DB xóa, tổng tiền cập nhật | P1 |
| C07 | Xóa món cuối cùng → dialog đóng bàn | Click X món cuối | Dialog "Đóng bàn?" xuất hiện | P1 |
| C08 | Dialog đóng bàn → chọn YES | Click Yes | Bàn về TRONG, HoaDon+DonDatMon+ChiTietHoaDon xóa khỏi DB, UI reset | P1 |
| C09 | Dialog đóng bàn → chọn NO | Click No | Bàn vẫn DANG_PHUC_VU, có thể gọi thêm | P1 |
| C10 | Tìm kiếm món ăn | Gõ tên món | Danh sách lọc real-time, không phân biệt hoa thường | P2 |
| C11 | Lọc theo danh mục | Click nút danh mục | Chỉ hiện món thuộc danh mục đó | P2 |
| C12 | Gọi món bàn ĐÃ ĐẶT TRƯỚC | Click bàn DA_DAT_TRUOC → Gọi Món | Dialog "Nhận bàn?" → Yes → Bàn chuyển DANG_PHUC_VU | P1 |
| C13 | Nút "Gửi bếp" | Click Gửi bếp | Socket ORDER_UPDATED gửi đi, nút hiện "Đã gửi!" 1.5s | P1 |

---

## Module 4 — Đặt Bàn Trước

| ID | Test Case | Input | Kết quả mong đợi | P |
|----|-----------|-------|-----------------|---|
| D01 | Đặt bàn hợp lệ — khách mới | SĐT mới, tên, ngày/giờ tương lai, chọn bàn | DonDatMon tạo, bàn chuyển DA_DAT_TRUOC, xuất hiện trong danh sách | P1 |
| D02 | Đặt bàn — khách cũ (SĐT đã có) | SĐT đã có trong DB | Tên tự điền từ DB, đặt thành công | P1 |
| D03 | Đặt bàn trùng giờ (±2h) | Bàn đã có đặt chỗ trong khung ±2h | Cảnh báo bàn đã được đặt, không cho phép | P1 |
| D04 | Đặt bàn giờ quá khứ | Ngày/giờ < hiện tại | Validate thất bại, hiện lỗi | P1 |
| D05 | SĐT sai định dạng | 9 số / chữ | Lỗi "SĐT không hợp lệ" | P1 |
| D06 | Đặt nhiều bàn (ghép) | Chọn 2+ bàn | DonDatMon có ghiChu LINKED cho bàn phụ | P2 |
| D07 | Hủy đặt bàn | Click xóa trên card đặt chỗ | DonDatMon xóa, bàn về TRONG | P1 |
| D08 | Tìm kiếm đặt chỗ theo SĐT | Nhập SĐT | Lọc đúng đặt chỗ của khách đó | P2 |

---

## Module 5 — Màn Hình Bếp

| ID | Test Case | Input | Kết quả mong đợi | P |
|----|-----------|-------|-----------------|---|
| E01 | Hiển thị món đang chờ | Có món trangThaiMon='Chờ' trong DB | Card bàn hiện đúng tên món và số lượng delta | P1 |
| E02 | Xác nhận món | Click "Đã lên" trên item | DB cập nhật trangThaiMon='Đã lên', soLuongDaXacNhan=soLuong, card cập nhật | P1 |
| E03 | Nhận socket ORDER_UPDATED | Nhân viên thêm món | Màn bếp tự động refresh, hiện món mới | P1 |
| E04 | Nhận socket HOA_DON_THANH_TOAN | Bàn thanh toán | Màn bếp xóa card bàn đó | P1 |
| E05 | Auto-refresh 30 giây | Chờ 30s | Cards cập nhật tự động | P3 |
| E06 | Nút Refresh thủ công | Click Refresh | Cards cập nhật ngay lập tức | P2 |
| E07 | Không có món chờ | DB không có món chờ | Hiện trạng thái rỗng (không crash) | P1 |

---

## Module 6 — Thanh Toán & Hóa Đơn

| ID | Test Case | Input | Kết quả mong đợi | P |
|----|-----------|-------|-----------------|---|
| F01 | Thanh toán tiền mặt | Nhập tiền khách ≥ tổng tiền | Tính tiền thối đúng, HoaDon trangThai='Đã thanh toán', bàn về TRONG | P1 |
| F02 | Thanh toán tiền mặt không đủ | Nhập tiền khách < tổng | Hiện thiếu X đồng, không cho thanh toán | P1 |
| F03 | Thanh toán chuyển khoản | Chọn hình thức CK | Thanh toán thành công, ghi nhận đúng hình thức | P2 |
| F04 | Thanh toán có giảm giá thành viên | Khách hạng GOLD | Tổng thanh toán = tổng - 5%, lưu DB | P1 |
| F05 | Thanh toán có mã KM | Mã hợp lệ đủ điều kiện | Tổng giảm đúng theo loại KM (% hoặc cố định) | P1 |
| F06 | In tạm tính | Click "In tạm tính" | Preview in hiện ra (không crash) | P2 |
| F07 | Màn hình Hóa đơn — load khi truy cập | Click menu Hóa đơn | Dữ liệu load mới nhất (đã fix refresh) | P1 |
| F08 | Lọc hóa đơn theo ngày | Chọn khoảng ngày | Chỉ hiện HĐ trong khoảng đó | P1 |
| F09 | Lọc hóa đơn hôm nay | Click "Hôm nay" | Chỉ hiện HĐ ngày hiện tại | P2 |
| F10 | Tìm kiếm hóa đơn theo mã | Nhập mã HĐ | Lọc đúng | P2 |
| F11 | Phân trang hóa đơn | Click Next/Prev | Chuyển trang đúng, 15 items/trang | P2 |
| F12 | Export Excel | Click Export | File .xlsx tải về đúng dữ liệu | P3 |

---

## Module 7 — Ghép Bàn & Chuyển Bàn

| ID | Test Case | Input | Kết quả mong đợi | P |
|----|-----------|-------|-----------------|---|
| G01 | Ghép bàn 2 bàn đang phục vụ | Chọn bàn A + B, B là chính | Món từ A gộp vào B, bàn A có ghiChu LINKED:B, cả 2 chuyển DANG_PHUC_VU | P1 |
| G02 | Tên hiển thị sau ghép | Mở Gọi Món bàn đã ghép | Header hiện "Bàn B + A" | P2 |
| G03 | Chuyển bàn | Chọn bàn nguồn + bàn đích (Trống) | DonDatMon.maBan đổi sang bàn đích, bàn nguồn về TRONG | P1 |
| G04 | Chuyển bàn — đích đang phục vụ | Chọn bàn đích đang có khách | Không cho phép / cảnh báo | P1 |
| G05 | Gọi món sau khi ghép | Thêm món vào bàn ghép | Món vào đúng maDon của bàn chính | P1 |

---

## Module 8 — Quản lý Khách Hàng

| ID | Test Case | Input | Kết quả mong đợi | P |
|----|-----------|-------|-----------------|---|
| H01 | Hiển thị danh sách KH | Vào tab Thành viên | Tất cả KH hiện đúng thông tin | P1 |
| H02 | Tìm kiếm KH theo SĐT | Nhập SĐT | Lọc đúng KH | P1 |
| H03 | Tìm kiếm KH theo tên | Nhập tên | Lọc đúng (case-insensitive) | P2 |
| H04 | Thêm KH mới | Điền form đầy đủ | KH lưu vào DB, hiện trong danh sách | P1 |
| H05 | Cập nhật KH | Sửa thông tin → Save | DB cập nhật đúng | P1 |
| H06 | Tổng chi tiêu ảnh hưởng hạng | tongChiTieu > 5M | Hạng tự động cập nhật (BRONZE/SILVER/...) | P2 |

---

## Module 9 — Quản lý Nhân Viên

| ID | Test Case | Input | Kết quả mong đợi | P |
|----|-----------|-------|-----------------|---|
| I01 | Hiển thị danh sách NV | Vào tab Nhân Viên | Tất cả NV hiện đúng | P1 |
| I02 | Thêm NV mới | Điền form đầy đủ, hợp lệ | NV lưu DB, tài khoản tạo | P1 |
| I03 | Validate SĐT NV | Không phải 10 số | Lỗi "SĐT không hợp lệ" | P1 |
| I04 | Validate ngày sinh | Sai định dạng dd/MM/yyyy | Lỗi parse | P2 |
| I05 | Tìm kiếm NV | Nhập tên/SĐT | Lọc đúng | P2 |

---

## Module 10 — Quản lý Thực Đơn

| ID | Test Case | Input | Kết quả mong đợi | P |
|----|-----------|-------|-----------------|---|
| J01 | Hiển thị món ăn | Vào Danh mục món ăn | Tất cả món hiện với ảnh, tên, giá | P1 |
| J02 | Lọc theo danh mục | Click tab danh mục | Chỉ hiện món của danh mục đó | P2 |
| J03 | Tìm kiếm món | Gõ từ khóa | Lọc real-time | P2 |
| J04 | Thêm món mới | Điền form + ảnh | Món xuất hiện trong menu gọi món | P1 |
| J05 | Cập nhật giá | Đổi giá món | Gọi món ngay sau đó dùng giá mới | P1 |
| J06 | Ẩn món (trangThai=Hết) | Đổi trạng thái | Món không hiện trong màn gọi món | P1 |
| J07 | Socket MENU_UPDATED | Thêm/sửa/ẩn món từ máy khác | Màn gọi món tự cập nhật real-time | P1 |

---

## Module 11 — Khuyến Mãi

| ID | Test Case | Input | Kết quả mong đợi | P |
|----|-----------|-------|-----------------|---|
| K01 | Hiển thị KM | Vào Khuyến mãi | Tất cả KM với trạng thái đúng | P1 |
| K02 | Thêm KM % | Tạo KM giảm 10%, điều kiện 200K | KM lưu DB | P1 |
| K03 | Thêm KM cố định | Tạo KM giảm 50K | KM lưu DB | P1 |
| K04 | Áp KM đủ điều kiện | Đơn ≥ điều kiện, mã đúng | Giảm đúng số tiền | P1 |
| K05 | Áp KM chưa đủ điều kiện | Đơn < điều kiện | Thông báo "Chưa đủ điều kiện" | P1 |
| K06 | KM hết hạn | Mã quá ngày kết thúc | Thông báo "Đã hết hạn" | P1 |

---

## Module 12 — Ca Làm Việc & Dashboard

| ID | Test Case | Input | Kết quả mong đợi | P |
|----|-----------|-------|-----------------|---|
| L01 | Bắt đầu ca | Nhập tiền đầu ca, click Bắt đầu | GiaoCa tạo DB, nút đổi thành Kết thúc ca | P1 |
| L02 | Kết thúc ca | Nhập tiền cuối ca, click Kết thúc | GiaoCa cập nhật, tính chênh lệch | P1 |
| L03 | Đăng xuất chưa kết ca | Click Đăng xuất | Cảnh báo, không cho logout | P1 |
| L04 | Dashboard QUANLY — doanh thu | Chọn khoảng ngày | Biểu đồ và số liệu đúng | P2 |
| L05 | Dashboard QUANLY — top món | Xem tuần | Hiện top 3 món bán chạy | P2 |
| L06 | Dashboard NV — thống kê cá nhân | Xem Dashboard | Hiện giờ làm, doanh thu ca hiện tại | P2 |

---

## Module 13 — Socket Real-time Sync

| ID | Test Case | Cách kiểm thử | Kết quả mong đợi | P |
|----|-----------|--------------|-----------------|---|
| M01 | Thêm món → bếp nhận | Máy 1: thêm món → Gửi bếp | Máy 2 (bếp): card mới xuất hiện | P1 |
| M02 | Bếp xác nhận → đồng bộ | Máy 2 (bếp): click Đã lên | Máy 1: delta món giảm đúng | P1 |
| M03 | Thanh toán → bếp xóa card | Máy 1: thanh toán | Máy 2 (bếp): card bàn đó biến mất | P1 |
| M04 | Trạng thái bàn đồng bộ | Máy 1: mở bàn | Máy 2: bàn đó đổi màu đỏ | P1 |
| M05 | Mất kết nối → auto-reconnect | Tắt server/mạng 10s | Client tự kết nối lại, không crash | P1 |
| M06 | Thêm/sửa món → menu đồng bộ | Máy 1 (QL): cập nhật menu | Máy 2 (NV): màn gọi món refresh | P2 |

---

## Module 14 — Edge Cases & Xử lý Lỗi

| ID | Test Case | Input | Kết quả mong đợi | P |
|----|-----------|-------|-----------------|---|
| N01 | Mất kết nối DB | Tắt MariaDB | Hiện lỗi thân thiện, không crash | P1 |
| N02 | Bàn DANG_PHUC_VU không có HoaDon | Trạng thái không nhất quán | Dialog phục hồi: Tạo mới / Đặt về Trống / Hủy | P1 |
| N03 | Mở cùng bàn trên 2 máy | 2 máy cùng mở bàn TRONG | Socket đồng bộ, máy sau load đúng | P2 |
| N04 | Spinner SL = 1 → giảm | Nhấn mũi tên xuống | Không xuống dưới 1 | P1 |
| N05 | Đóng app giữa chừng | Tắt app khi bàn đang phục vụ | DB giữ nguyên trạng thái, mở lại tiếp tục được | P1 |
| N06 | Tên món có ký tự đặc biệt | Tên chứa `'`, `"`, `%` | Lưu và hiển thị đúng (không SQL injection) | P1 |

---

## Checklist Trước Demo

```
[ ] Tất cả P1 pass
[ ] DB sạch (không có DonDatMon/HoaDon orphan)
[ ] Test với 2 máy kết nối socket
[ ] Test tài khoản: admin (QUANLY), nv01 (NHANVIEN), bep01 (BEP)
[ ] Kiểm tra log console: không có stack trace
```

---

## Thứ tự thực hiện kiểm thử

```
1. Module 1  — Login (chạy app được)
2. Module 2  — Bàn (thấy trạng thái đúng)
3. Module 3  — Gọi món (luồng cốt lõi)
4. Module 6  — Thanh toán (kết thúc vòng đời bàn)
5. Module 5  — Bếp (kết hợp với Module 3)
6. Module 13 — Socket (cần 2 máy)
7. Module 4  — Đặt bàn
8. Module 7  — Ghép/Chuyển bàn
9. Module 8-12 — Các module phụ
10. Module 14 — Edge cases
```
