package connectDB;

import config.AppConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

public class JPAUtil {

    private static final String PU_NAME = "QuanLyNhaHangPU";
    private static EntityManagerFactory emf;

    static {
        try {
            Map<String, String> overrides = new HashMap<>();
            overrides.put("jakarta.persistence.jdbc.url",      AppConfig.getDbUrl());
            overrides.put("jakarta.persistence.jdbc.user",     AppConfig.getDbUser());
            overrides.put("jakarta.persistence.jdbc.password", AppConfig.getDbPassword());
            emf = Persistence.createEntityManagerFactory(PU_NAME, overrides);
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Không thể khởi tạo JPA: " + e.getMessage());
        }
    }

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
