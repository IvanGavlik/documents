package org.jpwh.test.inheritance;


import org.jpwh.env.JPATest;
import org.jpwh.model.inheritance.associations.manytoone.BillingDetails;
import org.jpwh.model.inheritance.associations.manytoone.CreditCard;
import org.jpwh.model.inheritance.associations.manytoone.User;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import static org.testng.Assert.*;

public class PolymorphicManyToOne extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("PolymorphicManyToOnePU");
    }

    @Test
    public void storeAndLoadItemBids() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            CreditCard cc = new CreditCard(
                "John Doe", "1234123412341234", "06", "2015"
            );
            User johndoe = new User("johndoe");
            johndoe.setDefaultBilling(cc);

            em.persist(cc);
            em.persist(johndoe);

            tx.commit();
            em.close();

            Long USER_ID = johndoe.getId();

            tx.begin();
            em = JPA.createEntityManager();
            {
                User user = em.find(User.class, USER_ID);

                // Invoke the pay() method on a concrete subclass of BillingDetails
                user.getDefaultBilling().pay(123);
                assertEquals(user.getDefaultBilling().getOwner(), "John Doe");
            }

            tx.commit();
            em.close();

            tx.begin();
            em = JPA.createEntityManager();
            {
                User user = em.find(User.class, USER_ID);

                BillingDetails bd = user.getDefaultBilling();

                assertFalse(bd instanceof CreditCard);

                // Don't do this, ClassCastException!
                // CreditCard creditCard = (CreditCard) bd;
            }
            {
                User user = em.find(User.class, USER_ID);

                BillingDetails bd = user.getDefaultBilling();

                CreditCard creditCard =
                    em.getReference(CreditCard.class, bd.getId()); // No SELECT!

                assertTrue(bd != creditCard); // Careful!
            }
            tx.commit();
            em.close();

            tx.begin();
            em = JPA.createEntityManager();
            {
                User user = (User) em.createQuery(
                    "select u from User u " +
                        "left join fetch u.defaultBilling " +
                        "where u.id = :id")
                    .setParameter("id", USER_ID)
                    .getSingleResult();

                // No proxy has been used, the BillingDetails instance has been fetched eagerly
                CreditCard creditCard = (CreditCard) user.getDefaultBilling();
            }
            tx.commit();
            em.close();

        } finally {
            TM.rollback();
        }
    }

}