package org.jpwh.test.concurrency;

import org.jpwh.env.JPATest;
import org.jpwh.model.concurrency.versionall.Item;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.OptimisticLockException;
import javax.transaction.UserTransaction;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class VersioningAll extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("ConcurrencyVersioningAllPU");
    }

    @Test(expectedExceptions = OptimisticLockException.class)
    public void firstCommitWins() throws Throwable {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();
            Item someItem = new Item();
            someItem.setName("Some Item");
            em.persist(someItem);
            tx.commit();
            em.close();
            final Long ITEM_ID = someItem.getId();

            // Load an item and change its name
            tx.begin();
            em = JPA.createEntityManager();

            Item item = em.find(Item.class, ITEM_ID);

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
                        item.setName("Other Name");

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
                tx.commit();
                // Version check: How many rows have been updated?
            } catch (Exception ex) {
                throw unwrapCauseOfType(ex, OptimisticLockException.class);
            }
            em.close();
        } finally {
            TM.rollback();
        }
    }

}
