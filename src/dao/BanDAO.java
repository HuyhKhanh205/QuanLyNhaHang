package dao;

import entity.Ban;
import entity.TrangThaiBan;
import jakarta.persistence.EntityManager;
import socket.SocketEvent;
import socket.SocketManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BanDAO extends BaseDAO {

    @SuppressWarnings("unchecked")
    public List<Ban> getAllBan() {
        EntityManager em = getEM();
        try {
            return em.createQuery("FROM Ban", Ban.class).getResultList();
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        } finally {
            em.close();
        }
    }

    public Ban getBanByMa(String maBan) {
        EntityManager em = getEM();
        try {
            return em.find(Ban.class, maBan);
        } finally {
            em.close();
        }
    }

    public boolean updateBan(Ban ban) {
        try {
            inTransactionVoid(em -> em.merge(ban));
            SocketManager.sendEvent(SocketEvent.TABLE_STATUS_CHANGED,
                    Map.of("maBan", ban.getMaBan(), "trangThai", ban.getTrangThai().name()));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getTenBanByMa(String maBan) {
        EntityManager em = getEM();
        try {
            List<String> r = em.createQuery(
                    "SELECT b.tenBan FROM Ban b WHERE b.maBan = :ma", String.class)
                    .setParameter("ma", maBan).getResultList();
            return r.isEmpty() ? maBan : r.get(0);
        } finally {
            em.close();
        }
    }

    public int getSoThuTuBanLonNhat() {
        EntityManager em = getEM();
        try {
            List<?> r = em.createNativeQuery(
                    "SELECT MAX(CAST(SUBSTRING(maBan, 4) AS UNSIGNED)) FROM Ban WHERE maBan REGEXP '^BAN[0-9]'")
                    .getResultList();
            if (!r.isEmpty() && r.get(0) != null) return ((Number) r.get(0)).intValue();
            return 0;
        } catch (Exception e) {
            return 0;
        } finally {
            em.close();
        }
    }

    public Map<String, Integer> getTableStatusCounts() {
        EntityManager em = getEM();
        try {
            Map<String, Integer> counts = new HashMap<>();
            counts.put("Trống", 0);
            counts.put("Đang có khách", 0);
            counts.put("Đã đặt trước", 0);
            List<Object[]> rows = em.createQuery(
                    "SELECT b.trangThai, COUNT(b) FROM Ban b GROUP BY b.trangThai", Object[].class)
                    .getResultList();
            for (Object[] row : rows) {
                if (row[0] == null) continue;
                String key = switch (row[0].toString()) {
                    case "DANG_PHUC_VU" -> "Đang có khách";
                    case "DA_DAT_TRUOC"  -> "Đã đặt trước";
                    default              -> "Trống";
                };
                counts.put(key, ((Long) row[1]).intValue());
            }
            return counts;
        } finally {
            em.close();
        }
    }

    public boolean chuyenBan(Ban banCu, Ban banMoi) {
        boolean ok = inTransaction(em -> {
            // Cập nhật link ghiChu ở các bàn phụ
            em.createNativeQuery(
                    "UPDATE DonDatMon SET ghiChu = REPLACE(ghiChu, ?, ?) " +
                    "WHERE maBan != ? AND trangThai = 'Chưa thanh toán' AND ghiChu LIKE ?")
                    .setParameter(1, "LINKED:" + banCu.getMaBan())
                    .setParameter(2, "LINKED:" + banMoi.getMaBan())
                    .setParameter(3, banCu.getMaBan())
                    .setParameter(4, "%LINKED:" + banCu.getMaBan() + "%")
                    .executeUpdate();

            // Chuyển đơn sang bàn mới
            em.createNativeQuery(
                    "UPDATE DonDatMon SET maBan = ? WHERE maBan = ? " +
                    "AND trangThai != 'Đã thanh toán' AND trangThai != 'Đã hủy'")
                    .setParameter(1, banMoi.getMaBan())
                    .setParameter(2, banCu.getMaBan())
                    .executeUpdate();

            // Cập nhật trạng thái bàn mới
            String ttMoi = (banCu.getTrangThai() == TrangThaiBan.DA_DAT_TRUOC)
                    ? "DA_DAT_TRUOC" : "DANG_PHUC_VU";
            em.createNativeQuery("UPDATE Ban SET trangThai = ?, gioMoBan = ? WHERE maBan = ?")
                    .setParameter(1, ttMoi)
                    .setParameter(2, banCu.getGioMoBan() != null
                            ? java.sql.Timestamp.valueOf(banCu.getGioMoBan()) : null)
                    .setParameter(3, banMoi.getMaBan())
                    .executeUpdate();

            // Xóa trạng thái bàn cũ
            em.createNativeQuery("UPDATE Ban SET trangThai = 'TRONG', gioMoBan = NULL WHERE maBan = ?")
                    .setParameter(1, banCu.getMaBan())
                    .executeUpdate();
            return true;
        });
        if (ok) SocketManager.sendEvent(SocketEvent.TABLE_STATUS_CHANGED, Map.of());
        return ok;
    }

    public boolean ghepBanLienKet(List<Ban> listBanNguon, Ban banDich) {
        boolean ok = inTransaction(em -> {
            boolean coKhach = (banDich.getTrangThai() == TrangThaiBan.DANG_PHUC_VU);
            for (Ban b : listBanNguon) {
                if (b.getTrangThai() == TrangThaiBan.DANG_PHUC_VU) { coKhach = true; break; }
            }
            String ttSauGop = coKhach ? "DANG_PHUC_VU" : "DA_DAT_TRUOC";

            // Tìm hoặc tạo đơn đích
            List<?> donDichList = em.createNativeQuery(
                    "SELECT maDon FROM DonDatMon WHERE maBan = ? AND trangThai = 'Chưa thanh toán' " +
                    "AND trangThai != 'Đã hủy' LIMIT 1")
                    .setParameter(1, banDich.getMaBan()).getResultList();

            String maDonDich;
            if (!donDichList.isEmpty()) {
                maDonDich = donDichList.get(0).toString();
            } else {
                maDonDich = "DON" + System.currentTimeMillis();
                em.createNativeQuery(
                        "INSERT INTO DonDatMon(maDon, ngayKhoiTao, thoiGianDen, maNV, maBan, trangThai) " +
                        "VALUES(?, NOW(), NOW(), 'NV01102', ?, 'Chưa thanh toán')")
                        .setParameter(1, maDonDich).setParameter(2, banDich.getMaBan()).executeUpdate();
            }

            for (Ban bNguon : listBanNguon) {
                if (bNguon.getMaBan().equals(banDich.getMaBan())) continue;

                List<?> donNguonList = em.createNativeQuery(
                        "SELECT maDon FROM DonDatMon WHERE maBan = ? AND trangThai = 'Chưa thanh toán' " +
                        "AND trangThai != 'Đã hủy' LIMIT 1")
                        .setParameter(1, bNguon.getMaBan()).getResultList();

                if (!donNguonList.isEmpty()) {
                    String maDonNguon = donNguonList.get(0).toString();
                    if (!maDonNguon.equals(maDonDich)) {
                        // Gộp các món vào đơn đích
                        List<Object[]> items = em.createNativeQuery(
                                "SELECT maMonAn, soLuong, donGia FROM ChiTietHoaDon WHERE maDon = ?")
                                .setParameter(1, maDonNguon).getResultList();

                        em.createNativeQuery("DELETE FROM ChiTietHoaDon WHERE maDon = ?")
                                .setParameter(1, maDonNguon).executeUpdate();

                        for (Object[] item : items) {
                            String maMon = item[0].toString();
                            int soLuong = ((Number) item[1]).intValue();
                            double donGia = ((Number) item[2]).doubleValue();

                            long count = ((Number) em.createNativeQuery(
                                    "SELECT COUNT(*) FROM ChiTietHoaDon WHERE maDon = ? AND maMonAn = ?")
                                    .setParameter(1, maDonDich).setParameter(2, maMon)
                                    .getSingleResult()).longValue();

                            if (count > 0) {
                                em.createNativeQuery(
                                        "UPDATE ChiTietHoaDon SET soLuong = soLuong + ? WHERE maDon = ? AND maMonAn = ?")
                                        .setParameter(1, soLuong).setParameter(2, maDonDich)
                                        .setParameter(3, maMon).executeUpdate();
                            } else {
                                em.createNativeQuery(
                                        "INSERT INTO ChiTietHoaDon(maDon, maMonAn, soLuong, donGia) VALUES(?,?,?,?)")
                                        .setParameter(1, maDonDich).setParameter(2, maMon)
                                        .setParameter(3, soLuong).setParameter(4, donGia).executeUpdate();
                            }
                        }
                        em.createNativeQuery("UPDATE DonDatMon SET trangThai = 'Đã hủy' WHERE maDon = ?")
                                .setParameter(1, maDonNguon).executeUpdate();
                        em.createNativeQuery("UPDATE HoaDon SET trangThai = 'Đã hủy' WHERE maDon = ?")
                                .setParameter(1, maDonNguon).executeUpdate();
                    }
                }

                // Tạo dummy đơn linked
                String dummyID = "L" + (System.currentTimeMillis() % 100000000) + bNguon.getMaBan();
                em.createNativeQuery(
                        "INSERT INTO DonDatMon(maDon, ngayKhoiTao, thoiGianDen, maNV, maBan, trangThai, ghiChu) " +
                        "VALUES(?, NOW(), NOW(), 'NV01102', ?, 'Chưa thanh toán', ?)")
                        .setParameter(1, dummyID).setParameter(2, bNguon.getMaBan())
                        .setParameter(3, "LINKED:" + banDich.getMaBan()).executeUpdate();

                em.createNativeQuery("UPDATE Ban SET trangThai = ? WHERE maBan = ?")
                        .setParameter(1, ttSauGop).setParameter(2, bNguon.getMaBan()).executeUpdate();
            }

            em.createNativeQuery("UPDATE Ban SET trangThai = ? WHERE maBan = ?")
                    .setParameter(1, ttSauGop).setParameter(2, banDich.getMaBan()).executeUpdate();
            return true;
        });
        if (ok) SocketManager.sendEvent(SocketEvent.TABLE_STATUS_CHANGED, Map.of());
        return ok;
    }

    public String getTenHienThiGhep(String maBanCheck) {
        EntityManager em = getEM();
        try {
            String tenGoc = "";
            List<String> tenBanPhu = new ArrayList<>();
            String maBanMaster = maBanCheck;

            List<?> slave = em.createNativeQuery(
                    "SELECT ghiChu FROM DonDatMon WHERE maBan = ? AND trangThai = 'Chưa thanh toán' AND ghiChu LIKE '%LINKED:%'")
                    .setParameter(1, maBanCheck).getResultList();
            if (!slave.isEmpty()) {
                String ghiChu = slave.get(0).toString();
                int idx = ghiChu.indexOf("LINKED:");
                if (idx != -1) maBanMaster = ghiChu.substring(idx + 7).trim().split(" ")[0];
            }

            List<?> masterName = em.createNativeQuery("SELECT tenBan FROM Ban WHERE maBan = ?")
                    .setParameter(1, maBanMaster).getResultList();
            if (!masterName.isEmpty()) tenGoc = masterName.get(0).toString();

            List<?> slaves = em.createNativeQuery(
                    "SELECT b.tenBan FROM DonDatMon d JOIN Ban b ON d.maBan = b.maBan " +
                    "WHERE d.ghiChu LIKE ? AND d.trangThai = 'Chưa thanh toán'")
                    .setParameter(1, "%LINKED:" + maBanMaster + "%").getResultList();
            for (Object t : slaves) {
                String n = t.toString().replace("Bàn ", "");
                if (!tenGoc.contains(n)) tenBanPhu.add(n);
            }

            StringBuilder sb = new StringBuilder(tenGoc);
            for (String t : tenBanPhu) sb.append(" + ").append(t);
            return sb.toString();
        } finally {
            em.close();
        }
    }

    public String getMaBanChinh(String maBanCheck) {
        EntityManager em = getEM();
        try {
            List<?> r = em.createNativeQuery(
                    "SELECT ghiChu FROM DonDatMon WHERE maBan = ? AND trangThai = 'Chưa thanh toán' AND ghiChu LIKE 'LINKED:%'")
                    .setParameter(1, maBanCheck).getResultList();
            if (!r.isEmpty()) return r.get(0).toString().replace("LINKED:", "").trim();
            return maBanCheck;
        } finally {
            em.close();
        }
    }
}
