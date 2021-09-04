package org.jpwh.test.querying.criteria;

import org.jpwh.model.querying.Bid;
import org.jpwh.model.querying.Item;
import org.jpwh.model.querying.LogRecord;
import org.jpwh.model.querying.User;
import org.jpwh.test.querying.QueryingTest;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Fetch;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class Joins extends QueryingTest {

    @Test
    public void executeQueries() throws Exception {
        TestDataCategoriesItems testData = storeTestData();

        CriteriaBuilder cb =
            JPA.getEntityManagerFactory().getCriteriaBuilder();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();
            { // Implicit inner join

                CriteriaQuery criteria = cb.createQuery();
                Root<Bid> b = criteria.from(Bid.class);
                criteria.select(b).where(
                    cb.like(
                        b.get("item").<String>get("name"),
                        "Fo%"
                    )
                );

                Query q = em.createQuery(criteria);
                List<Bid> result = q.getResultList();
                assertEquals(result.size(), 3);
                for (Bid bid : result) {
                    assertEquals(bid.getItem().getId(), testData.items.getFirstId());
                }
            }
            em.clear();
            { // Multiple inner

                CriteriaQuery<Bid> criteria = cb.createQuery(Bid.class);
                Root<Bid> b = criteria.from(Bid.class);
                criteria.select(b).where(
                    cb.equal(
                        b.get("item").get("seller").get("username"),
                        "johndoe"
                    )
                );

                Query q = em.createQuery(criteria);
                List<Bid> result = q.getResultList();
                assertEquals(result.size(), 4);
            }
            em.clear();
            { // Multiple inner

                CriteriaQuery criteria = cb.createQuery();
                Root<Bid> b = criteria.from(Bid.class);
                criteria.select(b).where(
                    cb.and(
                        cb.equal(
                            b.get("item").get("seller").get("username"),
                            "johndoe"
                        ),
                        cb.isNotNull(b.get("item").get("buyNowPrice"))
                    )
                );

                Query q = em.createQuery(criteria);
                List<Bid> result = q.getResultList();
                assertEquals(result.size(), 3);
            }
            em.clear();
            { // Explicit inner

                CriteriaQuery criteria = cb.createQuery();
                Root<Item> i = criteria.from(Item.class);
                Join<Item, Bid> b = i.join("bids");
                criteria.select(i).where(
                    cb.gt(b.<BigDecimal>get("amount"), new BigDecimal(100))
                );

                Query q = em.createQuery(criteria);
                List<Item> result = q.getResultList();
                assertEquals(result.size(), 1);
                assertEquals(result.get(0).getId(), testData.items.getFirstId());
            }
            em.clear();
            { // Explicit outer
                CriteriaQuery criteria = cb.createQuery();
                Root<Item> i = criteria.from(Item.class);
                Join<Item, Bid> b = i.join("bids", JoinType.LEFT);
                b.on(
                    cb.gt(b.<BigDecimal>get("amount"), new BigDecimal(100))
                );
                criteria.multiselect(i, b);

                Query q = em.createQuery(criteria);
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
            { // Explicit right outer
                /* TODO Right outer joins not supported in criteria, Hibernate bug JPA-2

                CriteriaQuery criteria = cb.createQuery();
                Root<Bid> b = criteria.from(Bid.class);
                Join<Bid, Item> i = b.join("item", JoinType.RIGHT);
                criteria.multiselect(b, i).where(
                   cb.or(
                      cb.isNull(b),
                      cb.gt(b.<BigDecimal>get("amount"), new BigDecimal(100)))
                );

                Query q = em.createQuery(criteria);
                List<Object[]> result = q.getResultList();
                assertEquals(result.size(), 2);
                assertTrue(result.get(0)[0] instanceof Bid);
                assertTrue(result.get(0)[1] instanceof Item);
                assertEquals(result.get(1)[0], null);
                assertTrue(result.get(1)[1] instanceof Item);
                */
            }
            em.clear();
            { // A typical "fetch the bids collections for all items as a side effect" query

                CriteriaQuery criteria = cb.createQuery();
                Root<Item> i = criteria.from(Item.class);
                i.fetch("bids", JoinType.LEFT);
                criteria.select(i);

                Query q = em.createQuery(criteria);
                List<Item> result = q.getResultList();
                assertEquals(result.size(), 5); // 3 items, 4 bids, 5 "rows" in result!
                Set<Item> distinctResult = new LinkedHashSet<Item>(result); // In-memory "distinct"
                assertEquals(distinctResult.size(), 3); // It was only three items all along...

                boolean haveBids = false;
                for (Item item : distinctResult) {
                    em.detach(item); // No more lazy loading!
                    if (item.getBids().size() > 0) {
                        haveBids = true;
                        break;
                    }
                }
                assertTrue(haveBids);
            }
            em.clear();
            { // Hibernate can remove the duplicate SQL resultset rows for you
                // (...who knows, some of you actually WANT the duplicates!)

                CriteriaQuery criteria = cb.createQuery();
                Root<Item> i = criteria.from(Item.class);
                i.fetch("bids", JoinType.LEFT);
                criteria.select(i).distinct(true);

                Query q = em.createQuery(criteria);
                List<Item> result = q.getResultList();
                assertEquals(result.size(), 3); // Hibernate filtered it for us in-memory
                boolean haveBids = false;
                for (Item item : result) {
                    em.detach(item); // No more lazy loading!
                    if (item.getBids().size() > 0) {
                        haveBids = true;
                        break;
                    }
                }
                assertTrue(haveBids);
            }
            em.clear();
            { // Fetch multiple associated instances/collections

                CriteriaQuery criteria = cb.createQuery();
                Root<Item> i = criteria.from(Item.class);
                Fetch<Item, Bid> b = i.fetch("bids", JoinType.LEFT);
                b.fetch("bidder"); // These are non-nullable foreign key columns, inner join or
                i.fetch("seller", JoinType.LEFT); // outer doesn't make a difference!
                criteria.select(i).distinct(true);

                Query q = em.createQuery(criteria);
                List<Item> result = q.getResultList();
                assertEquals(result.size(), 2);
                boolean haveBids = false;
                boolean haveBidder = false;
                boolean haveSeller = false;
                for (Item item : result) {
                    em.detach(item); // No more lazy loading!
                    if (item.getBids().size() > 0) {
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
            { // SQL Cartesian product of multiple collections! Bad!

                CriteriaQuery criteria = cb.createQuery();
                Root<Item> i = criteria.from(Item.class);
                i.fetch("bids", JoinType.LEFT);
                i.fetch("images", JoinType.LEFT); // Cartesian product, bad!
                criteria.select(i).distinct(true);

                Query q = em.createQuery(criteria);
                List<Item> result = q.getResultList();
                assertEquals(result.size(), 3);
                boolean haveBids = false;
                boolean haveImages = false;
                for (Item item : result) {
                    em.detach(item); // No more lazy loading!
                    if (item.getBids().size() > 0)
                        haveBids = true;
                    if (item.getImages().size() > 0)
                        haveImages = true;
                }
                assertTrue(haveBids);
                assertTrue(haveImages);
            }
            em.clear();
            { // Theta style inner join

                CriteriaQuery criteria = cb.createQuery();
                Root<User> u = criteria.from(User.class);
                Root<LogRecord> log = criteria.from(LogRecord.class);
                criteria.where(
                    cb.equal(u.get("username"), log.get("username")));
                criteria.multiselect(u, log);

                Query q = em.createQuery(criteria);
                List<Object[]> result = q.getResultList();
                for (Object[] row : result) {
                    assertTrue(row[0] instanceof User);
                    assertTrue(row[1] instanceof LogRecord);
                }
                assertEquals(result.size(), 2);
            }
            em.clear();
            { // Theta style inner join, multiple

                CriteriaQuery criteria = cb.createQuery();
                Root<Item> i = criteria.from(Item.class);
                Root<Bid> b = criteria.from(Bid.class);
                criteria.where(
                    cb.equal(b.get("item"), i),
                    cb.equal(i.get("seller"), b.get("bidder"))
                );
                criteria.multiselect(i, b);

                Query q = em.createQuery(criteria);
                List<Object[]> result = q.getResultList();
                assertEquals(result.size(), 0);
            }
            em.clear();
            { // Inner join with implicit ID comparison

                CriteriaQuery criteria = cb.createQuery();
                Root<Item> i = criteria.from(Item.class);
                Root<User> u = criteria.from(User.class);
                criteria.where(
                    cb.equal(i.get("seller"), u),
                    cb.like(u.<String>get("username"), "j%")
                );
                criteria.multiselect(i, u);

                Query q = em.createQuery(criteria);
                List<Object[]> result = q.getResultList();
                assertEquals(result.size(), 3);
                for (Object[] row : result) {
                    assertTrue(row[0] instanceof Item);
                    assertTrue(row[1] instanceof User);
                }
            }
            em.clear();
            { // Inner join with explicit ID comparison

                CriteriaQuery criteria = cb.createQuery();
                Root<Item> i = criteria.from(Item.class);
                Root<User> u = criteria.from(User.class);
                criteria.where(
                    cb.equal(i.get("seller").get("id"), u.get("id")),
                    cb.like(u.<String>get("username"), "j%")
                );
                criteria.multiselect(i, u);

                Query q = em.createQuery(criteria);
                List<Object[]> result = q.getResultList();
                assertEquals(result.size(), 3);
                for (Object[] row : result) {
                    assertTrue(row[0] instanceof Item);
                    assertTrue(row[1] instanceof User);
                }
            }
            em.clear();
            { // Binding an entity parameter

                CriteriaQuery criteria = cb.createQuery();
                Root<Item> i = criteria.from(Item.class);
                criteria.where(
                    cb.equal(
                        i.get("seller"),
                        cb.parameter(User.class, "seller")
                    )
                );
                criteria.select(i);

                Query q = em.createQuery(criteria);
                User someUser = em.find(User.class, testData.users.getFirstId());
                q.setParameter("seller", someUser);
                List<Item> result = q.getResultList();
                assertEquals(result.size(), 2);
            }
            em.clear();
            { // Binding ID parameter

                CriteriaQuery criteria = cb.createQuery();
                Root<Item> i = criteria.from(Item.class);
                criteria.where(
                    cb.equal(
                        i.get("seller").get("id"),
                        cb.parameter(Long.class, "sellerId")
                    )
                );
                criteria.select(i);

                Query q = em.createQuery(criteria);
                Long USER_ID = testData.users.getFirstId();
                q.setParameter("sellerId", USER_ID);
                List<Item> result = q.getResultList();
                assertEquals(result.size(), 2);
            }
            em.clear();
            { // Not a join, just ID comparison

                CriteriaQuery<Bid> criteria = cb.createQuery(Bid.class);
                Root<Bid> b = criteria.from(Bid.class);
                criteria.where(
                    cb.equal(
                        b.get("item").get("id"),
                        cb.parameter(Long.class, "itemId")
                    )
                );
                criteria.select(b);

                Query q = em.createQuery(criteria);
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
