package org.jpwh.test.filtering;

import org.jpwh.env.JPATest;
import org.jpwh.model.filtering.callback.CurrentUser;
import org.jpwh.model.filtering.callback.Item;
import org.jpwh.model.filtering.callback.Mail;
import org.jpwh.model.filtering.callback.User;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class Callback extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("FilteringCallbackPU");
    }

    @Test
    public void notifyPostPersist() throws Throwable {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            {
                User user = new User("johndoe");
                CurrentUser.INSTANCE.set(user); // Thread-local

                em.persist(user);
                assertEquals(Mail.INSTANCE.size(), 0);
                em.flush();
                assertEquals(Mail.INSTANCE.size(), 1);
                assertTrue(Mail.INSTANCE.get(0).contains("johndoe"));
                Mail.INSTANCE.clear();


                Item item = new Item("Foo", user);
                em.persist(item);
                assertEquals(Mail.INSTANCE.size(), 0);
                em.flush();
                assertEquals(Mail.INSTANCE.size(), 1);
                assertTrue(Mail.INSTANCE.get(0).contains("johndoe"));
                Mail.INSTANCE.clear();

                CurrentUser.INSTANCE.set(null);
            }
            em.clear();

        } finally {
            TM.rollback();
        }
    }

}
