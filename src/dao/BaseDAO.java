package dao;

import connectDB.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class BaseDAO {

    protected EntityManager getEM() {
        return JPAUtil.getEntityManager();
    }

    protected <R> R inTransaction(Function<EntityManager, R> action) {
        EntityManager em = getEM();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            R result = action.apply(em);
            tx.commit();
            return result;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }

    protected void inTransactionVoid(Consumer<EntityManager> action) {
        inTransaction(em -> { action.accept(em); return null; });
    }
}
