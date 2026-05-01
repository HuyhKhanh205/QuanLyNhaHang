package entity;

import jakarta.persistence.*;

@Entity
@Table(name = "MonAn")
public class MonAn {

    @Id
    @Column(name = "maMonAn", length = 20)
    private String maMonAn;

    @Column(name = "tenMon", nullable = false, length = 100)
    private String tenMon;

    @Column(name = "moTa", length = 500)
    private String mota;

    @Column(name = "donGia", nullable = false)
    private float donGia;

    @Column(name = "donViTinh", nullable = false, length = 20)
    private String donViTinh;

    @Column(name = "trangThai", nullable = false, length = 50)
    private String trangThai;

    @Column(name = "hinhAnh", length = 255)
    private String hinhAnh;

    @Column(name = "maDM", nullable = false, length = 10)
    private String maDM;

    public MonAn() {
        this.maMonAn = "";
        this.tenMon = "";
        this.donGia = 0;
        this.donViTinh = "";
        this.trangThai = "Còn";
        this.mota = "";
        this.hinhAnh = "";
        this.maDM = "";
    }

    public MonAn(String maMonAn, String tenMon, String mota, float donGia,
                 String donViTinh, String trangThai, String hinhAnh, String maDM) {
        this.maMonAn = maMonAn;
        this.tenMon = tenMon;
        this.mota = mota;
        this.donGia = donGia;
        this.donViTinh = donViTinh;
        this.trangThai = trangThai;
        this.hinhAnh = hinhAnh;
        this.maDM = maDM;
    }

    public String getMaMonAn()              { return maMonAn; }
    public void setMaMonAn(String maMonAn)  { this.maMonAn = maMonAn; }
    public String getTenMon()               { return tenMon; }
    public void setTenMon(String tenMon)    { this.tenMon = tenMon; }
    public String getMota()                 { return mota; }
    public void setMota(String mota)        { this.mota = mota; }
    public float getDonGia()                { return donGia; }
    public void setDonGia(float donGia)     { this.donGia = donGia; }
    public String getDonViTinh()            { return donViTinh; }
    public void setDonViTinh(String d)      { this.donViTinh = d; }
    public String getTrangThai()            { return trangThai; }
    public void setTrangThai(String t)      { this.trangThai = t; }
    public String getHinhAnh()              { return hinhAnh; }
    public void setHinhAnh(String hinhAnh)  { this.hinhAnh = hinhAnh; }
    public String getMaDM()                 { return maDM; }
    public void setMaDM(String maDM)        { this.maDM = maDM; }

    @Override
    public String toString() { return tenMon; }
}
