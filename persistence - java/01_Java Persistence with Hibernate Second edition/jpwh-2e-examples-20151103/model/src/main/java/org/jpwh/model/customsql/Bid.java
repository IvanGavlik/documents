package org.jpwh.model.customsql;

import org.jpwh.model.Constants;

import javax.persistence.Entity;
import javax.persistence.EntityResult;
import javax.persistence.FetchType;
import javax.persistence.FieldResult;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.SqlResultSetMappings;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@NamedNativeQueries({
    @NamedNativeQuery(
        name = "findBidByIdFetchBidder",
        query =
            "select " +
                "b.ID as BID_ID, b.AMOUNT, b.ITEM_ID, b.BIDDER_ID, " +
                "u.ID as USER_ID, u.USERNAME, u.ACTIVATED " +
                "from BID b join USERS u on b.BIDDER_ID = u.ID " +
                "where b.ID = ?",
        resultSetMapping = "BidBidderResult"
    )
})
@SqlResultSetMappings({
    @SqlResultSetMapping(
        name = "BidBidderResult",
        entities = {
            @EntityResult(
                entityClass = Bid.class,
                fields = {
                    @FieldResult(name = "id", column = "BID_ID"),
                    @FieldResult(name = "amount", column = "AMOUNT"),
                    @FieldResult(name = "item", column = "ITEM_ID"),
                    @FieldResult(name = "bidder", column = "BIDDER_ID")
                }
            ),
            @EntityResult(
                entityClass = User.class,
                fields = {
                    @FieldResult(name = "id", column = "USER_ID"),
                    @FieldResult(name = "username", column = "USERNAME"),
                    @FieldResult(name = "activated", column = "ACTIVATED")
                }
            )
        }
    )
})
@org.hibernate.annotations.Loader(
    namedQuery = "findBidByIdFetchBidder"
)
@Entity
public class Bid {

    @Id
    @GeneratedValue(generator = Constants.ID_GENERATOR)
    protected Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    protected Item item;


    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    protected User bidder;


    @NotNull
    protected BigDecimal amount;

    public Bid() {
    }

    public Bid(Item item, User bidder, BigDecimal amount) {
        this.item = item;
        this.amount = amount;
        this.bidder = bidder;
    }

    public Long getId() {
        return id;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public User getBidder() {
        return bidder;
    }

    public void setBidder(User bidder) {
        this.bidder = bidder;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    // ...
}