package org.jpwh.test.querying.criteria;

import org.jpwh.model.querying.Address;
import org.jpwh.model.querying.Bid;
import org.jpwh.model.querying.Item;
import org.jpwh.model.querying.ItemSummary;
import org.jpwh.model.querying.User;
import org.jpwh.test.querying.QueryingTest;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.TupleElement;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
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

        CriteriaBuilder cb =
            JPA.getEntityManagerFactory().getCriteriaBuilder();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            { // Product
                CriteriaQuery criteria = cb.createQuery();
                Root<Item> i = criteria.from(Item.class);
                Root<Bid> b = criteria.from(Bid.class);
                criteria.select(cb.tuple(i, b));

                /* Convenient alternative:
                criteria.multiselect(
                    criteria.from(Item.class),
                    criteria.from(Bid.class)
                );
                */
                Query q = em.createQuery(criteria);
                List<Object[]> result = q.getResultList();
                assertEquals(result.size(), 12); // Cartesian product!

                Set<Item> items = new HashSet();
                Set<Bid> bids = new HashSet();
                for (Object[] row : result) {
                    assertTrue(row[0] instanceof Item);
                    items.add((Item) row[0]);

                    assertTrue(row[1] instanceof Bid);
                    bids.add((Bid) row[1]);
                }
                assertEquals(items.size(), 3);
                assertEquals(bids.size(), 4);
            }
            em.clear();
            { // Transient result
                CriteriaQuery criteria = cb.createQuery();
                Root<User> u = criteria.from(User.class);
                criteria.multiselect( // Returns List of Object[]
                    u.get("id"), u.get("username"), u.get("homeAddress")
                );

                Query q = em.createQuery(criteria);
                List<Object[]> result = q.getResultList();
                assertEquals(result.size(), 3);

                Object[] firstRow = result.get(0);
                assertTrue(firstRow[0] instanceof Long);
                assertTrue(firstRow[1] instanceof String);
                assertTrue(firstRow[2] instanceof Address);
            }
            em.clear();
            { // Distinct
                CriteriaQuery<String> criteria = cb.createQuery(String.class);

                criteria.select(
                    criteria.from(Item.class).<String>get("name")
                );
                Query q = em.createQuery(criteria);
                assertEquals(q.getResultList().size(), 3);
            }
            em.clear();
            { // Distinct
                CriteriaQuery<String> criteria = cb.createQuery(String.class);

                criteria.select(
                    criteria.from(Item.class).<String>get("name")
                );
                criteria.distinct(true);
                Query q = em.createQuery(criteria);
                assertEquals(q.getResultList().size(), 3);
            }
            em.clear();
            { // Dynamic instance creation
                CriteriaQuery criteria = cb.createQuery();
                Root<Item> i = criteria.from(Item.class);
                criteria.select(
                    cb.construct(
                        ItemSummary.class, // Must have the right constructor!
                        i.get("id"), i.get("name"), i.get("auctionEnd")
                    )
                );
                Query q = em.createQuery(criteria);
                List<ItemSummary> result = q.getResultList();
                assertEquals(result.size(), 3);
            }
            em.clear();
            { // Tuple API
                CriteriaQuery<Tuple> criteria = cb.createTupleQuery();

                // Or: CriteriaQuery<Tuple> criteria = cb.createQuery(Tuple.class);

                criteria.multiselect(
                    criteria.from(Item.class).alias("i"), // Aliases optional!
                    criteria.from(Bid.class).alias("b")
                );

                TypedQuery<Tuple> query = em.createQuery(criteria);
                List<Tuple> result = query.getResultList();

                Set<Item> items = new HashSet();
                Set<Bid> bids = new HashSet();

                for (Tuple tuple : result) {
                    // Indexed
                    Item item = tuple.get(0, Item.class);
                    Bid bid = tuple.get(1, Bid.class);

                    // Alias
                    item = tuple.get("i", Item.class);
                    bid = tuple.get("b", Bid.class);

                    // Meta
                    for (TupleElement<?> element : tuple.getElements()) {
                        Class clazz = element.getJavaType();
                        String alias = element.getAlias();
                        Object value = tuple.get(element);
                    }
                    items.add(item);
                    bids.add(bid);
                }
                assertEquals(result.size(), 12); // Cartesian product!
                assertEquals(items.size(), 3);
                assertEquals(bids.size(), 4);

            }
            em.clear();

            // All following are more function call examples, with variations

            { // Concat

                CriteriaQuery criteria = cb.createQuery();
                Root<Item> i = criteria.from(Item.class);
                criteria.select(
                    cb.concat(
                        cb.concat(i.<String>get("name"), ":"),
                        i.<String>get("auctionEnd") // Note the cast of Date!
                    )
                );
                Query q = em.createQuery(criteria);
                assertEquals(q.getResultList().size(), 3);
            }
            em.clear();

            { // Coalesce

                CriteriaQuery criteria = cb.createQuery();
                Root<Item> i = criteria.from(Item.class);
                criteria.multiselect(
                    i.get("name"),
                    cb.coalesce(i.<BigDecimal>get("buyNowPrice"), 0)
                );

                Query q = em.createQuery(criteria);
                List<Object[]> result = q.getResultList();
                assertEquals(result.size(), 3);
                for (Object[] row : result) {
                    assertTrue(row[0] instanceof String);
                    assertTrue(row[1] instanceof BigDecimal); // Never NULL!
                }
            }
            em.clear();
            { // Case When

                /*
                CriteriaQuery criteria = cb.createQuery();
                TODO String literals not supported, Hibernate bug HHH-8124
                Root<User> u = criteria.from(User.class);
                criteria.multiselect(
                    u.get("username"),
                    cb.selectCase()
                        .when(
                            cb.equal(
                                cb.length(u.get("homeAddress").<String>get("zipcode")), 5
                            ), "Germany"
                        )
                        .when(
                            cb.equal(
                                cb.length(u.get("homeAddress").<String>get("zipcode")), 4
                            ), "Switzerland"
                        )
                        .otherwise("Other")
                );

                Query q = em.createQuery(criteria);
                List<Object[]> result = q.getResultList();
                assertEquals(result.size(), 3);
                for (Object[] row : result) {
                    assertTrue(row[0] instanceof String);
                    assertTrue(row[1] instanceof String);
                }
                */
            }
            em.clear();

            { // Count

                CriteriaQuery criteria = cb.createQuery();
                criteria.select(
                    cb.count(criteria.from(Item.class))
                );

                Query q = em.createQuery(criteria);
                Long count = (Long) q.getSingleResult();
                assertEquals(count, new Long(3));
            }
            em.clear();
            { // count(distinct ...)

                CriteriaQuery criteria = cb.createQuery();
                criteria.select(
                    cb.countDistinct(
                        criteria.from(Item.class).get("name")
                    )
                );
                Query q = em.createQuery(criteria);
                Long count = (Long) q.getSingleResult();
                assertEquals(count, new Long(3));
            }
            em.clear();
            { // Sum

                CriteriaQuery<Number> criteria = cb.createQuery(Number.class);
                criteria.select(
                    cb.sum(
                        criteria.from(Bid.class).<BigDecimal>get("amount")
                    )
                );
                Query q = em.createQuery(criteria);
                BigDecimal sum = (BigDecimal) q.getSingleResult();
                assertEquals(sum.compareTo(new BigDecimal("304.99")), 0);
            }
            em.clear();
            {// Min/Max

                CriteriaQuery criteria = cb.createQuery();
                Root<Bid> b = criteria.from(Bid.class);
                criteria.multiselect(
                    cb.min(b.<BigDecimal>get("amount")),
                    cb.max(b.<BigDecimal>get("amount"))
                );
                criteria.where(
                    cb.equal(
                        b.get("item").<Long>get("id"),
                        cb.parameter(Long.class, "itemId")
                    )
                );

                Query q = em.createQuery(criteria);
                q.setParameter("itemId", testData.items.getFirstId());
                Object[] result = (Object[]) q.getSingleResult();
                assertEquals(((BigDecimal) result[0]).compareTo(new BigDecimal("99")), 0);
                assertEquals(((BigDecimal) result[1]).compareTo(new BigDecimal("101")), 0);
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

        CriteriaBuilder cb =
            JPA.getEntityManagerFactory().getCriteriaBuilder();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            { // Calling arbitrary functions

                CriteriaQuery criteria = cb.createQuery();
                Root<Item> i = criteria.from(Item.class);
                criteria.multiselect(
                    i.get("name"),
                    cb.function(
                        "DATEDIFF",
                        Integer.class,
                        cb.literal("DAY"),
                        i.get("createdOn"),
                        i.get("auctionEnd")
                    )
                );

                Query q = em.createQuery(criteria);
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
