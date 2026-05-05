package dao;

import entity.ChiTietHoaDon;
import entity.HoaDon;
import jakarta.persistence.EntityManager;
import socket.SocketEvent;
import socket.SocketManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class HoaDonDAO extends BaseDAO {

    private final ChiTietHoaDonDAO chiTietDAO = new ChiTietHoaDonDAO();
    private static final int ITEMS_PER_PAGE = 15;

    // ─── helpers ──────────────────────────────────────────────────────────────

    private String buildWhereClause(String trangThai, String keyword,
                                     LocalDateTime tuNgay, LocalDateTime denNgay) {
        StringBuilder sb = new StringBuilder(" WHERE 1=1");
        if (!"Tất cả".equalsIgnoreCase(trangThai))        sb.append(" AND hd.trangThai = ?");
        if (keyword != null && !keyword.trim().isEmpty())  sb.append(" AND hd.maHD LIKE ?");
        if (tuNgay  != null)                               sb.append(" AND hd.ngayLap >= ?");
        if (denNgay != null)                               sb.append(" AND hd.ngayLap <= ?");
        return sb.toString();
    }

    private void setFilterParams(jakarta.persistence.Query q, String trangThai, String keyword,
                                  LocalDateTime tuNgay, LocalDateTime denNgay, int[] pos) {
        if (!"Tất cả".equalsIgnoreCase(trangThai))       q.setParameter(pos[0]++, trangThai);
        if (keyword != null && !keyword.trim().isEmpty()) q.setParameter(pos[0]++, "%" + keyword + "%");
        if (tuNgay  != null)                              q.setParameter(pos[0]++, java.sql.Timestamp.valueOf(tuNgay));
        if (denNgay != null)                              q.setParameter(pos[0]++, java.sql.Timestamp.valueOf(denNgay));
    }

    private HoaDon rowToHoaDon(Object[] r) {
        String maHD = r[0].toString();
        LocalDateTime ngayLap = ((java.sql.Timestamp) r[1]).toLocalDateTime();
        float tongTien = ((Number) r[2]).floatValue();
        String trangThai = r[3] != null ? r[3].toString() : "";
        String hinhThuc = r[4] != null ? r[4].toString() : null;
        float tienKhach = r[5] != null ? ((Number) r[5]).floatValue() : 0;
        float giamGia   = r[6] != null ? ((Number) r[6]).floatValue() : 0;
        String maNV  = r[7] != null ? r[7].toString() : null;
        String maKM  = r[8] != null ? r[8].toString() : null;
        String maDon = r[9] != null ? r[9].toString() : null;
        String tenBan = r[10] != null ? r[10].toString() : null;

        HoaDon hd = new HoaDon(maHD, ngayLap, trangThai, hinhThuc, maDon, maNV, maKM);
        hd.setTongTienTuDB(tongTien + giamGia);
        hd.setGiamGia(giamGia);
        hd.setTienKhachDua(tienKhach);
        hd.setTenBan(tenBan);
        hd.capNhatTongThanhToanTuCacThanhPhan();
        return hd;
    }

    // ─── public API ───────────────────────────────────────────────────────────

    public int getTotalHoaDonCount(String trangThai, String keyword,
                                    LocalDateTime tuNgay, LocalDateTime denNgay) {
        EntityManager em = getEM();
        try {
            String sql = "SELECT COUNT(hd.maHD) FROM HoaDon hd" +
                         buildWhereClause(trangThai, keyword, tuNgay, denNgay);
            jakarta.persistence.Query q = em.createNativeQuery(sql);
            int[] pos = {1};
            setFilterParams(q, trangThai, keyword, tuNgay, denNgay, pos);
            return ((Number) q.getSingleResult()).intValue();
        } catch (Exception e) { e.printStackTrace(); return 0; }
        finally { em.close(); }
    }

    @SuppressWarnings("unchecked")
    public List<HoaDon> getHoaDonByPage(int page, String trangThai, String keyword,
                                         LocalDateTime tuNgay, LocalDateTime denNgay) {
        EntityManager em = getEM();
        try {
            int offset = (page - 1) * ITEMS_PER_PAGE;
            String sql = "SELECT hd.maHD, hd.ngayLap, hd.tongTien, hd.trangThai, hd.hinhThucThanhToan, " +
                         "hd.tienKhachDua, hd.giamGia, hd.maNV, hd.maKM, hd.maDon, b.tenBan " +
                         "FROM HoaDon hd LEFT JOIN DonDatMon ddm ON hd.maDon = ddm.maDon " +
                         "LEFT JOIN Ban b ON ddm.maBan = b.maBan" +
                         buildWhereClause(trangThai, keyword, tuNgay, denNgay) +
                         " ORDER BY hd.ngayLap DESC LIMIT ?, ?";
            jakarta.persistence.Query q = em.createNativeQuery(sql);
            int[] pos = {1};
            setFilterParams(q, trangThai, keyword, tuNgay, denNgay, pos);
            q.setParameter(pos[0]++, offset);
            q.setParameter(pos[0],   ITEMS_PER_PAGE);
            List<HoaDon> list = new ArrayList<>();
            for (Object[] r : (List<Object[]>) q.getResultList()) {
                try { list.add(rowToHoaDon(r)); } catch (Exception ignored) {}
            }
            return list;
        } catch (Exception e) { e.printStackTrace(); return new ArrayList<>(); }
        finally { em.close(); }
    }

    @SuppressWarnings("unchecked")
    public List<HoaDon> getAllHoaDonFiltered(String trangThai, String keyword,
                                              LocalDateTime tuNgay, LocalDateTime denNgay) {
        EntityManager em = getEM();
        try {
            String sql = "SELECT hd.maHD, hd.ngayLap, hd.tongTien, hd.trangThai, hd.hinhThucThanhToan, " +
                         "hd.tienKhachDua, hd.giamGia, hd.maNV, hd.maKM, hd.maDon, b.tenBan " +
                         "FROM HoaDon hd LEFT JOIN DonDatMon ddm ON hd.maDon = ddm.maDon " +
                         "LEFT JOIN Ban b ON ddm.maBan = b.maBan" +
                         buildWhereClause(trangThai, keyword, tuNgay, denNgay) +
                         " ORDER BY hd.ngayLap DESC";
            jakarta.persistence.Query q = em.createNativeQuery(sql);
            int[] pos = {1};
            setFilterParams(q, trangThai, keyword, tuNgay, denNgay, pos);
            List<HoaDon> list = new ArrayList<>();
            for (Object[] r : (List<Object[]>) q.getResultList()) {
                try { list.add(rowToHoaDon(r)); } catch (Exception ignored) {}
            }
            return list;
        } catch (Exception e) { e.printStackTrace(); return new ArrayList<>(); }
        finally { em.close(); }
    }

    public HoaDon getHoaDonChuaThanhToan(String maBan) {
        EntityManager em = getEM();
        try {
            List<Object[]> rows = em.createNativeQuery(
                    "SELECT hd.maHD, hd.ngayLap, hd.tongTien, hd.trangThai, hd.hinhThucThanhToan, " +
                    "hd.tienKhachDua, hd.giamGia, hd.maNV, hd.maKM, hd.maDon, NULL, ddm.maKH " +
                    "FROM HoaDon hd JOIN DonDatMon ddm ON hd.maDon = ddm.maDon " +
                    "WHERE ddm.maBan = ? AND hd.trangThai = 'Chưa thanh toán'")
                    .setParameter(1, maBan).getResultList();
            if (rows.isEmpty()) return null;
            Object[] r = rows.get(0);
            HoaDon hd = rowToHoaDon(r);
            if (r[11] != null) hd.setMaKH(r[11].toString());
            hd.setDsChiTiet(chiTietDAO.getChiTietTheoMaDon(hd.getMaDon()));
            hd.tinhLaiTongTienTuChiTiet();
            return hd;
        } catch (Exception e) { e.printStackTrace(); return null; }
        finally { em.close(); }
    }

    public boolean capNhatMaKM(String maHD, String maKM) {
        try {
            inTransactionVoid(em -> {
                if (maKM != null && !maKM.isEmpty())
                    em.createNativeQuery("UPDATE HoaDon SET maKM = ? WHERE maHD = ?")
                            .setParameter(1, maKM).setParameter(2, maHD).executeUpdate();
                else
                    em.createNativeQuery("UPDATE HoaDon SET maKM = NULL WHERE maHD = ?")
                            .setParameter(1, maHD).executeUpdate();
            });
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean capNhatTongTien(String maHD, float tongTienMoi) {
        try {
            inTransactionVoid(em ->
                em.createNativeQuery("UPDATE HoaDon SET tongTien = ? WHERE maHD = ?")
                        .setParameter(1, tongTienMoi).setParameter(2, maHD).executeUpdate());
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean themHoaDon(HoaDon hd) {
        try {
            inTransactionVoid(em -> em.createNativeQuery(
                    "INSERT INTO HoaDon(maHD, ngayLap, tongTien, trangThai, hinhThucThanhToan, " +
                    "tienKhachDua, maNV, maKM, maDon) VALUES(?,NOW(),?,?,?,?,?,?,?)")
                    .setParameter(1, hd.getMaHD())
                    .setParameter(2, hd.getTongTien())
                    .setParameter(3, hd.getTrangThai())
                    .setParameter(4, hd.getHinhThucThanhToan())
                    .setParameter(5, hd.getTienKhachDua())
                    .setParameter(6, hd.getMaNV())
                    .setParameter(7, hd.getMaKM())
                    .setParameter(8, hd.getMaDon())
                    .executeUpdate());
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean thanhToanHoaDon(String maHD, double tongTien, double tienKhachDua,
                                    String hinhThucTT, double tienGiamGia, String maKM,
                                    String tenBanGhiLai) {
        boolean result = inTransaction(em -> {
            List<Object[]> info = em.createNativeQuery(
                    "SELECT d.maDon, d.maBan FROM HoaDon h JOIN DonDatMon d ON h.maDon = d.maDon WHERE h.maHD = ?")
                    .setParameter(1, maHD).getResultList();
            if (info.isEmpty()) return false;
            String maDonHT = info.get(0)[0].toString();
            String maBanChinh = info.get(0)[1] != null ? info.get(0)[1].toString() : null;

            // Cập nhật HoaDon
            em.createNativeQuery(
                    "UPDATE HoaDon SET trangThai='Đã thanh toán', ngayLap=NOW(), tongTien=?, " +
                    "tienKhachDua=?, hinhThucThanhToan=?, giamGia=?, maKM=?, tenBan=? WHERE maHD=?")
                    .setParameter(1, tongTien).setParameter(2, tienKhachDua)
                    .setParameter(3, hinhThucTT).setParameter(4, tienGiamGia)
                    .setParameter(5, maKM).setParameter(6, tenBanGhiLai).setParameter(7, maHD)
                    .executeUpdate();

            // Đóng đơn chính
            em.createNativeQuery("UPDATE DonDatMon SET trangThai='Đã thanh toán' WHERE maDon=?")
                    .setParameter(1, maDonHT).executeUpdate();

            // Tìm các bàn được ghép
            List<String> banChecks = new ArrayList<>();
            if (maBanChinh != null) {
                banChecks.add(maBanChinh);
                List<?> linked = em.createNativeQuery(
                        "SELECT maBan FROM DonDatMon WHERE ghiChu=? " +
                        "AND trangThai NOT IN('Đã thanh toán','Đã hủy')")
                        .setParameter(1, "LINKED:" + maBanChinh).getResultList();
                for (Object b : linked) banChecks.add(b.toString());
            }

            // Đóng các đơn dummy linked
            if (banChecks.size() > 1) {
                em.createNativeQuery(
                        "UPDATE DonDatMon SET trangThai='Đã thanh toán' WHERE ghiChu=? " +
                        "AND maBan IN (" + String.join(",", banChecks.stream()
                                .map(b -> "'" + b + "'").toArray(String[]::new)) + ")")
                        .setParameter(1, "LINKED:" + maBanChinh).executeUpdate();
            }

            // Cập nhật trạng thái các bàn
            for (String mb : banChecks) {
                long soDonCho = ((Number) em.createNativeQuery(
                        "SELECT COUNT(*) FROM DonDatMon WHERE maBan=? AND maDon!=? " +
                        "AND (ghiChu IS NULL OR ghiChu NOT LIKE 'LINKED:%') " +
                        "AND trangThai NOT IN('Đã thanh toán','Đã hủy')")
                        .setParameter(1, mb).setParameter(2, maDonHT).getSingleResult()).longValue();
                String ttMoi = (soDonCho > 0) ? "DA_DAT_TRUOC" : "TRONG";
                em.createNativeQuery(
                        "UPDATE Ban SET gioMoBan=NULL, trangThai=?, " +
                        "tenBan=CASE " +
                        "WHEN LOCATE(' (Ghép', tenBan) > 0 THEN RTRIM(LEFT(tenBan, LOCATE(' (Ghép', tenBan)-1)) " +
                        "WHEN LOCATE(' +', tenBan) > 0 THEN RTRIM(LEFT(tenBan, LOCATE(' +', tenBan)-1)) " +
                        "ELSE tenBan END WHERE maBan=?")
                        .setParameter(1, ttMoi).setParameter(2, mb).executeUpdate();
            }
            return true;
        });
        if (


                result) SocketManager.sendEvent(SocketEvent.HOA_DON_THANH_TOAN,
                Map.of("maHD", maHD));
        return result;
    }

    public double getDoanhThuTheoHinhThuc(String maNV, LocalDateTime thoiGianBatDauCa, String hinhThuc) {
        EntityManager em = getEM();
        try {
            Object r = em.createNativeQuery(
                    "SELECT IFNULL(SUM(tongTien - IFNULL(giamGia,0)),0) FROM HoaDon " +
                    "WHERE maNV=? AND ngayLap>=? AND trangThai='Đã thanh toán' AND hinhThucThanhToan=?")
                    .setParameter(1, maNV)
                    .setParameter(2, java.sql.Timestamp.valueOf(thoiGianBatDauCa))
                    .setParameter(3, hinhThuc).getSingleResult();
            return ((Number) r).doubleValue();
        } catch (Exception e) { return 0; }
        finally { em.close(); }
    }

    public Map<LocalDate, Double> getDailyRevenue(LocalDate startDate, LocalDate endDate) {
        EntityManager em = getEM();
        try {
            List<Object[]> rows = em.createNativeQuery(
                    "SELECT CAST(ngayLap AS DATE) AS Ngay, SUM(tongTien) FROM HoaDon " +
                    "WHERE trangThai='Đã thanh toán' AND ngayLap>=? AND ngayLap<? " +
                    "GROUP BY CAST(ngayLap AS DATE) ORDER BY Ngay")
                    .setParameter(1, java.sql.Timestamp.valueOf(startDate.atStartOfDay()))
                    .setParameter(2, java.sql.Timestamp.valueOf(endDate.plusDays(1).atStartOfDay()))
                    .getResultList();
            Map<LocalDate, Double> result = new LinkedHashMap<>();
            for (Object[] r : rows)
                result.put(((java.sql.Date) r[0]).toLocalDate(), ((Number) r[1]).doubleValue());
            return result;
        } finally { em.close(); }
    }

    public int getOrderCount(LocalDate startDate, LocalDate endDate) {
        EntityManager em = getEM();
        try {
            Object r = em.createNativeQuery(
                    "SELECT COUNT(maHD) FROM HoaDon WHERE trangThai='Đã thanh toán' AND ngayLap>=? AND ngayLap<?")
                    .setParameter(1, java.sql.Timestamp.valueOf(startDate.atStartOfDay()))
                    .setParameter(2, java.sql.Timestamp.valueOf(endDate.plusDays(1).atStartOfDay()))
                    .getSingleResult();
            return ((Number) r).intValue();
        } finally { em.close(); }
    }
}
