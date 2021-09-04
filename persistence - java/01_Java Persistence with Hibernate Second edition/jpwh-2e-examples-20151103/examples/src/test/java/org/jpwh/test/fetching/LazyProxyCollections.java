package org.jpwh.test.fetching;

import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxyHelper;
import org.jpwh.env.JPATest;
import org.jpwh.model.fetching.proxy.Bid;
import org.jpwh.model.fetching.proxy.Category;
import org.jpwh.model.fetching.proxy.Item;
import org.jpwh.model.fetching.proxy.User;
import org.jpwh.shared.util.CalendarUtil;
import org.jpwh.shared.util.TestData;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.PersistenceUtil;
import javax.transaction.UserTransaction;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import static org.testng.Assert.*;

public class LazyProxyCollections extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("FetchingProxyPU");
    }

    public FetchTestData storeTestData() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        tx.begin();
        EntityManager em = JPA.createEntityManager();

        Long[] categoryIds = new Long[3];
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

        Category category = new Category("Category One");
        em.persist(category);
        categoryIds[0] = category.getId();

        Item item = new Item("Item One", CalendarUtil.TOMORROW.getTime(), johndoe);
        em.persist(item);
        itemIds[0] = item.getId();
        category.getItems().add(item);
        item.getCategories().add(category);
        for (int i = 1; i <= 3; i++) {
            Bid bid = new Bid(item, robertdoe, new BigDecimal(9 + i));
            item.getBids().add(bid);
            em.persist(bid);
        }

        category = new Category("Category Two");
        em.persist(category);
        categoryIds[1] = category.getId();

        item = new Item("Item Two", CalendarUtil.TOMORROW.getTime(), johndoe);
        em.persist(item);
        itemIds[1] = item.getId();
        category.getItems().add(item);
        item.getCategories().add(category);
        for (int i = 1; i <= 1; i++) {
            Bid bid = new Bid(item, janeroe, new BigDecimal(2 + i));
            item.getBids().add(bid);
            em.persist(bid);
        }

        item = new Item("Item Three", CalendarUtil.AFTER_TOMORROW.getTime(), janeroe);
        em.persist(item);
        itemIds[2] = item.getId();
        category.getItems().add(item);
        item.getCategories().add(category);

        category = new Category("Category Three");
        em.persist(category);
        categoryIds[2] = category.getId();

        tx.commit();
        em.close();

        FetchTestData testData = new FetchTestData();
        testData.items = new TestData(itemIds);
        testData.users = new TestData(userIds);
        return testData;
    }

    @Test
    public void lazyEntityProxies() throws Exception {
        FetchTestData testData = storeTestData();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            Long ITEM_ID = testData.items.getFirstId();
            Long USER_ID = testData.users.getFirstId();

            {
                Item item = em.getReference(Item.class, ITEM_ID); // No SELECT

                // Calling identifier getter (no field access!) doesn't trigger initialization
                assertEquals(item.getId(), ITEM_ID);

                // The class is runtime generated, named something like: Item_$$_javassist_1
                assertNotEquals(item.getClass(), Item.class);

                assertEquals(
                   HibernateProxyHelper.getClassWithoutInitializingProxy(item),
                   Item.class
                );

                PersistenceUtil persistenceUtil = Persistence.getPersistenceUtil();
                assertFalse(persistenceUtil.isLoaded(item));
                assertFalse(persistenceUtil.isLoaded(item, "seller"));

                assertFalse(Hibernate.isInitialized(item));
                // Would trigger initialization of item!
                // assertFalse(Hibernate.isInitialized(item.getSeller()));

                Hibernate.initialize(item);
                // select * from ITEM where ID = ?

                // Let's make sure the default EAGER of @ManyToOne has been overriden with LAZY
                assertFalse(Hibernate.isInitialized(item.getSeller()));

                Hibernate.initialize(item.getSeller());
                // select * from USERS where ID = ?
            }
            em.clear();
            {
                /* 
                   An <code>Item</code> entity instance is loaded in the persistence context, its
                   <code>seller</code> is not initialized, it's a <code>User</code> proxy.
                 */
                Item item = em.find(Item.class, ITEM_ID);
                // select * from ITEM where ID = ?

                /* 
                   You can manually detach the data from the persistence context, or close the
                   persistence context and detach everything.
                 */
                em.detach(item);
                em.detach(item.getSeller());
                // em.close();

                /* 
                   The static <code>PersistenceUtil</code> helper works without a persistence
                   context, you can check at any time if the data you want to access has
                   actually been loaded.
                 */
                PersistenceUtil persistenceUtil = Persistence.getPersistenceUtil();
                assertTrue(persistenceUtil.isLoaded(item));
                assertFalse(persistenceUtil.isLoaded(item, "seller"));

                /* 
                   In detached state, you can call the identifier getter method of the
                   <code>User</code> proxy. However, calling any other method on the proxy,
                   such as <code>getUsername()</code>, will throw a <code>LazyInitializationException</code>.
                   Data can only be loaded on-demand while the persistence context manages the proxy, not in detached
                   state.
                 */
                assertEquals(item.getSeller().getId(), USER_ID);
                // Throws exception!
                //assertNotNull(item.getSeller().getUsername());
            }
            em.clear();
            {
                // There is no SQL SELECT in this procedure, only one INSERT!
                Item item = em.getReference(Item.class, ITEM_ID);
                User user = em.getReference(User.class, USER_ID);

                Bid newBid = new Bid(new BigDecimal("99.00"));
                newBid.setItem(item);
                newBid.setBidder(user);

                em.persist(newBid);
                // insert into BID values (?, ? ,? , ...)

                em.flush();
                em.clear();
                assertEquals(em.find(Bid.class, newBid.getId()).getAmount().compareTo(new BigDecimal("99")), 0);
            }

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

    @Test
    public void lazyCollections() throws Exception {
        FetchTestData testData = storeTestData();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            long ITEM_ID = testData.items.getFirstId();

            {
                Item item = em.find(Item.class, ITEM_ID);
                // select * from ITEM where ID = ?

                Set<Bid> bids = item.getBids(); // Collection is not initialized
                PersistenceUtil persistenceUtil = Persistence.getPersistenceUtil();
                assertFalse(persistenceUtil.isLoaded(item, "bids"));

                // It's a Set
                assertTrue(Set.class.isAssignableFrom(bids.getClass()));

                // It's not a HashSet
                assertNotEquals(bids.getClass(), HashSet.class);
                assertEquals(bids.getClass(), org.hibernate.collection.internal.PersistentSet.class);

                Bid firstBid = bids.iterator().next();
                // select * from BID where ITEM_ID = ?

                // Alternative: Hibernate.initialize(bids);
            }
            em.clear();
            {
                Item item = em.find(Item.class, ITEM_ID);
                // select * from ITEM where ID = ?

                assertEquals(item.getBids().size(), 3);
                // select count(b) from BID b where b.ITEM_ID = ?
            }

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

}
