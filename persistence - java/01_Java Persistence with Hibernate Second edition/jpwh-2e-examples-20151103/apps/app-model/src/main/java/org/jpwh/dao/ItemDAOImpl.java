package org.jpwh.dao;

import org.jpwh.model.Bid;
import org.jpwh.model.Item;
import org.jpwh.model.ItemBidSummary;

import javax.ejb.Stateless;
import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.util.List;

@Stateless
public class ItemDAOImpl extends GenericDAOImpl<Item, Long>
    implements ItemDAO {

    public ItemDAOImpl() {
        super(Item.class);
    }

    @Override
    public List<Item> findAll(boolean withBids) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Item> criteria = cb.createQuery(Item.class);
        Root<Item> i = criteria.from(Item.class);
        criteria.select(i)
            .distinct(true) // In-memory "distinct"!
            .orderBy(cb.asc(i.get("auctionEnd")));
        if (withBids)
            i.fetch("bids", JoinType.LEFT);
        return em.createQuery(criteria).getResultList();
    }

    @Override
    public List<Item> findByName(String name, boolean substring) {
        return em.createNamedQuery(
            substring ? "getItemsByNameSubstring" : "getItemsByName"
        ).setParameter(
            "itemName",
            substring ? ("%" + name + "%") : name
        ).getResultList();
    }

    @Override
    public List<ItemBidSummary> findItemBidSummaries() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<ItemBidSummary> criteria =
            cb.createQuery(ItemBidSummary.class);
        Root<Item> i = criteria.from(Item.class);
        Join<Item, Bid> b = i.join("bids", JoinType.LEFT);
        criteria.select(
            cb.construct(
                ItemBidSummary.class,
                i.get("id"), i.get("name"), i.get("auctionEnd"),
                cb.max(b.<BigDecimal>get("amount"))
            )
        );
        criteria.orderBy(cb.asc(i.get("auctionEnd")));
        criteria.groupBy(i.get("id"), i.get("name"), i.get("auctionEnd"));
        return em.createQuery(criteria).getResultList();
    }
}
