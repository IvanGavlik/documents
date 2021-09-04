package org.jpwh.web.dao;

import org.jpwh.web.model.*;

import java.math.BigDecimal;
import java.util.List;

public interface ItemDAO extends GenericDAO<Item, Long> {

    List<ItemBidSummary> getItemBidSummaries(Page page);

    BigDecimal getMaxBidAmount(Item item);

}
