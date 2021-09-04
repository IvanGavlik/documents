package org.jpwh.test.querying.advanced;

import org.hibernate.Session;
import org.jpwh.model.querying.Bid;
import org.jpwh.model.querying.Item;
import org.jpwh.model.querying.User;
import org.jpwh.test.querying.QueryingTest;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class FilterCollections extends QueryingTest {

    @Test
    public void executeQueries() throws Exception {
        TestDataCategoriesItems testData = storeTestData();

        Long ITEM_ID = testData.items.getFirstId();
        Long USER_ID = testData.users.getLastId();


        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();
            Session session = em.unwrap(Session.class);

            {
                // Filter the bids by bidder, order by amount descending
                Item item = em.find(Item.class, ITEM_ID);
                User user = em.find(User.class, USER_ID);

                org.hibernate.Query query = session.createFilter(
                    item.getBids(),
                    "where this.bidder = :bidder order by this.amount desc"
                );

                query.setParameter("bidder", user);
                List<Bid> bids = query.list();

                assertEquals(bids.size(), 3);
                assertEquals(bids.get(0).getBidder(), user);
                assertEquals(bids.get(0).getAmount().compareTo(new BigDecimal("101")), 0);
                assertEquals(bids.get(1).getAmount().compareTo(new BigDecimal("100")), 0);
                assertEquals(bids.get(2).getAmount().compareTo(new BigDecimal("99")), 0);
            }
            em.clear();

            {
                // Retrieve a page of bids
                Item item = em.find(Item.class, ITEM_ID);

                org.hibernate.Query query = session.createFilter(
                    item.getBids(),
                    ""
                );

                // Retrieve only two bids
                query.setFirstResult(0);
                query.setMaxResults(2);
                List<Bid> bids = query.list();

                assertEquals(bids.size(), 2);
            }
            em.clear();

            {
                // Retrieve items sold by bidders on this item
                Item item = em.find(Item.class, ITEM_ID);

                org.hibernate.Query query = session.createFilter(
                    item.getBids(),
                    "from Item i where i.seller = this.bidder"
                );

                List<Item> items = query.list();

                assertEquals(items.size(), 0);
            }
            em.clear();


            {
                // Retrieve users who have bid on the item
                Item item = em.find(Item.class, ITEM_ID);

                org.hibernate.Query query = session.createFilter(
                    item.getBids(),
                    "select distinct this.bidder.username order by this.bidder.username asc"
                );

                List<String> bidders = query.list();

                assertEquals(bidders.size(), 1);
                assertEquals(bidders.get(0), "robertdoe");
            }
            em.clear();

            {
                // Limit the bids to greater or equal than 100
                Item item = em.find(Item.class, ITEM_ID);

                org.hibernate.Query query = session.createFilter(
                    item.getBids(),
                    "where this.amount >= :param"
                );

                query.setParameter("param", new BigDecimal(100));
                List<Bid> bids = query.list();

                assertEquals(bids.size(), 2);
            }
            em.clear();

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

}
