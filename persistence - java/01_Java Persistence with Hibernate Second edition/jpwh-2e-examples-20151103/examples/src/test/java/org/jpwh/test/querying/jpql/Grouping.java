package org.jpwh.test.querying.jpql;

import org.jpwh.model.querying.Item;
import org.jpwh.test.querying.QueryingTest;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.UserTransaction;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class Grouping extends QueryingTest {

    @Test
    public void executeQueries() throws Exception {
        storeTestData();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            {
                Query q = em.createNamedQuery("group");
                List<Object[]> result = q.getResultList();
                assertEquals(result.size(), 2);
                for (Object[] row : result) {
                    assertTrue(row[0] instanceof String);
                    assertTrue(row[1] instanceof Long);
                }
            }
            em.clear();
            {
                Query q = em.createNamedQuery("average");
                List<Object[]> result = q.getResultList();
                assertEquals(result.size(), 2);
                for (Object[] row : result) {
                    assertTrue(row[0] instanceof String);
                    assertTrue(row[1] instanceof Double);
                }
            }
            em.clear();
            {
                Query q = em.createNamedQuery("averageWorkaround");
                List<Object[]> result = q.getResultList();
                assertEquals(result.size(), 2);
                for (Object[] row : result) {
                    assertTrue(row[0] instanceof Item);
                    assertTrue(row[1] instanceof Double);
                }
            }
            em.clear();
            {
                Query q = em.createNamedQuery("having");
                List<Object[]> result = q.getResultList();
                assertEquals(result.size(), 1);
                for (Object[] row : result) {
                    assertTrue(row[0] instanceof String);
                    assertTrue(row[1] instanceof Long);
                }
            }
            em.clear();

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

}
