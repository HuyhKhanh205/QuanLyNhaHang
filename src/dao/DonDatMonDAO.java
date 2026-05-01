package dao;

import entity.DonDatMon;
import jakarta.persistence.EntityManager;
import socket.SocketEvent;
import socket.SocketManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DonDatMonDAO extends BaseDAO {

    private DonDatMon rowToEntity(Object[] r) {
        DonDatMon d = new DonDatMon(
                r[0].toString(),
                ((java.sql.Timestamp) r[1]).toLocalDateTime(),
                r[4] != null ? r[4].toString() : null,
                r[5] != null ? r[5].toString() : null,
                r[6] != null ? r[6].toString() : null,
                r[7] != null ? r[7].toString() : "");
        d.setThoiGianDen(r[2] != null ? ((java.sql.Timestamp) r[2]).toLocalDateTime() : null);
        d.setTrangThai(r[3] != null ? r[3].toString() : null);
        return d;
    }

    public DonDatMon getDonDatMonByMa(String maDon) {
        EntityManager em = getEM();
        try { return em.find(DonDatMon.class, maDon); }
        finally { em.close(); }
    }

    public boolean themDonDatMon(DonDatMon ddm) {
        try {
            inTransactionVoid(em -> em.persist(ddm));
            SocketManager.sendEvent(SocketEvent.ORDER_CREATED,
                    Map.of("maDon", ddm.getMaDon(),
                           "maBan", ddm.getMaBan() != null ? ddm.getMaBan() : ""));
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean xoaDonDatMon(String maDon) {
        try {
            inTransactionVoid(em -> {
                DonDatMon d = em.find(DonDatMon.class, maDon);
                if (d != null) em.remove(d);
            });
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public String getMaBanByMaDon(String maDon) {
        EntityManager em = getEM();
        try {
            List<String> r = em.createQuery("SELECT d.maBan FROM DonDatMon d WHERE d.maDon = :ma", String.class)
                    .setParameter("ma", maDon).getResultList();
            return r.isEmpty() ? null : r.get(0);
        } finally { em.close(); }
    }

    public boolean capNhatMaKH(String maDon, String maKH) {
        try {
            inTransactionVoid(em -> {
                if (maKH != null && !maKH.isEmpty())
                    em.createNativeQuery("UPDATE DonDatMon SET maKH=? WHERE maDon=?")
                            .setParameter(1, maKH).setParameter(2, maDon).executeUpdate();
                else
                    em.createNativeQuery("UPDATE DonDatMon SET maKH=NULL WHERE maDon=?")
                            .setParameter(1, maDon).executeUpdate();
            });
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean capNhatGhiChu(String maDon, String ghiChu) {
        try {
            inTransactionVoid(em ->
                em.createNativeQuery("UPDATE DonDatMon SET ghiChu=? WHERE maDon=?")
                        .setParameter(1, ghiChu).setParameter(2, maDon).executeUpdate());
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    @SuppressWarnings("unchecked")
    public List<DonDatMon> getAllDonDatMonChuaNhan() {
        EntityManager em = getEM();
        try {
            List<Object[]> rows = em.createNativeQuery(
                    "SELECT maDon, ngayKhoiTao, thoiGianDen, trangThai, maNV, maKH, maBan, ghiChu " +
                    "FROM DonDatMon d WHERE d.trangThai='Chưa thanh toán' " +
                    "AND NOT EXISTS (SELECT 1 FROM HoaDon h WHERE h.maDon=d.maDon) " +
                    "AND (d.ghiChu IS NULL OR d.ghiChu NOT LIKE '%LINKED:%') " +
                    "AND d.thoiGianDen >= CURDATE() ORDER BY d.thoiGianDen ASC")
                    .getResultList();
            List<DonDatMon> list = new ArrayList<>();
            for (Object[] r : rows) list.add(rowToEntity(r));
            return list;
        } catch (Exception e) { e.printStackTrace(); return new ArrayList<>(); }
        finally { em.close(); }
    }

    public DonDatMon getDonDatMonDatTruoc(String maBan) {
        EntityManager em = getEM();
        try {
            List<Object[]> rows = em.createNativeQuery(
                    "SELECT maDon, ngayKhoiTao, thoiGianDen, trangThai, maNV, maKH, maBan, ghiChu " +
                    "FROM DonDatMon WHERE maBan=? AND trangThai='Chưa thanh toán' " +
                    "AND (ghiChu IS NULL OR ghiChu NOT LIKE 'LINKED:%') " +
                    "AND NOT EXISTS (SELECT 1 FROM HoaDon hd WHERE hd.maDon=DonDatMon.maDon) " +
                    "ORDER BY thoiGianDen ASC LIMIT 1")
                    .setParameter(1, maBan).getResultList();
            return rows.isEmpty() ? null : rowToEntity(rows.get(0));
        } finally { em.close(); }
    }

    public String getMaBanDichCuaBanGhep(String maBanHienTai) {
        EntityManager em = getEM();
        try {
            List<?> r = em.createNativeQuery(
                    "SELECT ghiChu FROM DonDatMon WHERE maBan=? AND trangThai='Chưa thanh toán' " +
                    "AND trangThai!='Đã hủy'")
                    .setParameter(1, maBanHienTai).getResultList();
            if (!r.isEmpty()) {
                String gc = r.get(0).toString();
                if (gc.startsWith("LINKED:")) return gc.substring(7).trim();
            }
            return null;
        } finally { em.close(); }
    }

    public List<String> getMaBanCungDotDat(String maKH, LocalDateTime thoiGianDen, String maBanHienTai) {
        if (maKH == null || maKH.isEmpty()) return new ArrayList<>();
        EntityManager em = getEM();
        try {
            List<?> r = em.createNativeQuery(
                    "SELECT maBan FROM DonDatMon WHERE maKH=? AND maBan!=? AND trangThai!='Đã hủy' " +
                    "AND NOT EXISTS(SELECT 1 FROM HoaDon hd WHERE hd.maDon=DonDatMon.maDon) " +
                    "AND TIMESTAMPDIFF(MINUTE, thoiGianDen, ?)=0")
                    .setParameter(1, maKH).setParameter(2, maBanHienTai)
                    .setParameter(3, java.sql.Timestamp.valueOf(thoiGianDen)).getResultList();
            List<String> list = new ArrayList<>();
            for (Object o : r) list.add(o.toString());
            return list;
        } finally { em.close(); }
    }

    public List<String> getMaBanDaDatTrongKhoang(LocalDateTime tuGio, LocalDateTime denGio) {
        EntityManager em = getEM();
        try {
            List<?> r = em.createNativeQuery(
                    "SELECT DISTINCT maBan FROM DonDatMon WHERE thoiGianDen BETWEEN ? AND ? " +
                    "AND trangThai NOT IN('Đã hủy','Đã thanh toán')")
                    .setParameter(1, java.sql.Timestamp.valueOf(tuGio))
                    .setParameter(2, java.sql.Timestamp.valueOf(denGio)).getResultList();
            List<String> list = new ArrayList<>();
            for (Object o : r) list.add(o.toString());
            return list;
        } finally { em.close(); }
    }

    public int tuDongHuyDonQuaGio() {
        return inTransaction(em -> {
            LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
            List<Object[]> quaHan = em.createNativeQuery(
                    "SELECT maDon, maBan FROM DonDatMon WHERE thoiGianDen<? " +
                    "AND trangThai='Chưa thanh toán' AND trangThai!='Đã hủy' " +
                    "AND NOT EXISTS(SELECT 1 FROM HoaDon hd WHERE hd.maDon=DonDatMon.maDon)")
                    .setParameter(1, java.sql.Timestamp.valueOf(oneHourAgo)).getResultList();
            int count = 0;
            for (Object[] item : quaHan) {
                em.createNativeQuery("UPDATE DonDatMon SET trangThai='Đã hủy', " +
                        "ghiChu=CONCAT(IFNULL(ghiChu,''),' (Hủy tự động do quá giờ)') WHERE maDon=?")
                        .setParameter(1, item[0]).executeUpdate();
                em.createNativeQuery("UPDATE Ban SET trangThai='TRONG', gioMoBan=NULL " +
                        "WHERE maBan=? AND trangThai='DA_DAT_TRUOC'")
                        .setParameter(1, item[1]).executeUpdate();
                count++;
            }
            return count;
        });
    }

    public void capNhatTrangThaiBanTheoGio() {
        inTransactionVoid(em -> {
            em.createNativeQuery(
                    "UPDATE Ban SET trangThai='DA_DAT_TRUOC' WHERE trangThai='TRONG' AND maBan IN (" +
                    "SELECT maBan FROM DonDatMon WHERE trangThai='Chưa thanh toán' " +
                    "AND NOT EXISTS(SELECT 1 FROM HoaDon hd WHERE hd.maDon=DonDatMon.maDon) " +
                    "AND TIMESTAMPDIFF(MINUTE, NOW(), thoiGianDen) <= 120)")
                    .executeUpdate();
            em.createNativeQuery(
                    "UPDATE Ban SET trangThai='TRONG', gioMoBan=NULL WHERE trangThai='DA_DAT_TRUOC' " +
                    "AND maBan NOT IN (SELECT maBan FROM DonDatMon WHERE trangThai='Chưa thanh toán' " +
                    "AND TIMESTAMPDIFF(MINUTE, NOW(), thoiGianDen) <= 120)")
                    .executeUpdate();
        });
    }

    @SuppressWarnings("unchecked")
    public List<DonDatMon> timDonDatMonChuaNhan(String query) {
        EntityManager em = getEM();
        try {
            String like = "%" + query + "%";
            List<Object[]> rows = em.createNativeQuery(
                    "SELECT ddm.maDon, ddm.ngayKhoiTao, ddm.thoiGianDen, ddm.trangThai, " +
                    "ddm.maNV, ddm.maKH, ddm.maBan, ddm.ghiChu " +
                    "FROM DonDatMon ddm LEFT JOIN KhachHang kh ON ddm.maKH=kh.maKH " +
                    "WHERE NOT EXISTS(SELECT 1 FROM HoaDon hd WHERE hd.maDon=ddm.maDon) " +
                    "AND ddm.trangThai='Chưa thanh toán' AND ddm.trangThai!='Đã hủy' " +
                    "AND (ddm.ghiChu IS NULL OR ddm.ghiChu NOT LIKE 'LINKED:%') " +
                    "AND (kh.sdt LIKE ? OR kh.tenKH LIKE ?) ORDER BY ddm.ngayKhoiTao DESC")
                    .setParameter(1, like).setParameter(2, like).getResultList();
            List<DonDatMon> list = new ArrayList<>();
            for (Object[] r : rows) list.add(rowToEntity(r));
            return list;
        } catch (Exception e) { e.printStackTrace(); return new ArrayList<>(); }
        finally { em.close(); }
    }
}
