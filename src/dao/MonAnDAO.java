package dao;

import entity.MonAn;
import jakarta.persistence.EntityManager;
import socket.SocketEvent;
import socket.SocketManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MonAnDAO extends BaseDAO {

    public List<MonAn> getAllMonAn() {
        EntityManager em = getEM();
        try {
            return em.createQuery("FROM MonAn", MonAn.class).getResultList();
        } catch (Exception e) { e.printStackTrace(); return new ArrayList<>(); }
        finally { em.close(); }
    }

    public List<MonAn> getMonAnDangKinhDoanh() {
        EntityManager em = getEM();
        try {
            return em.createQuery("FROM MonAn m WHERE m.trangThai = 'Còn'", MonAn.class).getResultList();
        } catch (Exception e) { e.printStackTrace(); return new ArrayList<>(); }
        finally { em.close(); }
    }

    public String getTenMonByMa(String maMonAn) {
        EntityManager em = getEM();
        try {
            List<String> r = em.createQuery("SELECT m.tenMon FROM MonAn m WHERE m.maMonAn = :ma", String.class)
                    .setParameter("ma", maMonAn).getResultList();
            return r.isEmpty() ? maMonAn : r.get(0);
        } finally { em.close(); }
    }

    public float getDonGiaByMa(String maMon) {
        EntityManager em = getEM();
        try {
            List<Float> r = em.createQuery("SELECT m.donGia FROM MonAn m WHERE m.maMonAn = :ma", Float.class)
                    .setParameter("ma", maMon).getResultList();
            return r.isEmpty() ? 0f : r.get(0);
        } finally { em.close(); }
    }

    public String getNextMaMonAn() {
        EntityManager em = getEM();
        try {
            List<String> r = em.createQuery("SELECT m.maMonAn FROM MonAn m ORDER BY m.maMonAn DESC", String.class)
                    .setMaxResults(1).getResultList();
            if (!r.isEmpty() && r.get(0) != null && r.get(0).length() > 2) {
                try {
                    int num = Integer.parseInt(r.get(0).substring(2));
                    return "MA" + (num + 1);
                } catch (NumberFormatException ignored) {}
            }
            return "MA100";
        } finally { em.close(); }
    }

    public boolean themMonAn(MonAn m) {
        if (m.getMaMonAn() == null || m.getMaMonAn().isEmpty() || m.getMaMonAn().equals("Tự động tạo"))
            m.setMaMonAn(getNextMaMonAn());
        try {
            inTransactionVoid(em -> em.persist(m));
            SocketManager.sendEvent(SocketEvent.MENU_UPDATED, Map.of());
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean capNhatMonAn(MonAn m) {
        try {
            inTransactionVoid(em -> em.merge(m));
            SocketManager.sendEvent(SocketEvent.MENU_UPDATED, Map.of());
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean xoaMonAn(String maMon) {
        try {
            inTransactionVoid(em -> {
                MonAn m = em.find(MonAn.class, maMon);
                if (m != null) em.remove(m);
            });
            SocketManager.sendEvent(SocketEvent.MENU_UPDATED, Map.of());
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }
}
