package org.jpwh.test.fetching;

import org.jpwh.env.JPATest;
import org.jpwh.model.fetching.nplusoneselects.Bid;
import org.jpwh.model.fetching.nplusoneselects.Item;
import org.jpwh.model.fetching.nplusoneselects.User;
import org.jpwh.shared.util.CalendarUtil;
import org.jpwh.shared.util.TestData;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;
import java.util.List;

import static org.testng.Assert.*;

public class EagerQuery extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("FetchingNPlusOneSelectsPU");
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

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            {
                EntityManager em = JPA.createEntityManager();
                List<Item> items =
                    em.createQuery("select i from Item i join fetch i.seller")
                        .getResultList();
                // select i.*, u.*
                //  from ITEM i
                //   inner join USERS u on u.ID = i.SELLER_ID
                //  where i.ID = ?

                em.close(); // Detach all

                for (Item item : items) {
                    assertNotNull(item.getSeller().getUsername());
                }
            }
            {
                EntityManager em = JPA.createEntityManager();
                CriteriaBuilder cb = em.getCriteriaBuilder();
                CriteriaQuery criteria = cb.createQuery();

                Root<Item> i = criteria.from(Item.class);
                i.fetch("seller");
                criteria.select(i);

                List<Item> items = em.createQuery(criteria).getResultList();

                em.close(); // Detach all

                for (Item item : items) {
                    assertNotNull(item.getSeller().getUsername());
                }
            }
        } finally {
            TM.rollback();
        }
    }

    @Test
    public void fetchBids() throws Exception {
        storeTestData();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();

            {
                EntityManager em = JPA.createEntityManager();
                List<Item> items =
                    em.createQuery("select i from Item i left join fetch i.bids")
                        .getResultList();
                // select i.*, b.*
                //  from ITEM i
                //   left outer join BID b on b.ITEM_ID = i.ID
                //  where i.ID = ?

                em.close(); // Detach all

                for (Item item : items) {
                    assertTrue(item.getBids().size() > 0);
                }
            }
            {
                EntityManager em = JPA.createEntityManager();
                CriteriaBuilder cb = em.getCriteriaBuilder();
                CriteriaQuery criteria = cb.createQuery();

                Root<Item> i = criteria.from(Item.class);
                i.fetch("bids", JoinType.LEFT);
                criteria.select(i);

                List<Item> items = em.createQuery(criteria).getResultList();

                em.close(); // Detach all

                for (Item item : items) {
                    assertTrue(item.getBids().size() > 0);
                }
            }
        } finally {
            TM.rollback();
        }
    }

}
