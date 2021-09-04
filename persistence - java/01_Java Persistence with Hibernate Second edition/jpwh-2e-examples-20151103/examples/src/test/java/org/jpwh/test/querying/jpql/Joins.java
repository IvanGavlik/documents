package org.jpwh.test.querying.jpql;

import org.jpwh.model.querying.Bid;
import org.jpwh.model.querying.Item;
import org.jpwh.model.querying.LogRecord;
import org.jpwh.model.querying.User;
import org.jpwh.test.querying.QueryingTest;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.UserTransaction;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class Joins extends QueryingTest {

    @Test
    public void executeQueries() throws Exception {
        TestDataCategoriesItems testData = storeTestData();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();
            {
                Query q = em.createNamedQuery("implicitInner");
                List<Bid> result = q.getResultList();
                assertEquals(result.size(), 3);
                for (Bid bid : result) {
                    assertEquals(bid.getItem().getId(), testData.items.getFirstId());
                }
            }
            em.clear();
            {
                Query q = em.createNamedQuery("multipleImplicitInner");
                List<Bid> result = q.getResultList();
                assertEquals(result.size(), 4);
            }
            em.clear();
            {
                Query q = em.createNamedQuery("multipleImplicitInnerAnd");
                List<Bid> result = q.getResultList();
                assertEquals(result.size(), 3);
            }
            em.clear();
            {
                Query q = em.createNamedQuery("explicitInner");
                List<Item> result = q.getResultList();
                assertEquals(result.size(), 1);
                assertEquals(result.get(0).getId(), testData.items.getFirstId());
            }
            em.clear();
            {
                Query q = em.createNamedQuery("explicitOuter");
                List<Object[]> result = q.getResultList();
                assertEquals(result.size(), 3);
                assertTrue(result.get(0)[0] instanceof Item);
                assertTrue(result.get(0)[1] instanceof Bid);
                assertTrue(result.get(1)[0] instanceof Item);
                assertEquals(result.get(1)[1], null);
                assertTrue(result.get(2)[0] instanceof Item);
                assertEquals(result.get(2)[1], null);
            }
            em.clear();
            {
                Query q = em.createNamedQuery("explicitOuterRight");
                List<Object[]> result = q.getResultList();
                assertEquals(result.size(), 2);
                assertTrue(result.get(0)[0] instanceof Bid);
                assertTrue(result.get(0)[1] instanceof Item);
                assertEquals(result.get(1)[0], null);
                assertTrue(result.get(1)[1] instanceof Item);
            }
            em.clear();
            {
                Query query = em.createNamedQuery("outerFetchCollection");
                List<Item> result = query.getResultList();
                assertEquals(result.size(), 5); // 3 items, 4 bids, 5 "rows" in result!

                Set<Item> distinctResult = new LinkedHashSet<Item>(result); // In-memory "distinct"
                assertEquals(distinctResult.size(), 3); // It was only three items all along...

                boolean haveBids = false;
                for (Item item : distinctResult) {
                    em.detach(item); // No more lazy loading!
                    if(item.getBids().size() > 0) {
                        haveBids = true;
                        break;
                    }
                }
                assertTrue(haveBids);
            }
            em.clear();
            {
                Query q = em.createNamedQuery("outerFetchCollectionDistinct");
                List<Item> result = q.getResultList();
                assertEquals(result.size(), 3); // Hibernate filtered it for us in-memory
                boolean haveBids = false;
                for (Item item : result) {
                    em.detach(item); // No more lazy loading!
                    if(item.getBids().size() > 0) {
                        haveBids = true;
                        break;
                    }
                }
                assertTrue(haveBids);
            }
            em.clear();
            {
                Query q = em.createNamedQuery("outerFetchMultiple");
                List<Item> result = q.getResultList();
                assertEquals(result.size(), 2);
                boolean haveBids = false;
                boolean haveBidder = false;
                boolean haveSeller = false;
                for (Item item : result) {
                    em.detach(item); // No more lazy loading!
                    if(item.getBids().size() > 0) {
                        haveBids = true;
                        Bid bid = item.getBids().iterator().next();
                        if (bid.getBidder() != null && bid.getBidder().getUsername() != null) {
                            haveBidder = true;
                        }
                    }
                    if (item.getSeller() != null && item.getSeller().getUsername() != null)
                        haveSeller = true;
                }
                assertTrue(haveBids);
                assertTrue(haveBidder);
                assertTrue(haveSeller);
            }
            em.clear();
            {
                // SQL Cartesian product of multiple collections! Bad!
                Query q = em.createNamedQuery("badProductFetch");
                List<Item> result = q.getResultList();
                assertEquals(result.size(), 3);
                boolean haveBids = false;
                boolean haveImages = false;
                for (Item item : result) {
                    em.detach(item); // No more lazy loading!
                    if(item.getBids().size() > 0)
                        haveBids = true;
                    if(item.getImages().size() > 0)
                        haveImages = true;
                }
                assertTrue(haveBids);
                assertTrue(haveImages);
            }
            em.clear();
            {
                Query query = em.createNamedQuery("thetaStyle");
                List<Object[]> result = query.getResultList();
                for (Object[] row : result) {
                    assertTrue(row[0] instanceof User);
                    assertTrue(row[1] instanceof LogRecord);
                }
                assertEquals(result.size(), 2);
            }
            em.clear();
            {
                Query q = em.createNamedQuery("thetaEqualsId");
                List<Object[]> result = q.getResultList();
                assertEquals(result.size(), 0);
            }
            em.clear();
            {
                Query q = em.createNamedQuery("implicitEqualsId");
                List<Object[]> result = q.getResultList();
                assertEquals(result.size(), 3);
                for (Object[] row : result) {
                    assertTrue(row[0] instanceof Item);
                    assertTrue(row[1] instanceof User);
                }
            }
            em.clear();
            {
                Query q = em.createNamedQuery("explicitEqualsId");
                List<Object[]> result = q.getResultList();
                assertEquals(result.size(), 3);
                for (Object[] row : result) {
                    assertTrue(row[0] instanceof Item);
                    assertTrue(row[1] instanceof User);
                }
            }
            em.clear();
            {
                User someUser = em.find(User.class, testData.users.getFirstId());
                Query query = em.createNamedQuery("parameterEqualsEntity");
                query.setParameter("seller", someUser);
                List<Item> result = query.getResultList();
                assertEquals(result.size(), 2);
            }
            em.clear();
            {
                Long USER_ID = testData.users.getFirstId();
                Query query = em.createNamedQuery("parameterEqualsId");
                query.setParameter("sellerId", USER_ID);
                List<Item> result = query.getResultList();
                assertEquals(result.size(), 2);
            }
            em.clear();
            {
                Query q = em.createNamedQuery("nojoinEqualsId");
                q.setParameter("itemId", testData.items.getFirstId());
                List<Bid> result = q.getResultList();
                assertEquals(result.size(), 3);
            }
            em.clear();

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }
}
