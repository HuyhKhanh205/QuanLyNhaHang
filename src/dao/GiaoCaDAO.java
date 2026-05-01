package dao;

import entity.GiaoCa;
import jakarta.persistence.EntityManager;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GiaoCaDAO extends BaseDAO {

    public int getMaCaDangLamViec(String maNV) {
        if (maNV == null || maNV.trim().isEmpty()) return -1;
        EntityManager em = getEM();
        try {
            List<?> r = em.createNativeQuery(
                    "SELECT maGiaoCa FROM GiaoCa WHERE maNV=? AND thoiGianKetThuc IS NULL " +
                    "ORDER BY thoiGianBatDau DESC LIMIT 1")
                    .setParameter(1, maNV).getResultList();
            return r.isEmpty() ? -1 : ((Number) r.get(0)).intValue();
        } finally { em.close(); }
    }

    public boolean batDauCa(String maNV, double tienDauCa) {
        if (maNV == null || maNV.trim().isEmpty() || tienDauCa < 0) return false;
        if (getMaCaDangLamViec(maNV) > 0) return false;
        try {
            inTransactionVoid(em ->
                em.createNativeQuery("INSERT INTO GiaoCa(maNV, thoiGianBatDau, tienDauCa) VALUES(?, NOW(), ?)")
                        .setParameter(1, maNV).setParameter(2, tienDauCa).executeUpdate());
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public GiaoCa getThongTinCaDangLam(String maNV) {
        if (maNV == null || maNV.trim().isEmpty()) return null;
        EntityManager em = getEM();
        try {
            List<Object[]> r = em.createNativeQuery(
                    "SELECT maGiaoCa, maNV, thoiGianBatDau, tienDauCa FROM GiaoCa " +
                    "WHERE maNV=? AND thoiGianKetThuc IS NULL ORDER BY thoiGianBatDau DESC LIMIT 1")
                    .setParameter(1, maNV).getResultList();
            if (r.isEmpty()) return null;
            Object[] row = r.get(0);
            return new GiaoCa(((Number) row[0]).intValue(), row[1].toString(),
                    ((java.sql.Timestamp) row[2]).toLocalDateTime(), ((Number) row[3]).doubleValue());
        } finally { em.close(); }
    }

    public boolean ketThucCa(int maGiaoCa, double tienCuoiCa, String ghiChu) {
        if (maGiaoCa <= 0 || tienCuoiCa < 0) return false;
        return inTransaction(em -> {
            List<Object[]> info = em.createNativeQuery(
                    "SELECT maNV, thoiGianBatDau, tienDauCa FROM GiaoCa WHERE maGiaoCa=?")
                    .setParameter(1, maGiaoCa).getResultList();
            if (info.isEmpty()) return false;
            Object[] row = info.get(0);
            String maNV = row[0].toString();
            LocalDateTime tbd = ((java.sql.Timestamp) row[1]).toLocalDateTime();
            double tienDauCa = ((Number) row[2]).doubleValue();

            Object sys = em.createNativeQuery(
                    "SELECT IFNULL(SUM(tongTien - IFNULL(giamGia,0)),0) FROM HoaDon " +
                    "WHERE maNV=? AND trangThai='Đã thanh toán' AND hinhThucThanhToan='Tiền mặt' " +
                    "AND ngayLap>=? AND ngayLap<=NOW()")
                    .setParameter(1, maNV).setParameter(2, java.sql.Timestamp.valueOf(tbd))
                    .getSingleResult();
            double tienHT = ((Number) sys).doubleValue();
            double chenhLech = tienCuoiCa - (tienDauCa + tienHT);

            em.createNativeQuery("UPDATE GiaoCa SET thoiGianKetThuc=NOW(), tienCuoiCa=?, " +
                    "tienHeThongTinh=?, chenhLech=?, ghiChu=? WHERE maGiaoCa=?")
                    .setParameter(1, tienCuoiCa).setParameter(2, tienHT)
                    .setParameter(3, chenhLech).setParameter(4, ghiChu)
                    .setParameter(5, maGiaoCa).executeUpdate();
            return true;
        });
    }

    public double getTongGioLamTheoThang(String maNV, LocalDate startOfMonth) {
        if (maNV == null || maNV.trim().isEmpty() || startOfMonth == null) return 0;
        EntityManager em = getEM();
        try {
            Object r = em.createNativeQuery(
                    "SELECT IFNULL(SUM(CASE WHEN TIMESTAMPDIFF(MINUTE,thoiGianBatDau," +
                    "IFNULL(thoiGianKetThuc,NOW()))<0 THEN 0 ELSE " +
                    "TIMESTAMPDIFF(MINUTE,thoiGianBatDau,IFNULL(thoiGianKetThuc,NOW())) END),0) " +
                    "FROM GiaoCa WHERE maNV=? AND MONTH(thoiGianBatDau)=? AND YEAR(thoiGianBatDau)=? " +
                    "AND thoiGianBatDau<=NOW()")
                    .setParameter(1, maNV).setParameter(2, startOfMonth.getMonthValue())
                    .setParameter(3, startOfMonth.getYear()).getSingleResult();
            return ((Number) r).doubleValue() / 60.0;
        } finally { em.close(); }
    }

    public double getTongGioLamTheoTuan(String maNV, LocalDate startOfWeek) {
        if (maNV == null || maNV.trim().isEmpty() || startOfWeek == null) return 0;
        EntityManager em = getEM();
        try {
            Object r = em.createNativeQuery(
                    "SELECT IFNULL(SUM(CASE WHEN TIMESTAMPDIFF(MINUTE,thoiGianBatDau," +
                    "IFNULL(thoiGianKetThuc,NOW()))<0 THEN 0 ELSE " +
                    "TIMESTAMPDIFF(MINUTE,thoiGianBatDau,IFNULL(thoiGianKetThuc,NOW())) END),0) " +
                    "FROM GiaoCa WHERE maNV=? AND CAST(thoiGianBatDau AS DATE)>=? " +
                    "AND CAST(thoiGianBatDau AS DATE)<DATE_ADD(?,INTERVAL 7 DAY) AND thoiGianBatDau<=NOW()")
                    .setParameter(1, maNV).setParameter(2, java.sql.Date.valueOf(startOfWeek))
                    .setParameter(3, java.sql.Date.valueOf(startOfWeek)).getSingleResult();
            return ((Number) r).doubleValue() / 60.0;
        } finally { em.close(); }
    }

    public List<String> getNhanVienDangLamViecChiTiet() {
        EntityManager em = getEM();
        try {
            List<Object[]> rows = em.createNativeQuery(
                    "SELECT nv.hoTen, cl.tenCa, cl.gioBatDau, cl.gioKetThuc " +
                    "FROM GiaoCa ls JOIN NhanVien nv ON ls.maNV=nv.maNV " +
                    "LEFT JOIN PhanCongCa pc ON ls.maNV=pc.maNV AND pc.ngayLam=CURDATE() " +
                    "LEFT JOIN CaLam cl ON pc.maCa=cl.maCa " +
                    "WHERE ls.thoiGianKetThuc IS NULL ORDER BY ls.thoiGianBatDau DESC")
                    .getResultList();
            List<String> list = new ArrayList<>();
            for (Object[] r : rows) {
                String tenNV = r[0].toString();
                if (r[1] != null) {
                    String timeStr = r[2].toString().substring(0, 5) + " - " + r[3].toString().substring(0, 5);
                    list.add(tenNV + " (" + r[1] + ": " + timeStr + ")");
                } else list.add(tenNV + " (Ca bổ sung)");
            }
            return list;
        } catch (Exception e) { e.printStackTrace(); return new ArrayList<>(); }
        finally { em.close(); }
    }

    public Map<String, Double> getTopStaffByWorkHours(LocalDate startDate, LocalDate endDate, int limit) {
        EntityManager em = getEM();
        try {
            List<Object[]> rows = em.createNativeQuery(
                    "SELECT nv.hoTen, SUM(CASE WHEN TIMESTAMPDIFF(MINUTE,ls.thoiGianBatDau," +
                    "IFNULL(ls.thoiGianKetThuc,NOW()))<0 THEN 0 ELSE " +
                    "TIMESTAMPDIFF(MINUTE,ls.thoiGianBatDau,IFNULL(ls.thoiGianKetThuc,NOW())) END)/60.0 AS TongGio " +
                    "FROM GiaoCa ls JOIN NhanVien nv ON ls.maNV=nv.maNV " +
                    "WHERE ls.thoiGianBatDau>=? AND ls.thoiGianBatDau<? AND ls.thoiGianBatDau<=NOW() " +
                    "AND nv.vaiTro='NHANVIEN' GROUP BY nv.hoTen ORDER BY TongGio DESC LIMIT ?")
                    .setParameter(1, java.sql.Timestamp.valueOf(startDate.atStartOfDay()))
                    .setParameter(2, java.sql.Timestamp.valueOf(endDate.plusDays(1).atStartOfDay()))
                    .setParameter(3, limit).getResultList();
            Map<String, Double> result = new LinkedHashMap<>();
            for (Object[] r : rows) result.put(r[0].toString(), ((Number) r[1]).doubleValue());
            return result;
        } finally { em.close(); }
    }

    public Map<String, Double> getGioLamTheoNgay(String maNV, int soNgay) {
        Map<String, Double> data = new LinkedHashMap<>();
        if (maNV == null || maNV.trim().isEmpty()) return data;
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd/MM");
        for (int i = soNgay - 1; i >= 0; i--) data.put(today.minusDays(i).format(fmt), 0.0);
        EntityManager em = getEM();
        try {
            List<Object[]> rows = em.createNativeQuery(
                    "SELECT CAST(thoiGianBatDau AS DATE) AS Ngay, " +
                    "SUM(CASE WHEN TIMESTAMPDIFF(MINUTE,thoiGianBatDau,IFNULL(thoiGianKetThuc,NOW()))<0 " +
                    "THEN 0 ELSE TIMESTAMPDIFF(MINUTE,thoiGianBatDau,IFNULL(thoiGianKetThuc,NOW())) END)/60.0 " +
                    "FROM GiaoCa WHERE maNV=? AND thoiGianBatDau>=DATE_ADD(CURDATE(),INTERVAL -? DAY) " +
                    "AND thoiGianBatDau<=NOW() GROUP BY CAST(thoiGianBatDau AS DATE) ORDER BY Ngay")
                    .setParameter(1, maNV).setParameter(2, soNgay).getResultList();
            for (Object[] r : rows) {
                String dateStr = ((java.sql.Date) r[0]).toLocalDate().format(fmt);
                if (data.containsKey(dateStr)) data.put(dateStr, ((Number) r[1]).doubleValue());
            }
        } catch (Exception e) { e.printStackTrace(); }
        finally { em.close(); }
        return data;
    }

    public List<String> getCacCaLamSapToi(String maNV) {
        if (maNV == null || maNV.trim().isEmpty()) return new ArrayList<>();
        EntityManager em = getEM();
        try {
            List<Object[]> rows = em.createNativeQuery(
                    "SELECT pc.ngayLam, cl.tenCa, cl.gioBatDau, cl.gioKetThuc " +
                    "FROM PhanCongCa pc JOIN CaLam cl ON pc.maCa=cl.maCa " +
                    "WHERE pc.maNV=? AND pc.ngayLam>=CURDATE() ORDER BY pc.ngayLam ASC, cl.gioBatDau ASC LIMIT 3")
                    .setParameter(1, maNV).getResultList();
            List<String> list = new ArrayList<>();
            java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM");
            for (Object[] r : rows) {
                String ngay = ((java.sql.Date) r[0]).toLocalDate().format(dtf);
                String gioBD = r[2].toString().substring(0, 5);
                String gioKT = r[3].toString().substring(0, 5);
                list.add(ngay + ": " + r[1] + " (" + gioBD + " - " + gioKT + ")");
            }
            return list;
        } finally { em.close(); }
    }

    public List<GiaoCa> getLichSuGiaoCa(LocalDate tuNgay, LocalDate denNgay) {
        EntityManager em = getEM();
        try {
            List<Object[]> rows = em.createNativeQuery(
                    "SELECT maGiaoCa, maNV, thoiGianBatDau, thoiGianKetThuc, tienDauCa, " +
                    "tienCuoiCa, tienHeThongTinh, chenhLech, ghiChu FROM GiaoCa " +
                    "WHERE CAST(thoiGianBatDau AS DATE) BETWEEN ? AND ? ORDER BY thoiGianBatDau DESC")
                    .setParameter(1, java.sql.Date.valueOf(tuNgay))
                    .setParameter(2, java.sql.Date.valueOf(denNgay)).getResultList();
            List<GiaoCa> list = new ArrayList<>();
            for (Object[] r : rows) {
                LocalDateTime kt = r[3] != null ? ((java.sql.Timestamp) r[3]).toLocalDateTime() : null;
                list.add(new GiaoCa(((Number) r[0]).intValue(), r[1] != null ? r[1].toString() : null,
                        ((java.sql.Timestamp) r[2]).toLocalDateTime(), kt,
                        ((Number) r[4]).doubleValue(), r[5] != null ? ((Number) r[5]).doubleValue() : 0,
                        r[6] != null ? ((Number) r[6]).doubleValue() : 0,
                        r[7] != null ? ((Number) r[7]).doubleValue() : 0,
                        r[8] != null ? r[8].toString() : null));
            }
            return list;
        } finally { em.close(); }
    }
}
