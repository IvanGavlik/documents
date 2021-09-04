package org.jpwh.test.advanced;

import org.jpwh.env.JPATest;
import org.jpwh.model.advanced.Address;
import org.jpwh.model.advanced.City;
import org.jpwh.model.advanced.User;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.util.Locale;

import static org.testng.Assert.assertEquals;

public class NestedComponents extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("AdvancedPU");
    }

    @Test
    public void storeAndLoadUsers() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            City city = new City();
            city.setZipcode("12345");
            city.setName("Some City");
            city.setCountry(Locale.GERMANY.getCountry());

            Address address = new Address();
            address.setStreet("Some Street 123");
            address.setCity(city);

            User userOne = new User();
            userOne.setAddress(address);

            em.persist(userOne);

            tx.commit();
            em.close();

            tx.begin();
            em = JPA.createEntityManager();

            User u = em.find(User.class, userOne.getId());

            assertEquals(u.getAddress().getStreet(), "Some Street 123");
            assertEquals(u.getAddress().getCity().getZipcode(), "12345");
            assertEquals(u.getAddress().getCity().getCountry(), Locale.GERMANY.getCountry());

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

}
