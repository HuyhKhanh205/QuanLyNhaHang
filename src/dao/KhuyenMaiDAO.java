package dao;

import entity.KhuyenMai;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class KhuyenMaiDAO extends BaseDAO {

    public void autoUpdateExpiredStatuses() {
        try {
            inTransactionVoid(em ->
                em.createNativeQuery("UPDATE KhuyenMai SET trangThai='Ngưng áp dụng' " +
                        "WHERE ngayKetThuc < ? AND trangThai='Đang áp dụng'")
                        .setParameter(1, java.sql.Date.valueOf(LocalDate.now())).executeUpdate());
        } catch (Exception e) { e.printStackTrace(); }
    }

    public List<KhuyenMai> getAllKhuyenMai() {
        autoUpdateExpiredStatuses();
        EntityManager em = getEM();
        try {
            return em.createQuery("FROM KhuyenMai", KhuyenMai.class).getResultList();
        } catch (Exception e) { e.printStackTrace(); return new ArrayList<>(); }
        finally { em.close(); }
    }

    public boolean themKhuyenMai(KhuyenMai km) {
        try { inTransactionVoid(em -> em.persist(km)); return true; }
        catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean updateKhuyenMai(KhuyenMai km) {
        try { inTransactionVoid(em -> em.merge(km)); return true; }
        catch (Exception e) { e.printStackTrace(); return false; }
    }

    public List<KhuyenMai> timKiemVaLoc(String tuKhoa, String trangThai) {
        autoUpdateExpiredStatuses();
        EntityManager em = getEM();
        try {
            boolean hasTuKhoa = tuKhoa != null && !tuKhoa.trim().isEmpty() && !tuKhoa.equals("Tìm kiếm khuyến mãi");
            boolean hasTrangThai = trangThai != null && !trangThai.equals("Lọc khuyến mãi");
            StringBuilder jpql = new StringBuilder("FROM KhuyenMai k WHERE 1=1");
            if (hasTuKhoa)   jpql.append(" AND (k.maKM LIKE :kw OR k.tenChuongTrinh LIKE :kw)");
            if (hasTrangThai) jpql.append(" AND k.trangThai = :tt");
            jakarta.persistence.TypedQuery<KhuyenMai> q = em.createQuery(jpql.toString(), KhuyenMai.class);
            if (hasTuKhoa)    q.setParameter("kw", "%" + tuKhoa + "%");
            if (hasTrangThai) q.setParameter("tt", trangThai);
            return q.getResultList();
        } catch (Exception e) { e.printStackTrace(); return new ArrayList<>(); }
        finally { em.close(); }
    }

    public KhuyenMai getKhuyenMaiHopLeByMa(String maKM) {
        autoUpdateExpiredStatuses();
        EntityManager em = getEM();
        try {
            List<KhuyenMai> r = em.createQuery(
                    "FROM KhuyenMai k WHERE k.maKM=:ma AND k.trangThai='Đang áp dụng'", KhuyenMai.class)
                    .setParameter("ma", maKM).getResultList();
            if (r.isEmpty()) return null;
            KhuyenMai km = r.get(0);
            LocalDate now = LocalDate.now();
            if (now.isBefore(km.getNgayBatDau())) return null;
            if (km.getNgayKetThuc() != null && now.isAfter(km.getNgayKetThuc())) return null;
            return km;
        } finally { em.close(); }
    }

    public String kiemTraDieuKienSuDung(String maKM, String maKH, double tongTien) {
        EntityManager em = getEM();
        try {
            List<KhuyenMai> r = em.createQuery("FROM KhuyenMai k WHERE k.maKM=:ma", KhuyenMai.class)
                    .setParameter("ma", maKM).getResultList();
            if (r.isEmpty()) return "Lỗi kiểm tra.";
            KhuyenMai km = r.get(0);
            if (!"Đang áp dụng".equals(km.getTrangThai())) return "Mã đã ngưng áp dụng.";
            LocalDate now = LocalDate.now();
            if (now.isBefore(km.getNgayBatDau())) return "Chưa đến ngày áp dụng.";
            if (km.getNgayKetThuc() != null && now.isAfter(km.getNgayKetThuc())) return "Mã đã hết hạn.";
            if (tongTien < km.getDieuKienApDung()) return "Chưa đủ giá trị tối thiểu.";
            if (km.getSoLuongGioiHan() > 0 && km.getSoLuotDaDung() >= km.getSoLuongGioiHan())
                return "Mã đã hết lượt sử dụng.";
            long used = ((Number) em.createNativeQuery(
                    "SELECT COUNT(*) FROM LichSuSuDungKM WHERE maKM=? AND maKH=?")
                    .setParameter(1, maKM).setParameter(2, maKH).getSingleResult()).longValue();
            if (used > 0) return "Khách hàng đã dùng mã này rồi.";
            return "OK";
        } catch (Exception e) { e.printStackTrace(); return "Lỗi kiểm tra."; }
        finally { em.close(); }
    }

    public void ghiNhanSuDung(String maKM, String maKH) {
        inTransactionVoid(em -> {
            em.createNativeQuery("UPDATE KhuyenMai SET soLuotDaDung=soLuotDaDung+1 WHERE maKM=?")
                    .setParameter(1, maKM).executeUpdate();
            em.createNativeQuery("INSERT INTO LichSuSuDungKM(maKH, maKM) VALUES(?,?)")
                    .setParameter(1, maKH).setParameter(2, maKM).executeUpdate();
            em.createNativeQuery("UPDATE KhuyenMai SET trangThai='Ngưng áp dụng' " +
                    "WHERE maKM=? AND soLuongGioiHan>0 AND soLuotDaDung>=soLuongGioiHan")
                    .setParameter(1, maKM).executeUpdate();
        });
    }
}
