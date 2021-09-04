package org.jpwh.test.advanced;

import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.jpwh.env.DatabaseProduct;
import org.jpwh.env.JPATest;
import org.jpwh.model.advanced.Bid;
import org.jpwh.model.advanced.Item;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import static org.testng.Assert.*;

public class GeneratedProperties extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("AdvancedPU");
    }

    @Override
    public void afterJPABootstrap() throws Exception {
        if (!TM.databaseProduct.equals(DatabaseProduct.H2)) return;
        // The LASTMODIFIED test shows how a database trigger generates property values, we actually need a trigger
        try (Session session = JPA.createEntityManager().unwrap(Session.class)) {
            session.doWork(
                new Work() {
                    @Override
                    public void execute(Connection connection) throws SQLException {
                        Statement stat = connection.createStatement();
                        stat.execute("drop trigger if exists TRG_ITEM_LASTMODIFIED_INSERT");
                        stat.execute("create trigger TRG_ITEM_LASTMODIFIED_INSERT after insert on ITEM" +
                            " for each row call \"" + org.jpwh.shared.trigger.UpdateLastModifiedTrigger.class.getName() + "\"");
                        stat.execute("drop trigger if exists TRG_ITEM_LASTMODIFIED_UPDATE");
                        stat.execute("create trigger TRG_ITEM_LASTMODIFIED_UPDATE after update on ITEM" +
                            " for each row call \"" + org.jpwh.shared.trigger.UpdateLastModifiedTrigger.class.getName() + "\"");
                        stat.close();
                    }
                }
            );
        }
    }

    @Test(groups = {"H2"})
    public void storeLoadLastModified() throws Exception {
        long ITEM_ID = storeItemAndBids();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            Item item = em.find(Item.class, ITEM_ID);

            assertNotNull(item.getCreatedOn());

            Date lastModified = item.getLastModified();
            assertNotNull(lastModified);

            assertTrue(item.getCreatedOn().getTime() < lastModified.getTime());

            item.setDescription("Some modification.");
            em.flush();

            Date newLastModified = item.getLastModified();
            assertNotEquals(lastModified, newLastModified, "Modification time should have been updated");

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

    @Test
    public void storeLoadInitialPrice() throws Exception {
        long ITEM_ID = storeItemAndBids();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            Item item = em.find(Item.class, ITEM_ID);

            tx.commit();
            em.close();

            assertEquals(item.getInitialPrice().compareTo(new BigDecimal("1")), 0);
        } finally {
            TM.rollback();
        }
    }

    public Long storeItemAndBids() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        tx.begin();
        EntityManager em = JPA.createEntityManager();
        Item item = new Item();
        item.setName("Some item");
        item.setDescription("This is some description.");
        em.persist(item);
        for (int i = 1; i <= 3; i++) {
            Bid bid = new Bid();
            bid.setAmount(new BigDecimal(10 + i));
            bid.setItem(item);
            em.persist(bid);
        }
        tx.commit();
        em.close();
        return item.getId();
    }

}
