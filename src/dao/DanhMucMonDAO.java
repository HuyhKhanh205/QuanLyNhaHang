package dao;

import entity.DanhMucMon;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

public class DanhMucMonDAO extends BaseDAO {

    public List<DanhMucMon> getAllDanhMuc() {
        EntityManager em = getEM();
        try {
            return em.createQuery("FROM DanhMucMon", DanhMucMon.class).getResultList();
        } catch (Exception e) { e.printStackTrace(); return new ArrayList<>(); }
        finally { em.close(); }
    }

    public boolean themDanhMuc(DanhMucMon dm) {
        if (dm.getMadm() == null || dm.getMadm().isEmpty())
            dm.setMadm(generateNewMaDM());
        try {
            inTransactionVoid(em -> em.persist(dm));
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean capNhatDanhMuc(DanhMucMon dm) {
        try {
            inTransactionVoid(em -> em.merge(dm));
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean xoaDanhMuc(String maDM) {
        try {
            inTransactionVoid(em -> {
                DanhMucMon d = em.find(DanhMucMon.class, maDM);
                if (d != null) em.remove(d);
            });
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    private String generateNewMaDM() {
        EntityManager em = getEM();
        try {
            List<String> r = em.createQuery("SELECT d.madm FROM DanhMucMon d ORDER BY d.madm DESC", String.class)
                    .setMaxResults(1).getResultList();
            if (!r.isEmpty() && r.get(0) != null) {
                try {
                    int num = Integer.parseInt(r.get(0).replace("DM", ""));
                    return String.format("DM%04d", num + 1);
                } catch (NumberFormatException ignored) {}
            }
            return "DM0001";
        } finally { em.close(); }
    }
}
