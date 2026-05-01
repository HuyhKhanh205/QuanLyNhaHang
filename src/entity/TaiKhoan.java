package entity;

import jakarta.persistence.*;

@Entity
@Table(name = "TaiKhoan")
public class TaiKhoan {

    @Id
    @Column(name = "tenTK", length = 50)
    private String tentk;

    @Column(name = "matKhau", nullable = false, length = 255)
    private String matkhau;

    @Column(name = "trangThai", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private boolean trangthai;

    public TaiKhoan(String tentk, String matkhau, VaiTro vaitro, boolean trangthai) {
        this.setTentk(tentk);
        this.matkhau = matkhau;
        this.trangthai = trangthai;
    }

    public TaiKhoan() {}

    public String getTentk() { return tentk; }
    public void setTentk(String tentk) {
        if (tentk == null || tentk.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên tài khoản không được để rỗng.");
        }
        this.tentk = tentk;
    }

    public String getMatkhau() { return matkhau; }
    public void setMatkhau(String matkhau) { this.matkhau = matkhau; }

    public Boolean getTrangthai() { return trangthai; }
    public void setTrangthai(Boolean trangthai) { this.trangthai = trangthai; }

    private String hashPassword(String plainPassword) {
        return "hashed_" + plainPassword.hashCode();
    }

    @Override
    public String toString() {
        return "TaiKhoan{tentk='" + tentk + "', matkhau='***', trangthai=" + trangthai + '}';
    }
}
