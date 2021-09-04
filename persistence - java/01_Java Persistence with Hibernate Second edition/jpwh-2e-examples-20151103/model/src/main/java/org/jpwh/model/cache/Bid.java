package org.jpwh.model.cache;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.jpwh.model.Constants;
import org.jpwh.model.bulkbatch.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@org.hibernate.annotations.Immutable
@Cacheable
@org.hibernate.annotations.Cache(
    usage = CacheConcurrencyStrategy.READ_ONLY
)
public class Bid {

    @Id
    @GeneratedValue(generator = Constants.ID_GENERATOR)
    protected Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    protected Item item;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    protected User bidder;

    @NotNull
    protected BigDecimal amount;

    protected Bid() {
    }

    public Bid(Item item, User bidder, BigDecimal amount) {
        this.item = item;
        this.amount = amount;
        this.bidder = bidder;
    }

    public Item getItem() {
        return item;
    }

    public User getBidder() {
        return bidder;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    // ...
}
