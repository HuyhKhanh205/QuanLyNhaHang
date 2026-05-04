package dao;

import entity.CaLam;
import entity.NhanVien;
import entity.PhanCong;
import entity.PhanCongId;
import jakarta.persistence.EntityManager;
import java.sql.Time;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PhanCongDAO extends BaseDAO {

    public List<PhanCong> getPhanCongChiTiet(LocalDate tuNgay, LocalDate denNgay) {
        EntityManager em = getEM();
        try {
            List<Object[]> rows = em.createNativeQuery(
                    "SELECT pc.ngayLam, c.maCa, c.tenCa, c.gioBatDau, c.gioKetThuc, nv.maNV, nv.hoTen " +
                    "FROM PhanCongCa pc JOIN CaLam c ON pc.maCa=c.maCa JOIN NhanVien nv ON pc.maNV=nv.maNV " +
                    "WHERE pc.ngayLam BETWEEN ? AND ? ORDER BY pc.ngayLam, c.gioBatDau")
                    .setParameter(1, java.sql.Date.valueOf(tuNgay))
                    .setParameter(2, java.sql.Date.valueOf(denNgay)).getResultList();
            List<PhanCong> list = new ArrayList<>();
            for (Object[] r : rows) {
                LocalDate ngay = ((java.sql.Date) r[0]).toLocalDate();
                CaLam ca = new CaLam(r[1].toString(), r[2].toString(),
                        ((Time) r[3]).toLocalTime(), ((Time) r[4]).toLocalTime());
                NhanVien nv = new NhanVien();
                nv.setManv(r[5].toString());
                nv.setHoten(r[6].toString());
                list.add(new PhanCong(ca, nv, ngay));
            }
            return list;
        } catch (Exception e) { e.printStackTrace(); return new ArrayList<>(); }
        finally { em.close(); }
    }

    public boolean themPhanCong(String maNV, String maCa, LocalDate ngayLam) {
        try {
            inTransactionVoid(em -> {
                em.createNativeQuery("INSERT INTO PhanCongCa(maNV, maCa, ngayLam) VALUES(?,?,?)")
                        .setParameter(1, maNV).setParameter(2, maCa)
                        .setParameter(3, java.sql.Date.valueOf(ngayLam)).executeUpdate();
            });
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean xoaPhanCong(String maNV, String maCa, LocalDate ngayLam) {
        try {
            inTransactionVoid(em ->
                em.createNativeQuery("DELETE FROM PhanCongCa WHERE maNV=? AND maCa=? AND ngayLam=?")
                        .setParameter(1, maNV).setParameter(2, maCa)
                        .setParameter(3, java.sql.Date.valueOf(ngayLam)).executeUpdate());
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public CaLam getCaLamViecCuaNhanVien(String maNV, LocalDate date) {
        EntityManager em = getEM();
        try {
            List<Object[]> r = em.createNativeQuery(
                    "SELECT cl.maCa, cl.tenCa, cl.gioBatDau, cl.gioKetThuc " +
                    "FROM PhanCongCa pc JOIN CaLam cl ON pc.maCa=cl.maCa WHERE pc.maNV=? AND pc.ngayLam=?")
                    .setParameter(1, maNV).setParameter(2, java.sql.Date.valueOf(date)).getResultList();
            if (r.isEmpty()) return null;
            Object[] row = r.get(0);
            return new CaLam(row[0].toString(), row[1].toString(),
                    ((Time) row[2]).toLocalTime(), ((Time) row[3]).toLocalTime());
        } catch (Exception e) { e.printStackTrace(); return null; }
        finally { em.close(); }
    }

    public String[] getThongTinCaTruocSau(String maNV, LocalDate today) {
        String[] result = {"<html><center>-- Trống --</center></html>", "<html><center>-- Trống --</center></html>"};
        EntityManager em = getEM();
        try {
            List<Object[]> prev = em.createNativeQuery(
                    "SELECT pc.maCa, pc.ngayLam, cl.tenCa, cl.gioBatDau, cl.gioKetThuc " +
                    "FROM PhanCongCa pc JOIN CaLam cl ON pc.maCa=cl.maCa " +
                    "WHERE pc.maNV=? AND (pc.ngayLam < CURDATE() OR (pc.ngayLam=CURDATE() AND cl.gioKetThuc<=CURTIME())) " +
                    "ORDER BY pc.ngayLam DESC, cl.gioKetThuc DESC LIMIT 1")
                    .setParameter(1, maNV).getResultList();
            if (!prev.isEmpty()) {
                Object[] r = prev.get(0);
                List<String> names = getNhVienTrongCa(em, r[0].toString(), ((java.sql.Date)r[1]).toLocalDate());
                result[0] = formatShift(((java.sql.Date)r[1]).toLocalDate(), r[2].toString(),
                        (Time)r[3], (Time)r[4], names);
            }
            List<Object[]> next = em.createNativeQuery(
                    "SELECT pc.maCa, pc.ngayLam, cl.tenCa, cl.gioBatDau, cl.gioKetThuc " +
                    "FROM PhanCongCa pc JOIN CaLam cl ON pc.maCa=cl.maCa " +
                    "WHERE pc.maNV=? AND (pc.ngayLam > CURDATE() OR (pc.ngayLam=CURDATE() AND cl.gioBatDau>CURTIME())) " +
                    "ORDER BY pc.ngayLam ASC, cl.gioBatDau ASC LIMIT 1")
                    .setParameter(1, maNV).getResultList();
            if (!next.isEmpty()) {
                Object[] r = next.get(0);
                List<String> names = getNhVienTrongCa(em, r[0].toString(), ((java.sql.Date)r[1]).toLocalDate());
                result[1] = formatShift(((java.sql.Date)r[1]).toLocalDate(), r[2].toString(),
                        (Time)r[3], (Time)r[4], names);
            }
        } catch (Exception e) { e.printStackTrace(); }
        finally { em.close(); }
        return result;
    }

    private List<String> getNhVienTrongCa(EntityManager em, String maCa, LocalDate ngay) {
        List<String> list = new ArrayList<>();
        try {
            List<?> r = em.createNativeQuery(
                    "SELECT nv.hoTen FROM PhanCongCa pc JOIN NhanVien nv ON pc.maNV=nv.maNV " +
                    "WHERE pc.maCa=? AND pc.ngayLam=?")
                    .setParameter(1, maCa).setParameter(2, java.sql.Date.valueOf(ngay)).getResultList();
            for (Object o : r) list.add(o.toString());
        } catch (Exception ignored) {}
        return list;
    }

    private String formatShift(LocalDate date, String tenCa, Time start, Time end, List<String> names) {
        LocalDate today = LocalDate.now();
        String dow = date.getDayOfWeek().getDisplayName(TextStyle.FULL, new Locale("vi", "VN"))
                .replace("Thứ Hai","thứ 2").replace("Thứ Ba","thứ 3").replace("Thứ Tư","thứ 4")
                .replace("Thứ Năm","thứ 5").replace("Thứ Sáu","thứ 6").replace("Thứ Bảy","thứ 7")
                .replace("Chủ Nhật","CN");
        String ds = date.isEqual(today) ? "Hôm nay (" + dow + ")"
                : date.isEqual(today.minusDays(1)) ? "Hôm qua (" + dow + ")"
                : date.isEqual(today.plusDays(1)) ? "Ngày mai (" + dow + ")"
                : date.format(DateTimeFormatter.ofPattern("dd/MM")) + " (" + dow + ")";
        String ts = start.toString().substring(0,2) + "h-" + end.toString().substring(0,2) + "h";
        String staff = names.isEmpty() ? "<i style='color:gray'>Chưa phân công</i>" : String.join(", ", names);
        return "<html><div style='text-align:center;width:180px;'>" +
               "<span style='color:#2980b9;font-weight:bold;font-size:10px;'>" + ds + "</span><br/>" +
               "<span style='color:#2c3e50;font-weight:bold;font-size:11px;'>" + tenCa + " " + ts + "</span><br/>" +
               "<div style='margin-top:4px;color:#555;font-size:12px;'>" + staff + "</div></div></html>";
    }

    public Map<String, Double> getTongGioLamTheoThang(int thang, int nam) {
        EntityManager em = getEM();
        try {
            List<Object[]> rows = em.createNativeQuery(
                    "SELECT pc.maNV, SUM(TIMESTAMPDIFF(MINUTE,c.gioBatDau,c.gioKetThuc))/60.0 " +
                    "FROM PhanCongCa pc JOIN CaLam c ON pc.maCa=c.maCa " +
                    "WHERE MONTH(pc.ngayLam)=? AND YEAR(pc.ngayLam)=? GROUP BY pc.maNV")
                    .setParameter(1, thang).setParameter(2, nam).getResultList();
            Map<String, Double> map = new HashMap<>();
            for (Object[] r : rows) map.put(r[0].toString(), ((Number) r[1]).doubleValue());
            return map;
        } finally { em.close(); }
    }
}
