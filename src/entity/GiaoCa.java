package entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "GiaoCa")
public class GiaoCa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "maGiaoCa")
    private int maGiaoCa;

    @Column(name = "maNV", length = 20)
    private String maNV;

    @Column(name = "thoiGianBatDau", nullable = false)
    private LocalDateTime thoiGianBatDau;

    @Column(name = "thoiGianKetThuc")
    private LocalDateTime thoiGianKetThuc;

    @Column(name = "tienDauCa", nullable = false)
    private double tienDauCa;

    @Column(name = "tienCuoiCa")
    private double tienCuoiCa;

    @Column(name = "tienHeThongTinh")
    private double tienHeThongTinh;

    @Column(name = "chenhLech")
    private double chenhLech;

    @Column(name = "ghiChu", length = 255)
    private String ghiChu;

    public GiaoCa() {}

    public GiaoCa(int maGiaoCa, String maNV, LocalDateTime thoiGianBatDau, double tienDauCa) {
        this.maGiaoCa = maGiaoCa;
        this.maNV = maNV;
        this.thoiGianBatDau = thoiGianBatDau;
        this.tienDauCa = tienDauCa;
    }

    public GiaoCa(int maGiaoCa, String maNV, LocalDateTime thoiGianBatDau, LocalDateTime thoiGianKetThuc,
                  double tienDauCa, double tienCuoiCa, double tienHeThongTinh, double chenhLech, String ghiChu) {
        this.maGiaoCa = maGiaoCa;
        this.maNV = maNV;
        this.thoiGianBatDau = thoiGianBatDau;
        this.thoiGianKetThuc = thoiGianKetThuc;
        this.tienDauCa = tienDauCa;
        this.tienCuoiCa = tienCuoiCa;
        this.tienHeThongTinh = tienHeThongTinh;
        this.chenhLech = chenhLech;
        this.ghiChu = ghiChu;
    }

    public int getMaGiaoCa()                                    { return maGiaoCa; }
    public void setMaGiaoCa(int maGiaoCa)                       { this.maGiaoCa = maGiaoCa; }
    public String getMaNV()                                     { return maNV; }
    public void setMaNV(String maNV)                            { this.maNV = maNV; }
    public LocalDateTime getThoiGianBatDau()                    { return thoiGianBatDau; }
    public void setThoiGianBatDau(LocalDateTime v)              { this.thoiGianBatDau = v; }
    public LocalDateTime getThoiGianKetThuc()                   { return thoiGianKetThuc; }
    public void setThoiGianKetThuc(LocalDateTime v)             { this.thoiGianKetThuc = v; }
    public double getTienDauCa()                                { return tienDauCa; }
    public void setTienDauCa(double v)                          { this.tienDauCa = v; }
    public double getTienCuoiCa()                               { return tienCuoiCa; }
    public void setTienCuoiCa(double v)                         { this.tienCuoiCa = v; }
    public double getTienHeThongTinh()                          { return tienHeThongTinh; }
    public void setTienHeThongTinh(double v)                    { this.tienHeThongTinh = v; }
    public double getChenhLech()                                { return chenhLech; }
    public void setChenhLech(double v)                          { this.chenhLech = v; }
    public String getGhiChu()                                   { return ghiChu; }
    public void setGhiChu(String ghiChu)                        { this.ghiChu = ghiChu; }
}
