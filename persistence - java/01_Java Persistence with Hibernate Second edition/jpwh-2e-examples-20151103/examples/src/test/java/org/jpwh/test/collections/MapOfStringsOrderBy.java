package org.jpwh.test.collections;

import org.jpwh.env.JPATest;
import org.jpwh.model.collections.mapofstringsorderby.Item;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.util.Iterator;
import java.util.Map;

import static org.testng.Assert.assertEquals;

public class MapOfStringsOrderBy extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("MapOfStringsOrderByPU");
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

            // Iteration order as retrieved from database with ORDER BY clause
            Iterator<Map.Entry<String,String>> it = item.getImages().entrySet().iterator();
            Map.Entry<String,String> entry;
            entry = it.next();
            assertEquals(entry.getKey(), "foo.jpg");
            assertEquals(entry.getValue(), "Foo");
            entry = it.next();
            assertEquals(entry.getKey(), "baz.jpg");
            assertEquals(entry.getValue(), "Baz");
            entry = it.next();
            assertEquals(entry.getKey(), "bar.jpg");
            assertEquals(entry.getValue(), "Bar");

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

}
