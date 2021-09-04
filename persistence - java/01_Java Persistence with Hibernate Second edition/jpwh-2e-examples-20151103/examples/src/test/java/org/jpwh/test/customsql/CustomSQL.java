package org.jpwh.test.customsql;

import org.jpwh.env.JPATest;
import org.jpwh.model.customsql.Bid;
import org.jpwh.model.customsql.Category;
import org.jpwh.model.customsql.Image;
import org.jpwh.model.customsql.Item;
import org.jpwh.model.customsql.User;
import org.jpwh.shared.FetchTestLoadEventListener;
import org.jpwh.shared.util.CalendarUtil;
import org.jpwh.shared.util.TestData;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;
import java.util.Set;

import static org.testng.Assert.*;

public class CustomSQL extends JPATest {

    FetchTestLoadEventListener loadEventListener;

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("CustomSQLPU", "customsql/ItemQueries.hbm.xml");
    }

    @Override
    public void afterJPABootstrap() throws Exception {
        loadEventListener = new FetchTestLoadEventListener(JPA.getEntityManagerFactory());
    }

    class CustomSQLTestData {
        TestData categories;
        TestData items;
        TestData bids;
        TestData users;
    }

    public CustomSQLTestData create() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        tx.begin();
        EntityManager em = JPA.createEntityManager();

        CustomSQLTestData testData = new CustomSQLTestData();
        testData.categories = new TestData(new Long[1]);
        testData.items = new TestData(new Long[2]);
        testData.bids = new TestData(new Long[3]);
        testData.users = new TestData(new Long[2]);

        User johndoe = new User("johndoe");
        em.persist(johndoe);
        testData.users.identifiers[0] = johndoe.getId();

        User janeroe = new User("janeroe");
        em.persist(janeroe);
        testData.users.identifiers[1] = janeroe.getId();

        Category category = new Category();
        category.setName("Foo");
        em.persist(category);
        testData.categories.identifiers[0] = category.getId();

        Item item = new Item();
        item.setName("Some item");
        item.setCategory(category);
        item.setSeller(johndoe);
        item.setAuctionEnd(CalendarUtil.TOMORROW.getTime());

        item.getImages().add(
            new Image("foo.jpg", 640, 480)
        );
        item.getImages().add(
            new Image("bar.jpg", 800, 600)
        );
        item.getImages().add(
            new Image("baz.jpg", 640, 480)
        );

        em.persist(item);
        testData.items.identifiers[0] = item.getId();

        for (int i = 1; i <= 3; i++) {
            Bid bid = new Bid();
            bid.setAmount(new BigDecimal(10 + i));
            bid.setItem(item);
            bid.setBidder(janeroe);
            em.persist(bid);
            testData.bids.identifiers[i-1] = bid.getId();
        }

        Item otherItem = new Item(category, "Inactive item", CalendarUtil.TOMORROW.getTime(), johndoe);
        otherItem.setActive(false);
        em.persist(otherItem);

        tx.commit();
        em.close();

        return testData;
    }

    @Test
    public void read() throws Exception {
        CustomSQLTestData testData = create();
        Long CATEGORY_ID = testData.categories.getFirstId();
        Long ITEM_ID = testData.items.getFirstId();
        Long BID_ID = testData.bids.getFirstId();
        Long USER_ID = testData.users.getFirstId();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            {
                User user = em.find(User.class, USER_ID);
                assertEquals(loadEventListener.getLoadCount(User.class), 1);
                assertEquals(user.getId(), USER_ID);
            }
            em.clear();
            loadEventListener.reset();

            {
                Bid bid = em.find(Bid.class, BID_ID);
                assertEquals(loadEventListener.getLoadCount(Bid.class), 1);
                assertEquals(loadEventListener.getLoadCount(User.class), 1);
                assertEquals(loadEventListener.getLoadCount(Item.class), 0);
                assertEquals(bid.getId(), BID_ID);
                assertNotNull(bid.getBidder().getUsername());
            }
            em.clear();
            loadEventListener.reset();

            {
                Item item = em.find(Item.class, ITEM_ID);
                assertEquals(loadEventListener.getLoadCount(Item.class), 1);
                assertEquals(loadEventListener.getLoadCount(Bid.class), 3);
                assertEquals(item.getId(), ITEM_ID);
                assertEquals(item.getBids().size(), 3);
                assertEquals(item.getImages().size(), 3);
            }
            em.clear();
            loadEventListener.reset();

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

    // The "ACTIVE = 'true'" SQL restriction doesn't work on Oracle, they
    // still don't have a boolean datatype...
    @Test(groups = {"H2", "POSTGRESQL"})
    public void readRestrictedCollection() throws Exception {
        CustomSQLTestData testData = create();
        Long CATEGORY_ID = testData.categories.getFirstId();
        Long ITEM_ID = testData.items.getFirstId();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();
            {
                Category category = em.find(Category.class, CATEGORY_ID);
                assertEquals(loadEventListener.getLoadCount(Category.class), 1);
                Set<Item> items = category.getItems();
                assertEquals(items.size(), 1);
                assertEquals(items.iterator().next().getId(), ITEM_ID);
            }
            em.clear();
            loadEventListener.reset();

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

    @Test
    public void update() throws Exception {
        CustomSQLTestData testData = create();
        Long USER_ID = testData.users.getFirstId();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            {
                User user = em.find(User.class, USER_ID);
                user.setUsername("jdoe");
                em.flush();

            }
            em.clear();
            loadEventListener.reset();

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

    @Test
    public void delete() throws Exception {
        CustomSQLTestData testData = create();
        Long ITEM_ID = testData.items.getFirstId();
        Long USER_ID = testData.users.getFirstId();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            {
                Item item = em.find(Item.class, ITEM_ID);
                assertEquals(item.getImages().size(), 3);
                item.getImages().remove(item.getImages().iterator().next());
                em.flush();
            }
            em.clear();
            loadEventListener.reset();

            {
                Item item = em.find(Item.class, ITEM_ID);
                assertEquals(item.getImages().size(), 2);
                item.getImages().clear();
                em.flush();
            }
            em.clear();
            loadEventListener.reset();

            {
                // Clean up FK references so we can delete a User
                em.createQuery("delete Bid").executeUpdate();
                em.createQuery("delete Item").executeUpdate();
                em.clear();

                User user = em.find(User.class, USER_ID);
                em.remove(user);
                em.flush();
                em.clear();

                assertNull(em.find(User.class, USER_ID));
            }
            em.clear();
            loadEventListener.reset();

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }
}
