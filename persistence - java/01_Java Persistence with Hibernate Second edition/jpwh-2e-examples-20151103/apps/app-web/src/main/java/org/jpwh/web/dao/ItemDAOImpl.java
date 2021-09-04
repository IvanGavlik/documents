package org.jpwh.web.dao;

import org.jpwh.web.model.Item;
import org.jpwh.web.model.ItemBidSummary;
import org.jpwh.web.model.Item_;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.math.BigDecimal;
import java.util.List;

public class ItemDAOImpl
    extends GenericDAOImpl<Item, Long>
    implements ItemDAO {

    @Inject
    public ItemDAOImpl(EntityManager em) {
        super(em, Item.class);
    }

    @Override
    public List<ItemBidSummary> getItemBidSummaries(Page page) {

        /* 
            This is a regular criteria query you have seen many times before.
         */
        CriteriaBuilder cb =
            getEntityManager().getCriteriaBuilder();

        CriteriaQuery<ItemBidSummary> criteria =
            cb.createQuery(ItemBidSummary.class);

        Root<Item> i = criteria.from(Item.class);

        // Some query details...
        criteria.select(cb.construct(
            ItemBidSummary.class,
            i.get(Item_.id),
            i.get(Item_.name),
            i.get(Item_.auctionEnd),
            i.get(Item_.maxBidAmount)
        ));

        /* 
            Delegate finishing the query to the given <code>Page</code>.
         */
        TypedQuery<ItemBidSummary> query =
            page.createQuery(em, criteria, i);

        return query.getResultList();
    }

    @Override
    public BigDecimal getMaxBidAmount(Item item) {
        return (BigDecimal) getEntityManager().createNamedQuery("getMaxBidAmount")
            .setParameter("item", item)
            .getSingleResult();
    }
}
