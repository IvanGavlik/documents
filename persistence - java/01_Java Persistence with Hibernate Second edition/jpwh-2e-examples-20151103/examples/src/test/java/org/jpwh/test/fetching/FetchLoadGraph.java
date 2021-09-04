package org.jpwh.test.fetching;

import org.jpwh.env.JPATest;
import org.jpwh.model.fetching.fetchloadgraph.Bid;
import org.jpwh.model.fetching.fetchloadgraph.Bid_;
import org.jpwh.model.fetching.fetchloadgraph.Item;
import org.jpwh.model.fetching.fetchloadgraph.Item_;
import org.jpwh.model.fetching.fetchloadgraph.User;
import org.jpwh.shared.FetchTestLoadEventListener;
import org.jpwh.shared.util.CalendarUtil;
import org.jpwh.shared.util.TestData;
import org.testng.annotations.Test;

import javax.persistence.*;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

public class FetchLoadGraph extends JPATest {

    FetchTestLoadEventListener loadEventListener;

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("FetchingFetchLoadGraphPU");
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
        Long[] bidIds = new Long[3];

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
            bidIds[i - 1] = bid.getId();
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
        testData.bids = new TestData(bidIds);
        testData.users = new TestData(userIds);
        return testData;
    }

    @Test
    public void loadItem() throws Exception {
        FetchTestData testData = storeTestData();
        long ITEM_ID = testData.items.getFirstId();
        PersistenceUtil persistenceUtil = Persistence.getPersistenceUtil();
        loadEventListener.reset();
        {
            UserTransaction tx = TM.getUserTransaction();
            try {
                tx.begin();
                EntityManager em = JPA.createEntityManager();

                Map<String, Object> properties = new HashMap<>();
                properties.put(
                    "javax.persistence.loadgraph",
                    em.getEntityGraph(Item.class.getSimpleName()) // "Item"
                );

                Item item = em.find(Item.class, ITEM_ID, properties);
                // select * from ITEM where ID = ?

                assertTrue(persistenceUtil.isLoaded(item));
                assertTrue(persistenceUtil.isLoaded(item, "name"));
                assertTrue(persistenceUtil.isLoaded(item, "auctionEnd"));
                assertFalse(persistenceUtil.isLoaded(item, "seller"));
                assertFalse(persistenceUtil.isLoaded(item, "bids"));

                tx.commit();
                em.close();
            } finally {
                TM.rollback();
            }
        }
        {
            UserTransaction tx = TM.getUserTransaction();
            try {
                tx.begin();
                EntityManager em = JPA.createEntityManager();

                EntityGraph<Item> itemGraph = em.createEntityGraph(Item.class);

                Map<String, Object> properties = new HashMap<>();
                properties.put("javax.persistence.loadgraph", itemGraph);

                Item item = em.find(Item.class, ITEM_ID, properties);

                assertTrue(persistenceUtil.isLoaded(item));
                assertTrue(persistenceUtil.isLoaded(item, "name"));
                assertTrue(persistenceUtil.isLoaded(item, "auctionEnd"));
                assertFalse(persistenceUtil.isLoaded(item, "seller"));
                assertFalse(persistenceUtil.isLoaded(item, "bids"));

                tx.commit();
                em.close();
            } finally {
                TM.rollback();
            }
        }
    }

    @Test
    public void loadItemSeller() throws Exception {
        FetchTestData testData = storeTestData();
        long ITEM_ID = testData.items.getFirstId();
        PersistenceUtil persistenceUtil = Persistence.getPersistenceUtil();
        loadEventListener.reset();
        {
            UserTransaction tx = TM.getUserTransaction();
            try {
                tx.begin();
                EntityManager em = JPA.createEntityManager();

                Map<String, Object> properties = new HashMap<>();
                properties.put(
                    "javax.persistence.loadgraph",
                    em.getEntityGraph("ItemSeller")
                );

                Item item = em.find(Item.class, ITEM_ID, properties);
                // select i.*, u.*
                //  from ITEM i
                //   inner join USERS u on u.ID = i.SELLER_ID
                // where i.ID = ?

                assertTrue(persistenceUtil.isLoaded(item));
                assertTrue(persistenceUtil.isLoaded(item, "name"));
                assertTrue(persistenceUtil.isLoaded(item, "auctionEnd"));
                assertTrue(persistenceUtil.isLoaded(item, "seller"));
                assertFalse(persistenceUtil.isLoaded(item, "bids"));

                tx.commit();
                em.close();
            } finally {
                TM.rollback();
            }
        }
        {
            UserTransaction tx = TM.getUserTransaction();
            try {
                tx.begin();
                EntityManager em = JPA.createEntityManager();

                EntityGraph<Item> itemGraph = em.createEntityGraph(Item.class);
                itemGraph.addAttributeNodes(Item_.seller); // Static metamodel

                Map<String, Object> properties = new HashMap<>();
                properties.put("javax.persistence.loadgraph", itemGraph);

                Item item = em.find(Item.class, ITEM_ID, properties);
                // select i.*, u.*
                //  from ITEM i
                //   inner join USERS u on u.ID = i.SELLER_ID
                // where i.ID = ?

                assertTrue(persistenceUtil.isLoaded(item));
                assertTrue(persistenceUtil.isLoaded(item, "name"));
                assertTrue(persistenceUtil.isLoaded(item, "auctionEnd"));
                assertTrue(persistenceUtil.isLoaded(item, "seller"));
                assertFalse(persistenceUtil.isLoaded(item, "bids"));

                tx.commit();
                em.close();
            } finally {
                TM.rollback();
            }
        }
        {
            UserTransaction tx = TM.getUserTransaction();
            try {
                tx.begin();
                EntityManager em = JPA.createEntityManager();

                EntityGraph<Item> itemGraph = em.createEntityGraph(Item.class);
                itemGraph.addAttributeNodes("seller");

                List<Item> items =
                    em.createQuery("select i from Item i")
                        .setHint("javax.persistence.loadgraph", itemGraph)
                        .getResultList();
                // select i.*, u.*
                //  from ITEM i
                //   left outer join USERS u on u.ID = i.SELLER_ID

                assertEquals(items.size(), 3);

                for (Item item : items) {
                    assertTrue(persistenceUtil.isLoaded(item));
                    assertTrue(persistenceUtil.isLoaded(item, "name"));
                    assertTrue(persistenceUtil.isLoaded(item, "auctionEnd"));
                    assertTrue(persistenceUtil.isLoaded(item, "seller"));
                    assertFalse(persistenceUtil.isLoaded(item, "bids"));
                }

                tx.commit();
                em.close();
            } finally {
                TM.rollback();
            }
        }
    }

    @Test
    public void loadBidBidderItem() throws Exception {
        FetchTestData testData = storeTestData();
        long BID_ID = testData.bids.getFirstId();
        PersistenceUtil persistenceUtil = Persistence.getPersistenceUtil();
        loadEventListener.reset();
        {
            UserTransaction tx = TM.getUserTransaction();
            try {
                tx.begin();
                EntityManager em = JPA.createEntityManager();

                Map<String, Object> properties = new HashMap<>();
                properties.put(
                    "javax.persistence.loadgraph",
                    em.getEntityGraph("BidBidderItem")
                );

                Bid bid = em.find(Bid.class, BID_ID, properties);

                assertTrue(persistenceUtil.isLoaded(bid));
                assertTrue(persistenceUtil.isLoaded(bid, "amount"));
                assertTrue(persistenceUtil.isLoaded(bid, "bidder"));
                assertTrue(persistenceUtil.isLoaded(bid, "item"));
                assertTrue(persistenceUtil.isLoaded(bid.getItem(), "name"));
                assertFalse(persistenceUtil.isLoaded(bid.getItem(), "seller"));

                tx.commit();
                em.close();
            } finally {
                TM.rollback();
            }
        }
        {
            UserTransaction tx = TM.getUserTransaction();
            try {
                tx.begin();
                EntityManager em = JPA.createEntityManager();

                EntityGraph<Bid> bidGraph = em.createEntityGraph(Bid.class);
                bidGraph.addAttributeNodes("bidder", "item");

                Map<String, Object> properties = new HashMap<>();
                properties.put("javax.persistence.loadgraph", bidGraph);

                Bid bid = em.find(Bid.class, BID_ID, properties);

                assertTrue(persistenceUtil.isLoaded(bid));
                assertTrue(persistenceUtil.isLoaded(bid, "amount"));
                assertTrue(persistenceUtil.isLoaded(bid, "bidder"));
                assertTrue(persistenceUtil.isLoaded(bid, "item"));
                assertTrue(persistenceUtil.isLoaded(bid.getItem(), "name"));
                assertFalse(persistenceUtil.isLoaded(bid.getItem(), "seller"));

                tx.commit();
                em.close();
            } finally {
                TM.rollback();
            }
        }
    }

    @Test
    public void loadBidBidderItemSellerBids() throws Exception {
        FetchTestData testData = storeTestData();
        long BID_ID = testData.bids.getFirstId();
        PersistenceUtil persistenceUtil = Persistence.getPersistenceUtil();
        loadEventListener.reset();
        {
            UserTransaction tx = TM.getUserTransaction();
            try {
                tx.begin();
                EntityManager em = JPA.createEntityManager();

                Map<String, Object> properties = new HashMap<>();
                properties.put(
                    "javax.persistence.loadgraph",
                    em.getEntityGraph("BidBidderItemSellerBids")
                );

                Bid bid = em.find(Bid.class, BID_ID, properties);

                assertTrue(persistenceUtil.isLoaded(bid));
                assertTrue(persistenceUtil.isLoaded(bid, "amount"));
                assertTrue(persistenceUtil.isLoaded(bid, "bidder"));
                assertTrue(persistenceUtil.isLoaded(bid, "item"));
                assertTrue(persistenceUtil.isLoaded(bid.getItem(), "name"));
                assertTrue(persistenceUtil.isLoaded(bid.getItem(), "seller"));
                assertTrue(persistenceUtil.isLoaded(bid.getItem().getSeller(), "username"));
                assertTrue(persistenceUtil.isLoaded(bid.getItem(), "bids"));

                tx.commit();
                em.close();
            } finally {
                TM.rollback();
            }
        }
        {
            UserTransaction tx = TM.getUserTransaction();
            try {
                tx.begin();
                EntityManager em = JPA.createEntityManager();

                EntityGraph<Bid> bidGraph = em.createEntityGraph(Bid.class);
                bidGraph.addAttributeNodes(Bid_.bidder, Bid_.item);
                Subgraph<Item> itemGraph = bidGraph.addSubgraph(Bid_.item);
                itemGraph.addAttributeNodes(Item_.seller, Item_.bids);

                Map<String, Object> properties = new HashMap<>();
                properties.put("javax.persistence.loadgraph", bidGraph);

                Bid bid = em.find(Bid.class, BID_ID, properties);

                assertTrue(persistenceUtil.isLoaded(bid));
                assertTrue(persistenceUtil.isLoaded(bid, "amount"));
                assertTrue(persistenceUtil.isLoaded(bid, "bidder"));
                assertTrue(persistenceUtil.isLoaded(bid, "item"));
                assertTrue(persistenceUtil.isLoaded(bid.getItem(), "name"));
                assertTrue(persistenceUtil.isLoaded(bid.getItem(), "seller"));
                assertTrue(persistenceUtil.isLoaded(bid.getItem().getSeller(), "username"));
                assertTrue(persistenceUtil.isLoaded(bid.getItem(), "bids"));

                tx.commit();
                em.close();
            } finally {
                TM.rollback();
            }
        }
    }
}
