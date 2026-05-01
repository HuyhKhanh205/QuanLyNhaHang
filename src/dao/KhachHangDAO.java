package dao;

import entity.HangThanhVien;
import entity.KhachHang;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class KhachHangDAO extends BaseDAO {

    public List<KhachHang> getAllKhachHang() {
        EntityManager em = getEM();
        try {
            return em.createQuery("FROM KhachHang", KhachHang.class).getResultList();
        } catch (Exception e) { e.printStackTrace(); return new ArrayList<>(); }
        finally { em.close(); }
    }

    public boolean themKhachHang(KhachHang kh) {
        try {
            inTransactionVoid(em -> em.persist(kh));
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean updateKhachHang(KhachHang kh) {
        try {
            inTransactionVoid(em -> em.merge(kh));
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public List<KhachHang> timKhachHang(String tuKhoa) {
        EntityManager em = getEM();
        try {
            return em.createQuery(
                    "FROM KhachHang k WHERE k.tenKH LIKE :kw OR k.sdt LIKE :kw", KhachHang.class)
                    .setParameter("kw", "%" + tuKhoa + "%").getResultList();
        } catch (Exception e) { e.printStackTrace(); return new ArrayList<>(); }
        finally { em.close(); }
    }

    public KhachHang timTheoMaKH(String maKH) {
        EntityManager em = getEM();
        try { return em.find(KhachHang.class, maKH); }
        finally { em.close(); }
    }

    public KhachHang timTheoSDT(String sdt) {
        EntityManager em = getEM();
        try {
            List<KhachHang> r = em.createQuery(
                    "FROM KhachHang k WHERE k.sdt = :sdt", KhachHang.class)
                    .setParameter("sdt", sdt).getResultList();
            return r.isEmpty() ? null : r.get(0);
        } finally { em.close(); }
    }
}
