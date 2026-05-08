package entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Entity
@Table(name = "KhachHang")
public class KhachHang {

    @Id
    @Column(name = "maKH", length = 20)
    private String maKH;

    @Column(name = "tenKH", nullable = false, length = 100)
    private String tenKH;

    @Column(name = "gioiTinh", nullable = false, length = 10)
    private String gioitinh;

    @Column(name = "sdt", unique = true, length = 15)
    private String sdt;

    @Enumerated(EnumType.STRING)
    @Column(name = "hangThanhVien", nullable = false, length = 20)
    private HangThanhVien hangThanhVien;

    @Column(name = "tongChiTieu", nullable = false)
    private double tongChiTieu;

    @Column(name = "ngaySinh")
    private LocalDate ngaySinh;

    @Column(name = "diaChi", length = 255)
    private String diaChi;

    @Column(name = "ngayThamGia", nullable = false)
    private LocalDate ngayThamGia;

    @Column(name = "email", length = 100)
    private String email;

    private String generateMaKH() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");
        String datePart = LocalDate.now().format(dtf);
        int xxx = new Random().nextInt(1000);
        return "KH" + datePart + String.format("%03d", xxx);
    }

    public KhachHang() {
        this.maKH = generateMaKH();
        this.tenKH = "";
        this.gioitinh = "Nam";
        this.sdt = "";
        this.hangThanhVien = HangThanhVien.MEMBER;
        this.tongChiTieu = 0.0f;
        this.ngaySinh = LocalDate.of(2000, 1, 1);
        this.diaChi = "";
        this.ngayThamGia = LocalDate.now();
        this.email = null;
    }

    public KhachHang(String maKH, String tenKH, String gioitinh, String sdt,
                     LocalDate ngaySinh, String diaChi, String email, LocalDate ngayThamGia,
                     double tongChiTieu, HangThanhVien hangThanhVien) {
        setMaKH(maKH);
        setTenKH(tenKH);
        setGioitinh(gioitinh);
        setSdt(sdt);
        setNgaySinh(ngaySinh);
        setDiaChi(diaChi);
        setEmail(email);
        setNgayThamGia(ngayThamGia);
        setHangThanhVien(hangThanhVien);
        setTongChiTieu(tongChiTieu);
    }

    public KhachHang(KhachHang khachHang) {
        this(khachHang.maKH, khachHang.tenKH, khachHang.gioitinh, khachHang.sdt,
                khachHang.ngaySinh, khachHang.diaChi, khachHang.email, khachHang.ngayThamGia,
                khachHang.tongChiTieu, khachHang.hangThanhVien);
    }

    public KhachHang(String maKH, String tenKH, String gioitinh, String sdt, String email,
                     double tongChiTieu, HangThanhVien hang) {
        this.maKH = maKH;
        this.tenKH = tenKH;
        this.gioitinh = gioitinh;
        this.sdt = sdt;
        this.email = email;
        this.tongChiTieu = tongChiTieu;
        this.hangThanhVien = hang;
        this.ngaySinh = LocalDate.of(2005, 11, 19);
        this.diaChi = "";
        this.ngayThamGia = LocalDate.of(2019, 9, 13);
    }

    public void setMaKH(String maKH) {
        if (maKH == null || maKH.isEmpty()) { this.maKH = generateMaKH(); return; }
        this.maKH = maKH;
    }
    public void setTenKH(String tenKH) {
        if (tenKH == null || tenKH.trim().isEmpty()) throw new IllegalArgumentException("Họ tên không được rỗng");
        this.tenKH = tenKH;
    }
    public void setGioitinh(String gioitinh) {
        if (gioitinh == null || gioitinh.trim().isEmpty()) throw new IllegalArgumentException("Giới tính không được rỗng");
        this.gioitinh = gioitinh;
    }
    public void setSdt(String sdt) {
        if (sdt == null || sdt.trim().isEmpty() || !sdt.matches("\\d{10}"))
            throw new IllegalArgumentException("Số điện thoại không hợp lệ (10 ký tự số)");
        this.sdt = sdt;
    }
    public void setThanhVien(boolean thanhVien) {
        if (!thanhVien) setHangThanhVien(HangThanhVien.NONE);
        else if (this.hangThanhVien == HangThanhVien.NONE) capNhatHangThanhVien();
    }
    public void setTongChiTieu(double tongChiTieu) {
        if (tongChiTieu < 0) throw new IllegalArgumentException("Tổng chi tiêu phải >= 0");
        this.tongChiTieu = tongChiTieu;
        capNhatHangThanhVien();
    }
    public void setHangThanhVien(HangThanhVien hangThanhVien) {
        if (hangThanhVien == null) throw new IllegalArgumentException("Hạng thành viên không được rỗng");
        this.hangThanhVien = hangThanhVien;
    }

    public String getMaKH()                     { return maKH; }
    public String getTenKH()                    { return tenKH; }
    public String getGioitinh()                 { return gioitinh; }
    public String getSdt()                      { return sdt; }
    public HangThanhVien getHangThanhVien()     { return hangThanhVien; }
    public double getTongChiTieu()               { return tongChiTieu; }
    public LocalDate getNgaySinh()              { return ngaySinh; }
    public String getDiaChi()                   { return diaChi; }
    public LocalDate getNgayThamGia()           { return ngayThamGia; }
    public String getEmail()                    { return email; }

    public void setNgaySinh(LocalDate ngaySinh)         { this.ngaySinh = ngaySinh; }
    public void setDiaChi(String diaChi)                { this.diaChi = diaChi; }
    public void setNgayThamGia(LocalDate ngayThamGia)   { this.ngayThamGia = ngayThamGia; }
    public void setEmail(String email)                  { this.email = email; }

    public double capNhatTongChiTieu(double soTien) {
        if (soTien < 0) throw new IllegalArgumentException("Số tiền cộng thêm không hợp lệ");
        this.tongChiTieu += soTien;
        capNhatHangThanhVien();
        return this.tongChiTieu;
    }

    public void capNhatHangThanhVien() {
        if (this.hangThanhVien == HangThanhVien.NONE) return;
        if (tongChiTieu > 50000000)      hangThanhVien = HangThanhVien.DIAMOND;
        else if (tongChiTieu > 25000000) hangThanhVien = HangThanhVien.GOLD;
        else if (tongChiTieu > 10000000) hangThanhVien = HangThanhVien.SILVER;
        else if (tongChiTieu > 5000000)  hangThanhVien = HangThanhVien.BRONZE;
        else if (hangThanhVien.ordinal() < HangThanhVien.MEMBER.ordinal())
            hangThanhVien = HangThanhVien.MEMBER;
    }

    @Override
    public String toString() {
        return "KhachHang{maKH='" + maKH + "', tenKH='" + tenKH + "', hangThanhVien=" + hangThanhVien + '}';
    }
}
