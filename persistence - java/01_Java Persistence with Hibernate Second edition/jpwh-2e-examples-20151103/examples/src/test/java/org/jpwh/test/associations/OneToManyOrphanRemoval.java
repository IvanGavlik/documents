package org.jpwh.test.associations;


import org.jpwh.env.JPATest;
import org.jpwh.model.associations.onetomany.orphanremoval.Bid;
import org.jpwh.model.associations.onetomany.orphanremoval.Item;
import org.jpwh.model.associations.onetomany.orphanremoval.User;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;

import static org.testng.Assert.assertEquals;

public class OneToManyOrphanRemoval extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("OneToManyOrphanRemovalPU");
    }

    @Test
    public void storeAndLoadItemBids() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            User johndoe = new User();
            em.persist(johndoe);

            Item anItem = new Item("Some Item");
            Bid bidA = new Bid(new BigDecimal("123.00"), anItem);
            bidA.setBidder(johndoe);
            anItem.getBids().add(bidA);

            Bid bidB = new Bid(new BigDecimal("456.00"), anItem);
            anItem.getBids().add(bidB);
            bidB.setBidder(johndoe);

            em.persist(anItem);

            tx.commit();
            em.close();
            Long ITEM_ID = anItem.getId();
            Long USER_ID = johndoe.getId();

            tx.begin();
            em = JPA.createEntityManager();

            User user = em.find(User.class, USER_ID);
            assertEquals(user.getBids().size(), 2); // User made two bids...

            Item item = em.find(Item.class, ITEM_ID);
            Bid firstBid = item.getBids().iterator().next();
            item.getBids().remove(firstBid); // One bid is removed

            // FAILURE!
            // assertEquals(user.getBids().size(), 1);
            assertEquals(user.getBids().size(), 2); // Still two!

            tx.commit();
            em.close();

            tx.begin();
            em = JPA.createEntityManager();

            item = em.find(Item.class, ITEM_ID);
            assertEquals(item.getBids().size(), 1); // One of the bids is gone

            tx.commit();
            em.close();

        } finally {
            TM.rollback();
        }
    }

}