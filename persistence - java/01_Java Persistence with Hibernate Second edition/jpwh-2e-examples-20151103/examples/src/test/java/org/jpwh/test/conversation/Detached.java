package org.jpwh.test.conversation;

import org.jpwh.env.JPATest;
import org.jpwh.model.conversation.User;
import org.jpwh.model.conversation.Item;
import org.jpwh.model.conversation.Image;
import org.jpwh.shared.util.TestData;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.util.HashSet;
import java.util.Set;

import static org.testng.Assert.*;

public class Detached extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("ConversationPU");
    }

    @Test
    public void businessKeyEquality() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();
            User user = new User("johndoe");
            em.persist(user);
            tx.commit();
            em.close();

            Long USER_ID = user.getId();

            tx.begin();
            em = JPA.createEntityManager();

            User a = em.find(User.class, USER_ID);
            User b = em.find(User.class, USER_ID);
            assertTrue(a == b);
            assertTrue(a.equals(b));
            assertEquals(a.getId(), b.getId());

            tx.commit();
            em.close();

            // Now compare with detached instances...
            tx.begin();
            em = JPA.createEntityManager();

            User c = em.find(User.class, USER_ID);
            assertFalse(a == c); // Still false, of course!
            assertTrue(a.equals(c)); // Now true!
            assertEquals(a.getId(), c.getId());

            tx.commit();
            em.close();

            Set<User> allUsers = new HashSet();
            allUsers.add(a);
            allUsers.add(b);
            allUsers.add(c);
            assertEquals(allUsers.size(), 1); // Correct!

        } finally {
            TM.rollback();
        }
    }

    /* ################################################################################### */

    public TestData storeItemImagesTestData() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        tx.begin();
        EntityManager em = JPA.createEntityManager();
        Long[] ids = new Long[1];
        Item item = new Item();
        item.setName("Some Item");
        em.persist(item);
        ids[0] = item.getId();
        for (int i = 1; i <= 3; i++) {
            item.getImages().add(
                new Image("Image " + i, "image" + i + ".jpg", 640, 480));
        }
        tx.commit();
        em.close();
        return new TestData(ids);
    }

}
