package org.jpwh.stateless;

import org.jpwh.model.Bid;
import org.jpwh.model.InvalidBidException;
import org.jpwh.model.Item;

import java.util.List;

public interface RemoteAuctionService {

    List<Item> getItems(boolean withBids);

    Item storeItem(Item item);

    Item placeBid(Bid bid) throws InvalidBidException;
}
