package entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Entity
@Table(name = "DonDatMon")
public class DonDatMon {

    @Id
    @Column(name = "maDon", length = 20)
    private String maDon;

    @Column(name = "ngayKhoiTao", nullable = false)
    private LocalDateTime ngayKhoiTao;

    @Column(name = "thoiGianDen")
    private LocalDateTime thoiGianDen;

    @Column(name = "trangThai", nullable = false, length = 50)
    private String trangThai;

    @Column(name = "maNV", nullable = false, length = 20)
    private String maNV;

    @Column(name = "maKH", length = 20)
    private String maKH;

    @Column(name = "maBan", length = 10)
    private String maBan;

    @Column(name = "ghiChu", columnDefinition = "TEXT")
    private String ghiChu;

    private String generateMaDon() {
        return "DON" + (new Random().nextInt(9000) + 1000);
    }

    public DonDatMon() {
        this.maDon = generateMaDon();
        this.ngayKhoiTao = LocalDateTime.now();
        this.ghiChu = "";
    }

    public DonDatMon(String maDon, LocalDateTime ngayKhoiTao, String maNV,
                     String maKH, String maBan, String ghiChu) {
        setMaDon(maDon);
        setNgayKhoiTao(ngayKhoiTao);
        setMaNV(maNV);
        setMaKH(maKH);
        setMaBan(maBan);
        setGhiChu(ghiChu);
    }

    public DonDatMon(DonDatMon d) {
        this.maDon = d.maDon;
        this.ngayKhoiTao = d.ngayKhoiTao;
        this.maNV = d.maNV;
        this.maKH = d.maKH;
        this.maBan = d.maBan;
        this.ghiChu = d.ghiChu;
    }

    public String getMaDon()                        { return maDon; }
    public LocalDateTime getNgayKhoiTao()           { return ngayKhoiTao; }
    public LocalDateTime getThoiGianDen()           { return thoiGianDen; }
    public String getTrangThai()                    { return trangThai; }
    public String getMaNV()                         { return maNV; }
    public String getMaKH()                         { return maKH; }
    public String getMaBan()                        { return maBan; }
    public String getGhiChu()                       { return ghiChu; }

    public void setMaDon(String maDon) {
        if (maDon == null || maDon.trim().isEmpty()) throw new IllegalArgumentException("Mã đơn không được rỗng.");
        this.maDon = maDon;
    }
    public void setNgayKhoiTao(LocalDateTime ngayKhoiTao) {
        if (ngayKhoiTao == null) throw new IllegalArgumentException("Ngày khởi tạo không được rỗng.");
        this.ngayKhoiTao = ngayKhoiTao;
    }
    public void setThoiGianDen(LocalDateTime thoiGianDen)   { this.thoiGianDen = thoiGianDen; }
    public void setTrangThai(String trangThai)               { this.trangThai = trangThai; }
    public void setMaNV(String maNV)                         { this.maNV = maNV; }
    public void setMaKH(String maKH)                         { this.maKH = maKH; }
    public void setMaBan(String maBan)                       { this.maBan = maBan; }
    public void setGhiChu(String ghiChu)                     { this.ghiChu = ghiChu; }

    @Override
    public String toString() {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("HH:mm:ss dd-MM-yyyy");
        return "DonDatMon{maDon='" + maDon + "', maBan='" + maBan + "', ghiChu='" + ghiChu + "'}";
    }
}
