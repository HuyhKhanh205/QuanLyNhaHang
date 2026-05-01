package entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "KhuyenMai")
public class KhuyenMai {

    @Id
    @Column(name = "maKM", length = 20)
    private String maKM;

    @Column(name = "tenKM", nullable = false, length = 100)
    private String tenChuongTrinh;

    @Column(name = "moTa", length = 255)
    private String moTa;

    @Column(name = "loaiGiam", nullable = false, length = 50)
    private String loaiKhuyenMai;

    @Column(name = "giaTriGiam", nullable = false)
    private double giaTri;

    @Column(name = "dieuKienApDung")
    private double dieuKienApDung;

    @Column(name = "ngayBatDau", nullable = false)
    private LocalDate ngayBatDau;

    @Column(name = "ngayKetThuc", nullable = false)
    private LocalDate ngayKetThuc;

    @Column(name = "trangThai", nullable = false, length = 50)
    private String trangThai;

    @Column(name = "soLuongGioiHan")
    private int soLuongGioiHan = 0;

    @Column(name = "soLuotDaDung", nullable = false)
    private int soLuotDaDung = 0;

    public KhuyenMai() {}

    public KhuyenMai(String maKM, String tenChuongTrinh, String moTa, String loaiKhuyenMai,
                     double giaTri, double dieuKienApDung, LocalDate ngayBatDau,
                     LocalDate ngayKetThuc, String trangThai) {
        this.maKM = maKM;
        this.tenChuongTrinh = tenChuongTrinh;
        this.moTa = moTa;
        this.loaiKhuyenMai = loaiKhuyenMai;
        this.giaTri = giaTri;
        this.dieuKienApDung = dieuKienApDung;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.trangThai = trangThai;
    }

    public String getMaKM()                         { return maKM; }
    public void setMaKM(String maKM)                { this.maKM = maKM; }
    public String getTenChuongTrinh()               { return tenChuongTrinh; }
    public String getMoTa()                         { return moTa; }
    public String getLoaiKhuyenMai()                { return loaiKhuyenMai; }
    public double getGiaTri()                       { return giaTri; }
    public double getDieuKienApDung()               { return dieuKienApDung; }
    public LocalDate getNgayBatDau()                { return ngayBatDau; }
    public LocalDate getNgayKetThuc()               { return ngayKetThuc; }
    public String getTrangThai()                    { return trangThai; }
    public void setTrangThai(String trangThai)      { this.trangThai = trangThai; }
    public int getSoLuongGioiHan()                  { return soLuongGioiHan; }
    public void setSoLuongGioiHan(int v)            { this.soLuongGioiHan = v; }
    public int getSoLuotDaDung()                    { return soLuotDaDung; }
    public void setSoLuotDaDung(int v)              { this.soLuotDaDung = v; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(maKM, ((KhuyenMai) o).maKM);
    }

    @Override
    public int hashCode() { return Objects.hash(maKM); }
}
