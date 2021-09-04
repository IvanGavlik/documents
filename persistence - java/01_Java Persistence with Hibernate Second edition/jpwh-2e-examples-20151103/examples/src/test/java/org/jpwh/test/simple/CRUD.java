package org.jpwh.test.simple;

import org.jpwh.env.JPATest;
import org.jpwh.model.simple.Item;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.UserTransaction;
import java.util.Date;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class CRUD extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("SimplePU");
    }

    @Test
    public void storeAndQueryItems() throws Exception {
        storeAndQueryItems("findItems");
    }

    public void storeAndQueryItems(String queryName) throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            Item itemOne = new Item();
            itemOne.setName("Item One");
            itemOne.setAuctionEnd(new Date(System.currentTimeMillis() + 100000));
            em.persist(itemOne);

            Item itemTwo = new Item();
            itemTwo.setName("Item Two");
            itemTwo.setAuctionEnd(new Date(System.currentTimeMillis() + 100000));

            em.persist(itemTwo);

            tx.commit();
            em.close();

            tx.begin();
            em = JPA.createEntityManager();

            Query q = em.createNamedQuery(queryName);
            List<Item> items = q.getResultList();

            assertEquals(items.size(), 2);

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

}
