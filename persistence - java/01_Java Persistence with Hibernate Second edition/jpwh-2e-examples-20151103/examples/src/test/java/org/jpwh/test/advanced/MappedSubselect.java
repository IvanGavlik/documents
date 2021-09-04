package org.jpwh.test.advanced;

import org.jpwh.env.JPATest;
import org.jpwh.model.advanced.Bid;
import org.jpwh.model.advanced.Item;
import org.jpwh.model.advanced.ItemBidSummary;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;

import static org.testng.Assert.assertEquals;

public class MappedSubselect extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("AdvancedPU");
    }

    @Test
    public void loadSubselectEntity() throws Exception {
        long ITEM_ID = storeItemAndBids();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            {
                ItemBidSummary itemBidSummary = em.find(ItemBidSummary.class, ITEM_ID);
                // select * from (
                //      select i.ID as ITEMID, i.ITEM_NAME as NAME, ...
                // ) where ITEMID = ?

                assertEquals(itemBidSummary.getName(), "AUCTION: Some item");
            }
            em.clear();

            { // Hibernate will synchronize the right tables before querying
                Item item = em.find(Item.class, ITEM_ID);
                item.setName("New name");

                // No flush before retrieval by identifier!
                // ItemBidSummary itemBidSummary = em.find(ItemBidSummary.class, ITEM_ID);

                // Automatic flush before queries if synchronized tables are affected!
                Query query = em.createQuery(
                    "select ibs from ItemBidSummary ibs where ibs.itemId = :id"
                );
                ItemBidSummary itemBidSummary =
                    (ItemBidSummary)query.setParameter("id", ITEM_ID).getSingleResult();

                assertEquals(itemBidSummary.getName(), "AUCTION: New name");
            }

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

    public Long storeItemAndBids() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        tx.begin();
        EntityManager em = JPA.createEntityManager();
        Item item = new Item();
        item.setName("Some item");
        item.setDescription("This is some description.");
        em.persist(item);
        for (int i = 1; i <= 3; i++) {
            Bid bid = new Bid();
            bid.setAmount(new BigDecimal(10 + i));
            bid.setItem(item);
            em.persist(bid);
        }
        tx.commit();
        em.close();
        return item.getId();
    }

}
