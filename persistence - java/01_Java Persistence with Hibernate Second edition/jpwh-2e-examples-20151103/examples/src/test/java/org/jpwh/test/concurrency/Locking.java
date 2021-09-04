package org.jpwh.test.concurrency;

import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.jpwh.env.DatabaseProduct;
import org.jpwh.model.concurrency.version.Category;
import org.jpwh.model.concurrency.version.Item;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.LockTimeoutException;
import javax.persistence.PersistenceException;
import javax.persistence.PessimisticLockException;
import javax.persistence.PessimisticLockScope;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class Locking extends Versioning {

    @Test
    public void pessimisticReadWrite() throws Exception {
        final ConcurrencyTestData testData = storeCategoriesAndItems();
        Long[] CATEGORIES = testData.categories.identifiers;

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            BigDecimal totalPrice = new BigDecimal(0);
            for (Long categoryId : CATEGORIES) {

                /* 
                   For each <code>Category</code>, query all <code>Item</code> instances in
                   <code>PESSIMISTIC_READ</code> lock mode. Hibernate will lock the rows in
                   the database with the SQL query. If possible, wait for 5 seconds if some
                   other transaction already holds a conflicting lock. If the lock can't
                   be obtained, the query throws an exception.
                 */
                List<Item> items =
                    em.createQuery("select i from Item i where i.category.id = :catId")
                        .setLockMode(LockModeType.PESSIMISTIC_READ)
                        .setHint("javax.persistence.lock.timeout", 5000)
                        .setParameter("catId", categoryId)
                        .getResultList();

                /* 
                   If the query returns successfully, you know that you hold an exclusive lock
                   on the data and no other transaction can access it with an exclusive lock or
                   modify it until this transaction commits.
                 */
                for (Item item : items)
                    totalPrice = totalPrice.add(item.getBuyNowPrice());

                // Now a concurrent transaction will try to obtain a write lock, it fails because
                // we hold a read lock on the data already. Note that on H2 there actually are no
                // read or write locks, only exclusive locks.
                if (categoryId.equals(testData.categories.getFirstId())) {
                    Executors.newSingleThreadExecutor().submit(new Callable<Object>() {
                        @Override
                        public Object call() throws Exception {
                            UserTransaction tx = TM.getUserTransaction();
                            try {
                                tx.begin();
                                EntityManager em = JPA.createEntityManager();

                                // The next query's lock attempt must fail at _some_ point, and
                                // we'd like to wait 5 seconds for the lock to become available:
                                //
                                // - H2 fails with a default global lock timeout of 1 second.
                                //
                                // - Oracle supports dynamic lock timeouts, we set it with
                                //   the 'javax.persistence.lock.timeout' hint on the query:
                                //
                                //      no hint == FOR UPDATE
                                //      javax.persistence.lock.timeout 0ms == FOR UPDATE NOWAIT
                                //      javax.persistence.lock.timeout >0ms == FOR UPDATE WAIT [seconds]
                                //
                                // - PostgreSQL doesn't timeout and just hangs indefinitely if
                                //   NOWAIT isn't specified for the query. One possible way to
                                //   wait for a lock is to set a statement timeout for the whole
                                //   connection/session.
                                if (TM.databaseProduct.equals(DatabaseProduct.POSTGRESQL)) {
                                    em.unwrap(Session.class).doWork(new Work() {
                                        @Override
                                        public void execute(Connection connection) throws SQLException {
                                            connection.createStatement().execute("set statement_timeout = 5000");
                                        }
                                    });
                                }
                                // - MySQL also doesn't support query lock timeouts, but you
                                //   can set a timeout for the whole connection/session.
                                if (TM.databaseProduct.equals(DatabaseProduct.MYSQL)) {
                                    em.unwrap(Session.class).doWork(new Work() {
                                        @Override
                                        public void execute(Connection connection) throws SQLException {
                                            connection.createStatement().execute("set innodb_lock_wait_timeout = 5;");
                                        }
                                    });
                                }

                                // Moving the first item from the first category into the last category
                                // This query should fail as someone else holds a lock on the rows.
                                List<Item> items =
                                    em.createQuery("select i from Item i where i.category.id = :catId")
                                        .setParameter("catId", testData.categories.getFirstId())
                                        .setLockMode(LockModeType.PESSIMISTIC_WRITE) // Prevent concurrent access
                                        .setHint("javax.persistence.lock.timeout", 5000) // Only works on Oracle...
                                        .getResultList();

                                Category lastCategory = em.getReference(
                                    Category.class, testData.categories.getLastId()
                                );

                                items.iterator().next().setCategory(lastCategory);

                                tx.commit();
                                em.close();
                            } catch (Exception ex) {
                                // This should fail, as the data is already locked!
                                TM.rollback();

                                if (TM.databaseProduct.equals(DatabaseProduct.POSTGRESQL)) {
                                    // A statement timeout on PostgreSQL doesn't produce a specific exception
                                    assertTrue(ex instanceof PersistenceException);
                                } else if (TM.databaseProduct.equals(DatabaseProduct.MYSQL)) {
                                    // On MySQL we get a LockTimeoutException
                                    assertTrue(ex instanceof LockTimeoutException);
                                } else {
                                    // On H2 and Oracle we get a PessimisticLockException
                                    assertTrue(ex instanceof PessimisticLockException);
                                }
                            }
                            return null;
                        }
                    }).get();
                }
            }

            /* 
               Our locks will be released after commit, when the transaction completes.
             */
            tx.commit();
            em.close();

            assertEquals(totalPrice.compareTo(new BigDecimal("108")), 0);
        } finally {
            TM.rollback();
        }
    }

    @Test
    public void findLock() throws Exception {
        final ConcurrencyTestData testData = storeCategoriesAndItems();
        Long CATEGORY_ID = testData.categories.getFirstId();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            Map<String, Object> hints = new HashMap<String, Object>();
            hints.put("javax.persistence.lock.timeout", 5000);

            // Executes a SELECT .. FOR UPDATE WAIT 5000 if supported by dialect
            Category category =
                em.find(
                    Category.class,
                    CATEGORY_ID,
                    LockModeType.PESSIMISTIC_WRITE,
                    hints
                );

            category.setName("New Name");

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

    // TODO: This test fails because nullable outer joined tuples can't be locked on Postgres
    @Test(groups = {"H2", "MYSQL", "ORACLE"})
    public void extendedLock() throws Exception {
        final ConcurrencyTestData testData = storeCategoriesAndItems();
        Long ITEM_ID = testData.items.getFirstId();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            Map<String, Object> hints = new HashMap<String, Object>();
            hints.put("javax.persistence.lock.scope", PessimisticLockScope.EXTENDED);

            Item item =
                em.find(
                    Item.class,
                    ITEM_ID,
                    LockModeType.PESSIMISTIC_READ,
                    hints
                );

            // TODO This query loading the images should lock the images rows, it doesn't.
            assertEquals(item.getImages().size(), 0);

            item.setName("New Name");

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }
}
