package org.jpwh.test.fetching;

import org.jpwh.env.JPATest;
import org.jpwh.model.fetching.nplusoneselects.Bid;
import org.jpwh.model.fetching.nplusoneselects.Item;
import org.jpwh.model.fetching.nplusoneselects.User;
import org.jpwh.shared.FetchTestLoadEventListener;
import org.jpwh.shared.util.CalendarUtil;
import org.jpwh.shared.util.TestData;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;
import java.util.List;

import static org.testng.Assert.*;

public class NPlusOneSelects extends JPATest {

    FetchTestLoadEventListener loadEventListener;

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("FetchingNPlusOneSelectsPU");
    }

    @Override
    public void afterJPABootstrap() throws Exception {
        loadEventListener = new FetchTestLoadEventListener(JPA.getEntityManagerFactory());
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
        for (int i = 1; i <= 1; i++) {
            Bid bid = new Bid(item, johndoe, new BigDecimal(3 + i));
            item.getBids().add(bid);
            em.persist(bid);
        }

        tx.commit();
        em.close();

        FetchTestData testData = new FetchTestData();
        testData.items = new TestData(itemIds);
        testData.users = new TestData(userIds);
        return testData;
    }

    @Test
    public void fetchUsers() throws Exception {
        storeTestData();
        loadEventListener.reset();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            List<Item> items = em.createQuery("select i from Item i").getResultList();
            // select * from ITEM
            assertEquals(loadEventListener.getLoadCount(Item.class), 3);
            assertEquals(loadEventListener.getLoadCount(User.class), 0);

            for (Item item : items) {
                // Each seller has to be loaded with an additional SELECT
                assertNotNull(item.getSeller().getUsername());
                // select * from USERS where ID = ?
            }
            assertEquals(loadEventListener.getLoadCount(User.class), 2);

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

    @Test
    public void fetchBids() throws Exception {
        storeTestData();
        loadEventListener.reset();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            List<Item> items = em.createQuery("select i from Item i").getResultList();
            // select * from ITEM
            assertEquals(loadEventListener.getLoadCount(Item.class), 3);
            assertEquals(loadEventListener.getLoadCount(Bid.class), 0);

            for (Item item : items) {
                // Each bids collection has to be loaded with an additional SELECT
                assertTrue(item.getBids().size() > 0);
                // select * from BID where ITEM_ID = ?
            }
            assertEquals(loadEventListener.getLoadCount(Bid.class), 5);

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

}
