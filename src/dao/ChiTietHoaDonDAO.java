package dao;

import entity.ChiTietHoaDon;
import entity.ChiTietHoaDonId;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ChiTietHoaDonDAO extends BaseDAO {

    public List<ChiTietHoaDon> getChiTietTheoMaDon(String maDon) {
        EntityManager em = getEM();
        try {
            List<Object[]> rows = em.createNativeQuery(
                    "SELECT ct.maDon, ct.maMonAn, m.tenMon, ct.soLuong, ct.donGia " +
                    "FROM ChiTietHoaDon ct JOIN MonAn m ON ct.maMonAn = m.maMonAn WHERE ct.maDon = ?")
                    .setParameter(1, maDon).getResultList();
            List<ChiTietHoaDon> list = new ArrayList<>();
            for (Object[] r : rows) {
                list.add(new ChiTietHoaDon(
                        r[0].toString(), r[1].toString(), r[2] != null ? r[2].toString() : "",
                        ((Number) r[3]).intValue(), ((Number) r[4]).doubleValue()));
            }
            return list;
        } finally { em.close(); }
    }

    public boolean themChiTiet(ChiTietHoaDon ct) {
        try {
            inTransactionVoid(em ->
                em.createNativeQuery(
                    "INSERT INTO ChiTietHoaDon(maDon, maMonAn, soLuong, donGia) VALUES(?,?,?,?)")
                    .setParameter(1, ct.getMaDon())
                    .setParameter(2, ct.getMaMon())
                    .setParameter(3, ct.getSoluong())
                    .setParameter(4, ct.getDongia())
                    .executeUpdate());
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean xoaChiTiet(String maDon, String maMon) {
        try {
            inTransactionVoid(em ->
                em.createNativeQuery(
                    "DELETE FROM ChiTietHoaDon WHERE maDon = ? AND maMonAn = ?")
                    .setParameter(1, maDon).setParameter(2, maMon).executeUpdate());
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean xoaHetChiTietTheoMaDon(String maDon) {
        try {
            inTransactionVoid(em ->
                em.createNativeQuery("DELETE FROM ChiTietHoaDon WHERE maDon = ?")
                        .setParameter(1, maDon).executeUpdate());
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean suaChiTiet(ChiTietHoaDon ct) {
        try {
            inTransactionVoid(em ->
                em.createNativeQuery(
                    "UPDATE ChiTietHoaDon SET soLuong = ?, trangThaiMon = 'Chờ' " +
                    "WHERE maDon = ? AND maMonAn = ?")
                        .setParameter(1, ct.getSoluong()).setParameter(2, ct.getMaDon())
                        .setParameter(3, ct.getMaMon()).executeUpdate());
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> getAllMonDangCho() {
        EntityManager em = getEM();
        try {
            return em.createNativeQuery(
                    "SELECT ct.maDon, ddm.maBan, b.tenBan, ct.maMonAn, m.tenMon, " +
                    "(ct.soLuong - ct.soLuongDaXacNhan) AS soLuongChoBep, ddm.ngayKhoiTao " +
                    "FROM ChiTietHoaDon ct " +
                    "JOIN DonDatMon ddm ON ct.maDon = ddm.maDon " +
                    "JOIN Ban b ON ddm.maBan = b.maBan " +
                    "JOIN MonAn m ON ct.maMonAn = m.maMonAn " +
                    "WHERE b.trangThai = 'DANG_PHUC_VU' " +
                    "AND ddm.trangThai = 'Chưa thanh toán' " +
                    "AND ct.trangThaiMon = 'Chờ' " +
                    "AND (ct.soLuong - ct.soLuongDaXacNhan) > 0 " +
                    "ORDER BY ddm.ngayKhoiTao, ddm.maBan")
                    .getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally { em.close(); }
    }

    public boolean xacNhanMon(String maDon, String maMon) {
        try {
            inTransactionVoid(em ->
                em.createNativeQuery(
                    "UPDATE ChiTietHoaDon SET trangThaiMon = 'Đã lên', soLuongDaXacNhan = soLuong " +
                    "WHERE maDon = ? AND maMonAn = ?")
                    .setParameter(1, maDon).setParameter(2, maMon).executeUpdate());
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public Map<String, Integer> getTopSellingItems(LocalDate startDate, LocalDate endDate, int limit) {
        EntityManager em = getEM();
        try {
            List<Object[]> rows = em.createNativeQuery(
                    "SELECT m.tenMon, SUM(ct.soLuong) AS TongSoLuong " +
                    "FROM ChiTietHoaDon ct JOIN MonAn m ON ct.maMonAn = m.maMonAn " +
                    "JOIN DonDatMon ddm ON ct.maDon = ddm.maDon " +
                    "JOIN HoaDon hd ON ddm.maDon = hd.maDon " +
                    "WHERE hd.trangThai = 'Đã thanh toán' AND hd.ngayLap >= ? AND hd.ngayLap < ? " +
                    "GROUP BY m.tenMon ORDER BY TongSoLuong DESC LIMIT ?")
                    .setParameter(1, java.sql.Date.valueOf(startDate))
                    .setParameter(2, java.sql.Date.valueOf(endDate.plusDays(1)))
                    .setParameter(3, limit).getResultList();
            Map<String, Integer> result = new LinkedHashMap<>();
            for (Object[] r : rows) result.put(r[0].toString(), ((Number) r[1]).intValue());
            return result;
        } finally { em.close(); }
    }

    public Map<String, Integer> getLeastSellingItems(LocalDate startDate, LocalDate endDate, int limit) {
        EntityManager em = getEM();
        try {
            List<Object[]> rows = em.createNativeQuery(
                    "SELECT ma.tenMon, SUM(ct.soLuong) AS SoLuongBan " +
                    "FROM ChiTietHoaDon ct JOIN MonAn ma ON ct.maMonAn = ma.maMonAn " +
                    "JOIN HoaDon hd ON ct.maDon = hd.maDon " +
                    "WHERE hd.ngayLap >= ? AND hd.ngayLap < ? AND hd.trangThai = 'Đã thanh toán' " +
                    "GROUP BY ma.tenMon ORDER BY SoLuongBan ASC LIMIT ?")
                    .setParameter(1, java.sql.Date.valueOf(startDate))
                    .setParameter(2, java.sql.Date.valueOf(endDate.plusDays(1)))
                    .setParameter(3, limit).getResultList();
            Map<String, Integer> result = new LinkedHashMap<>();
            for (Object[] r : rows) result.put(r[0].toString(), ((Number) r[1]).intValue());
            return result;
        } finally { em.close(); }
    }

    public List<String> getTopMonBanChayTrongNgay() {
        EntityManager em = getEM();
        try {
            List<Object[]> rows = em.createNativeQuery(
                    "SELECT m.tenMon, SUM(ct.soLuong) AS SL " +
                    "FROM ChiTietHoaDon ct JOIN HoaDon hd ON ct.maDon = hd.maDon " +
                    "JOIN MonAn m ON ct.maMonAn = m.maMonAn " +
                    "WHERE CAST(hd.ngayLap AS DATE) = CURDATE() " +
                    "GROUP BY m.tenMon ORDER BY SL DESC LIMIT 3")
                    .getResultList();
            List<String> list = new ArrayList<>();
            int rank = 1;
            for (Object[] r : rows) {
                list.add("#" + rank + " " + r[0] + " (" + r[1] + " suất)");
                rank++;
            }
            if (list.isEmpty()) list.add("Chưa có dữ liệu hôm nay");
            return list;
        } finally { em.close(); }
    }
}
