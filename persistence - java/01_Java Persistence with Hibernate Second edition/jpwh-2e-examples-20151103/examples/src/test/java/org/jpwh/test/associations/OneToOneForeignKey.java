package org.jpwh.test.associations;


import org.jpwh.env.JPATest;
import org.jpwh.model.associations.onetoone.foreignkey.Address;
import org.jpwh.model.associations.onetoone.foreignkey.User;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import static org.testng.Assert.assertEquals;

public class OneToOneForeignKey extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("OneToOneForeignKeyPU");
    }

    @Test
    public void storeAndLoadUserAddress() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            User someUser =
                new User("johndoe");

            Address someAddress =
                new Address("Some Street 123", "12345", "Some City");

            someUser.setShippingAddress(someAddress); // Link

            em.persist(someUser); // Transitive persistence of shippingAddress

            tx.commit();
            em.close();
            Long USER_ID = someUser.getId();
            Long ADDRESS_ID = someAddress.getId();

            tx.begin();
            em = JPA.createEntityManager();

            User user = em.find(User.class, USER_ID);
            assertEquals(user.getShippingAddress().getZipcode(), "12345");

            Address address = em.find(Address.class, ADDRESS_ID);
            assertEquals(address.getZipcode(), "12345");

            tx.commit();
            em.close();

        } finally {
            TM.rollback();
        }
    }

    @Test(expectedExceptions = org.hibernate.exception.ConstraintViolationException.class)
    public void storeNonUniqueRelationship() throws Throwable {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            Address someAddress = new Address("Some Street 123", "12345", "Some City");

            User userOne = new User("johndoe");
            userOne.setShippingAddress(someAddress);
            em.persist(userOne); // OK

            User userTwo = new User("janeroe");
            userTwo.setShippingAddress(someAddress);
            em.persist(userTwo); // Fails, true unique @OneToOne!

            try {
                // Hibernate tries the INSERT but fails
                em.flush();
            } catch (Exception ex) {
                throw unwrapCauseOfType(ex, org.hibernate.exception.ConstraintViolationException.class);
            }
        } finally {
            TM.rollback();
        }
    }

}