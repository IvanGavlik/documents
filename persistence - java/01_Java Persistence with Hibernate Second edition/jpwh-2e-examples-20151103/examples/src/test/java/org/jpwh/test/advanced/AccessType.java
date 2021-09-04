package org.jpwh.test.advanced;

import org.jpwh.env.JPATest;
import org.jpwh.model.advanced.Item;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import static org.testng.Assert.assertEquals;

public class AccessType extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("AdvancedPU");
    }

    @Test
    public void storeLoadAccessType() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();
            Item someItem = new Item();
            someItem.setName("Some item");
            someItem.setDescription("This is some description.");
            em.persist(someItem);
            tx.commit();
            em.close();

            Long ITEM_ID = someItem.getId();

            tx.begin();
            em = JPA.createEntityManager();

            Item item = em.find(Item.class, ITEM_ID);
            assertEquals(item.getName(), "AUCTION: Some item");
            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

}
