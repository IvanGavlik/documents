package org.jpwh.test.inheritance;


import org.jpwh.env.JPATest;
import org.jpwh.model.inheritance.associations.onetomany.BankAccount;
import org.jpwh.model.inheritance.associations.onetomany.BillingDetails;
import org.jpwh.model.inheritance.associations.onetomany.CreditCard;
import org.jpwh.model.inheritance.associations.onetomany.User;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import static org.testng.Assert.assertEquals;

public class PolymorphicOneToMany extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("PolymorphicOneToManyPU");
    }

    @Test
    public void storeAndLoadItemBids() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            BankAccount ba = new BankAccount(
                "Jane Roe", "445566", "One Percent Bank Inc.", "999"
            );
            CreditCard cc = new CreditCard(
                "John Doe", "1234123412341234", "06", "2015"
            );
            User johndoe = new User("johndoe");

            johndoe.getBillingDetails().add(ba);
            ba.setUser(johndoe);

            johndoe.getBillingDetails().add(cc);
            cc.setUser(johndoe);

            em.persist(ba);
            em.persist(cc);
            em.persist(johndoe);

            tx.commit();
            em.close();

            Long USER_ID = johndoe.getId();

            tx.begin();
            em = JPA.createEntityManager();
            {
                User user = em.find(User.class, USER_ID);

                for (BillingDetails billingDetails : user.getBillingDetails()) {
                    billingDetails.pay(123);
                }
                assertEquals(user.getBillingDetails().size(), 2);
            }

            tx.commit();
            em.close();

        } finally {
            TM.rollback();
        }
    }

}