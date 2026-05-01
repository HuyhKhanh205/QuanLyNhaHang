package entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "PhanCongCa")
public class PhanCong {

    @EmbeddedId
    private PhanCongId id;

    // Giữ để backward-compat với GUI code
    @Transient private CaLam caLam;
    @Transient private NhanVien nhanVien;

    public PhanCong() {}

    public PhanCong(CaLam caLam, NhanVien nhanVien, LocalDate ngayLam) {
        this.id = new PhanCongId(nhanVien.getManv(), caLam.getMaCa(), ngayLam);
        this.caLam = caLam;
        this.nhanVien = nhanVien;
    }

    public CaLam getCaLam()             { return caLam; }
    public NhanVien getNhanVien()        { return nhanVien; }
    public LocalDate getNgayLam()        { return id != null ? id.getNgayLam() : null; }
    public String getMaNV()              { return id != null ? id.getMaNV() : null; }
    public String getMaCa()              { return id != null ? id.getMaCa() : null; }
    public PhanCongId getId()            { return id; }
}
