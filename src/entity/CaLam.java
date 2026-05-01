package entity;

import jakarta.persistence.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Entity
@Table(name = "CaLam")
public class CaLam {

    @Id
    @Column(name = "maCa", length = 20)
    private String maCa;

    @Column(name = "tenCa", nullable = false, length = 50)
    private String tenCa;

    @Column(name = "gioBatDau", nullable = false)
    private LocalTime gioBatDau;

    @Column(name = "gioKetThuc", nullable = false)
    private LocalTime gioKetThuc;

    public CaLam() {
        this.maCa = "Chưa có mã";
        this.tenCa = "Chưa đặt tên";
        this.gioBatDau = LocalTime.now();
        this.gioKetThuc = this.gioBatDau.plusHours(1);
    }

    public CaLam(String maCa, String tenCa, LocalTime gioBatDau, LocalTime gioKetThuc) {
        setMaCa(maCa);
        setTenCa(tenCa);
        setGioBatDau(gioBatDau);
        setGioKetThuc(gioKetThuc);
    }

    public CaLam(CaLam ca) {
        this.maCa = ca.maCa;
        this.tenCa = ca.tenCa;
        this.gioBatDau = ca.gioBatDau;
        this.gioKetThuc = ca.gioKetThuc;
    }

    public String getMaCa()                     { return maCa; }
    public String getTenCa()                    { return tenCa; }
    public LocalTime getGioBatDau()             { return gioBatDau; }
    public LocalTime getGioKetThuc()            { return gioKetThuc; }

    public void setMaCa(String maCa) {
        if (maCa == null || maCa.trim().isEmpty()) throw new IllegalArgumentException("Mã ca không được rỗng.");
        this.maCa = maCa;
    }
    public void setTenCa(String tenCa) {
        if (tenCa == null || tenCa.trim().isEmpty()) throw new IllegalArgumentException("Tên ca không được rỗng");
        this.tenCa = tenCa;
    }
    public void setGioBatDau(LocalTime gioBatDau) {
        if (gioBatDau == null) throw new IllegalArgumentException("Giờ bắt đầu không được rỗng.");
        this.gioBatDau = gioBatDau;
    }
    public void setGioKetThuc(LocalTime gioKetThuc) {
        if (gioKetThuc == null) throw new IllegalArgumentException("Giờ kết thúc không được rỗng.");
        this.gioKetThuc = gioKetThuc;
    }

    @Override
    public String toString() {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("HH:mm");
        return "CaLam{maCa='" + maCa + "', tenCa='" + tenCa + "', " +
                (gioBatDau != null ? gioBatDau.format(f) : "null") + "-" +
                (gioKetThuc != null ? gioKetThuc.format(f) : "null") + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return Objects.equals(maCa, ((CaLam) o).maCa);
    }

    @Override
    public int hashCode() { return Objects.hash(maCa); }
}
