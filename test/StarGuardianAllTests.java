import entity.*;
import org.junit.jupiter.api.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 107 Unit Tests — StarGuardian Restaurant
 * Test business logic của entity (không cần kết nối DB).
 *
 * Chạy: mvn test -Ptest
 */
@DisplayName("StarGuardian — 107 Unit Tests")
class StarGuardianAllTests {

    // ══════════════════════════════════════════════════════════
    // MODULE 1: NhanVien — 20 tests
    // ══════════════════════════════════════════════════════════
    @Nested @DisplayName("Module 1 — NhanVien (20)")
    class NhanVienTest {

        private NhanVien nv;

        @BeforeEach void setup() {
            nv = new NhanVien("Nguyễn Văn An",
                    LocalDate.now().minusYears(25),
                    "Nam", "0901234567", "Hà Nội",
                    LocalDate.now().minusYears(2),
                    8_000_000, VaiTro.NHANVIEN, "an@test.com");
        }

        @Test @DisplayName("NV01 — maNV sinh tự động, không rỗng")
        void NV01() { assertNotNull(nv.getManv()); assertFalse(nv.getManv().isEmpty()); }

        @Test @DisplayName("NV02 — maNV có prefix NV01 (NHANVIEN)")
        void NV02() { assertTrue(nv.getManv().startsWith("NV01")); }

        @Test @DisplayName("NV03 — maNV quản lý có prefix NV02")
        void NV03() {
            NhanVien ql = new NhanVien("Trần Thị B",
                    LocalDate.now().minusYears(30), "Nữ", "0912345678",
                    "HCM", LocalDate.now().minusYears(3), 15_000_000, VaiTro.QUANLY, "b@test.com");
            assertTrue(ql.getManv().startsWith("NV02"));
        }

        @Test @DisplayName("NV04 — setHoTen hợp lệ không ném exception")
        void NV04() { assertDoesNotThrow(() -> nv.setHoten("Trần Thị Bình")); }

        @Test @DisplayName("NV05 — setHoTen null → IllegalArgumentException")
        void NV05() { assertThrows(IllegalArgumentException.class, () -> nv.setHoten(null)); }

        @Test @DisplayName("NV06 — setHoTen rỗng → IllegalArgumentException")
        void NV06() { assertThrows(IllegalArgumentException.class, () -> nv.setHoten("")); }

        @Test @DisplayName("NV07 — setHoTen chứa số → IllegalArgumentException")
        void NV07() { assertThrows(IllegalArgumentException.class, () -> nv.setHoten("Nguyen123")); }

        @Test @DisplayName("NV08 — setNgaySinh đủ 18 tuổi → hợp lệ")
        void NV08() {
            assertDoesNotThrow(() -> nv.setNgaysinh(LocalDate.now().minusYears(18).minusDays(1)));
        }

        @Test @DisplayName("NV09 — setNgaySinh dưới 18 → IllegalArgumentException")
        void NV09() {
            assertThrows(IllegalArgumentException.class,
                    () -> nv.setNgaysinh(LocalDate.now().minusYears(17)));
        }

        @Test @DisplayName("NV10 — setNgaySinh null → IllegalArgumentException")
        void NV10() { assertThrows(IllegalArgumentException.class, () -> nv.setNgaysinh(null)); }

        @Test @DisplayName("NV11 — setSdt 10 số bắt đầu 0 → hợp lệ")
        void NV11() { assertDoesNotThrow(() -> nv.setSdt("0987654321")); }

        @Test @DisplayName("NV12 — setSdt null → IllegalArgumentException")
        void NV12() { assertThrows(IllegalArgumentException.class, () -> nv.setSdt(null)); }

        @Test @DisplayName("NV13 — setSdt 9 chữ số → IllegalArgumentException")
        void NV13() { assertThrows(IllegalArgumentException.class, () -> nv.setSdt("090123456")); }

        @Test @DisplayName("NV14 — setSdt không bắt đầu 0 → IllegalArgumentException")
        void NV14() { assertThrows(IllegalArgumentException.class, () -> nv.setSdt("1901234567")); }

        @Test @DisplayName("NV15 — setSdt chứa chữ → IllegalArgumentException")
        void NV15() { assertThrows(IllegalArgumentException.class, () -> nv.setSdt("090abc4567")); }

        @Test @DisplayName("NV16 — setLuong > 0 → hợp lệ")
        void NV16() { assertDoesNotThrow(() -> nv.setLuong(5_000_000)); }

        @Test @DisplayName("NV17 — setLuong = 0 → IllegalArgumentException")
        void NV17() { assertThrows(IllegalArgumentException.class, () -> nv.setLuong(0)); }

        @Test @DisplayName("NV18 — setLuong âm → IllegalArgumentException")
        void NV18() { assertThrows(IllegalArgumentException.class, () -> nv.setLuong(-1)); }

        @Test @DisplayName("NV19 — setEmail hợp lệ → không ném exception")
        void NV19() { assertDoesNotThrow(() -> nv.setEmail("valid@email.com")); }

        @Test @DisplayName("NV20 — setEmail không có @ → IllegalArgumentException")
        void NV20() { assertThrows(IllegalArgumentException.class, () -> nv.setEmail("invalidemail")); }
    }

    // ══════════════════════════════════════════════════════════
    // MODULE 2: KhachHang — 16 tests
    // ══════════════════════════════════════════════════════════
    @Nested @DisplayName("Module 2 — KhachHang (16)")
    class KhachHangTest {

        private KhachHang kh;

        @BeforeEach void setup() { kh = new KhachHang(); }

        @Test @DisplayName("KH01 — maKH sinh tự động không rỗng")
        void KH01() { assertNotNull(kh.getMaKH()); assertFalse(kh.getMaKH().isEmpty()); }

        @Test @DisplayName("KH02 — hangThanhVien mặc định MEMBER")
        void KH02() { assertEquals(HangThanhVien.MEMBER, kh.getHangThanhVien()); }

        @Test @DisplayName("KH03 — tongChiTieu mặc định = 0")
        void KH03() { assertEquals(0.0, kh.getTongChiTieu()); }

        @Test @DisplayName("KH04 — setTongChiTieu âm → IllegalArgumentException")
        void KH04() { assertThrows(IllegalArgumentException.class, () -> kh.setTongChiTieu(-1)); }

        @Test @DisplayName("KH05 — setTongChiTieu = 0 → hợp lệ")
        void KH05() { assertDoesNotThrow(() -> kh.setTongChiTieu(0)); }

        @Test @DisplayName("KH06 — tongChiTieu < 5M → vẫn MEMBER (không hạ cấp)")
        void KH06() { kh.setTongChiTieu(4_999_999); assertEquals(HangThanhVien.MEMBER, kh.getHangThanhVien()); }

        @Test @DisplayName("KH07 — tongChiTieu > 5M → BRONZE")
        void KH07() { kh.setTongChiTieu(5_000_001); assertEquals(HangThanhVien.BRONZE, kh.getHangThanhVien()); }

        @Test @DisplayName("KH08 — tongChiTieu = 5M chính xác → vẫn MEMBER")
        void KH08() { kh.setTongChiTieu(5_000_000); assertEquals(HangThanhVien.MEMBER, kh.getHangThanhVien()); }

        @Test @DisplayName("KH09 — tongChiTieu > 10M → SILVER")
        void KH09() { kh.setTongChiTieu(10_000_001); assertEquals(HangThanhVien.SILVER, kh.getHangThanhVien()); }

        @Test @DisplayName("KH10 — tongChiTieu > 25M → GOLD")
        void KH10() { kh.setTongChiTieu(25_000_001); assertEquals(HangThanhVien.GOLD, kh.getHangThanhVien()); }

        @Test @DisplayName("KH11 — tongChiTieu > 50M → DIAMOND")
        void KH11() { kh.setTongChiTieu(50_000_001); assertEquals(HangThanhVien.DIAMOND, kh.getHangThanhVien()); }

        @Test @DisplayName("KH12 — tongChiTieu = 50M → vẫn GOLD (chưa đủ DIAMOND)")
        void KH12() { kh.setTongChiTieu(50_000_000); assertEquals(HangThanhVien.GOLD, kh.getHangThanhVien()); }

        @Test @DisplayName("KH13 — capNhatTongChiTieu cộng dồn đúng")
        void KH13() {
            kh.setTongChiTieu(1_000_000);
            kh.capNhatTongChiTieu(2_000_000);
            assertEquals(3_000_000, kh.getTongChiTieu(), 0.01);
        }

        @Test @DisplayName("KH14 — capNhatTongChiTieu vượt ngưỡng → nâng hạng")
        void KH14() {
            kh.setTongChiTieu(4_000_000);
            kh.capNhatTongChiTieu(2_000_000);
            assertEquals(HangThanhVien.BRONZE, kh.getHangThanhVien());
        }

        @Test @DisplayName("KH15 — setSdt sai định dạng → IllegalArgumentException")
        void KH15() { assertThrows(IllegalArgumentException.class, () -> kh.setSdt("12345")); }

        @Test @DisplayName("KH16 — setSdt hợp lệ → không ném exception")
        void KH16() { assertDoesNotThrow(() -> kh.setSdt("0933445566")); }
    }

    // ══════════════════════════════════════════════════════════
    // MODULE 3: Ban — 10 tests
    // ══════════════════════════════════════════════════════════
    @Nested @DisplayName("Module 3 — Ban (10)")
    class BanTest {

        private Ban ban;

        @BeforeEach void setup() {
            ban = new Ban("Bàn 01", 4, TrangThaiBan.TRONG, null, "Tầng trệt");
        }

        @Test @DisplayName("BAN01 — maBan sinh tự động có prefix BAN")
        void BAN01() { assertTrue(ban.getMaBan().startsWith("BAN")); }

        @Test @DisplayName("BAN02 — trangThai mặc định TRONG")
        void BAN02() { assertEquals(TrangThaiBan.TRONG, new Ban().getTrangThai()); }

        @Test @DisplayName("BAN03 — setSoGhe > 0 → hợp lệ")
        void BAN03() { assertDoesNotThrow(() -> ban.setSoGhe(6)); }

        @Test @DisplayName("BAN04 — setSoGhe = 0 → IllegalArgumentException")
        void BAN04() { assertThrows(IllegalArgumentException.class, () -> ban.setSoGhe(0)); }

        @Test @DisplayName("BAN05 — setSoGhe âm → IllegalArgumentException")
        void BAN05() { assertThrows(IllegalArgumentException.class, () -> ban.setSoGhe(-1)); }

        @Test @DisplayName("BAN06 — setTenBan null → IllegalArgumentException")
        void BAN06() { assertThrows(IllegalArgumentException.class, () -> ban.setTenBan(null)); }

        @Test @DisplayName("BAN07 — setTenBan rỗng → IllegalArgumentException")
        void BAN07() { assertThrows(IllegalArgumentException.class, () -> ban.setTenBan("")); }

        @Test @DisplayName("BAN08 — setTenBan hợp lệ → không ném exception")
        void BAN08() { assertDoesNotThrow(() -> ban.setTenBan("Bàn VIP 01")); }

        @Test @DisplayName("BAN09 — setKhuVuc null → 'Chưa phân loại'")
        void BAN09() { ban.setKhuVuc(null); assertEquals("Chưa phân loại", ban.getKhuVuc()); }

        @Test @DisplayName("BAN10 — setKhuVuc rỗng → 'Chưa phân loại'")
        void BAN10() { ban.setKhuVuc(""); assertEquals("Chưa phân loại", ban.getKhuVuc()); }
    }

    // ══════════════════════════════════════════════════════════
    // MODULE 4: TaiKhoan — 5 tests
    // ══════════════════════════════════════════════════════════
    @Nested @DisplayName("Module 4 — TaiKhoan (5)")
    class TaiKhoanTest {

        @Test @DisplayName("TK01 — Constructor lưu tenTK đúng")
        void TK01() {
            TaiKhoan tk = new TaiKhoan("admin", "admin123", VaiTro.QUANLY, true);
            assertEquals("admin", tk.getTentk());
        }

        @Test @DisplayName("TK02 — trangThai = true (hoạt động)")
        void TK02() {
            assertTrue(new TaiKhoan("nv01", "password1", VaiTro.NHANVIEN, true).getTrangthai());
        }

        @Test @DisplayName("TK03 — trangThai = false (bị khóa)")
        void TK03() {
            assertFalse(new TaiKhoan("nv02", "password1", VaiTro.NHANVIEN, false).getTrangthai());
        }

        @Test @DisplayName("TK04 — matKhau được lưu và truy xuất được")
        void TK04() {
            TaiKhoan tk = new TaiKhoan("user1", "mypassword", VaiTro.NHANVIEN, true);
            assertNotNull(tk.getMatkhau());
            assertFalse(tk.getMatkhau().isEmpty());
        }

        @Test @DisplayName("TK05 — hai tài khoản khác nhau, cùng mật khẩu → matKhau như nhau")
        void TK05() {
            TaiKhoan tk1 = new TaiKhoan("user1", "pass1234", VaiTro.NHANVIEN, true);
            TaiKhoan tk2 = new TaiKhoan("user2", "pass1234", VaiTro.NHANVIEN, true);
            assertEquals(tk1.getMatkhau(), tk2.getMatkhau());
        }
    }

    // ══════════════════════════════════════════════════════════
    // MODULE 5: HoaDon — 10 tests
    // ══════════════════════════════════════════════════════════
    @Nested @DisplayName("Module 5 — HoaDon (10)")
    class HoaDonTest {

        private HoaDon hd;

        @BeforeEach void setup() { hd = new HoaDon(); }

        @Test @DisplayName("HD01 — maHD sinh tự động không rỗng")
        void HD01() { assertNotNull(hd.getMaHD()); assertFalse(hd.getMaHD().isEmpty()); }

        @Test @DisplayName("HD02 — trangThai mặc định 'Chưa thanh toán'")
        void HD02() { assertEquals("Chưa thanh toán", hd.getTrangThai()); }

        @Test @DisplayName("HD03 — giamGia mặc định 0")
        void HD03() { assertEquals(0.0, hd.getGiamGia(), 0.01); }

        @Test @DisplayName("HD04 — ngayLap mặc định không null")
        void HD04() { assertNotNull(hd.getNgayLap()); }

        @Test @DisplayName("HD05 — tinhTienThoi: khách đưa đủ → tiền thối đúng")
        void HD05() {
            hd.setTongTienTuDB(150_000);
            hd.setGiamGia(0);
            hd.capNhatTongThanhToanTuCacThanhPhan();
            hd.setTienKhachDua(200_000);
            assertEquals(50_000, hd.tinhTienThoi(), 0.01);
        }

        @Test @DisplayName("HD06 — tinhTienThoi: khách đưa thiếu → trả về 0")
        void HD06() {
            hd.setTongTienTuDB(200_000);
            hd.setGiamGia(0);
            hd.capNhatTongThanhToanTuCacThanhPhan();
            hd.setTienKhachDua(100_000);
            assertEquals(0, hd.tinhTienThoi(), 0.01);
        }

        @Test @DisplayName("HD07 — capNhatTongThanhToan: tongTien - giamGia")
        void HD07() {
            hd.setTongTienTuDB(200_000);
            hd.setGiamGia(20_000);
            hd.capNhatTongThanhToanTuCacThanhPhan();
            assertEquals(180_000, hd.getTongThanhToan(), 0.01);
        }

        @Test @DisplayName("HD08 — setGiamGia âm → được xử lý về 0")
        void HD08() {
            hd.setGiamGia(-5_000);
            assertEquals(0, hd.getGiamGia(), 0.01);
        }

        @Test @DisplayName("HD09 — tinhTienThoi khách trả đúng bằng tổng → thối 0")
        void HD09() {
            hd.setTongTienTuDB(100_000);
            hd.setGiamGia(0);
            hd.capNhatTongThanhToanTuCacThanhPhan();
            hd.setTienKhachDua(100_000);
            assertEquals(0, hd.tinhTienThoi(), 0.01);
        }

        @Test @DisplayName("HD10 — giamGia > tongTien → tongThanhToan không âm")
        void HD10() {
            hd.setTongTienTuDB(50_000);
            hd.setGiamGia(100_000);
            hd.capNhatTongThanhToanTuCacThanhPhan();
            assertTrue(hd.getTongThanhToan() >= 0);
        }
    }

    // ══════════════════════════════════════════════════════════
    // MODULE 6: ChiTietHoaDon — 10 tests
    // ══════════════════════════════════════════════════════════
    @Nested @DisplayName("Module 6 — ChiTietHoaDon (10)")
    class ChiTietHoaDonTest {

        @Test @DisplayName("CT01 — Constructor: thanhtien = soLuong × donGia")
        void CT01() {
            ChiTietHoaDon ct = new ChiTietHoaDon("MON001", "DON001", 3, 75_000.0);
            assertEquals(225_000.0, ct.getThanhtien(), 0.01);
        }

        @Test @DisplayName("CT02 — trangThaiMon mặc định 'Chờ'")
        void CT02() {
            assertEquals("Chờ", new ChiTietHoaDon("MON001", "DON001", 1, 50_000.0).getTrangThaiMon());
        }

        @Test @DisplayName("CT03 — setTrangThaiMon cập nhật đúng")
        void CT03() {
            ChiTietHoaDon ct = new ChiTietHoaDon("MON001", "DON001", 2, 60_000.0);
            ct.setTrangThaiMon("Đã lên");
            assertEquals("Đã lên", ct.getTrangThaiMon());
        }

        @Test @DisplayName("CT04 — donGia lưu đúng")
        void CT04() {
            assertEquals(80_000.0, new ChiTietHoaDon("MON002", "DON001", 1, 80_000.0).getDongia(), 0.01);
        }

        @Test @DisplayName("CT05 — soLuong lưu đúng")
        void CT05() {
            assertEquals(5, new ChiTietHoaDon("MON003", "DON001", 5, 30_000.0).getSoluong());
        }

        @Test @DisplayName("CT06 — soLuong < 1 → IllegalArgumentException")
        void CT06() {
            assertThrows(IllegalArgumentException.class,
                    () -> new ChiTietHoaDon("MON001", "DON001", 0, 50_000.0));
        }

        @Test @DisplayName("CT07 — thanhtien với donGia thập phân")
        void CT07() {
            ChiTietHoaDon ct = new ChiTietHoaDon("MON001", "DON001", 4, 12_500.5);
            assertEquals(50_002.0, ct.getThanhtien(), 1.0);
        }

        @Test @DisplayName("CT08 — maMon lưu đúng")
        void CT08() {
            assertEquals("MON005", new ChiTietHoaDon("MON005", "DON002", 1, 25_000.0).getMaMon());
        }

        @Test @DisplayName("CT09 — maDon lưu đúng")
        void CT09() {
            assertEquals("DON999", new ChiTietHoaDon("MON001", "DON999", 1, 50_000.0).getMaDon());
        }

        @Test @DisplayName("CT10 — Copy constructor sao chép đúng thanhtien")
        void CT10() {
            ChiTietHoaDon ct1 = new ChiTietHoaDon("MON001", "DON001", 3, 75_000.0);
            ChiTietHoaDon ct2 = new ChiTietHoaDon(ct1);
            assertEquals(ct1.getThanhtien(), ct2.getThanhtien(), 0.01);
        }
    }

    // ══════════════════════════════════════════════════════════
    // MODULE 7: MonAn — 8 tests
    // ══════════════════════════════════════════════════════════
    @Nested @DisplayName("Module 7 — MonAn (8)")
    class MonAnTest {

        private MonAn mon;

        @BeforeEach void setup() {
            mon = new MonAn("MON001", "Cơm sườn nướng", "Ngon", 75_000.0,
                    "Phần", "Còn", null, "DM001");
        }

        @Test @DisplayName("MA01 — maMonAn lưu đúng")
        void MA01() { assertEquals("MON001", mon.getMaMonAn()); }

        @Test @DisplayName("MA02 — tenMon lưu đúng")
        void MA02() { assertEquals("Cơm sườn nướng", mon.getTenMon()); }

        @Test @DisplayName("MA03 — donGia lưu đúng")
        void MA03() { assertEquals(75_000.0, mon.getDonGia(), 0.01); }

        @Test @DisplayName("MA04 — trangThai lưu đúng")
        void MA04() { assertEquals("Còn", mon.getTrangThai()); }

        @Test @DisplayName("MA05 — setTrangThai 'Hết' → cập nhật đúng")
        void MA05() { mon.setTrangThai("Hết"); assertEquals("Hết", mon.getTrangThai()); }

        @Test @DisplayName("MA06 — hinhAnh cho phép null")
        void MA06() { assertNull(mon.getHinhAnh()); }

        @Test @DisplayName("MA07 — maDM lưu đúng")
        void MA07() { assertEquals("DM001", mon.getMaDM()); }

        @Test @DisplayName("MA08 — setDonGia cập nhật giá mới")
        void MA08() { mon.setDonGia(90_000.0); assertEquals(90_000.0, mon.getDonGia(), 0.01); }
    }

    // ══════════════════════════════════════════════════════════
    // MODULE 8: KhuyenMai — 8 tests
    // ══════════════════════════════════════════════════════════
    @Nested @DisplayName("Module 8 — KhuyenMai (8)")
    class KhuyenMaiTest {

        private KhuyenMai km;

        @BeforeEach void setup() {
            km = new KhuyenMai("KM001", "Giảm 10%", "", "Phần trăm",
                    10.0, 200_000.0,
                    LocalDate.now(), LocalDate.now().plusDays(30),
                    "Đang áp dụng");
        }

        @Test @DisplayName("KM01 — maKM lưu đúng")
        void KM01() { assertEquals("KM001", km.getMaKM()); }

        @Test @DisplayName("KM02 — soLuotDaDung mặc định 0")
        void KM02() { assertEquals(0, km.getSoLuotDaDung()); }

        @Test @DisplayName("KM03 — soLuongGioiHan mặc định 0 (không giới hạn)")
        void KM03() { assertEquals(0, km.getSoLuongGioiHan()); }

        @Test @DisplayName("KM04 — trangThai lưu đúng")
        void KM04() { assertEquals("Đang áp dụng", km.getTrangThai()); }

        @Test @DisplayName("KM05 — setTrangThai 'Ngưng áp dụng' → cập nhật")
        void KM05() { km.setTrangThai("Ngưng áp dụng"); assertEquals("Ngưng áp dụng", km.getTrangThai()); }

        @Test @DisplayName("KM06 — setSoLuotDaDung tăng → lưu đúng")
        void KM06() { km.setSoLuotDaDung(5); assertEquals(5, km.getSoLuotDaDung()); }

        @Test @DisplayName("KM07 — setSoLuongGioiHan → lưu đúng")
        void KM07() { km.setSoLuongGioiHan(100); assertEquals(100, km.getSoLuongGioiHan()); }

        @Test @DisplayName("KM08 — dieuKienApDung lưu đúng")
        void KM08() { assertEquals(200_000.0, km.getDieuKienApDung(), 0.01); }
    }

    // ══════════════════════════════════════════════════════════
    // MODULE 9: CaLam — 6 tests
    // ══════════════════════════════════════════════════════════
    @Nested @DisplayName("Module 9 — CaLam (6)")
    class CaLamTest {

        private CaLam ca;

        @BeforeEach void setup() {
            ca = new CaLam("CA001", "Ca Sáng", LocalTime.of(7, 0), LocalTime.of(14, 0));
        }

        @Test @DisplayName("CA01 — maCa lưu đúng")
        void CA01() { assertEquals("CA001", ca.getMaCa()); }

        @Test @DisplayName("CA02 — tenCa lưu đúng")
        void CA02() { assertEquals("Ca Sáng", ca.getTenCa()); }

        @Test @DisplayName("CA03 — setTenCa hợp lệ → không ném exception")
        void CA03() { assertDoesNotThrow(() -> ca.setTenCa("Ca Tối")); }

        @Test @DisplayName("CA04 — setTenCa rỗng → IllegalArgumentException")
        void CA04() { assertThrows(IllegalArgumentException.class, () -> ca.setTenCa("")); }

        @Test @DisplayName("CA05 — gioBatDau lưu đúng")
        void CA05() { assertEquals(LocalTime.of(7, 0), ca.getGioBatDau()); }

        @Test @DisplayName("CA06 — gioKetThuc sau gioBatDau")
        void CA06() { assertTrue(ca.getGioKetThuc().isAfter(ca.getGioBatDau())); }
    }

    // ══════════════════════════════════════════════════════════
    // MODULE 10: DonDatMon — 5 tests
    // ══════════════════════════════════════════════════════════
    @Nested @DisplayName("Module 10 — DonDatMon (5)")
    class DonDatMonTest {

        private DonDatMon don;

        @BeforeEach void setup() { don = new DonDatMon(); }

        @Test @DisplayName("DDM01 — maDon sinh tự động không rỗng")
        void DDM01() { assertNotNull(don.getMaDon()); assertFalse(don.getMaDon().isEmpty()); }

        @Test @DisplayName("DDM02 — ngayKhoiTao mặc định không null")
        void DDM02() { assertNotNull(don.getNgayKhoiTao()); }

        @Test @DisplayName("DDM03 — ngayKhoiTao không ở tương lai xa")
        void DDM03() {
            assertTrue(don.getNgayKhoiTao().isBefore(LocalDateTime.now().plusSeconds(5)));
        }

        @Test @DisplayName("DDM04 — thoiGianDen mặc định null")
        void DDM04() { assertNull(don.getThoiGianDen()); }

        @Test @DisplayName("DDM05 — ghiChu mặc định rỗng hoặc null")
        void DDM05() { assertTrue(don.getGhiChu() == null || don.getGhiChu().isEmpty()); }
    }

    // ══════════════════════════════════════════════════════════
    // MODULE 11: GiaoCa — 5 tests
    // ══════════════════════════════════════════════════════════
    @Nested @DisplayName("Module 11 — GiaoCa (5)")
    class GiaoCaTest {

        private GiaoCa gc;

        @BeforeEach void setup() {
            gc = new GiaoCa();
            gc.setMaNV("NV01001");
            gc.setThoiGianBatDau(LocalDateTime.now());
            gc.setTienDauCa(500_000);
        }

        @Test @DisplayName("GC01 — maNV lưu đúng")
        void GC01() { assertEquals("NV01001", gc.getMaNV()); }

        @Test @DisplayName("GC02 — tienDauCa lưu đúng")
        void GC02() { assertEquals(500_000, gc.getTienDauCa(), 0.01); }

        @Test @DisplayName("GC03 — thoiGianKetThuc mặc định null (chưa kết ca)")
        void GC03() { assertNull(gc.getThoiGianKetThuc()); }

        @Test @DisplayName("GC04 — thoiGianBatDau được lưu đúng")
        void GC04() { assertNotNull(gc.getThoiGianBatDau()); }

        @Test @DisplayName("GC05 — chenhLech = tienCuoiCa - (tienDauCa + tienHeThongTinh)")
        void GC05() {
            gc.setTienDauCa(500_000);
            gc.setTienCuoiCa(1_200_000);
            gc.setTienHeThongTinh(650_000);
            double expected = 1_200_000 - (500_000 + 650_000);
            gc.setChenhLech(gc.getTienCuoiCa() - (gc.getTienDauCa() + gc.getTienHeThongTinh()));
            assertEquals(expected, gc.getChenhLech(), 0.01);
        }
    }

    // ══════════════════════════════════════════════════════════
    // MODULE 12: Enum — 4 tests
    // ══════════════════════════════════════════════════════════
    @Nested @DisplayName("Module 12 — Enum (4)")
    class EnumTest {

        @Test @DisplayName("EV01 — VaiTro có đủ 3 giá trị: NHANVIEN, QUANLY, BEP")
        void EV01() { assertEquals(3, VaiTro.values().length); }

        @Test @DisplayName("EV02 — HangThanhVien có đủ 6 bậc từ NONE đến DIAMOND")
        void EV02() { assertEquals(6, HangThanhVien.values().length); }

        @Test @DisplayName("EV03 — TrangThaiBan có đủ 3 trạng thái")
        void EV03() { assertEquals(3, TrangThaiBan.values().length); }

        @Test @DisplayName("EV04 — DIAMOND > GOLD về thứ tự hạng (ordinal)")
        void EV04() {
            assertTrue(HangThanhVien.DIAMOND.ordinal() > HangThanhVien.GOLD.ordinal());
        }
    }
}
