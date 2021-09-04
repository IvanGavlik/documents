package org.jpwh.stateful;

import org.jpwh.dao.ItemDAO;
import org.jpwh.model.ItemBidSummary;

import javax.inject.Inject;
import java.util.List;

@javax.ejb.Stateless
@javax.ejb.Local(AuctionService.class)
@javax.ejb.Remote(RemoteAuctionService.class)
public class AuctionServiceImpl implements AuctionService {

    @Inject
    protected ItemDAO itemDAO;

    @Override
    public List<ItemBidSummary> getSummaries() {
        return itemDAO.findItemBidSummaries();
    }
}
