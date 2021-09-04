package org.jpwh.test.fetching;

import org.jpwh.env.JPATest;
import org.jpwh.model.fetching.batch.Bid;
import org.jpwh.model.fetching.batch.Item;
import org.jpwh.model.fetching.batch.User;
import org.jpwh.shared.util.CalendarUtil;
import org.jpwh.shared.util.TestData;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;
import java.util.List;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class Batch extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("FetchingBatchPU");
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
    public void fetchProxyBatches() throws Exception {
        storeTestData();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            List<Item> items = em.createQuery("select i from Item i").getResultList();
            // select * from ITEM

            for (Item item : items) {
                assertNotNull(item.getSeller().getUsername());
                // select * from USERS where ID in (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            }
            em.clear();


            // The actual test
            /*
            NOTE: This test doesn't work because of a quirk in how Hibernate handles proxies. When
            you access the first proxy, batch fetching will load its data and at the same time more
            data for other proxies currently present in the persistence context (the other sellers you
            haven't accessed so far). But only the seller you have accessed will actually be initialized,
            meaning, it will be "connected" to the loaded data. The other proxies are still in status
            "uninitialized", although their data is now available in the persistence context. You will get
            a lazy initialization exception when you hit such a proxy after clearing/closing the persistence
            context. You have to access an uninitialized proxy within the persistence context life cycle,
            it will then be "connected" to the loaded data in the persistence context. If you want to have
            the data available in detached state, FetchType.EAGER gives you this guarantee. Of course then
            you no longer have lazy batch fetching.

            items = em.createQuery("select i from Item i").getResultList();
            // Access should load all sellers (we only have 3, batch size is 10)
            assertNotNull(items.iterator().next().getSeller().getUsername());
            em.clear(); // Detach all

            for (Item item : items) {
                assertNotNull(item.getSeller().getUsername());
            }
            */

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

    @Test
    public void fetchCollectionBatches() throws Exception {
        storeTestData();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            List<Item> items = em.createQuery("select i from Item i").getResultList();
            // select * from ITEM

            for (Item item : items) {
                assertTrue(item.getBids().size() > 0);
                // select * from BID where ITEM_ID in (?, ?, ?, ?, ?)
            }

            // The actual test
            em.clear();
            items = em.createQuery("select i from Item i").getResultList();
            // Access should load all (well, batches, but we only have 3) collections
            assertTrue(items.iterator().next().getBids().size() > 0);
            em.clear(); // Detach all
            for (Item item : items) {
                assertTrue(item.getBids().size() > 0);
            }

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

}
