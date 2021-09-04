package org.jpwh.test.fetching;

import org.hibernate.Hibernate;
import org.jpwh.env.JPATest;
import org.jpwh.model.fetching.interception.Item;
import org.jpwh.model.fetching.interception.User;
import org.jpwh.shared.util.CalendarUtil;
import org.jpwh.shared.util.TestData;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

// TODO: See model/pom.xml, the new bytecode enhancer is broken, you will not see the lazy loading SQL as outlined here!
public class LazyInterception extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("FetchingInterceptionPU");
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

        Item item = new Item("Item One", CalendarUtil.TOMORROW.getTime(), johndoe, "Some description.");
        em.persist(item);
        itemIds[0] = item.getId();

        item = new Item("Item Two", CalendarUtil.TOMORROW.getTime(), johndoe, "Some description.");
        em.persist(item);
        itemIds[1] = item.getId();

        item = new Item("Item Three", CalendarUtil.AFTER_TOMORROW.getTime(), janeroe, "Some description.");
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
    public void noUserProxy() throws Exception {
        FetchTestData testData = storeTestData();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            Long ITEM_ID = testData.items.getFirstId();
            Long USER_ID = testData.users.getFirstId();

            {
                // Proxies are disabled, getReference() will return an initialized instance
                User user = em.getReference(User.class, USER_ID);
                // select * from USERS where ID = ?

                assertTrue(Hibernate.isInitialized(user));
            }
            em.clear();

            {
                Item item = em.find(Item.class, ITEM_ID);
                // select * from ITEM where ID = ?

                assertEquals(item.getSeller().getId(), USER_ID);
                // select * from USERS where ID = ?
                // Even item.getSeller() would trigger the SELECT!
            }

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

    @Test
    public void lazyBasic() throws Exception {
        FetchTestData testData = storeTestData();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            Long ITEM_ID = testData.items.getFirstId();

            Item item = em.find(Item.class, ITEM_ID);
            // select NAME, AUCTIONEND, ... from ITEM where ID = ?

             // Accessing one loads _all_ lazy properties (description, seller, ...)
            assertTrue(item.getDescription().length() > 0);
            // select DESCRIPTION from ITEM where ID = ?
            // select * from USERS where ID = ?

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }


}
