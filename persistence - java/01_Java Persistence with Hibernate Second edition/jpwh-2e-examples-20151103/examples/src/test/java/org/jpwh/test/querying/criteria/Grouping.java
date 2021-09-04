package org.jpwh.test.querying.criteria;

import org.jpwh.model.querying.Bid;
import org.jpwh.model.querying.Item;
import org.jpwh.model.querying.User;
import org.jpwh.test.querying.QueryingTest;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class Grouping extends QueryingTest {

    @Test
    public void executeQueries() throws Exception {
        storeTestData();

        CriteriaBuilder cb =
            JPA.getEntityManagerFactory().getCriteriaBuilder();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            { // Group

                CriteriaQuery criteria = cb.createQuery();
                Root<User> u = criteria.from(User.class);
                criteria.multiselect(
                    u.get("lastname"),
                    cb.count(u)
                );
                criteria.groupBy(u.get("lastname"));

                Query q = em.createQuery(criteria);
                List<Object[]> result = q.getResultList();
                assertEquals(result.size(), 2);
                for (Object[] row : result) {
                    assertTrue(row[0] instanceof String);
                    assertTrue(row[1] instanceof Long);
                }
            }
            em.clear();
            { // Average

                CriteriaQuery criteria = cb.createQuery();
                Root<Bid> b = criteria.from(Bid.class);
                criteria.multiselect(
                    b.get("item").get("name"),
                    cb.avg(b.<BigDecimal>get("amount"))
                );
                criteria.groupBy(b.get("item").get("name"));

                Query q = em.createQuery(criteria);
                List<Object[]> result = q.getResultList();
                assertEquals(result.size(), 2);
                for (Object[] row : result) {
                    assertTrue(row[0] instanceof String);
                    assertTrue(row[1] instanceof Double);
                }
            }
            em.clear();
            { // Average Workaround

                CriteriaQuery criteria = cb.createQuery();
                Root<Bid> b = criteria.from(Bid.class);
                Join<Bid, Item> i = b.join("item");
                criteria.multiselect(
                    i,
                    cb.avg(b.<BigDecimal>get("amount"))
                );
                criteria.groupBy(
                    i.get("id"), i.get("name"), i.get("createdOn"), i.get("auctionEnd"),
                    i.get("auctionType"), i.get("approved"), i.get("buyNowPrice"),
                    i.get("seller")
                );

                Query q = em.createQuery(criteria);
                List<Object[]> result = q.getResultList();
                assertEquals(result.size(), 2);
                for (Object[] row : result) {
                    assertTrue(row[0] instanceof Item);
                    assertTrue(row[1] instanceof Double);
                }
            }
            em.clear();
            {// Having

                CriteriaQuery criteria = cb.createQuery();
                Root<User> u = criteria.from(User.class);
                criteria.multiselect(
                    u.get("lastname"),
                    cb.count(u)
                );
                criteria.groupBy(u.get("lastname"));
                criteria.having(cb.like(u.<String>get("lastname"), "D%"));

                Query q = em.createQuery(criteria);
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
