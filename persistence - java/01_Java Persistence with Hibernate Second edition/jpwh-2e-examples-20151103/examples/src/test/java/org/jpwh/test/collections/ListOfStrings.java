package org.jpwh.test.collections;

import org.jpwh.env.JPATest;
import org.jpwh.model.collections.listofstrings.Item;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import static org.testng.Assert.assertEquals;

public class ListOfStrings extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("ListOfStringsPU");
    }

    @Test
    public void storeLoadCollection() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();
            Item someItem = new Item();

            someItem.getImages().add("foo.jpg");
            someItem.getImages().add("bar.jpg");
            someItem.getImages().add("baz.jpg");
            someItem.getImages().add("baz.jpg");

            em.persist(someItem);
            tx.commit();
            em.close();
            Long ITEM_ID = someItem.getId();

            tx.begin();
            em = JPA.createEntityManager();
            Item item = em.find(Item.class, ITEM_ID);
            assertEquals(item.getImages().size(), 4);
            assertEquals(item.getImages().get(0), "foo.jpg");
            assertEquals(item.getImages().get(1), "bar.jpg");
            assertEquals(item.getImages().get(2), "baz.jpg");
            assertEquals(item.getImages().get(3), "baz.jpg");
            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

}
