package org.jpwh.test.collections;

import org.jpwh.env.JPATest;
import org.jpwh.model.collections.mapofstringsembeddables.Image;
import org.jpwh.model.collections.mapofstringsembeddables.Item;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.UserTransaction;

import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class MapOfStringsEmbeddables extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("MapOfStringsEmbeddablesPU");
    }

    @Test
    public void storeLoadCollection() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();
            Item someItem = new Item();

            someItem.getImages().put("foo.jpg",
                    new Image("Foo", 640, 480));
            someItem.getImages().put("bar.jpg",
                    new Image(null, 800, 600));
            someItem.getImages().put("baz.jpg",
                    new Image("Baz", 1024, 768));
            someItem.getImages().put("baz.jpg",
                    new Image("Baz", 1024, 768)); // Duplicate key filtered!

            em.persist(someItem);
            tx.commit();
            em.close();
            Long ITEM_ID = someItem.getId();

            tx.begin();
            em = JPA.createEntityManager();
            Item item = em.find(Item.class, ITEM_ID);
            assertEquals(item.getImages().size(), 3);
            assertEquals(item.getImages().get("foo.jpg").getTitle(), "Foo");
            assertEquals(item.getImages().get("bar.jpg").getTitle(), null);
            assertEquals(item.getImages().get("baz.jpg"), new Image("Baz", 1024, 768));
            tx.commit();
            em.close();

            {
                tx.begin();
                em = JPA.createEntityManager();
                Query q = em.createQuery(
                    "select value(img)\n" +
                        "    from Item i join i.images img\n" +
                        "    where key(img) like '%.jpg'"
                );
                List<Image> result = q.getResultList();
                assertEquals(result.size(), 3);
                assertTrue(result.get(0) instanceof Image);
                tx.commit();
                em.close();
            }
            {
                tx.begin();
                em = JPA.createEntityManager();
                Query q = em.createQuery(
                    "select entry(img)\n" +
                        "    from Item i join i.images img\n" +
                        "    where key(img) like '%.jpg'"
                );
                List<Map.Entry<String, Image>> result = q.getResultList();
                assertEquals(result.size(), 3);
                assertTrue(result.get(0) instanceof Map.Entry);
                assertTrue(result.get(0).getKey().endsWith(".jpg"));
                tx.commit();
                em.close();
            }

        } finally {
            TM.rollback();
        }
    }

}
