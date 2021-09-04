package org.jpwh.test.concurrency;

import org.jpwh.env.JPATest;
import org.jpwh.model.concurrency.version.Item;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class NonTransactional extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("ConcurrencyVersioningPU");
    }

    // TODO: Broken on MySQL https://hibernate.atlassian.net/browse/HHH-8402
    @Test(groups = {"H2", "ORACLE", "POSTGRESQL"})
    public void autoCommit() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        Long ITEM_ID;
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();
            Item someItem = new Item("Original Name");
            em.persist(someItem);
            tx.commit();
            em.close();
            ITEM_ID = someItem.getId();
        } finally {
            TM.rollback();
        }

        {
            /* 
               No transaction is active when we create the <code>EntityManager</code>. The
               persistence context is now in a special <em>unsynchronized</em> mode, Hibernate
               will not flush automatically at any time.
             */
            EntityManager em = JPA.createEntityManager();

            /* 
               You can access the database to read data; this operation will execute a
               <code>SELECT</code> statement, sent to the database in auto-commit mode.
             */
            Item item = em.find(Item.class, ITEM_ID);
            item.setName("New Name");

            /* 
               Usually Hibernate would flush the persistence context when you execute a
               <code>Query</code>. However, because the context is <em>unsynchronized</em>,
               flushing will not occur and the query will return the old, original database
               value. Queries with scalar results are not repeatable, you'll see whatever
               values are present in the database and given to Hibernate in the
               <code>ResultSet</code>. Note that this isn't a repeatable read either if
               you are in <em>synchronized</em> mode.
             */
            assertEquals(
                em.createQuery("select i.name from Item i where i.id = :id)")
                    .setParameter("id", ITEM_ID).getSingleResult(),
                "Original Name"
            );

            /* 
               Retrieving a managed entity instance involves a lookup, during JDBC
               result set marshaling, in the current persistence context. The
               already loaded <code>Item</code> instance with the changed name will
               be returned from the persistence context, values from the database
               will be ignored. This is a repeatable read of an entity instance,
               even without a system transaction.
             */
            assertEquals(
                ((Item) em.createQuery("select i from Item i where i.id = :id)")
                    .setParameter("id", ITEM_ID).getSingleResult()).getName(),
                "New Name"
            );

            /* 
               If you try to flush the persistence context manually, to store the new
               <code>Item#name</code>, Hibernate will throw a
               <code>javax.persistence.TransactionRequiredException</code>. You are
               prevented from executing an <code>UPDATE</code> statement in
               <em>unsynchronized</em> mode, as you wouldn't be able to roll back the change.
            */
            // em.flush();

            /* 
               You can roll back the change you made with the <code>refresh()</code>
               method, it loads the current <code>Item</code> state from the database
               and overwrites the change you have made in memory.
             */
            em.refresh(item);
            assertEquals(item.getName(), "Original Name");

            em.close();
        }

        {
            EntityManager em = JPA.createEntityManager();

            Item newItem = new Item("New Item");
            /* 
               You can call <code>persist()</code> to save a transient entity instance with an
               unsynchronized persistence context. Hibernate will only fetch a new identifier
               value, typically by calling a database sequence, and assign it to the instance.
               The instance is now in persistent state in the context but the SQL
               <code>INSERT</code> hasn't happened. Note that this is only possible with
               <em>pre-insert</em> identifier generators; see <a href="#GeneratorStrategies"/>.
            */
            em.persist(newItem);
            assertNotNull(newItem.getId());

            /* 
               When you are ready to store the changes, join the persistence context with
               a transaction. Synchronization and flushing will occur as usual, when the
               transaction commits. Hibernate writes all queued operations to the database.
             */
            tx.begin();
            if (!em.isJoinedToTransaction())
                em.joinTransaction();
            tx.commit(); // Flush!
            em.close();
        }

        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();
            assertEquals(em.find(Item.class, ITEM_ID).getName(), "Original Name");
            assertEquals(em.createQuery("select count(i) from Item i)").getSingleResult(), 2l);
            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }

        {
            EntityManager tmp = JPA.createEntityManager();
            Item detachedItem = tmp.find(Item.class, ITEM_ID);
            tmp.close();

            detachedItem.setName("New Name");
            EntityManager em = JPA.createEntityManager();

            Item mergedItem = em.merge(detachedItem);

            tx.begin();
            em.joinTransaction();
            tx.commit(); // Flush!
            em.close();
        }

        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();
            assertEquals(em.find(Item.class, ITEM_ID).getName(), "New Name");
            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }

        {
            EntityManager em = JPA.createEntityManager();

            Item item = em.find(Item.class, ITEM_ID);
            em.remove(item);

            tx.begin();
            em.joinTransaction();
            tx.commit(); // Flush!
            em.close();
        }

        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();
            assertEquals(em.createQuery("select count(i) from Item i)").getSingleResult(), 1l);
            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

}
