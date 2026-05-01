package entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

@Embeddable
public class PhanCongId implements Serializable {

    @Column(name = "maNV", length = 20)
    private String maNV;

    @Column(name = "maCa", length = 20)
    private String maCa;

    @Column(name = "ngayLam")
    private LocalDate ngayLam;

    public PhanCongId() {}

    public PhanCongId(String maNV, String maCa, LocalDate ngayLam) {
        this.maNV = maNV;
        this.maCa = maCa;
        this.ngayLam = ngayLam;
    }

    public String getMaNV()     { return maNV; }
    public String getMaCa()     { return maCa; }
    public LocalDate getNgayLam() { return ngayLam; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PhanCongId)) return false;
        PhanCongId that = (PhanCongId) o;
        return Objects.equals(maNV, that.maNV) &&
               Objects.equals(maCa, that.maCa) &&
               Objects.equals(ngayLam, that.ngayLam);
    }

    @Override
    public int hashCode() { return Objects.hash(maNV, maCa, ngayLam); }
}
