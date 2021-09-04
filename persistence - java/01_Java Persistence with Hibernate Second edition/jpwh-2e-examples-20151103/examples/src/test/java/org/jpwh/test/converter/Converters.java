package org.jpwh.test.converter;

import org.jpwh.env.JPATest;
import org.jpwh.model.advanced.MonetaryAmount;
import org.jpwh.model.advanced.converter.Address;
import org.jpwh.model.advanced.converter.GermanZipcode;
import org.jpwh.model.advanced.converter.Item;
import org.jpwh.model.advanced.converter.User;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;
import java.util.Currency;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class Converters extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("ConverterPU");
    }

    @Test
    public void storeLoadMonetaryAmount() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            EntityManager em;

            tx.begin();
            em = JPA.createEntityManager();

            Item item = new Item();
            item.setName("Some Item");
            MonetaryAmount amount =
                new MonetaryAmount(
                    new BigDecimal("11.23"), Currency.getInstance("USD")
                );
            item.setBuyNowPrice(amount);
            em.persist(item);
            tx.commit();
            em.close();

            Long ITEM_ID = item.getId();

            tx.begin();
            em = JPA.createEntityManager();
            assertEquals(em.find(Item.class, ITEM_ID).getBuyNowPrice(), amount);
            assertEquals(em.find(Item.class, ITEM_ID).getBuyNowPrice().getValue(), new BigDecimal("11.23"));
            assertEquals(em.find(Item.class, ITEM_ID).getBuyNowPrice().getCurrency(), Currency.getInstance("USD"));
            tx.commit();
            em.close();

        } finally {
            TM.rollback();
        }
    }

    @Test
    public void storeAndLoadZipcode() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            User user = new User();
            user.setUsername("johndoe");
            Address homeAddress =
                new Address(
                    "Some Street 123",
                    new GermanZipcode("12345"),
                    "Some City"
                );
            user.setHomeAddress(homeAddress);
            em.persist(user);

            tx.commit();
            em.close();

            tx.begin();
            em = JPA.createEntityManager();

            User u = em.find(User.class, user.getId());

            assertEquals(u.getUsername(), "johndoe");
            assertTrue(u.getHomeAddress().getZipcode() instanceof GermanZipcode);
            assertEquals(u.getHomeAddress().getZipcode().getValue(), "12345");

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }
}
