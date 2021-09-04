package org.jpwh.test.fetching;

import org.jpwh.env.JPATest;
import org.jpwh.model.fetching.eagerselect.Bid;
import org.jpwh.model.fetching.eagerselect.Item;
import org.jpwh.model.fetching.eagerselect.User;
import org.jpwh.shared.util.CalendarUtil;
import org.jpwh.shared.util.TestData;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class EagerSelect extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("FetchingEagerSelectPU");
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
    public void fetchEagerSelect() throws Exception {
        FetchTestData testData = storeTestData();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            Long ITEM_ID = testData.items.getFirstId();

            Item item = em.find(Item.class, ITEM_ID);
            // select * from ITEM where ID = ?
            // select * from USERS where ID = ?
            // select * from BID where ITEM_ID = ?

            em.detach(item);

            assertEquals(item.getBids().size(), 3);
            assertNotNull(item.getBids().iterator().next().getAmount());
            assertEquals(item.getSeller().getUsername(), "johndoe");

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }
}
