package org.jpwh.test.collections;

import org.jpwh.env.JPATest;
import org.jpwh.model.collections.mapofembeddables.Image;
import org.jpwh.model.collections.mapofembeddables.Item;
import org.jpwh.model.collections.mapofembeddables.Filename;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import static org.testng.Assert.assertEquals;

public class MapOfEmbeddables extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("MapOfEmbeddablesPU");
    }

    @Test
    public void storeLoadCollection() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();
            Item someItem = new Item();

            someItem.getImages().put(
                new Filename("foo", "jpg"),
                new Image("Foo", 640, 480));
            someItem.getImages().put(
                new Filename("bar", "jpg"),
                new Image(null, 800, 600));
            someItem.getImages().put(
                new Filename("baz", "jpg"),
                new Image("Baz", 1024, 768));
            someItem.getImages().put(
                new Filename("baz", "jpg"),
                new Image("Baz", 1024, 768)); // Duplicate key filtered!

            em.persist(someItem);
            tx.commit();
            em.close();
            Long ITEM_ID = someItem.getId();

            tx.begin();
            em = JPA.createEntityManager();
            Item item = em.find(Item.class, ITEM_ID);
            assertEquals(item.getImages().size(), 3);
            assertEquals(item.getImages().get(new Filename("foo","jpg")).getTitle(), "Foo");
            assertEquals(item.getImages().get(new Filename("bar", "jpg")).getTitle(), null);
            assertEquals(item.getImages().get(new Filename("baz", "jpg")), new Image("Baz", 1024, 768));

            // Remove one
            item.getImages().remove(new Filename("foo","jpg"));
            tx.commit();
            em.close();

            tx.begin();
            em = JPA.createEntityManager();
            item = em.find(Item.class, ITEM_ID);
            // Should be one less
            assertEquals(item.getImages().size(), 2);
            tx.commit();
            em.close();

        } finally {
            TM.rollback();
        }
    }

}
