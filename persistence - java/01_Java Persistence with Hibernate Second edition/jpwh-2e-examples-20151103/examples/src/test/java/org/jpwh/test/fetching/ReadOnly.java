package org.jpwh.test.fetching;

import org.hibernate.Session;
import org.jpwh.env.JPATest;
import org.jpwh.model.fetching.readonly.Bid;
import org.jpwh.model.fetching.readonly.Item;
import org.jpwh.model.fetching.readonly.User;
import org.jpwh.shared.util.CalendarUtil;
import org.jpwh.shared.util.TestData;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;
import java.util.List;

import static org.testng.Assert.*;

public class ReadOnly extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("FetchingReadOnlyPU");
    }

    public FetchTestData storeTestData() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        tx.begin();
        EntityManager em = JPA.createEntityManager();

        Long[] itemIds = new Long[3];
        Long[] userIds = new Long[3];

        User johndoe = new User("johndoe");
        em.persist(johndoe);
        userIds[0] = johndoe.getId();

        User janeroe = new User("janeroe");
        em.persist(janeroe);
        userIds[1] = janeroe.getId();

        User robertdoe = new User("robertdoe");
        em.persist(robertdoe);
        userIds[2] = robertdoe.getId();

        Item item = new Item("Item One", CalendarUtil.TOMORROW.getTime(), johndoe);
        em.persist(item);
        itemIds[0] = item.getId();
        for (int i = 1; i <= 3; i++) {
            Bid bid = new Bid(item, robertdoe, new BigDecimal(9 + i));
            item.getBids().add(bid);
            em.persist(bid);
        }

        item = new Item("Item Two", CalendarUtil.TOMORROW.getTime(), johndoe);
        em.persist(item);
        itemIds[1] = item.getId();
        for (int i = 1; i <= 1; i++) {
            Bid bid = new Bid(item, janeroe, new BigDecimal(2 + i));
            item.getBids().add(bid);
            em.persist(bid);
        }

        item = new Item("Item Three", CalendarUtil.AFTER_TOMORROW.getTime(), janeroe);
        em.persist(item);
        itemIds[2] = item.getId();

        tx.commit();
        em.close();

        FetchTestData testData = new FetchTestData();
        testData.items = new TestData(itemIds);
        testData.users = new TestData(userIds);
        return testData;
    }

    @Test
    public void immutableEntity() throws Exception {
        FetchTestData testData = storeTestData();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            Long ITEM_ID = testData.items.getFirstId();

            Item item = em.find(Item.class, ITEM_ID);
            for (Bid bid : item.getBids()) {
                bid.setAmount(new BigDecimal("99.99")); // This has no effect
            }
            em.flush();
            em.clear();

            item = em.find(Item.class, ITEM_ID);
            for (Bid bid : item.getBids()) {
                assertNotEquals(bid.getAmount().toString(), "99.99");
            }

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

    @Test
    public void selectiveReadOnly() throws Exception {
        FetchTestData testData = storeTestData();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            Long ITEM_ID = testData.items.getFirstId();

            {
                em.unwrap(Session.class).setDefaultReadOnly(true);

                Item item = em.find(Item.class, ITEM_ID);
                item.setName("New Name");

                em.flush(); // No UPDATE
            }
            {
                em.clear();
                Item item = em.find(Item.class, ITEM_ID);
                assertNotEquals(item.getName(), "New Name");
            }
            {
                Item item = em.find(Item.class, ITEM_ID);

                em.unwrap(Session.class).setReadOnly(item, true);

                item.setName("New Name");

                em.flush(); // No UPDATE
            }
            {
                em.clear();
                Item item = em.find(Item.class, ITEM_ID);
                assertNotEquals(item.getName(), "New Name");
            }
            {
                org.hibernate.Query query = em.unwrap(Session.class)
                    .createQuery("select i from Item i");

                query.setReadOnly(true).list();

                List<Item> result = query.list();

                for (Item item : result)
                    item.setName("New Name");

                em.flush(); // No UPDATE
            }
            {
                List<Item> items = em.createQuery("select i from Item i")
                    .setHint(
                        org.hibernate.annotations.QueryHints.READ_ONLY,
                        true
                    ).getResultList();

                for (Item item : items)
                    item.setName("New Name");
                em.flush(); // No UPDATE
            }
            {
                em.clear();
                Item item = em.find(Item.class, ITEM_ID);
                assertNotEquals(item.getName(), "New Name");
            }

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

}
