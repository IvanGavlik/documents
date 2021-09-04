package org.jpwh.test.fetching;

import org.jpwh.env.JPATest;
import org.jpwh.model.fetching.subselect.Bid;
import org.jpwh.model.fetching.subselect.Item;
import org.jpwh.model.fetching.subselect.User;
import org.jpwh.shared.util.CalendarUtil;
import org.jpwh.shared.util.TestData;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;
import java.util.List;

import static org.testng.Assert.assertTrue;

public class Subselect extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("FetchingSubselectPU");
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
    public void fetchCollectionSubselect() throws Exception {
        storeTestData();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            List<Item> items = em.createQuery("select i from Item i").getResultList();
            // select * from ITEM

            for (Item item : items) {
                assertTrue(item.getBids().size() > 0);
                // select * from BID where ITEM_ID in (
                //  select ID from ITEM
                // )
            }

            // The actual test
            em.clear();
            items = em.createQuery("select i from Item i").getResultList();
            // Access should load all collections
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
