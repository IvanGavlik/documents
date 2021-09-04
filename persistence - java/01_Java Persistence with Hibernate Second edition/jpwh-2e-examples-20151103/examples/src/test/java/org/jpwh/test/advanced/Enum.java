package org.jpwh.test.advanced;

import org.jpwh.env.JPATest;
import org.jpwh.model.advanced.AuctionType;
import org.jpwh.model.advanced.Item;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import static org.testng.Assert.assertEquals;

public class Enum extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("AdvancedPU");
    }

    @Test
    public void storeLoadEnum() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();
            Item someItem = new Item();
            someItem.setName("Some item");
            someItem.setDescription("This is some description.");
            someItem.setAuctionType(AuctionType.LOWEST_BID);
            em.persist(someItem);
            tx.commit();
            em.close();
            Long ITEM_ID = someItem.getId();

            tx.begin();
            em = JPA.createEntityManager();
            Item item = em.find(Item.class, ITEM_ID);
            assertEquals(item.getAuctionType(), AuctionType.LOWEST_BID);
            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

}
