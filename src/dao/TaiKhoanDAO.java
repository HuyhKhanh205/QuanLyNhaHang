package dao;

import jakarta.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaiKhoanDAO extends BaseDAO {

    public Map<String, String> checkLoginAndGetInfo(String tenTK, String plainPassword) {
        String cleanPassword = plainPassword.trim().toLowerCase();
        String cleanTenTK = tenTK.trim();
        String inputHashed = "hashed_" + cleanPassword.hashCode();

        EntityManager em = getEM();
        try {
            List<Object[]> rows = em.createNativeQuery(
                    "SELECT T.matKhau, T.trangThai, N.vaiTro, N.hoTen, N.maNV " +
                    "FROM TaiKhoan T JOIN NhanVien N ON T.tenTK = N.tenTK WHERE T.tenTK = ?")
                    .setParameter(1, cleanTenTK).getResultList();

            if (rows.isEmpty()) return null;
            Object[] row = rows.get(0);
            String dbHashed = row[0].toString().trim();
            Object rawTT = row[1];
            int trangThai = (rawTT instanceof Boolean) ? ((Boolean) rawTT ? 1 : 0) : ((Number) rawTT).intValue();

            if (!inputHashed.equals(dbHashed)) return null;

            if (trangThai == 0) {
                Map<String, String> locked = new HashMap<>();
                locked.put("status", "LOCKED");
                return locked;
            }

            Map<String, String> info = new HashMap<>();
            info.put("role",  row[2].toString());
            info.put("name",  row[3].toString());
            info.put("maNV",  row[4].toString());
            return info;
        } finally {
            em.close();
        }
    }

    public boolean updatePassword(String tenTK, String newPlainPassword) {
        String hashed = "hashed_" + newPlainPassword.trim().toLowerCase().hashCode();
        try {
            inTransactionVoid(em ->
                em.createNativeQuery("UPDATE TaiKhoan SET matKhau = ? WHERE tenTK = ?")
                    .setParameter(1, hashed).setParameter(2, tenTK.trim()).executeUpdate()
            );
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
