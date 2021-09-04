package org.jpwh.test.bulkbatch;

import org.jpwh.env.JPATest;
import org.jpwh.model.bulkbatch.Item;
import org.jpwh.model.bulkbatch.User;
import org.jpwh.shared.util.CalendarUtil;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.util.Date;
import java.util.logging.Logger;

import static org.testng.Assert.assertEquals;

public class BatchInsertUpdate extends JPATest {

    final private static Logger log = Logger.getLogger(BatchInsertUpdate.class.getName());

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("BulkBatchPU");
    }

    @Test
    public void batchInsertUpdate() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            long ONE_HUNDRED_THOUSAND = 10000; // Yes, we are lying here so the test doesn't run for 5 minutes
            {
                tx.setTransactionTimeout(300); // 5 minutes, this is the UserTransaction API
                // Only future transactions started on this thread will have the new timeout

                long startTime = new Date().getTime();
                tx.begin();
                EntityManager em = JPA.createEntityManager();

                User johndoe = new User("johndoe");
                em.persist(johndoe);

                /* 
                   Here you create and persist 100,000 <code>Item</code> instances.
                 */
                for (int i = 0; i < ONE_HUNDRED_THOUSAND; i++) {
                    Item item = new Item(
                        // ...
                       "Item " + i, CalendarUtil.TOMORROW.getTime(), johndoe
                    );
                    em.persist(item);

                    /* 
                       After 100 operations, flush and clear the persistence context. This executes
                       the SQL <code>INSERT</code> statements for 100 <code>Item</code> instances, and
                       as they are now in detached state and no longer referenced, the JVM garbage
                       collection can reclaim that memory.
                     */
                    if (i % 100 == 0) {
                        em.flush();
                        em.clear();
                    }
                }

                tx.commit();
                em.close();

                long endTime = new Date().getTime();
                log.info("### Batch insert time in seconds: " + ((endTime-startTime)/1000));
            }
            {
                // Check if all items have been inserted
                tx.begin();
                EntityManager em = JPA.createEntityManager();
                assertEquals(
                   em.createQuery("select count(i) from Item i").getSingleResult(),
                   ONE_HUNDRED_THOUSAND
                );
                tx.commit();
                em.close();
            }
            {
                long startTime = new Date().getTime();

                tx.begin();
                EntityManager em = JPA.createEntityManager();

                /* 
                   You use a JPQL query to load all <code>Item</code> instances from the
                   database. Instead of retrieving the result of the query completely
                   into application memory, you open an online database cursor.
                 */
                org.hibernate.ScrollableResults itemCursor =
                   em.unwrap(org.hibernate.Session.class)
                       .createQuery("select i from Item i")
                       .scroll(org.hibernate.ScrollMode.SCROLL_INSENSITIVE);

                int count = 0;
                /* 
                    You control the cursor with the <code>ScrollableResults</code> API and move it along
                    the result. Each call to <code>next()</code> forwards the cursor to the next record.
                 */
                while (itemCursor.next()) {
                    /* 
                       The <code>get(int i)</code> call retrieves a single entity instance into memory,
                       the record the cursor is currently pointing to.
                     */
                    Item item = (Item) itemCursor.get(0);

                    modifyItem(item);

                    /* 
                       To avoid memory exhaustion, you flush and clear the persistence context
                       before loading the next 100 records into it.
                     */
                    if (++count % 100 == 0) { // Set hibernate.jdbc.batch_size to 100!
                        em.flush();
                        em.clear();
                    }
                }

                itemCursor.close();
                tx.commit();
                em.close();

                long endTime = new Date().getTime();
                log.info("### Batch update time in seconds: " + ((endTime - startTime) / 1000));
            }
            {
                // Check if all items have been updated
                tx.begin();
                EntityManager em = JPA.createEntityManager();
                assertEquals(
                   em.createQuery("select count(i) from Item i where i.active = false").getSingleResult(),
                   0l
                );
                tx.commit();
                em.close();
            }

        } finally {
            TM.rollback();
        }
    }

    // TODO: Bugs
    @Test
    public void batchInsertUpdateWithStatelessSession() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {

            long ONE_HUNDRED_THOUSAND = 10000; // Yes, we are lying here so the test doesn't run for 5 minutes
            {
                tx.begin();
                org.hibernate.SessionFactory sf =
                    JPA.getEntityManagerFactory().unwrap(org.hibernate.SessionFactory.class);
                org.hibernate.StatelessSession statelessSession = sf .openStatelessSession();

                User johndoe = new User("johndoe");
                statelessSession.insert(johndoe);

                for (int i = 0; i < ONE_HUNDRED_THOUSAND; i++) {
                    Item item = new Item(
                       "Item " + i, CalendarUtil.TOMORROW.getTime(), johndoe
                    );

                    statelessSession.insert(item);
                }

                tx.commit();
                statelessSession.close();
            }
            {
                tx.begin();
                org.hibernate.SessionFactory sf =
                    JPA.getEntityManagerFactory().unwrap(org.hibernate.SessionFactory.class);
                org.hibernate.StatelessSession statelessSession = sf .openStatelessSession();

                // TODO I should be seeing this issue but it works: https://hibernate.atlassian.net/browse/HHH-4042
                long count = (Long)statelessSession.createQuery("select count(i) from Item i").uniqueResult();
                assertEquals(count, ONE_HUNDRED_THOUSAND);

                tx.commit();
                statelessSession.close();
            }
            {
                long startTime = new Date().getTime();

                // TODO: This fails if you put a batch size on User.java

                tx.begin();

                /* 
                   You open a <code>StatelessSession</code> on the Hibernate <code>SessionFactory</code>,
                   which you can unwrap from an <code>EntityManagerFactory</code>.
                 */
                org.hibernate.SessionFactory sf =
                    JPA.getEntityManagerFactory().unwrap(org.hibernate.SessionFactory.class);
                org.hibernate.StatelessSession statelessSession = sf .openStatelessSession();

                /* 
                   You use a JPQL query to load all <code>Item</code> instances from the
                   database. Instead of retrieving the result of the query completely
                   into application memory, you open an online database cursor.
                 */
                org.hibernate.ScrollableResults itemCursor =
                    statelessSession
                        .createQuery("select i from Item i")
                       .scroll(org.hibernate.ScrollMode.SCROLL_INSENSITIVE);

                /* 
                   You scroll through the result with the cursor and retrieve an <code>Item</code>
                   entity instance. This instance is in detached state, there is no persistence
                   context!
                 */
                while (itemCursor.next()) {
                    Item item = (Item) itemCursor.get(0);

                    modifyItem(item);

                    /* 
                       Because Hibernate does not detect changes automatically without a persistence context,
                       you have to execute SQL <code>UPDATE</code> statements manually.
                     */
                    statelessSession.update(item);
                }

                itemCursor.close();
                tx.commit();
                statelessSession.close();

                long endTime = new Date().getTime();
                log.info("### Stateless session update time in seconds: " + ((endTime - startTime) / 1000));
            }
            {
                // Check if all items have been updated
                tx.begin();
                EntityManager em = JPA.createEntityManager();
                assertEquals(
                   em.createQuery("select count(i) from Item i where i.active = false").getSingleResult(),
                   0l
                );
                tx.commit();
                em.close();
            }

        } finally {
            TM.rollback();
        }
    }
    protected void modifyItem(Item item) {
        item.setActive(true); // Well, this is trivial but you get the idea
    }

}
