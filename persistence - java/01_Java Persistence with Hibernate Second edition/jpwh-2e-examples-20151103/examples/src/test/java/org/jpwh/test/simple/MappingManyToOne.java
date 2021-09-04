package org.jpwh.test.simple;

import org.jpwh.env.JPATest;
import org.jpwh.model.simple.Bid;
import org.jpwh.model.simple.Item;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;

import static org.testng.Assert.assertEquals;

public class MappingManyToOne extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("SimplePU");
    }

    @Test
    public void storeAndLoadBids() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            // Store in one persistence context (transaction)
            Item anItem = new Item();
            anItem.setName("Example Item");

            Bid firstBid = new Bid(new BigDecimal("123.00"), anItem);

            Bid secondBid = new Bid(new BigDecimal("456.00"), anItem);

            // Order is important here, Hibernate isn't smart enough anymore!
            em.persist(anItem);
            em.persist(firstBid);
            em.persist(secondBid);

            tx.commit();
            em.close();

            tx.begin();
            em = JPA.createEntityManager();

            Long BID_ID = firstBid.getId();

            // Load in another persistence context
            Bid someBid = em.find(Bid.class, BID_ID); // SQL SELECT

            // Initializes the Item proxy because we call getId(), which is
            // not mapped as an identifier property (the field is!)
            assertEquals(someBid.getItem().getId(), anItem.getId()); // SQL SELECT

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

}
