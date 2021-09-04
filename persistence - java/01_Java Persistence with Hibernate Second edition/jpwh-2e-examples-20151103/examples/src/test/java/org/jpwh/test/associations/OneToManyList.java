package org.jpwh.test.associations;


import org.jpwh.env.JPATest;
import org.jpwh.model.associations.onetomany.list.Bid;
import org.jpwh.model.associations.onetomany.list.Item;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class OneToManyList extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("OneToManyListPU");
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
            someItem.getBids().add(someBid);
            //someItem.getBids().add(someBid); // No persistent effect!
            em.persist(someBid);

            Bid secondBid = new Bid(new BigDecimal("456.00"), someItem);
            someItem.getBids().add(secondBid);
            em.persist(secondBid);

            assertEquals(someItem.getBids().size(), 2);

            tx.commit();
            em.close();

            Long ITEM_ID = someItem.getId();

            tx.begin();
            em = JPA.createEntityManager();

            Item item = em.find(Item.class, ITEM_ID);
            List<Bid> bids =  item.getBids();
            assertEquals(bids.size(), 2);
            assertEquals(bids.get(0).getAmount().compareTo(new BigDecimal("123")), 0);
            assertEquals(bids.get(1).getAmount().compareTo(new BigDecimal("456")), 0);

            tx.commit();
            em.close();

        } finally {
            TM.rollback();
        }
    }

}