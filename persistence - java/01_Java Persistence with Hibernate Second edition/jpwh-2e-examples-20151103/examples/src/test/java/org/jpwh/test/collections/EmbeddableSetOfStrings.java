package org.jpwh.test.collections;

import org.jpwh.env.JPATest;
import org.jpwh.model.collections.embeddablesetofstrings.User;
import org.jpwh.model.collections.embeddablesetofstrings.Address;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import static org.testng.Assert.assertEquals;

public class EmbeddableSetOfStrings extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("EmbeddableSetOfStringsPU");
    }

    @Test
    public void storeLoadCollection() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            User user = new User("johndoe");
            Address address = new Address("Some Street", "1234", "Some City");
            user.setAddress(address);

            address.getContacts().add("Foo");
            address.getContacts().add("Bar");
            address.getContacts().add("Baz");

            em.persist(user);
            tx.commit();
            em.close();

            Long USER_ID = user.getId();

            tx.begin();
            em = JPA.createEntityManager();

            User johndoe = em.find(User.class, USER_ID);
            assertEquals(johndoe.getAddress().getContacts().size(), 3);

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

}
