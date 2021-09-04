package org.jpwh.web.jsf;

import org.jpwh.web.dao.BidDAO;
import org.jpwh.web.dao.ItemDAO;
import org.jpwh.web.model.Bid;
import org.jpwh.web.model.Item;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.math.BigDecimal;

@Named
/* 
    We don't need to hold state across requests for this use case. A
    service instance will be created when the auction page view is
    rendered with a <code>GET</code> request, and JSF will bind the
    request parameter by calling <code>setId()</code>. The service
    instance is destroyed after rendering is complete. Your server
    does not hold any state between requests. When the auction form
    is submitted and processing of that <code>POST</code> request
    starts, JSF will call <code>setId()</code> to bind the hidden
    form field, and you can again initialize the state of the service.
 */
@RequestScoped
public class AuctionService {

    @Inject
    ItemDAO itemDAO;

    @Inject
    BidDAO bidDAO;

    /* 
        The state you hold for each request is the identifier value of
        the <code>Item</code> the user is working on, the actual
        <code>Item</code> after it was loaded, the currently highest
        bid amount for that item, and the new bid amount entered by
        the user.
     */
    long id;
    Item item;
    BigDecimal highestBidAmount;
    BigDecimal newBidAmount;

    public void setId(long id) {
        this.id = id;
        if (item == null) {
            item = itemDAO.findById(id);
            if (item == null)
                throw new EntityNotFoundException();
            highestBidAmount = itemDAO.getMaxBidAmount(item);
        }
    }

    // Other plain getters and setters...
    public long getId() {
        return id;
    }

    public Item getItem() {
        return item;
    }

    public BigDecimal getNewBidAmount() {
        return newBidAmount;
    }

    public void setNewBidAmount(BigDecimal newBidAmount) {
        this.newBidAmount = newBidAmount;
    }

    public BigDecimal getHighestBidAmount() {
        return highestBidAmount;
    }

    /* 
        The <code>@Transactional</code> annotation is new in Java EE 7 (from JTA 1.2)
        and similar to <code>@TransactionAttribute</code> on EJB components. Internally,
        an interceptor will wrap the method call in a system transaction context, just
        like an EJB method.
     */
    @Transactional
    public String placeBid() {
        /* 
            In this method, you want to perform transactional work and store a new bid
            in the database, and prevent concurrent bids. You must join the persistence
            context with the transaction. It doesn't actually matter which DAO you
            call, as all of them share the same request-scoped
            <code>EntityManager</code>.
         */
        itemDAO.joinTransaction();

        /* 
            If another transaction is committed for a higher bid, in the time our user was
            thinking and looking at the rendered auction page, fail and re-render
            the auction page with a message.
         */
        if (!getItem().isValidBidAmount(
                getHighestBidAmount(),
                getNewBidAmount()
            )) {
            ValidationMessages.addFacesMessage("Auction.bid.TooLow");
            return null;
        }

        /* 
            You must force a version increment of the <code>Item</code> at flush
            time to prevent concurrent bids. If another transaction runs at the
            same time as this, and loads the same <code>Item</code> version
            and current highest bid from the database in <code>setId()</code>,
            one of the transactions must fail in <code>placeBid()</code>.
         */
        itemDAO.checkVersion(getItem(), true);

        bidDAO.makePersistent(new Bid(getNewBidAmount(), getItem()));

        /* 
            This is a simple redirect-after-POST in JSF, so users can safely reload
            the page after submitting a bid.
         */
        return "auction?id=" + getId() + "&faces-redirect=true";
    }

}
