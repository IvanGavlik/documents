package org.jpwh.test.advanced;

import org.hibernate.Session;
import org.hibernate.engine.jdbc.StreamUtils;
import org.jpwh.env.JPATest;
import org.jpwh.model.advanced.Item;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Blob;
import java.util.Random;

import static org.testng.Assert.assertEquals;

public class LazyProperties extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("AdvancedPU");
    }

    @Test
    public void storeLoadProperties() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();
            Item someItem = new Item();
            someItem.setName("Some item");
            someItem.setDescription("This is some description.");
            byte[] bytes = new byte[131072];
            new Random().nextBytes(bytes);
            someItem.setImage(bytes);
            em.persist(someItem);
            tx.commit();
            em.close();
            Long ITEM_ID = someItem.getId();

            tx.begin();
            em = JPA.createEntityManager();

            Item item = em.find(Item.class, ITEM_ID);

            // Accessing one initializes ALL lazy properties in a single SELECT
            assertEquals(item.getDescription(), "This is some description.");
            assertEquals(item.getImage().length, 131072); // 128 kilobytes

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

    @Test
    public void storeLoadLocator() throws Exception {
        // TODO: This test fails on H2 standalone
        // http://groups.google.com/group/h2-database/browse_thread/thread/9c6f4893a62c9b1a
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            byte[] bytes = new byte[131072];
            new Random().nextBytes(bytes);
            InputStream imageInputStream = new ByteArrayInputStream(bytes);
            int byteLength = bytes.length;

            Item someItem = new Item();
            someItem.setName("Some item");
            someItem.setDescription("This is some description.");

            // Need the native Hibernate API
            Session session = em.unwrap(Session.class);
            // You need to know the number of bytes you want to read from the stream!
            Blob blob = session.getLobHelper()
                    .createBlob(imageInputStream, byteLength);

            someItem.setImageBlob(blob);
            em.persist(someItem);

            tx.commit();
            em.close();

            Long ITEM_ID = someItem.getId();

            tx.begin();
            em = JPA.createEntityManager();

            Item item = em.find(Item.class, ITEM_ID);

            // You can stream the bytes directly...
            InputStream imageDataStream = item.getImageBlob().getBinaryStream();

            // ... or materialize them into memory:
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            StreamUtils.copy(imageDataStream, outStream);
            byte[] imageBytes = outStream.toByteArray();
            assertEquals(imageBytes.length, 131072);

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }


}
