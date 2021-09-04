package org.jpwh.test.querying.jpql;

import org.jpwh.model.querying.Bid;
import org.jpwh.model.querying.Item;
import org.jpwh.model.querying.User;
import org.jpwh.test.querying.QueryingTest;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.UserTransaction;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class Subselects extends QueryingTest {

    @Test
    public void executeQueries() throws Exception {
        TestDataCategoriesItems testData = storeTestData();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();
            {
                Query q = em.createNamedQuery("correlated");
                List<User> result = q.getResultList();
                assertEquals(result.size(), 1);
                User user = result.iterator().next();
                assertEquals(user.getId(), testData.users.getFirstId());
            }
            em.clear();
            {
                Query q = em.createNamedQuery("uncorrelated");
                List<Bid> result = q.getResultList();
                assertEquals(result.size(), 2);
            }
            {
                Query q = em.createNamedQuery("exists");
                List<Item> result = q.getResultList();
                assertEquals(result.size(), 2);
            }
            em.clear();
            {
                Query q = em.createNamedQuery("quantifyAll");
                List<Item> result = q.getResultList();
                assertEquals(result.size(), 2);
            }
            em.clear();
            {
                Query q = em.createNamedQuery("quantifyAny");
                List<Item> result = q.getResultList();
                assertEquals(result.size(), 1);
            }
            em.clear();

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

}
