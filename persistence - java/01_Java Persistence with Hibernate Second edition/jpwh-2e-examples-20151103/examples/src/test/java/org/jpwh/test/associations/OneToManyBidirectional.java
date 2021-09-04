package org.jpwh.test.associations;


import org.jpwh.env.JPATest;
import org.jpwh.model.associations.onetomany.bidirectional.Bid;
import org.jpwh.model.associations.onetomany.bidirectional.Item;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;
import java.util.Collection;

import static org.testng.Assert.assertEquals;

public class OneToManyBidirectional extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("OneToManyBidirectionalPU");
    }

    @Test
    public void storeAndLoadItemBids() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            Item someItem = new Item("Some Item");
            em.persist(someItem);

            Bid someBid = new Bid(new BigDecimal("123.00"), someItem);
            someItem.getBids().add(someBid); // Don't forget!
            em.persist(someBid);

            Bid secondBid = new Bid(new BigDecimal("456.00"), someItem);
            someItem.getBids().add(secondBid);
            em.persist(secondBid);

            tx.commit(); // Dirty checking, SQL execution
            em.close();

            Long ITEM_ID = someItem.getId();

            tx.begin();
            em = JPA.createEntityManager();

            Item item = em.find(Item.class, ITEM_ID); // First SELECT to load ITEM row
            assertEquals(item.getBids().size(), 2); // The size() method triggers second SELECT

            tx.commit();
            em.close();

            // Repeat the same with an explicit query, no collection mapping needed!
            tx.begin();
            em = JPA.createEntityManager();

            Collection<Bid> bids =
                    em.createQuery("select b from Bid b where b.item.id = :itemId")
                    .setParameter("itemId", ITEM_ID)
                    .getResultList();
            assertEquals(bids.size(), 2);

            tx.commit();
            em.close();

        } finally {
            TM.rollback();
        }
    }

}