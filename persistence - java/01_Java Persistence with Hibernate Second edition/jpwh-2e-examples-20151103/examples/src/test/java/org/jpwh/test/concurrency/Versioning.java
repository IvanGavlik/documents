package org.jpwh.test.concurrency;

import org.jpwh.env.JPATest;
import org.jpwh.model.concurrency.version.Bid;
import org.jpwh.model.concurrency.version.Category;
import org.jpwh.model.concurrency.version.InvalidBidException;
import org.jpwh.model.concurrency.version.Item;
import org.jpwh.shared.util.TestData;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static org.testng.Assert.assertEquals;

public class Versioning extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("ConcurrencyVersioningPU");
    }

    @Test(expectedExceptions = OptimisticLockException.class)
    public void firstCommitWins() throws Throwable {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();
            Item someItem = new Item("Some Item");
            em.persist(someItem);
            tx.commit();
            em.close();
            final Long ITEM_ID = someItem.getId();

            tx.begin();
            em = JPA.createEntityManager();

            /* 
               Retrieving an entity instance by identifier loads the current version from the
               database with a <code>SELECT</code>.
             */
            Item item = em.find(Item.class, ITEM_ID);
            // select * from ITEM where ID = ?

            /* 
               The current version of the <code>Item</code> instance is 0.
             */
            assertEquals(item.getVersion(), 0);

            item.setName("New Name");

            // The concurrent second unit of work doing the same
            Executors.newSingleThreadExecutor().submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    UserTransaction tx = TM.getUserTransaction();
                    try {
                        tx.begin();
                        EntityManager em = JPA.createEntityManager();

                        Item item = em.find(Item.class, ITEM_ID);
                        // select * from ITEM where ID = ?

                        assertEquals(item.getVersion(), 0);

                        item.setName("Other Name");

                        tx.commit();
                        // update ITEM set NAME = ?, VERSION = 1 where ID = ? and VERSION = 0
                        // This succeeds, there is a row with ID = ? and VERSION = 0 in the database!
                        em.close();

                    } catch (Exception ex) {
                        // This shouldn't happen, this commit should win!
                        TM.rollback();
                        throw new RuntimeException("Concurrent operation failure: " + ex, ex);
                    }
                    return null;
                }
            }).get();

            /* 
               When the persistence context is flushed Hibernate will detect the dirty
               <code>Item</code> instance and increment its version to 1. The SQL
               <code>UPDATE</code> now performs the version check, storing the new version
               in the database, but only if the database version is still 0.
             */
            em.flush();
            // update ITEM set NAME = ?, VERSION = 1 where ID = ? and VERSION = 0

        } catch (Exception ex) {
            throw unwrapCauseOfType(ex, OptimisticLockException.class);
        } finally {
            TM.rollback();
        }
    }

    // TODO This throws the wrong exception!
    // @Test(expectedExceptions = OptimisticLockException.class)
    @Test(expectedExceptions = org.hibernate.OptimisticLockException.class)
    public void manualVersionChecking() throws Throwable {
        final ConcurrencyTestData testData = storeCategoriesAndItems();
        Long[] CATEGORIES = testData.categories.identifiers;

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            BigDecimal totalPrice = new BigDecimal(0);
            for (Long categoryId : CATEGORIES) {

                /* 
                   For each <code>Category</code>, query all <code>Item</code> instances with
                   an <code>OPTIMISTIC</code> lock mode. Hibernate now knows it has to
                   check each <code>Item</code> at flush time.
                 */
                List<Item> items =
                    em.createQuery("select i from Item i where i.category.id = :catId")
                        .setLockMode(LockModeType.OPTIMISTIC)
                        .setParameter("catId", categoryId)
                        .getResultList();

                for (Item item : items)
                    totalPrice = totalPrice.add(item.getBuyNowPrice());

                // Now a concurrent transaction will move an item to another category
                if (categoryId.equals(testData.categories.getFirstId())) {
                    Executors.newSingleThreadExecutor().submit(new Callable<Object>() {
                        @Override
                        public Object call() throws Exception {
                            UserTransaction tx = TM.getUserTransaction();
                            try {
                                tx.begin();
                                EntityManager em = JPA.createEntityManager();

                                // Moving the first item from the first category into the last category
                                List<Item> items =
                                    em.createQuery("select i from Item i where i.category.id = :catId")
                                        .setParameter("catId", testData.categories.getFirstId())
                                        .getResultList();

                                Category lastCategory = em.getReference(
                                    Category.class, testData.categories.getLastId()
                                );

                                items.iterator().next().setCategory(lastCategory);

                                tx.commit();
                                em.close();
                            } catch (Exception ex) {
                                // This shouldn't happen, this commit should win!
                                TM.rollback();
                                throw new RuntimeException("Concurrent operation failure: " + ex, ex);
                            }
                            return null;
                        }
                    }).get();
                }
            }

            /* 
               For each <code>Item</code> loaded earlier with the locking query, Hibernate will
               now execute a <code>SELECT</code> during flushing. It checks if the database
               version of each <code>ITEM</code> row is still the same as when it was loaded
               earlier. If any <code>ITEM</code> row has a different version, or the row doesn't
               exist anymore, an <code>OptimisticLockException</code> will be thrown.
             */
            tx.commit();
            em.close();

            assertEquals(totalPrice.toString(), "108.00");
        } catch (Exception ex) {
            throw unwrapCauseOfType(ex, org.hibernate.OptimisticLockException.class);
        } finally {
            TM.rollback();
        }
    }

    // TODO This throws the wrong exception!
    //@Test(expectedExceptions = OptimisticLockException.class)
    @Test(expectedExceptions = org.hibernate.StaleObjectStateException.class)
    public void forceIncrement() throws Throwable {
        final TestData testData = storeItemAndBids();
        Long ITEM_ID = testData.getFirstId();

        UserTransaction tx = TM.getUserTransaction();
        try {

            tx.begin();
            EntityManager em = JPA.createEntityManager();

            /* 
               The <code>find()</code> method accepts a <code>LockModeType</code>. The
               <code>OPTIMISTIC_FORCE_INCREMENT</code> mode tells Hibernate that the version
               of the retrieved <code>Item</code> should be incremented after loading,
               even if it's never modified in the unit of work.
             */
            Item item = em.find(
                Item.class,
                ITEM_ID,
                LockModeType.OPTIMISTIC_FORCE_INCREMENT
            );

            Bid highestBid = queryHighestBid(em, item);

            // Now a concurrent transaction will place a bid for this item, and
            // succeed because the first commit wins!
            Executors.newSingleThreadExecutor().submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    UserTransaction tx = TM.getUserTransaction();
                    try {
                        tx.begin();
                        EntityManager em = JPA.createEntityManager();

                        Item item = em.find(
                            Item.class,
                            testData.getFirstId(),
                            LockModeType.OPTIMISTIC_FORCE_INCREMENT
                        );
                        Bid highestBid = queryHighestBid(em, item);
                        try {
                            Bid newBid = new Bid(
                                new BigDecimal("44.44"),
                                item,
                                highestBid
                            );
                            em.persist(newBid);
                        } catch (InvalidBidException ex) {
                            // Ignore
                        }

                        tx.commit();
                        em.close();
                    } catch (Exception ex) {
                        // This shouldn't happen, this commit should win!
                        TM.rollback();
                        throw new RuntimeException("Concurrent operation failure: " + ex, ex);
                    }
                    return null;
                }
            }).get();

            try {
                /* 
                   The code persists a new <code>Bid</code> instance; this does not affect
                   any values of the <code>Item</code> instance. A new row will be inserted
                   into the <code>BID</code> table. Hibernate would not detect concurrently
                   made bids at all without a forced version increment of the
                   <code>Item</code>. We also use a checked exception to validate the
                   new bid amount; it must be greater than the currently highest bid.
                */
                Bid newBid = new Bid(
                    new BigDecimal("44.44"),
                    item,
                    highestBid
                );
                em.persist(newBid);
            } catch (InvalidBidException ex) {
                // Bid too low, show a validation error screen...
            }

            /* 
               When flushing the persistence context, Hibernate will execute an
               <code>INSERT</code> for the new <code>Bid</code> and force an
               <code>UPDATE</code> of the <code>Item</code> with a version check.
               If someone modified the <code>Item</code> concurrently, or placed a
               <code>Bid</code> concurrently with this procedure, Hibernate throws
               an exception.
             */
            tx.commit();
            em.close();
        } catch (Exception ex) {
            throw unwrapCauseOfType(ex, org.hibernate.StaleObjectStateException.class);
        } finally {
            TM.rollback();
        }
    }

    /* ################################################################################### */

    class ConcurrencyTestData {
        TestData categories;
        TestData items;
    }

    public ConcurrencyTestData storeCategoriesAndItems() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        tx.begin();
        EntityManager em = JPA.createEntityManager();
        ConcurrencyTestData testData = new ConcurrencyTestData();
        testData.categories = new TestData(new Long[3]);
        testData.items= new TestData(new Long[5]);
        for (int i = 1; i <= testData.categories.identifiers.length; i++) {
            Category category = new Category();
            category.setName("Category: " + i);
            em.persist(category);
            testData.categories.identifiers[i - 1] = category.getId();
            for (int j = 1; j <= testData.categories.identifiers.length; j++) {
                Item item = new Item("Item " + j);
                item.setCategory(category);
                item.setBuyNowPrice(new BigDecimal(10 + j));
                em.persist(item);
                testData.items.identifiers[(i - 1) + (j - 1)] = item.getId();
            }
        }
        tx.commit();
        em.close();
        return testData;
    }

    public TestData storeItemAndBids() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        tx.begin();
        EntityManager em = JPA.createEntityManager();
        Long[] ids = new Long[1];
        Item item = new Item("Some Item");
        em.persist(item);
        ids[0] = item.getId();
        for (int i = 1; i <= 3; i++) {
            Bid bid = new Bid(new BigDecimal(10 + i), item);
            em.persist(bid);
        }
        tx.commit();
        em.close();
        return new TestData(ids);
    }

    protected Bid queryHighestBid(EntityManager em, Item item) {
        // Can't scroll with cursors in JPA, have to use setMaxResult()
        try {
            return (Bid) em.createQuery(
                "select b from Bid b" +
                    " where b.item = :itm" +
                    " order by b.amount desc"
            )
                .setParameter("itm", item)
                .setMaxResults(1)
                .getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

}
