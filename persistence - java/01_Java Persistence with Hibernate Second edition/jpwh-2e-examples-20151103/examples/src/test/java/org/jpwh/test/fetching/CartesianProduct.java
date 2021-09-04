package org.jpwh.test.fetching;

import org.jpwh.env.JPATest;
import org.jpwh.model.fetching.cartesianproduct.Bid;
import org.jpwh.model.fetching.cartesianproduct.Item;
import org.jpwh.model.fetching.cartesianproduct.User;
import org.jpwh.shared.FetchTestLoadEventListener;
import org.jpwh.shared.util.CalendarUtil;
import org.jpwh.shared.util.TestData;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;

import static org.testng.Assert.assertEquals;

public class CartesianProduct extends JPATest {

    FetchTestLoadEventListener loadEventListener;

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("FetchingCartesianProductPU");
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
        item.getImages().add("foo.jpg");
        item.getImages().add("bar.jpg");
        item.getImages().add("baz.jpg");
        em.persist(item);
        itemIds[0] = item.getId();
        for (int i = 1; i <= 3; i++) {
            Bid bid = new Bid(item, new BigDecimal(9 + i));
            item.getBids().add(bid);
            em.persist(bid);
        }

        item = new Item("Item Two", CalendarUtil.TOMORROW.getTime(), johndoe);
        item.getImages().add("a.jpg");
        item.getImages().add("b.jpg");
        em.persist(item);
        itemIds[1] = item.getId();
        for (int i = 1; i <= 1; i++) {
            Bid bid = new Bid(item, new BigDecimal(2 + i));
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
    public void fetchCollections() throws Exception {
        FetchTestData testData = storeTestData();
        loadEventListener.reset();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            long ITEM_ID = testData.items.getFirstId();

            Item item = em.find(Item.class, ITEM_ID);
            // select i.*, b.*, img.*
            //  from ITEM i
            //   left outer join BID b on b.ITEM_ID = i.ID
            //   left outer join IMAGE img on img.ITEM_ID = i.ID
            //  where i.ID = ?
            assertEquals(loadEventListener.getLoadCount(Item.class), 1);
            assertEquals(loadEventListener.getLoadCount(Bid.class), 3);

            em.detach(item);

            assertEquals(item.getImages().size(), 3);
            assertEquals(item.getBids().size(), 3);

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

}
