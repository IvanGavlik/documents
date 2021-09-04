package org.jpwh.test.service;

import org.jpwh.web.dao.ItemDAO;
import org.jpwh.web.dao.OffsetPage;
import org.jpwh.web.dao.SeekPage;
import org.jpwh.web.model.Item;
import org.jpwh.web.model.ItemBidSummary;
import org.jpwh.web.model.Item_;
import org.testng.annotations.Test;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import java.util.List;

import static org.jpwh.web.dao.Page.SortDirection.ASC;
import static org.jpwh.web.dao.Page.SortDirection.DESC;
import static org.testng.Assert.*;

public class PagingTest extends IntegrationTest {

    @Inject
    ItemDAO itemDAO;

    @Inject
    EntityManager em;

    @Test
    public void offset() {

        OffsetPage page = new OffsetPage(
            /* 
                The page size.
             */
            3,
            /* 
                The total number of records available.
             */
            itemDAO.getCount(),
            /* 
                The default sort attribute and sort direction.
             */
            Item_.name, ASC,
            /* 
                All attributes allowed as sort attributes for this page.
             */
            Item_.name, Item_.auctionEnd, Item_.maxBidAmount
        );

        assertEquals(page.getSize(), 3l);
        assertEquals(page.getTotalRecords(), 7l);
        assertEquals(page.getSortAttribute(), Item_.name);
        assertEquals(page.getSortDirection(), ASC);
        assertEquals(page.getAllowedAttributes().length, 3);
        assertTrue(page.isSortedAscending());
        assertTrue(page.isMoreThanOneAvailable());
        assertFalse(page.isPreviousAvailable());
        assertTrue(page.isNextAvailable());
        assertEquals(page.getRangeStart(), 0l);
        assertEquals(page.getRangeEnd(), 2l);

        List<ItemBidSummary> result = itemDAO.getItemBidSummaries(page);
        assertEquals(result.size(), 3);

        ItemBidSummary summary;

        summary = result.get(0);
        assertEquals(summary.getName(), "Aquarium");
        assertEquals(summary.getHighestBid().toString(), "0");

        summary = result.get(1);
        assertEquals(summary.getName(), "Baseball Glove");
        assertEquals(summary.getHighestBid().toString(), "13.00");

        summary = result.get(2);
        assertEquals(summary.getName(), "Coffee Machine");
        // Can't tell which coffee machine this is!

        page.setCurrent(2);
        result = itemDAO.getItemBidSummaries(page);

        assertTrue(page.isSortedAscending());
        assertTrue(page.isMoreThanOneAvailable());
        assertTrue(page.isPreviousAvailable());
        assertTrue(page.isNextAvailable());

        assertEquals(result.size(), 3);

        summary = result.get(0);
        assertEquals(summary.getName(), "Coffee Machine");
        // Can't tell which coffee machine this is!

        summary = result.get(1);
        assertEquals(summary.getName(), "Golf GTI");
        assertEquals(summary.getHighestBid().toString(), "0");
    }

    @Test
    public void offsetDescending() {

        OffsetPage page = new OffsetPage(
            3,
            itemDAO.getCount(),
            Item_.auctionEnd, DESC,
            Item_.name, Item_.auctionEnd, Item_.maxBidAmount
        );

        assertEquals(page.getSize(), 3l);
        assertEquals(page.getTotalRecords(), 7l);
        assertEquals(page.getSortAttribute(), Item_.auctionEnd);
        assertEquals(page.getSortDirection(), DESC);
        assertEquals(page.getAllowedAttributes().length, 3);
        assertFalse(page.isSortedAscending());

        List<ItemBidSummary> result = itemDAO.getItemBidSummaries(page);
        assertEquals(result.size(), 3);

        ItemBidSummary summary;

        summary = result.get(0);
        assertEquals(summary.getName(), "Golf GTI");
        assertEquals(summary.getHighestBid().toString(), "0");

        summary = result.get(1);
        assertEquals(summary.getName(), "Coffee Machine");
        assertEquals(summary.getHighestBid().toString(), "0");

        summary = result.get(2);
        assertEquals(summary.getName(), "Coffee Machine");
        assertEquals(summary.getHighestBid().toString(), "6.00");

        page.setCurrent(2);

        assertFalse(page.isSortedAscending());
        assertTrue(page.isMoreThanOneAvailable());
        assertTrue(page.isPreviousAvailable());
        assertTrue(page.isNextAvailable());

        result = itemDAO.getItemBidSummaries(page);
        assertEquals(result.size(), 3);

        summary = result.get(0);
        assertEquals(summary.getName(), "Snowboard");
        assertEquals(summary.getHighestBid().toString(), "0");
    }

    @Test
    public void seek() {

        SeekPage page = new SeekPage(
            /* 
                The page size.
             */
            3,
            /* 
                The total number of records available.
             */
            itemDAO.getCount(),
            /* 
                The default sort attribute and sort direction.
             */
            Item_.name, ASC,
            /* 
                The additional unique key attribute for ordering and seeking.
             */
            Item_.id,
            /* 
                All attributes allowed as sort attributes for this page.
             */
            Item_.name, Item_.auctionEnd, Item_.maxBidAmount
        );

        assertEquals(page.getSize(), 3l);
        assertEquals(page.getTotalRecords(), 7l);
        assertEquals(page.getSortAttribute(), Item_.name);
        assertEquals(page.getSortDirection(), ASC);
        assertEquals(page.getAllowedAttributes().length, 3);

        assertTrue(page.isSortedAscending());
        assertTrue(page.isMoreThanOneAvailable());

        List<ItemBidSummary> result = itemDAO.getItemBidSummaries(page);
        assertEquals(result.size(), 3);

        ItemBidSummary summary;

        summary = result.get(0);
        assertEquals(summary.getName(), "Aquarium");
        assertEquals(summary.getHighestBid().toString(), "0");

        summary = result.get(1);
        assertEquals(summary.getName(), "Baseball Glove");
        assertEquals(summary.getHighestBid().toString(), "13.00");

        summary = result.get(2);
        assertEquals(summary.getName(), "Coffee Machine");
        assertEquals(summary.getHighestBid().toString(), "6.00");

        ItemBidSummary lastShownOnPreviousPage = // ...
            summary;

        page.setLastValue(lastShownOnPreviousPage.getName());
        page.setLastUniqueValue(lastShownOnPreviousPage.getItemId());

        result = itemDAO.getItemBidSummaries(page);

        assertEquals(result.size(), 3);

        summary = result.get(0);
        assertEquals(summary.getName(), "Coffee Machine");
        assertEquals(summary.getHighestBid().toString(), "0");

        summary = result.get(1);
        assertEquals(summary.getName(), "Golf GTI");
        assertEquals(summary.getHighestBid().toString(), "0");
    }

    @Test
    public void seekDescending() {

        SeekPage page = new SeekPage(
            3,
            itemDAO.getCount(),
            Item_.auctionEnd, DESC,
            Item_.id,
            Item_.name, Item_.auctionEnd, Item_.maxBidAmount
        );

        assertEquals(page.getSize(), 3l);
        assertEquals(page.getTotalRecords(), 7l);
        assertEquals(page.getSortAttribute(), Item_.auctionEnd);
        assertEquals(page.getSortDirection(), DESC);
        assertEquals(page.getAllowedAttributes().length, 3);
        assertFalse(page.isSortedAscending());

        List<ItemBidSummary> result = itemDAO.getItemBidSummaries(page);
        assertEquals(result.size(), 3);

        ItemBidSummary summary;


        summary = result.get(0);
        assertEquals(summary.getName(), "Golf GTI");
        assertEquals(summary.getHighestBid().toString(), "0");

        summary = result.get(1);
        assertEquals(summary.getName(), "Coffee Machine");
        assertEquals(summary.getHighestBid().toString(), "0");

        summary = result.get(2);
        assertEquals(summary.getName(), "Coffee Machine");
        assertEquals(summary.getHighestBid().toString(), "6.00");

        ItemBidSummary lastShownOnPreviousPage = // ...
            summary;

        page.setLastValue(lastShownOnPreviousPage.getAuctionEnd());
        page.setLastUniqueValue(lastShownOnPreviousPage.getItemId());

        result = itemDAO.getItemBidSummaries(page);
        assertEquals(result.size(), 3);

        summary = result.get(0);
        assertEquals(summary.getName(), "Snowboard");
        assertEquals(summary.getHighestBid().toString(), "0");
    }

    @Test
    public void calculateSeekPageBoundaries() {
        CriteriaBuilder cb =
            em.getCriteriaBuilder();
        CriteriaQuery criteria = cb.createQuery();

        Root<Item> i = criteria.from(Item.class);

        Subquery<Long> sq = criteria.subquery(Long.class);
        Root<Item> i2 = sq.from(Item.class);
        sq.select(cb.count(i2));
        sq.where(
            cb.and(
                // Switch to greaterThanOrEqualTo for DESC sorting
                cb.lessThanOrEqualTo(
                    i2.get(Item_.name),
                    i.get(Item_.name)
                ),
                cb.or(
                    cb.notEqual(
                        i2.get(Item_.name),
                        i.get(Item_.name)
                    ),
                    // Switch to greaterThanOrEqualTo for DESC sorting
                    cb.lessThanOrEqualTo(
                        i2.get(Item_.id),
                        i.get(Item_.id)
                    )
                )
            )
        );

        criteria.multiselect(
            i.get(Item_.name),
            i.get(Item_.id)
        );

        criteria.where(
            cb.equal(cb.mod(cb.toInteger(sq), 3), 0)
        );

        // ASC sorting
        criteria.orderBy(
            cb.asc(i.get(Item_.name)),
            cb.asc(i.get(Item_.id))
        );

        List<Object[]> result = em.createQuery(criteria).getResultList();

        for (Object[] tuple : result) {
            String name = (String) tuple[0];
            Long itemId = (Long) tuple[1];
            // ...
        }
    }

}
