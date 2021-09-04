package org.jpwh.stateful;

import org.jpwh.model.ItemBidSummary;

import java.util.List;

public interface RemoteAuctionService {
    List<ItemBidSummary> getSummaries();
}
