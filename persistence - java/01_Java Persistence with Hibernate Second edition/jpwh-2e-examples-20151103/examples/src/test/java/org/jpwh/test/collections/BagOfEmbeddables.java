package org.jpwh.test.collections;

import org.jpwh.env.JPATest;
import org.jpwh.model.collections.bagofembeddables.Image;
import org.jpwh.model.collections.bagofembeddables.Item;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import static org.testng.Assert.assertEquals;

public class BagOfEmbeddables extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("BagOfEmbeddablesPU");
    }

    @Test
    public void storeLoadCollection() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            Item someItem = new Item();

            someItem.getImages().add(new Image(
                    "Foo", "foo.jpg", 640, 480
            ));
            someItem.getImages().add(new Image(
                    null, "bar.jpg", 800, 600 // Columns can be NULL now!
            ));
            someItem.getImages().add(new Image(
                    "Baz", "baz.jpg", 1024, 768
            ));
            someItem.getImages().add(new Image(
                    "Baz", "baz.jpg", 1024, 768
            )); // Duplicate allowed!

            em.persist(someItem);
            tx.commit();
            em.close();
            Long ITEM_ID = someItem.getId();

            tx.begin();
            em = JPA.createEntityManager();
            Item item = em.find(Item.class, ITEM_ID);
            assertEquals(item.getImages().size(), 4);
            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

}
