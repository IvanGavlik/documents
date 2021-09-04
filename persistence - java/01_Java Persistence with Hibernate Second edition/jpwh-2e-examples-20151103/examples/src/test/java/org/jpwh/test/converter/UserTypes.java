package org.jpwh.test.converter;

import org.jpwh.env.JPATest;
import org.jpwh.model.advanced.MonetaryAmount;
import org.jpwh.model.advanced.usertype.Item;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class UserTypes extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("UserTypePU");
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

            item.setBuyNowPrice(
                new MonetaryAmount(
                    new BigDecimal("456"), Currency.getInstance("CHF")
                )
            );

            item.setInitialPrice(
                new MonetaryAmount(
                    new BigDecimal("123"), Currency.getInstance("GBP")
                )
            );

            em.persist(item);
            tx.commit();
            em.close();

            Long ITEM_ID = item.getId();

            tx.begin();
            em = JPA.createEntityManager();
            Item i = em.find(Item.class, ITEM_ID);
            assertEquals(i.getBuyNowPrice().getValue().compareTo(new BigDecimal("912")), 0);
            assertEquals(i.getBuyNowPrice().getCurrency(), Currency.getInstance("USD"));
            assertEquals(i.getInitialPrice().getValue().compareTo(new BigDecimal("246")), 0);
            assertEquals(i.getInitialPrice().getCurrency(), Currency.getInstance("EUR"));

            List<Double> averagePrice = em.createQuery(
                "select avg(i.initialPrice.value) from Item i"
            ).getResultList();
            assertEquals(averagePrice.get(0), 246.0d);

            tx.commit();
            em.close();



        } finally {
            TM.rollback();
        }
    }
}
