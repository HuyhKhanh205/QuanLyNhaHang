package dao;

import entity.CaLam;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

public class CaLamDAO extends BaseDAO {

    public List<CaLam> getAllCaLam() {
        EntityManager em = getEM();
        try {
            return em.createQuery("FROM CaLam c ORDER BY c.gioBatDau", CaLam.class).getResultList();
        } catch (Exception e) { e.printStackTrace(); return new ArrayList<>(); }
        finally { em.close(); }
    }
}
