package org.jpwh.test.simple;

import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.jpwh.env.JPATest;
import org.jpwh.model.simple.Address;
import org.jpwh.model.simple.Item;
import org.jpwh.model.simple.User;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FlushModeType;
import javax.persistence.PersistenceUnitUtil;
import javax.transaction.Status;
import javax.transaction.UserTransaction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static org.testng.Assert.*;
import static org.testng.Assert.assertEquals;

public class SimpleTransitions extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("SimplePU");
    }

    @Test
    public void basicUOW() {
        EntityManager em = null;
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            em = JPA.createEntityManager(); // Application-managed

            Item someItem = new Item();
            someItem.setName("Some Item");
            em.persist(someItem);

            tx.commit(); // Synchronize/flush persistence context
        } catch (Exception ex) {
            // Transaction rollback, exception handling
            try {
                if (tx.getStatus() == Status.STATUS_ACTIVE
                    || tx.getStatus() == Status.STATUS_MARKED_ROLLBACK)
                    tx.rollback();
            } catch (Exception rbEx) {
                System.err.println("Rollback of transaction failed, trace follows!");
                rbEx.printStackTrace(System.err);
            }
            throw new RuntimeException(ex);
        } finally {
            if (em != null && em.isOpen())
                em.close(); // You create it, you close it!
        }
    }

    @Test
    public void makePersistent() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            EntityManager em;

            tx.begin();
            em = JPA.createEntityManager();
            Item item = new Item();
            item.setName("Some Item"); // Item#name is NOT NULL!

            em.persist(item);

            Long ITEM_ID = item.getId(); // Has been assigned

            tx.commit();
            em.close();

            tx.begin();
            em = JPA.createEntityManager();
            assertEquals(em.find(Item.class, ITEM_ID).getName(), "Some Item");
            tx.commit();
            em.close();

        } finally {
            TM.rollback();
        }
    }

    @Test
    public void retrievePersistent() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();
            Item someItem = new Item();
            someItem.setName("Some Item");
            em.persist(someItem);
            tx.commit();
            em.close();
            long ITEM_ID = someItem.getId();

            {
                tx.begin();
                em = JPA.createEntityManager();

                // Hit the database if not already in persistence context
                Item item = em.find(Item.class, ITEM_ID);

                if (item != null)
                    item.setName("New Name"); // Modify

                tx.commit(); // Flush: Dirty check and SQL UPDATE
                em.close();
            }

            {
                tx.begin();
                em = JPA.createEntityManager();

                Item itemA = em.find(Item.class, ITEM_ID);
                Item itemB = em.find(Item.class, ITEM_ID); // Repeatable read

                assertTrue(itemA == itemB);
                assertTrue(itemA.equals(itemB));
                assertTrue(itemA.getId().equals(itemB.getId()));

                tx.commit(); // Flush: Dirty check and SQL UPDATE
                em.close();
            }

            tx.begin();
            em = JPA.createEntityManager();
            assertEquals(em.find(Item.class, ITEM_ID).getName(), "New Name");
            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

    @Test(expectedExceptions = org.hibernate.LazyInitializationException.class)
    public void retrievePersistentReference() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();
            Item someItem = new Item();
            someItem.setName("Some Item");
            em.persist(someItem);
            tx.commit();
            em.close();
            long ITEM_ID = someItem.getId();

            tx.begin();
            em = JPA.createEntityManager();

            /* 
               If the persistence context already contains an <code>Item</code> with the given identifier, that
               <code>Item</code> instance is returned by <code>getReference()</code> without hitting the database.
               Furthermore, if <em>no</em> persistent instance with that identifier is currently managed, a hollow
               placeholder will be produced by Hibernate, a proxy. This means <code>getReference()</code> will not
               access the database, and it doesn't return <code>null</code>, unlike <code>find()</code>.
             */
            Item item = em.getReference(Item.class, ITEM_ID);

            /* 
               JPA offers <code>PersistenceUnitUtil</code> helper methods such as <code>isLoaded()</code> to
               detect if you are working with an uninitialized proxy.
            */
            PersistenceUnitUtil persistenceUtil =
                JPA.getEntityManagerFactory().getPersistenceUnitUtil();
            assertFalse(persistenceUtil.isLoaded(item));

            /* 
               As soon as you call any method such as <code>Item#getName()</code> on the proxy, a
               <code>SELECT</code> is executed to fully initialize the placeholder. The exception to this rule is
               a method that is a mapped database identifier getter method, such as <code>getId()</code>. A proxy
               might look like the real thing but it is only a placeholder carrying the identifier value of the
               entity instance it represents. If the database record doesn't exist anymore when the proxy is
               initialized, an <code>EntityNotFoundException</code> will be thrown.
             */
            // assertEquals(item.getName(), "Some Item");
            /* 
               Hibernate has a convenient static <code>initialize()</code> method, loading the proxy's data.
             */
            // Hibernate.initialize(item);

            tx.commit();
            em.close();

            /* 
               After the persistence context is closed, <code>item</code> is in detached state. If you do
               not initialize the proxy while the persistence context is still open, you get a
               <code>LazyInitializationException</code> if you access the proxy. You can't load
               data on-demand once the persistence context is closed. The solution is simple: Load the
               data before you close the persistence context.
             */
            assertEquals(item.getName(), "Some Item");
        } finally {
            TM.rollback();
        }
    }

    @Test
    public void makeTransient() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();
            Item someItem = new Item();
            someItem.setName("Some Item");
            em.persist(someItem);
            tx.commit();
            em.close();
            long ITEM_ID = someItem.getId();

            tx.begin();
            em = JPA.createEntityManager();
            /* 
               If you call <code>find()</code>, Hibernate will execute a <code>SELECT</code> to
               load the <code>Item</code>. If you call <code>getReference()</code>, Hibernate
               will attempt to avoid the <code>SELECT</code> and return a proxy.
             */
            Item item = em.find(Item.class, ITEM_ID);
            //Item item = em.getReference(Item.class, ITEM_ID);

            /* 
               Calling <code>remove()</code> will queue the entity instance for deletion when
               the unit of work completes, it is now in <em>removed</em> state. If <code>remove()</code>
               is called on a proxy, Hibernate will execute a <code>SELECT</code> to load the data.
               An entity instance has to be fully initialized during life cycle transitions. You may
               have life cycle callback methods or an entity listener enabled
               (see <a href="#EventListenersInterceptors"/>), and the instance must pass through these
               interceptors to complete its full life cycle.
             */
            em.remove(item);

            /* 
                An entity in removed state is no longer in persistent state, this can be
                checked with the <code>contains()</code> operation.
             */
            assertFalse(em.contains(item));

            /* 
               You can make the removed instance persistent again, cancelling the deletion.
             */
            // em.persist(item);

            // hibernate.use_identifier_rollback was enabled, it now looks like a transient instance
            assertNull(item.getId());

            /* 
               When the transaction commits, Hibernate synchronizes the state transitions with the
               database and executes the SQL <code>DELETE</code>. The JVM garbage collector detects that the
               <code>item</code> is no longer referenced by anyone and finally deletes the last trace of
               the data.
             */
            tx.commit();
            em.close();

            tx.begin();
            em = JPA.createEntityManager();
            item = em.find(Item.class, ITEM_ID);
            assertNull(item);
            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

    @Test
    public void refresh() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();
            Item someItem = new Item();
            someItem.setName("Some Item");
            em.persist(someItem);
            tx.commit();
            em.close();
            final long ITEM_ID = someItem.getId();

            tx.begin();
            em = JPA.createEntityManager();

            Item item = em.find(Item.class, ITEM_ID);
            item.setName("Some Name");

            // Someone updates this row in the database!
            Executors.newSingleThreadExecutor().submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    UserTransaction tx = TM.getUserTransaction();
                    try {
                        tx.begin();
                        EntityManager em = JPA.createEntityManager();

                        Session session = em.unwrap(Session.class);
                        session.doWork(new Work() {
                            @Override
                            public void execute(Connection con) throws SQLException {
                                PreparedStatement ps = con.prepareStatement("update ITEM set name = ? where ID = ?");
                                ps.setString(1, "Concurrent Update Name");
                                ps.setLong(2, ITEM_ID);

                                /* Alternative: you get an EntityNotFoundException on refresh
                                PreparedStatement ps = con.prepareStatement("delete from ITEM where ID = ?");
                                ps.setLong(1, ITEM_ID);
                                */

                                if (ps.executeUpdate() != 1)
                                    throw new SQLException("ITEM row was not updated");
                            }
                        });

                        tx.commit();
                        em.close();

                    } catch (Exception ex) {
                        TM.rollback();
                        throw new RuntimeException("Concurrent operation failure: " + ex, ex);
                    }
                    return null;
                }
            }).get();

            String oldName = item.getName();
            em.refresh(item);
            assertNotEquals(item.getName(), oldName);
            assertEquals(item.getName(), "Concurrent Update Name");

            tx.commit(); // Flush: Dirty check and SQL UPDATE
            em.close();

        } finally {
            TM.rollback();
        }
    }

    @Test(groups = {"H2", "POSTGRESQL", "ORACLE"})
    public void replicate() throws Exception {

        Long ITEM_ID;
        try {
            UserTransaction tx = TM.getUserTransaction();
            tx.begin();
            EntityManager em = JPA.createEntityManager();
            Item someItem = new Item();
            someItem.setName("Some Item");
            em.persist(someItem);
            tx.commit();
            em.close();
            ITEM_ID = someItem.getId();
        } finally {
            TM.rollback();
        }

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();

            EntityManager emA = getDatabaseA().createEntityManager();
            Item item = emA.find(Item.class, ITEM_ID);

            EntityManager emB = getDatabaseB().createEntityManager();
            emB.unwrap(Session.class)
                .replicate(item, org.hibernate.ReplicationMode.LATEST_VERSION);

            tx.commit();
            emA.close();
            emB.close();
        } finally {
            TM.rollback();
        }
    }

    protected EntityManagerFactory getDatabaseA() {
        return JPA.getEntityManagerFactory();
    }

    protected EntityManagerFactory getDatabaseB() {
        // TODO: This fails as we can't enlist two non-XA connections in the same transaction
        // on MySQL. XA is broken in MySQL, so we have to use the Bitronix XA wrapper, it can
        // only handle one non-XA resource per transaction. See DatabaseProduct.java
        return JPA.getEntityManagerFactory();
    }

    @Test
    public void flushModeType() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        Long ITEM_ID;
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();
            Item someItem = new Item();
            someItem.setName("Original Name");
            em.persist(someItem);
            tx.commit();
            em.close();
            ITEM_ID = someItem.getId();
        } finally {
            TM.rollback();
        }

        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            Item item = em.find(Item.class, ITEM_ID);
            item.setName("New Name");

            // Disable flushing before queries:
            em.setFlushMode(FlushModeType.COMMIT);

            assertEquals(
                em.createQuery("select i.name from Item i where i.id = :id")
                    .setParameter("id", ITEM_ID).getSingleResult(),
                "Original Name"
            );

            tx.commit(); // Flush!
            em.close();
        } finally {
            TM.rollback();
        }
    }

    @Test
    public void scopeOfIdentity() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();
            Item someItem = new Item();
            someItem.setName("Some Item");
            em.persist(someItem);
            tx.commit();
            em.close();
            long ITEM_ID = someItem.getId();

            tx.begin();
            em = JPA.createEntityManager();

            Item a = em.find(Item.class, ITEM_ID);
            Item b = em.find(Item.class, ITEM_ID);
            assertTrue(a == b);
            assertTrue(a.equals(b));
            assertEquals(a.getId(), b.getId());

            tx.commit();
            em.close();
            // PC is gone, 'a' and 'b' are now references to instances in detached state!

            tx.begin();
            em = JPA.createEntityManager();

            Item c = em.find(Item.class, ITEM_ID);
            assertTrue(a != c); // The 'a' reference is still detached!
            assertFalse(a.equals(c));
            assertEquals(a.getId(), c.getId());

            tx.commit();
            em.close();

            Set<Item> allItems = new HashSet<>();
            allItems.add(a);
            allItems.add(b);
            allItems.add(c);
            assertEquals(allItems.size(), 2); // That seems wrong and arbitrary!

        } finally {
            TM.rollback();
        }
    }

    @Test
    public void detach() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            User someUser = new User();
            someUser.setUsername("johndoe");
            someUser.setHomeAddress(new Address("Some Street", "1234", "Some City"));
            em.persist(someUser);
            tx.commit();
            em.close();
            long USER_ID = someUser.getId();

            tx.begin();
            em = JPA.createEntityManager();

            User user = em.find(User.class, USER_ID);

            em.detach(user);

            assertFalse(em.contains(user));

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

    @Test
    public void mergeDetached() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            User detachedUser = new User();
            detachedUser.setUsername("foo");
            detachedUser.setHomeAddress(new Address("Some Street", "1234", "Some City"));
            em.persist(detachedUser);
            tx.commit();
            em.close();
            long USER_ID = detachedUser.getId();

            detachedUser.setUsername("johndoe");

            tx.begin();
            em = JPA.createEntityManager();

            User mergedUser = em.merge(detachedUser);
            // Discard 'detachedUser' reference after merging!

            // The 'mergedUser' is in persistent state
            mergedUser.setUsername("doejohn");

            tx.commit(); // UPDATE in database
            em.close();

            tx.begin();
            em = JPA.createEntityManager();
            User user = em.find(User.class, USER_ID);
            assertEquals(user.getUsername(), "doejohn");
            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

}
