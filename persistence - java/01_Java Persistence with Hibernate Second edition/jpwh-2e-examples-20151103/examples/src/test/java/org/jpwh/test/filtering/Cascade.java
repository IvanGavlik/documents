package org.jpwh.test.filtering;

import org.hibernate.ReplicationMode;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.jpwh.env.DatabaseProduct;
import org.jpwh.env.JPATest;
import org.jpwh.model.filtering.cascade.BankAccount;
import org.jpwh.model.filtering.cascade.Bid;
import org.jpwh.model.filtering.cascade.BillingDetails;
import org.jpwh.model.filtering.cascade.CreditCard;
import org.jpwh.model.filtering.cascade.Item;
import org.jpwh.model.filtering.cascade.User;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.LockTimeoutException;
import javax.persistence.PersistenceException;
import javax.persistence.PessimisticLockException;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static org.testng.Assert.*;
import static org.testng.Assert.assertTrue;

public class Cascade extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("FilteringCascadePU");
    }

    @Test
    public void detachMerge() throws Throwable {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            Long ITEM_ID;
            {
                User user = new User("johndoe");
                em.persist(user);

                Item item = new Item("Some Item", user);
                em.persist(item);
                ITEM_ID = item.getId();

                Bid firstBid = new Bid(new BigDecimal("99.00"), item);
                item.getBids().add(firstBid);
                em.persist(firstBid);

                Bid secondBid = new Bid(new BigDecimal("100.00"), item);
                item.getBids().add(secondBid);
                em.persist(secondBid);

                em.flush();
            }
            em.clear();

            Item item = em.find(Item.class, ITEM_ID);
            assertEquals(item.getBids().size(), 2); // Initializes bids
            em.detach(item);

            em.clear();

            item.setName("New Name");

            Bid bid = new Bid(new BigDecimal("101.00"), item);
            item.getBids().add(bid);

            /* 
               Hibernate merges the detached <code>item</code>: First, it checks if the
               persistence context already contains an <code>Item</code> with the given
               identifier value. In this case, there isn't any, so the <code>Item</code>
               is loaded from the database. Hibernate is smart enough to know that
               it will also need the <code>bids</code> during merging, so it fetches them
               right away in the same SQL query. Hibernate then copies the detached <code>item</code>
               values onto the loaded instance, which it returns to you in persistent state.
               The same procedure is applied to every <code>Bid</code>, and Hibernate
               will detect that one of the <code>bids</code> is new.
             */
            Item mergedItem = em.merge(item);
            // select i.*, b.*
            //  from ITEM i
            //    left outer join BID b on i.ID = b.ITEM_ID
            //  where i.ID = ?

            /* 
               Hibernate made the new <code>Bid</code> persistent during merging, it
               now has an identifier value assigned.
             */
            for (Bid b : mergedItem.getBids()) {
                assertNotNull(b.getId());
            }

            /* 
               When you flush the persistence context, Hibernate detects that the
               <code>name</code> of the <code>Item</code> changed during merging.
               The new <code>Bid</code> will also be stored.
             */
            em.flush();
            // update ITEM set NAME = ? where ID = ?
            // insert into BID values (?, ?, ?, ...)

            em.clear();

            item = em.find(Item.class, ITEM_ID);
            assertEquals(item.getName(), "New Name");
            assertEquals(item.getBids().size(), 3);

            tx.commit();
            em.close();

        } finally {
            TM.rollback();
        }
    }

    @Test
    public void refresh() throws Throwable {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            Long USER_ID;
            Long CREDIT_CARD_ID = null;
            {

                User user = new User("johndoe");
                user.getBillingDetails().add(
                    new CreditCard("John Doe", "1234567890", "11", "2020")
                );
                user.getBillingDetails().add(
                    new BankAccount("John Doe", "45678", "Some Bank", "1234")
                );
                em.persist(user);
                em.flush();

                USER_ID = user.getId();
                for (BillingDetails bd : user.getBillingDetails()) {
                    if (bd instanceof CreditCard)
                        CREDIT_CARD_ID = bd.getId();
                }
                assertNotNull(CREDIT_CARD_ID);
            }
            tx.commit();
            em.close();
            // Locks from INSERTs must be released, commit and start a new unit of work

            tx.begin();
            em = JPA.createEntityManager();

            /* 
               An instance of <code>User</code> is loaded from the database.
             */
            User user = em.find(User.class, USER_ID);

            /* 
               Its lazy <code>billingDetails</code> collection is initialized when
               you iterate through the elements or when you call <code>size()</code>.
             */
            assertEquals(user.getBillingDetails().size(), 2);
            for (BillingDetails bd : user.getBillingDetails()) {
                assertEquals(bd.getOwner(), "John Doe");
            }

            // Someone modifies the billing information in the database!
            final Long SOME_USER_ID = USER_ID;
            final Long SOME_CREDIT_CARD_ID = CREDIT_CARD_ID;
            // In a separate transaction, so no locks are held in the database on the
            // updated/deleted rows and we can SELECT them again in the original transaction
            Executors.newSingleThreadExecutor().submit(new Callable<Object>() {
                @Override
                public Object call() throws Exception {

                    UserTransaction tx = TM.getUserTransaction();
                    try {
                        tx.begin();
                        EntityManager em = JPA.createEntityManager();

                        em.unwrap(Session.class).doWork(new Work() {
                            @Override
                            public void execute(Connection con) throws SQLException {
                                PreparedStatement ps;

                                /* Delete the credit card, this will cause the refresh to
                                   fail with EntityNotFoundException!
                                ps = con.prepareStatement(
                                    "delete from CREDITCARD where ID = ?"
                                );
                                ps.setLong(1, SOME_CREDIT_CARD_ID);
                                ps.executeUpdate();
                                ps = con.prepareStatement(
                                    "delete from BILLINGDETAILS where ID = ?"
                                );
                                ps.setLong(1, SOME_CREDIT_CARD_ID);
                                ps.executeUpdate();
                                */

                                // Update the bank account
                                ps = con.prepareStatement(
                                    "update BILLINGDETAILS set OWNER = ? where USER_ID = ?"
                                );
                                ps.setString(1, "Doe John");
                                ps.setLong(2, SOME_USER_ID);
                                ps.executeUpdate();
                            }
                        });

                        tx.commit();
                        em.close();
                    } catch (Exception ex) {
                        // This should NOT fail
                        TM.rollback();
                    }
                    return null;
                }
            }).get();


            /* 
               When you <code>refresh()</code> the managed <code>User</code> instance,
               Hibernate cascades the operation to the managed <code>BillingDetails</code>
               and refreshes each with a SQL <code>SELECT</code>. If one of these instances
               is no longer in the database, Hibernate throws an <code>EntityNotFoundException</code>.
               Then, Hibernate refreshes the <code>User</code> instance and eagerly
               loads the whole <code>billingDetails</code> collection to discover any
               new <code>BillingDetails</code>.
             */
            em.refresh(user);
            // select * from CREDITCARD join BILLINGDETAILS where ID = ?
            // select * from BANKACCOUNT join BILLINGDETAILS where ID = ?
            // select * from USERS
            //  left outer join BILLINGDETAILS
            //  left outer join CREDITCARD
            //  left outer JOIN BANKACCOUNT
            // where ID = ?

            for (BillingDetails bd : user.getBillingDetails()) {
                assertEquals(bd.getOwner(), "Doe John");
            }

            tx.commit();
            em.close();

        } finally {
            TM.rollback();
        }
    }

    @Test
    public void replicate() throws Throwable {
        UserTransaction tx = TM.getUserTransaction();
        try {
            Long ITEM_ID;
            Long USER_ID;

            {
                tx.begin();
                EntityManager em = JPA.createEntityManager();

                User user = new User("johndoe");
                em.persist(user);
                USER_ID = user.getId();

                Item item = new Item("Some Item", user);
                em.persist(item);
                ITEM_ID = item.getId();

                tx.commit();
                em.close();
            }

            tx.begin();
            EntityManager em = JPA.createEntityManager();

            Item item = em.find(Item.class, ITEM_ID);

            // Initialize the lazy Item#seller
            assertNotNull(item.getSeller().getUsername());

            tx.commit();
            em.close();

            tx.begin();
            EntityManager otherDatabase = // ... get EntityManager
                JPA.createEntityManager();

            otherDatabase.unwrap(Session.class)
                .replicate(item, ReplicationMode.OVERWRITE);
            // select ID from ITEM where ID = ?
            // select ID from USERS where ID = ?

            tx.commit();
            // update ITEM set NAME = ?, SELLER_ID = ?, ... where ID = ?
            // update USERS set USERNAME = ?, ... where ID = ?
            otherDatabase.close();

        } finally {
            TM.rollback();
        }
    }

}
