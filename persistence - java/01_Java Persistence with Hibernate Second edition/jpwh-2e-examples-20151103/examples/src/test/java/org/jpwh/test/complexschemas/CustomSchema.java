package org.jpwh.test.complexschemas;

import org.jpwh.env.JPATest;
import org.jpwh.model.complexschemas.custom.Bid;
import org.jpwh.model.complexschemas.custom.Item;
import org.jpwh.model.complexschemas.custom.User;
import org.jpwh.shared.util.CalendarUtil;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test(groups = "H2")
public class CustomSchema extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("CustomSchemaPU");
    }

    // All these tests are testing for failure

    @Test(expectedExceptions = org.hibernate.exception.ConstraintViolationException.class)
    public void storeLoadDomainInvalid() throws Throwable {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            // This will fail and therefore validate that we actually
            // have a custom SQL datatype in the DDL
            User user = new User();
            user.setEmail("@invalid.address");
            user.setUsername("someuser");
            em.persist(user);

            try {
                em.flush();
            } catch (Exception ex) {
                throw unwrapCauseOfType(ex, org.hibernate.exception.ConstraintViolationException.class);
            }
        } finally {
            TM.rollback();
        }
    }

    @Test(expectedExceptions = org.hibernate.exception.ConstraintViolationException.class)
    public void storeLoadCheckColumnInvalid() throws Throwable {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            User user = new User();
            user.setEmail("valid@test.com");
            user.setUsername("adminPretender");
            em.persist(user);

            try {
                em.flush();
            } catch (Exception ex) {
                throw unwrapCauseOfType(ex, org.hibernate.exception.ConstraintViolationException.class);
            }
        } finally {
            TM.rollback();
        }
    }

    @Test(expectedExceptions = org.hibernate.exception.ConstraintViolationException.class)
    public void storeLoadCheckSingleRowInvalid() throws Throwable {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            // Wrong start/end time
            Item item = new Item("Some Item", CalendarUtil.TOMORROW.getTime(), CalendarUtil.TODAY.getTime());
            em.persist(item);

            try {
                em.flush();
            } catch (Exception ex) {
                throw unwrapCauseOfType(ex, org.hibernate.exception.ConstraintViolationException.class);
            }
        } finally {
            TM.rollback();
        }
    }

    @Test(expectedExceptions = org.hibernate.exception.ConstraintViolationException.class)
    public void storeLoadUniqueMultiColumnValid() throws Throwable {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            User user = new User();
            user.setEmail("valid@test.com");
            user.setUsername("someuser");
            em.persist(user);

            user = new User();
            user.setEmail("valid@test.com");
            user.setUsername("someuser");
            em.persist(user);

            try {
                em.flush();
            } catch (Exception ex) {
                throw unwrapCauseOfType(ex, org.hibernate.exception.ConstraintViolationException.class);
            }
        } finally {
            TM.rollback();
        }
    }

    @Test(expectedExceptions = org.hibernate.exception.ConstraintViolationException.class)
    public void storeLoadCheckSubselectValid() throws Throwable {
        UserTransaction tx = TM.getUserTransaction();
        try {

            tx.begin();
            EntityManager em = JPA.createEntityManager();

            Item item = new Item("Some Item", CalendarUtil.TODAY.getTime(), CalendarUtil.TOMORROW.getTime());
            Bid bid = new Bid(new BigDecimal(1), item);
            bid.setCreatedOn(CalendarUtil.AFTER_TOMORROW.getTime()); // Out of date range of auction
            item.getBids().add(bid);

            em.persist(item);
            em.persist(bid);

            try {
                em.flush();
            } catch (Exception ex) {
                throw unwrapCauseOfType(ex, org.hibernate.exception.ConstraintViolationException.class);
            }
        } finally {
            TM.rollback();
        }
    }


    @Test // The control
    public void storeLoadValid() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            User user = new User();
            user.setEmail("valid@test.com");
            user.setUsername("someuser");
            em.persist(user);

            user = new User();
            user.setEmail("valid2@test.com");
            user.setUsername("otheruser");
            em.persist(user);

            tx.commit();
            em.close();

            tx.begin();
            em = JPA.createEntityManager();
            user = em.find(User.class, user.getId());
            assertEquals(user.getUsername(), "otheruser");
            tx.commit();
            em.close();

        } finally {
            TM.rollback();
        }
    }
}
