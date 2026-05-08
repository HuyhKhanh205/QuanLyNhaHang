package entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ChiTietHoaDon")
public class ChiTietHoaDon {

    @EmbeddedId
    private ChiTietHoaDonId id;

    @Column(name = "soLuong", nullable = false)
    private int soluong;

    @Column(name = "donGia", nullable = false)
    private double dongia;

    @Column(name = "trangThaiMon", nullable = false, length = 20)
    private String trangThaiMon = "Chờ";

    @Column(name = "soLuongDaXacNhan", nullable = false)
    private int soLuongDaXacNhan = 0;

    @Transient
    private double thanhtien;

    @Transient
    private String tenMon;

    public ChiTietHoaDon() {}

    public ChiTietHoaDon(String maMon, String maDon, int soluong, double dongia) {
        this.id = new ChiTietHoaDonId(maDon, maMon);
        setDongia(dongia);
        setSoluong(soluong);
    }

    public ChiTietHoaDon(String maDon, String maMon, String tenMon, int soluong, double dongia) {
        this.id = new ChiTietHoaDonId(maDon, maMon);
        this.tenMon = tenMon;
        setDongia(dongia);
        setSoluong(soluong);
    }

    public ChiTietHoaDon(ChiTietHoaDon other) {
        this.id = new ChiTietHoaDonId(other.getMaDon(), other.getMaMon());
        this.soluong = other.soluong;
        this.dongia = other.dongia;
        this.thanhtien = other.thanhtien;
    }

    public void tinhThanhTien() {
        this.thanhtien = this.soluong * this.dongia;
    }

    public String getMaDon()    { return id != null ? id.getMaDon() : null; }
    public String getMaMon()    { return id != null ? id.getMaMonAn() : null; }
    public ChiTietHoaDonId getId() { return id; }

    public void setMaDon(String maDon) {
        if (maDon == null || maDon.trim().isEmpty()) throw new IllegalArgumentException("Mã đơn không được rỗng.");
        if (id == null) id = new ChiTietHoaDonId(maDon, null);
        else id = new ChiTietHoaDonId(maDon, id.getMaMonAn());
    }

    public void setMaMon(String maMon) {
        if (maMon == null || maMon.trim().isEmpty()) throw new IllegalArgumentException("Mã món không được rỗng.");
        if (id == null) id = new ChiTietHoaDonId(null, maMon);
        else id = new ChiTietHoaDonId(id.getMaDon(), maMon);
    }

    public String getTenMon()                   { return tenMon; }
    public void setTenMon(String tenMon)         { this.tenMon = tenMon; }

    public int getSoluong()                      { return soluong; }
    public void setSoluong(int soluong) {
        if (soluong < 1) throw new IllegalArgumentException("Số lượng phải >= 1.");
        this.soluong = soluong;
        tinhThanhTien();
    }

    public double getDongia()                     { return dongia; }
    public void setDongia(double dongia) {
        if (dongia < 0) throw new IllegalArgumentException("Đơn giá phải >= 0.");
        this.dongia = dongia;
        tinhThanhTien();
    }

    public String getTrangThaiMon()              { return trangThaiMon; }
    public void setTrangThaiMon(String v)        { this.trangThaiMon = v != null ? v : "Chờ"; }

    public double getThanhtien()                  { return thanhtien; }

    @Override
    public String toString() {
        return "ChiTietHoaDon{maMon='" + getMaMon() + "', tenMon='" + tenMon +
                "', soluong=" + soluong + ", thanhtien=" + thanhtien + '}';
    }
}
