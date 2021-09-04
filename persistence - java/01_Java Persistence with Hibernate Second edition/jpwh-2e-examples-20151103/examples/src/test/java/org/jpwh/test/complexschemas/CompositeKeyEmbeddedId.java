package org.jpwh.test.complexschemas;

import org.jpwh.env.JPATest;
import org.jpwh.model.complexschemas.compositekey.embedded.User;
import org.jpwh.model.complexschemas.compositekey.embedded.UserId;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import static org.testng.Assert.assertEquals;

public class CompositeKeyEmbeddedId extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("CompositeKeyEmbeddedIdPU");
    }

    @Test
    public void storeLoad() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            {
                UserId id = new UserId("johndoe", "123");
                User user = new User(id);
                em.persist(user);
            }

            tx.commit();
            em.close();

            tx.begin();
            em = JPA.createEntityManager();

            {
                UserId id = new UserId("johndoe", "123");
                User user = em.find(User.class, id);
                assertEquals(user.getId().getDepartmentNr(), "123");
            }

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

}
