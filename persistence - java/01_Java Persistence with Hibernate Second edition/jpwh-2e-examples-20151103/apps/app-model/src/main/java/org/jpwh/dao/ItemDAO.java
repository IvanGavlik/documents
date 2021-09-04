package org.jpwh.dao;

import org.jpwh.model.Item;
import org.jpwh.model.ItemBidSummary;

import java.util.List;

public interface ItemDAO extends GenericDAO<Item, Long> {

    List<Item> findAll(boolean withBids);

    List<Item> findByName(String name, boolean fuzzy);

    List<ItemBidSummary> findItemBidSummaries();

}
