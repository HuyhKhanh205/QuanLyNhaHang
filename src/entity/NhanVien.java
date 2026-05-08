package entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@Entity
@Table(name = "NhanVien")
public class NhanVien {

    @Id
    @Column(name = "maNV", length = 20)
    private String manv;

    @Column(name = "hoTen", nullable = false, length = 100)
    private String hoten;

    @Column(name = "ngaySinh")
    private LocalDate ngaysinh;

    @Column(name = "gioiTinh", nullable = false, length = 10)
    private String gioitinh;

    @Column(name = "sdt", nullable = false, unique = true, length = 15)
    private String sdt;

    @Column(name = "diaChi", length = 255)
    private String diachi;

    @Column(name = "ngayVaoLam", nullable = false)
    private LocalDate ngayvaolam;

    @Column(name = "luong", nullable = false)
    private double luong;

    @Enumerated(EnumType.STRING)
    @Column(name = "vaiTro", nullable = false, length = 20)
    private VaiTro vaiTro;

    @Column(name = "tenTK", nullable = false, unique = true, length = 50)
    private String tenTK;

    @Column(name = "email", unique = true, length = 100)
    private String email;

    public NhanVien() {
        this.vaiTro = VaiTro.NHANVIEN;
        this.manv = phatSinhMaNV(vaiTro);
        this.hoten = "Chưa có tên";
        this.ngaysinh = LocalDate.now().minusYears(18);
        this.gioitinh = "Khác";
        this.sdt = "0000000000";
        this.diachi = "Chưa cập nhật";
        this.ngayvaolam = LocalDate.now();
        this.luong = 2000000f;
        this.tenTK = "";
        this.email = "example@starguardian.com";
    }

    public NhanVien(String hoTen, LocalDate ngaySinh, String gioiTinh, String sdt,
                    String diaChi, LocalDate ngayVaoLam, float luong, VaiTro vaiTro, String email) {
        this(hoTen, ngaySinh, gioiTinh, sdt, diaChi, ngayVaoLam, luong, vaiTro, "", email);
    }

    public NhanVien(String hoTen, LocalDate ngaySinh, String gioiTinh, String sdt,
                    String diaChi, LocalDate ngayVaoLam, float luong, VaiTro vaiTro, String tenTK, String email) {
        setVaiTro(vaiTro);
        this.manv = phatSinhMaNV(vaiTro);
        setHoten(hoTen);
        setNgaysinh(ngaySinh);
        setGioitinh(gioiTinh);
        setSdt(sdt);
        setDiachi(diaChi);
        setNgayvaolam(ngayVaoLam);
        setLuong(luong);
        setTenTK(tenTK);
        setEmail(email);
    }

    public NhanVien(String maNV, String hoTen, LocalDate ngaySinh, String gioiTinh, String sdt,
                    String diaChi, LocalDate ngayVaoLam, float luong, VaiTro vaiTro, String email) {
        this.manv = maNV;
        setVaiTro(vaiTro);
        setHoten(hoTen);
        setNgaysinh(ngaySinh);
        setGioitinh(gioiTinh);
        setSdt(sdt);
        setDiachi(diaChi);
        setNgayvaolam(ngayVaoLam);
        setLuong(luong);
        this.tenTK = "";
        setEmail(email);
    }

    public NhanVien(NhanVien other) {
        this.vaiTro = other.vaiTro;
        this.manv = phatSinhMaNV(other.vaiTro);
        this.hoten = other.hoten;
        this.ngaysinh = other.ngaysinh;
        this.gioitinh = other.gioitinh;
        this.sdt = other.sdt;
        this.diachi = other.diachi;
        this.ngayvaolam = other.ngayvaolam;
        this.luong = other.luong;
        this.tenTK = other.tenTK;
        this.email = other.email;
    }

    public NhanVien(String maNV, String hoTen) {
        this.manv = maNV;
        this.hoten = hoTen;
    }

    private String phatSinhMaNV(VaiTro vaiTro) {
        String maVaiTro = (vaiTro == VaiTro.QUANLY) ? "02" : "01";
        int soNgauNhien = ThreadLocalRandom.current().nextInt(100, 1000);
        return "NV" + maVaiTro + soNgauNhien;
    }

    public String getManv() { return manv; }
    public void setManv(String manv) { this.manv = manv; }

    public String getHoten() { return hoten; }
    public void setHoten(String hoten) {
        if (hoten == null || hoten.trim().isEmpty()) throw new IllegalArgumentException("Họ tên không được rỗng");
        if (!hoten.trim().matches("^[\\p{L} .'-]+$"))
            throw new IllegalArgumentException("Họ tên không hợp lệ.");
        this.hoten = hoten.trim();
    }

    public LocalDate getNgaysinh() { return ngaysinh; }
    public void setNgaysinh(LocalDate ngaysinh) {
        if (ngaysinh == null || Period.between(ngaysinh, LocalDate.now()).getYears() < 18)
            throw new IllegalArgumentException("Nhân viên phải >= 18 tuổi");
        this.ngaysinh = ngaysinh;
    }

    public String getGioitinh() { return gioitinh; }
    public void setGioitinh(String gioitinh) { this.gioitinh = gioitinh; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) {
        if (sdt == null || !sdt.matches("^0\\d{9}$"))
            throw new IllegalArgumentException("SĐT không hợp lệ (phải có 10 chữ số, bắt đầu bằng 0)");
        this.sdt = sdt;
    }

    public String getDiachi() { return diachi; }
    public void setDiachi(String diachi) { this.diachi = diachi; }

    public LocalDate getNgayvaolam() { return ngayvaolam; }
    public void setNgayvaolam(LocalDate ngayvaolam) { this.ngayvaolam = ngayvaolam; }

    public double getLuong() { return luong; }
    public void setLuong(double luong) {
        if (luong <= 0) throw new IllegalArgumentException("Lương phải > 0");
        this.luong = luong;
    }

    public VaiTro getVaiTro() { return vaiTro; }
    public void setVaiTro(VaiTro vaiTro) { this.vaiTro = vaiTro; }

    public String getTenTK() { return tenTK; }
    public void setTenTK(String tenTK) { this.tenTK = tenTK; }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) throw new IllegalArgumentException("Email không được rỗng.");
        if (!email.trim().matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"))
            throw new IllegalArgumentException("Email không đúng định dạng.");
        this.email = email.trim();
    }

    @Override
    public String toString() {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return "NhanVien{manv='" + manv + "', hoten='" + hoten + "', vaiTro=" + vaiTro + '}';
    }
}
