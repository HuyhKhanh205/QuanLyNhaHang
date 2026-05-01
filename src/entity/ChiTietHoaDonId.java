package entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ChiTietHoaDonId implements Serializable {

    @Column(name = "maDon", length = 20)
    private String maDon;

    @Column(name = "maMonAn", length = 20)
    private String maMonAn;

    public ChiTietHoaDonId() {}

    public ChiTietHoaDonId(String maDon, String maMonAn) {
        this.maDon = maDon;
        this.maMonAn = maMonAn;
    }

    public String getMaDon()    { return maDon; }
    public String getMaMonAn()  { return maMonAn; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChiTietHoaDonId)) return false;
        ChiTietHoaDonId that = (ChiTietHoaDonId) o;
        return Objects.equals(maDon, that.maDon) && Objects.equals(maMonAn, that.maMonAn);
    }

    @Override
    public int hashCode() { return Objects.hash(maDon, maMonAn); }
}
