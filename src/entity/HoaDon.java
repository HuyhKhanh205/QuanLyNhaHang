package entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import dao.KhachHangDAO;
import dao.KhuyenMaiDAO;

@Entity
@Table(name = "HoaDon")
public class HoaDon {

    @Id
    @Column(name = "maHD", length = 20)
    private String maHD;

    @Column(name = "ngayLap", nullable = false)
    private LocalDateTime ngayLap;

    @Column(name = "tongTien", nullable = false)
    private double tongTien;

    @Column(name = "trangThai", nullable = false, length = 50)
    private String trangThai;

    @Column(name = "hinhThucThanhToan", length = 50)
    private String hinhThucThanhToan;

    @Column(name = "tienKhachDua")
    private double tienKhachDua;

    @Column(name = "tenBan", length = 100)
    private String tenBan;

    @Column(name = "maDon", nullable = false, unique = true, length = 20)
    private String maDon;

    @Column(name = "maNV", nullable = false, length = 20)
    private String maNV;

    @Column(name = "maKM", length = 20)
    private String maKM;

    @Column(name = "giamGia")
    private double giamGia;

    // Không có trong DB - tính toán / load từ DonDatMon
    @Transient private String maKH;
    @Transient private double tongThanhToan;
    @Transient private List<ChiTietHoaDon> dsChiTiet;

    public HoaDon() {
        this.maHD = phatSinhMaHD();
        this.ngayLap = LocalDateTime.now();
        this.trangThai = "Chưa thanh toán";
        this.hinhThucThanhToan = "Tiền mặt";
        this.dsChiTiet = new ArrayList<>();
        this.tongTien = 0;
        this.giamGia = 0;
        this.tongThanhToan = 0;
    }

    public HoaDon(String maHD, LocalDateTime ngayLap, String trangThai,
                  String hinhThucThanhToan, String maDon, String maNV, String maKM) {
        this.maHD = maHD;
        this.ngayLap = ngayLap;
        this.trangThai = trangThai;
        this.hinhThucThanhToan = hinhThucThanhToan;
        this.maDon = maDon;
        this.maNV = maNV;
        this.maKM = maKM;
        this.dsChiTiet = new ArrayList<>();
        this.tongTien = 0;
        this.giamGia = 0;
        this.tongThanhToan = 0;
        this.tienKhachDua = 0;
    }

    public HoaDon(HoaDon other) {
        this.maHD = other.maHD;
        this.ngayLap = other.ngayLap;
        this.tongTien = other.tongTien;
        this.trangThai = other.trangThai;
        this.hinhThucThanhToan = other.hinhThucThanhToan;
        this.tienKhachDua = other.tienKhachDua;
        this.maDon = other.maDon;
        this.maNV = other.maNV;
        this.maKM = other.maKM;
        this.maKH = other.maKH;
        this.giamGia = other.giamGia;
        this.tongThanhToan = other.tongThanhToan;
        this.dsChiTiet = new ArrayList<>();
        if (other.dsChiTiet != null) {
            for (ChiTietHoaDon ct : other.dsChiTiet) this.dsChiTiet.add(new ChiTietHoaDon(ct));
        }
    }

    public void setDsChiTiet(List<ChiTietHoaDon> dsChiTiet) { this.dsChiTiet = dsChiTiet; }

    public void tinhLaiTongTienTuChiTiet() {
        this.tongTien = 0;
        if (this.dsChiTiet != null) {
            for (ChiTietHoaDon ct : dsChiTiet) {
                ct.tinhThanhTien();
                this.tongTien += ct.getThanhtien();
            }
        }
    }

    public void tinhLaiGiamGiaVaTongTien(KhachHangDAO khachHangDAO, KhuyenMaiDAO maKhuyenMaiDAO) {
        tinhLaiTongTienTuChiTiet();
        double tongCong = this.tongTien;
        double giamGiaTV = 0, giamGiaMa = 0;
        if (this.maKH != null && khachHangDAO != null) {
            KhachHang kh = khachHangDAO.timTheoMaKH(this.maKH);
            if (kh != null) giamGiaTV = tongCong * getPhanTramGiamTheoHang(kh.getHangThanhVien()) / 100;
        }
        if (this.maKM != null && !this.maKM.isEmpty() && maKhuyenMaiDAO != null) {
            KhuyenMai km = maKhuyenMaiDAO.getKhuyenMaiHopLeByMa(this.maKM);
            if (km != null && tongCong >= km.getDieuKienApDung()) {
                if (km.getLoaiKhuyenMai().toLowerCase().contains("phần trăm"))
                    giamGiaMa = tongCong * km.getGiaTri() / 100;
                else
                    giamGiaMa = km.getGiaTri();
            }
        }
        this.giamGia = giamGiaTV + giamGiaMa;
        tinhLaiTongThanhToan();
    }

    private double getPhanTramGiamTheoHang(HangThanhVien hang) {
        if (hang == null) return 0;
        switch (hang) {
            case DIAMOND: return 10;
            case GOLD:    return 5;
            case SILVER:  return 3;
            case BRONZE:  return 2;
            default:      return 0;
        }
    }

    public void setTienKhachDua(double tienKhachDua)         { this.tienKhachDua = tienKhachDua; }
    public void setTongTienTuDB(double tongTien)             { this.tongTien = tongTien; }
    public void capNhatTongThanhToanTuCacThanhPhan() {
        this.tongThanhToan = this.tongTien - this.giamGia;
        if (this.tongThanhToan < 0) this.tongThanhToan = 0;
    }
    public void setMaKH(String maKH)                        { this.maKH = maKH; }
    public double tinhTienThoi() {
        return (this.tienKhachDua >= this.tongThanhToan) ? this.tienKhachDua - this.tongThanhToan : 0;
    }

    public String getTenBan()               { return tenBan; }
    public String getMaKH()                 { return maKH; }
    public String getMaHD()                 { return maHD; }
    public LocalDateTime getNgayLap()       { return ngayLap; }
    public String getTrangThai()            { return trangThai; }
    public String getHinhThucThanhToan()    { return hinhThucThanhToan; }
    public double getTienKhachDua()          { return tienKhachDua; }
    public String getMaDon()                { return maDon; }
    public String getMaNV()                 { return maNV; }
    public String getMaKM()                 { return maKM; }
    public List<ChiTietHoaDon> getDsChiTiet() { return dsChiTiet; }
    public void setTenBan(String tenBan)    { this.tenBan = tenBan; }
    public double getTongTien()              { return tongTien; }
    public double getGiamGia()              { return giamGia; }
    public double getTongThanhToan()        { return tongThanhToan; }
    public double getTienThoi()             { return tinhTienThoi(); }
    public void setMaKM(String maKM)       { this.maKM = maKM; }
    public void setGiamGia(double giamGia)  { this.giamGia = (giamGia < 0) ? 0 : giamGia; }
    public void tinhLaiTongThanhToan() {
        this.tongThanhToan = this.tongTien - this.giamGia;
        if (this.tongThanhToan < 0) this.tongThanhToan = 0;
    }

    private String phatSinhMaHD() {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("ddMMyy");
        return "HD" + LocalDateTime.now().format(f) + ThreadLocalRandom.current().nextInt(1000, 10000);
    }

    @Override
    public String toString() {
        return "HoaDon{maHD='" + maHD + "', maDon='" + maDon + "', trangThai='" + trangThai + "'}";
    }
}
