package org.jpwh.test.advanced;

import org.jpwh.env.JPATest;
import org.jpwh.model.advanced.Item;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.testng.Assert.*;

public class Temporal extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("AdvancedPU");
    }

    @Test
    public void storeLoadTemporal() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();
            Item someItem = new Item();
            someItem.setName("Some item");
            someItem.setDescription("This is some description.");
            // someItem.setReviewedOn(Instant.now().plusSeconds(60)); // A future time
            em.persist(someItem);
            tx.commit();
            em.close();

            Long ITEM_ID = someItem.getId();
            Date ORIGINAL_CREATION_DATE = someItem.getCreatedOn();

            tx.begin();
            em = JPA.createEntityManager();

            Item item = em.find(Item.class, ITEM_ID);

            // java.util.Date and java.sql.Timestamp are not symmetric!
            assertFalse(item.getCreatedOn().equals(ORIGINAL_CREATION_DATE));
            assertFalse(item.getCreatedOn().getClass().equals(ORIGINAL_CREATION_DATE.getClass()));

            // This is how you properly compare time values in Java...
            assertEquals(ORIGINAL_CREATION_DATE.getTime(), item.getCreatedOn().getTime());

            // Or use the slightly less annoying but quite awful Calendar API
            Calendar oldDate = new GregorianCalendar();
            oldDate.setTime(ORIGINAL_CREATION_DATE);
            Calendar newDate = new GregorianCalendar();
            newDate.setTime(item.getCreatedOn());
            assertEquals(oldDate, newDate);

            // Or the Java 8 API
            // assertTrue(item.getReviewedOn().isAfter(item.getCreatedOn().toInstant()));

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

}
