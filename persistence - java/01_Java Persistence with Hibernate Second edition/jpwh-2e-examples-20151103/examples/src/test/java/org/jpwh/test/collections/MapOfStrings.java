package org.jpwh.test.collections;

import org.jpwh.env.JPATest;
import org.jpwh.model.collections.mapofstrings.Item;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import static org.testng.Assert.assertEquals;

public class MapOfStrings extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("MapOfStringsPU");
    }

    @Test
    public void storeLoadCollection() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();
            Item someItem = new Item();

            someItem.getImages().put("foo.jpg", "Foo");
            someItem.getImages().put("bar.jpg", "Bar");
            someItem.getImages().put("baz.jpg", "WRONG!");
            someItem.getImages().put("baz.jpg", "Baz");

            em.persist(someItem);
            tx.commit();
            em.close();
            Long ITEM_ID = someItem.getId();

            tx.begin();
            em = JPA.createEntityManager();
            Item item = em.find(Item.class, ITEM_ID);
            assertEquals(item.getImages().size(), 3);
            assertEquals(item.getImages().get("foo.jpg"), "Foo");
            assertEquals(item.getImages().get("bar.jpg"), "Bar");
            assertEquals(item.getImages().get("baz.jpg"), "Baz");
            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

}
