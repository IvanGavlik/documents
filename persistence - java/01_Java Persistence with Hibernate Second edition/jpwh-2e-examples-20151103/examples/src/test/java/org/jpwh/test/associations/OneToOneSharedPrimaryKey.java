package org.jpwh.test.associations;

import org.jpwh.env.JPATest;
import org.jpwh.model.associations.onetoone.sharedprimarykey.Address;
import org.jpwh.model.associations.onetoone.sharedprimarykey.User;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import static org.testng.Assert.assertEquals;

public class OneToOneSharedPrimaryKey extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("OneToOneSharedPrimaryKeyPU");
    }

    @Test
    public void storeAndLoadUserAddress() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            Address someAddress =
                new Address("Some Street 123", "12345", "Some City");

            em.persist(someAddress); // Generate identifier value

            User someUser =
                new User(
                    someAddress.getId(), // Assign same identifier value
                    "johndoe"
                );

            em.persist(someUser);

            someUser.setShippingAddress(someAddress); // Optional...

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

            assertEquals(user.getId(), address.getId());

            tx.commit();
            em.close();

        } finally {
            TM.rollback();
        }
    }

}