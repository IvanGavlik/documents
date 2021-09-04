package org.jpwh.test.associations;

import org.jpwh.env.JPATest;
import org.jpwh.model.associations.onetomany.bag.Bid;
import org.jpwh.model.associations.onetomany.bag.Item;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;

import static org.testng.Assert.assertEquals;

public class OneToManyBag extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("OneToManyBagPU");
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
            someItem.getBids().add(someBid); // No persistent effect!
            em.persist(someBid);

            assertEquals(someItem.getBids().size(), 2);

            tx.commit();
            em.close();

            Long ITEM_ID = someItem.getId();

            tx.begin();
            em = JPA.createEntityManager();
            {
                Item item = em.find(Item.class, ITEM_ID);
                assertEquals(item.getBids().size(), 1);
            }
            tx.commit();
            em.close();

            tx.begin();
            em = JPA.createEntityManager();
            {
                Item item = em.find(Item.class, ITEM_ID);

                Bid bid = new Bid(new BigDecimal("456.00"), item);
                item.getBids().add(bid); // No SELECT!
                em.persist(bid);
            }
            tx.commit();
            em.close();

        } finally {
            TM.rollback();
        }
    }

}