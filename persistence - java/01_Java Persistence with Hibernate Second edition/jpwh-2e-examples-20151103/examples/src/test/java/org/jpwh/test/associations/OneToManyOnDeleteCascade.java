package org.jpwh.test.associations;

import org.jpwh.env.JPATest;
import org.jpwh.model.associations.onetomany.ondeletecascade.Bid;
import org.jpwh.model.associations.onetomany.ondeletecascade.Item;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;
import java.util.Collection;

import static org.testng.Assert.assertEquals;

public class OneToManyOnDeleteCascade extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("OneToManyOnDeleteCascadePU");
    }

    @Test
    public void storeAndLoadItemBids() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            Item someItem = new Item("Some Item");
            em.persist(someItem); // Saves the bids automatically (later, at flush time)

            Bid someBid = new Bid(new BigDecimal("123.00"), someItem);
            em.persist(someBid);
            someItem.getBids().add(someBid);

            Bid secondBid = new Bid(new BigDecimal("456.00"), someItem);
            em.persist(secondBid);
            someItem.getBids().add(secondBid);

            tx.commit(); // Dirty checking, SQL execution
            em.close();

            Long ITEM_ID = someItem.getId();

            tx.begin();
            em = JPA.createEntityManager();
            Collection<Bid> bids =
               em.createQuery("select b from Bid b where b.item.id = :itemId")
                  .setParameter("itemId", ITEM_ID)
                  .getResultList();
            assertEquals(bids.size(), 2);
            tx.commit();
            em.close();

            tx.begin();
            em = JPA.createEntityManager();

            Item item = em.find(Item.class, ITEM_ID);
            em.remove(item); // The bids are deleted in the database, any Bid in memory might be outdated

            tx.commit();
            em.close();

            tx.begin();
            em = JPA.createEntityManager();
            bids = em.createQuery("select b from Bid b where b.item.id = :itemId")
               .setParameter("itemId", ITEM_ID)
               .getResultList();
            assertEquals(bids.size(), 0); // Bids are gone
            tx.commit();
            em.close();

        } finally {
            TM.rollback();
        }
    }

}