package org.jpwh.stateless;

import org.jpwh.dao.BidDAO;
import org.jpwh.dao.ItemDAO;
import org.jpwh.model.Bid;
import org.jpwh.model.InvalidBidException;
import org.jpwh.model.Item;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.util.List;

@javax.ejb.Stateless
@javax.ejb.Local(AuctionService.class)
@javax.ejb.Remote(RemoteAuctionService.class)
public class AuctionServiceImpl implements AuctionService {

    @Inject
    protected ItemDAO itemDAO;

    @Inject
    protected BidDAO bidDAO;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED) // Default
    public List<Item> getItems(boolean withBids) {
        return itemDAO.findAll(withBids);
    }

    @Override
    public Item storeItem(Item item) {
        return itemDAO.makePersistent(item);
    }

    @Override
    public Item placeBid(Bid bid) throws InvalidBidException {
        bid = bidDAO.makePersistent(bid);

        // Check that business rules are met
        if (!bid.getItem().isValidBid(bid))
            throw new InvalidBidException("Bid amount too low!");

        itemDAO.checkVersion(bid.getItem(), true);

        return bid.getItem();
    }
}