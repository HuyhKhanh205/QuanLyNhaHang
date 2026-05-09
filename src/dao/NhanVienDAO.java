package dao;

import entity.NhanVien;
import entity.VaiTro;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

public class NhanVienDAO extends BaseDAO {

    public List<NhanVien> getAllNhanVien() {
        EntityManager em = getEM();
        try {
            return em.createQuery("FROM NhanVien", NhanVien.class).getResultList();
        } catch (Exception e) { e.printStackTrace(); return new ArrayList<>(); }
        finally { em.close(); }
    }

    public NhanVien getChiTietNhanVien(String maNV) {
        EntityManager em = getEM();
        try { return em.find(NhanVien.class, maNV); }
        finally { em.close(); }
    }

    public List<NhanVien> searchNhanVienByName(String keyword) {
        EntityManager em = getEM();
        try {
            return em.createQuery("FROM NhanVien n WHERE LOWER(n.hoten) LIKE :kw", NhanVien.class)
                    .setParameter("kw", "%" + keyword.toLowerCase().trim() + "%").getResultList();
        } catch (Exception e) { e.printStackTrace(); return new ArrayList<>(); }
        finally { em.close(); }
    }

    public List<NhanVien> searchNhanVienBySdt(String keyword) {
        EntityManager em = getEM();
        try {
            return em.createQuery("FROM NhanVien n WHERE n.sdt LIKE :kw", NhanVien.class)
                    .setParameter("kw", "%" + keyword.trim() + "%").getResultList();
        } catch (Exception e) { e.printStackTrace(); return new ArrayList<>(); }
        finally { em.close(); }
    }

    public String getTenNhanVienByMa(String maNV) {
        EntityManager em = getEM();
        try {
            List<String> r = em.createQuery("SELECT n.hoten FROM NhanVien n WHERE n.manv = :ma", String.class)
                    .setParameter("ma", maNV).getResultList();
            return r.isEmpty() ? "N/A (" + maNV + ")" : r.get(0);
        } finally { em.close(); }
    }

    public String getEmailByTenTK(String tenTK) {
        EntityManager em = getEM();
        try {
            List<String> r = em.createQuery("SELECT n.email FROM NhanVien n WHERE n.tenTK = :tk", String.class)
                    .setParameter("tk", tenTK.trim()).getResultList();
            return r.isEmpty() ? null : r.get(0);
        } finally { em.close(); }
    }

    public boolean addNhanVienAndAccount(NhanVien nv, String tenTK, String plainPassword) {
        String hashed = "hashed_" + plainPassword.trim().toLowerCase().hashCode();
        try {
            inTransactionVoid(em -> {
                em.createNativeQuery(
                        "INSERT INTO TaiKhoan(tenTK, matKhau, trangThai) VALUES(?, ?, 1)")
                        .setParameter(1, tenTK).setParameter(2, hashed).executeUpdate();
                nv.setTenTK(tenTK);
                em.persist(nv);
            });
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean updateNhanVienAndAccount(NhanVien nv, String oldTenTK, String newTenTK, String newPlainPassword) {
        try {
            inTransactionVoid(em -> {
                if (!oldTenTK.equals(newTenTK)) {
                    em.createNativeQuery("UPDATE NhanVien SET tenTK = ? WHERE maNV = ? AND tenTK = ?")
                            .setParameter(1, newTenTK).setParameter(2, nv.getManv())
                            .setParameter(3, oldTenTK).executeUpdate();
                    em.createNativeQuery("UPDATE TaiKhoan SET tenTK = ? WHERE tenTK = ?")
                            .setParameter(1, newTenTK).setParameter(2, oldTenTK).executeUpdate();
                }
                nv.setTenTK(newTenTK);
                em.merge(nv);
                if (newPlainPassword != null && !newPlainPassword.isEmpty()) {
                    String hashed = "hashed_" + newPlainPassword.trim().toLowerCase().hashCode();
                    em.createNativeQuery("UPDATE TaiKhoan SET matKhau = ? WHERE tenTK = ?")
                            .setParameter(1, hashed).setParameter(2, newTenTK).executeUpdate();
                }
            });
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean suspendNhanVienAndAccount(String maNV, String tenTK, VaiTro vaiTro) {
        if (vaiTro != VaiTro.NHANVIEN)
            throw new IllegalArgumentException("Chỉ có thể tạm ngưng nhân viên có Vai trò NHANVIEN.");
        try {
            inTransactionVoid(em ->
                em.createNativeQuery("UPDATE TaiKhoan SET trangThai = 0 WHERE tenTK = ?")
                        .setParameter(1, tenTK).executeUpdate());
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean activateNhanVienAccount(String tenTK) {
        try {
            inTransactionVoid(em ->
                em.createNativeQuery("UPDATE TaiKhoan SET trangThai = 1 WHERE tenTK = ?")
                        .setParameter(1, tenTK).executeUpdate());
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public int getAccountStatus(String tenTK) {
        EntityManager em = getEM();
        try {
            List<?> r = em.createNativeQuery("SELECT trangThai FROM TaiKhoan WHERE tenTK = ?")
                    .setParameter(1, tenTK.trim()).getResultList();
            if (r.isEmpty()) return -1;
            Object val = r.get(0);
            if (val instanceof Boolean) return ((Boolean) val) ? 1 : 0;
            return ((Number) val).intValue();
        } finally { em.close(); }
    }
}
