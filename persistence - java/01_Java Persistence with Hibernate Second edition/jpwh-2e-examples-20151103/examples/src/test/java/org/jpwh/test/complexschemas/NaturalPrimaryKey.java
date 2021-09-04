package org.jpwh.test.complexschemas;

import org.jpwh.env.JPATest;
import org.jpwh.model.complexschemas.naturalprimarykey.User;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import static org.testng.Assert.assertNotNull;

public class NaturalPrimaryKey extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("NaturalPrimaryKeyPU");
    }

    @Test
    public void storeLoad() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();
            {
                User user = new User("johndoe");
                em.persist(user);
            }
            tx.commit();
            em.close();

            tx.begin();
            em = JPA.createEntityManager();
            {
                User user = em.find(User.class, "johndoe");
                assertNotNull(user);
            }
            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

}
