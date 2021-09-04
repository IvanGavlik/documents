package org.jpwh.test.associations;


import org.jpwh.env.JPATest;
import org.jpwh.model.associations.maps.mapkey.Bid;
import org.jpwh.model.associations.maps.mapkey.Item;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;
import java.util.Map;

import static org.testng.Assert.assertEquals;

public class MapsMapKey extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("MapsMapKeyPU");
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
            em.persist(someBid);
            someItem.getBids().put(someBid.getId(), someBid); // Optional...

            Bid secondBid = new Bid(new BigDecimal("456.00"), someItem);
            em.persist(secondBid);
            someItem.getBids().put(secondBid.getId(), secondBid); // Optional...

            tx.commit();
            em.close();

            Long ITEM_ID = someItem.getId();

            tx.begin();
            em = JPA.createEntityManager();

            Item item = em.find(Item.class, ITEM_ID);
            assertEquals(item.getBids().size(), 2);

            for (Map.Entry<Long, Bid> entry : item.getBids().entrySet()) {
                // The key is the identifier of each Bid
                assertEquals(entry.getKey(), entry.getValue().getId());
            }

            tx.commit();
            em.close();

        } finally {
            TM.rollback();
        }
    }

}