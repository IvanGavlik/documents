package org.jpwh.test.simple;

import org.jpwh.env.JPATest;
import org.jpwh.model.simple.Address;
import org.jpwh.model.simple.User;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.transaction.UserTransaction;

import java.util.logging.Level;
import java.util.logging.Logger;

import static org.testng.Assert.assertEquals;

public class MappingEmbeddables extends JPATest {

    private static final Logger LOG = Logger.getLogger(MappingEmbeddables.class.getName());

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("SimplePU");
    }

    @Test
    public void storeAndLoadUsers() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            User user = new User();
            user.setUsername("johndoe");
            Address homeAddress = new Address("Some Street 123", "12345", "Some City");
            user.setHomeAddress(homeAddress);
            em.persist(user);

            tx.commit();
            em.close();

            tx.begin();
            em = JPA.createEntityManager();

            User u = em.find(User.class, user.getId());

            assertEquals(u.getUsername(), "johndoe");
            assertEquals(u.getHomeAddress().getStreet(), "Some Street 123");

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

    @Test(expectedExceptions = org.hibernate.exception.ConstraintViolationException.class)
    public void storeAndLoadInvalidUsers() throws Throwable {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            User user = new User();
            user.setUsername("johndoe");
            Address homeAddress = new Address("Some Street 123", "12345", null); // NULL city!
            user.setHomeAddress(homeAddress);
            em.persist(user);

            try {
                // Hibernate tries the INSERT but fails
                em.flush();

                // Note: If you try instead with tx.commit() and a flush side-effect, you won't
                // get the ConstraintViolationException. Hibernate will catch it internally and
                // simply mark the transaction for rollback.

            } catch (Exception ex) {
                throw unwrapCauseOfType(ex, org.hibernate.exception.ConstraintViolationException.class);
            }
        } finally {
            TM.rollback();
        }
    }

}
