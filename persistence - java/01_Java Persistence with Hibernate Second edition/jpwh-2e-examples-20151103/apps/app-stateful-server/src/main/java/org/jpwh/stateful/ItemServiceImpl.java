package org.jpwh.stateful;

import org.jpwh.dao.BidDAO;
import org.jpwh.dao.ItemDAO;
import org.jpwh.model.Bid;
import org.jpwh.model.InvalidBidException;
import org.jpwh.model.Item;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;

import static javax.persistence.LockModeType.OPTIMISTIC_FORCE_INCREMENT;
import static javax.persistence.PersistenceContextType.EXTENDED;
import static javax.persistence.SynchronizationType.UNSYNCHRONIZED;

@javax.ejb.Stateful(passivationCapable = false)
@javax.ejb.StatefulTimeout(10) // Minutes
@javax.ejb.Local(ItemService.class)
@javax.ejb.Remote(RemoteItemService.class)
public class ItemServiceImpl implements ItemService {

    @PersistenceContext(type = EXTENDED, synchronization = UNSYNCHRONIZED)
    protected EntityManager em;

    @Inject
    protected ItemDAO itemDAO;

    @Inject
    protected BidDAO bidDAO;

    // Server-side conversational state
    protected Item item;
    protected boolean isItemLockRequired;

    @Override
    public void startConversation(Long itemId) {
        item = itemDAO.findById(itemId);
        if (item == null)
            throw new EntityNotFoundException(
                "No Item found with identifier: " + itemId
            );
    }

    @Override
    public void setItemName(String newName) {
        item.setName(newName);
        // Persistence context will NOT be flushed when the transaction
        // of this method commits, it's unsynchronized!
    }

    @Override
    public void placeBid(BigDecimal amount) throws InvalidBidException {
        Bid bid = new Bid(amount, item);

        // Check that business rules are met
        if (!bid.getItem().isValidBid(bid))
            throw new InvalidBidException("Bid amount too low!");

        // The item's version MUST be checked at flush time
        isItemLockRequired = true;

        item.getBids().add(bid);
        bidDAO.makePersistent(bid);
        // Persistence context will NOT be flushed when the transaction
        // of this method commits, it's unsynchronized!
    }

    @Override
    @javax.ejb.Remove // The bean will be removed after this method completes
    public void commitConversation() {
        em.joinTransaction();
        // Persistence context is joined with the current transaction and will
        // be flushed when this method returns, saving the changes

        // Now the EM is joined with the transaction, so we can lock
        if (isItemLockRequired)
            em.lock(item, OPTIMISTIC_FORCE_INCREMENT);
    }
}
