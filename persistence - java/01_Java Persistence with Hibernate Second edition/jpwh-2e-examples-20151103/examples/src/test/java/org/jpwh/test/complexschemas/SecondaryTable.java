package org.jpwh.test.complexschemas;

import org.jpwh.env.JPATest;
import org.jpwh.model.complexschemas.secondarytable.Address;
import org.jpwh.model.complexschemas.secondarytable.User;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import static org.testng.Assert.assertEquals;

public class SecondaryTable extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("SecondaryTablePU");
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

}
