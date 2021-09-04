package org.jpwh.test.querying.jpql;

import org.jpwh.model.querying.Address;
import org.jpwh.model.querying.Bid;
import org.jpwh.model.querying.Item;
import org.jpwh.model.querying.ItemSummary;
import org.jpwh.test.querying.QueryingTest;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class Projection extends QueryingTest {

    @Test
    public void executeQueries() throws Exception {
        TestDataCategoriesItems testData = storeTestData();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            {
                Query query = em.createNamedQuery("product");
                List<Object[]> result = query.getResultList();

                Set<Item> items = new HashSet();
                Set<Bid> bids = new HashSet();

                for (Object[] row : result) {
                    assertTrue(row[0] instanceof Item);
                    items.add((Item) row[0]);

                    assertTrue(row[1] instanceof Bid);
                    bids.add((Bid)row[1]);
                }

                assertEquals(items.size(), 3);
                assertEquals(bids.size(), 4);
                assertEquals(result.size(), 12); // Cartesian product!
            }
            em.clear();
            {
                Query q = em.createNamedQuery("scalarProduct");
                List<Object[]> result = q.getResultList();
                assertEquals(result.size(), 3);

                Set<Long> ids = new HashSet();
                Set<String> names = new HashSet();
                Set<BigDecimal> prices = new HashSet();
                for (Object[] row : result) {
                    assertTrue(row[0] instanceof Long);
                    ids.add((Long) row[0]);

                    assertTrue(row[1] instanceof String);
                    names.add((String)row[1]);

                    assertTrue(row[2] == null || row[2] instanceof BigDecimal);
                    prices.add((BigDecimal)row[2]);
                }
                assertEquals(ids.size(), 3, "ids");
                assertEquals(names.size(), 3, "names");
                assertEquals(prices.size(), 2, "prices");
            }
            em.clear();
            {
                Query q = em.createNamedQuery("transient");
                List<Object[]> result = q.getResultList();
                assertEquals(result.size(), 3);

                Object[] firstRow = result.get(0);
                assertTrue(firstRow[0] instanceof Long);
                assertTrue(firstRow[1] instanceof String);
                assertTrue(firstRow[2] instanceof Address);
            }
            em.clear();
            {
                Query q = em.createNamedQuery("distinct");
                assertEquals(q.getResultList().size(), 3);
            }
            em.clear();
            {
                Query q = em.createNamedQuery("dynamicInstance");
                List<ItemSummary> result = q.getResultList();
                assertEquals(result.size(), 3);
            }

            // All following are more function call examples, with variations

            em.clear();
            {
                Query q = em.createNamedQuery("concat");
                assertEquals(q.getResultList().size(), 3);
            }
            em.clear();
            {
                Query q = em.createNamedQuery("coalesce");
                List<Object[]> result = q.getResultList();
                assertEquals(result.size(), 3);
                for (Object[] row : result) {
                    assertTrue(row[0] instanceof String);
                    assertTrue(row[1] instanceof BigDecimal); // Never NULL!
                }
            }
            em.clear();
            {
                Query q = em.createNamedQuery("caseWhen");
                List<Object[]> result = q.getResultList();
                assertEquals(result.size(), 3);
                for (Object[] row : result) {
                    assertTrue(row[0] instanceof String);
                    assertTrue(row[1] instanceof String);
                }
            }
            em.clear();
            {
                Query query = em.createNamedQuery("count");
                Long count = (Long)query.getSingleResult();
                assertEquals(count, new Long(3));
            }
            em.clear();
            {
                Query q = em.createNamedQuery("countDistinct");
                Long count = (Long)q.getSingleResult();
                assertEquals(count, new Long(3));
            }
            em.clear();
            {
                Query q = em.createNamedQuery("sum");
                BigDecimal sum = (BigDecimal)q.getSingleResult();
                assertEquals(sum.toString(), "304.99");
            }
            em.clear();
            {
                Query q = em.createNamedQuery("minMax");
                q.setParameter("itemId", testData.items.getFirstId());
                Object[] result = (Object[])q.getSingleResult();
                assertEquals(((BigDecimal)result[0]).compareTo(new BigDecimal("99")), 0);
                assertEquals(((BigDecimal)result[1]).compareTo(new BigDecimal("101")), 0);
            }
            em.clear();

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

    @Test(groups = {"H2"})
    public void executeQueriesWithFunctions() throws Exception {
        TestDataCategoriesItems testData = storeTestData();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            // Create the named query dynamically, only works with certain dialects
            JPA.getEntityManagerFactory().addNamedQuery(
                "dateDiffProjection",
                em.createQuery(
                    "select i.name, " +
                    "function('DATEDIFF', 'DAY', i.createdOn, i.auctionEnd) " +
                    "from Item i"
                )
            );

            {
                Query q = em.createNamedQuery("dateDiffProjection");
                List<Object[]> result = q.getResultList();
                assertEquals(result.size(), 3);
                for (Object[] row : result) {
                    assertTrue(row[0] instanceof String);
                    assertTrue(row[1] instanceof Integer);
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
