package org.jpwh.test.collections;

import org.jpwh.env.JPATest;
import org.jpwh.model.collections.setofembeddablesorderby.Image;
import org.jpwh.model.collections.setofembeddablesorderby.Item;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import java.util.Iterator;

import static org.testng.Assert.assertEquals;

public class SetOfEmbeddablesOrderBy extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("SetOfEmbeddablesOrderByPU");
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
                    "Bar", "bar.jpg", 800, 600
            ));
            someItem.getImages().add(new Image(
                    "Baz", "baz.jpg", 1024, 768
            ));
            someItem.getImages().add(new Image(
                    "Baz", "baz.jpg", 1024, 768
            ));
            assertEquals(someItem.getImages().size(), 3);

            em.persist(someItem);
            tx.commit();
            em.close();
            Long ITEM_ID = someItem.getId();

            tx.begin();
            em = JPA.createEntityManager();
            Item item = em.find(Item.class, ITEM_ID);
            assertEquals(item.getImages().size(), 3);

            Iterator<Image> it = item.getImages().iterator();
            assertEquals(it.next().getFilename(), "bar.jpg");
            assertEquals(it.next().getFilename(), "baz.jpg");
            assertEquals(it.next().getFilename(), "foo.jpg");
            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

}
