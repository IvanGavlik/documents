package org.jpwh.model.concurrency.version;

import org.jpwh.model.Constants;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
public class Bid {

    @Id
    @GeneratedValue(generator = Constants.ID_GENERATOR)
    protected Long id;

    @NotNull
    protected BigDecimal amount;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    protected Item item;

    protected Bid() {
    }

    public Bid(BigDecimal amount, Item item) {
        this.amount = amount;
        this.item = item;
    }

    public Bid(BigDecimal amount, Item item, Bid lastBid) throws InvalidBidException {
        if (lastBid != null && amount.compareTo(lastBid.getAmount()) < 1) {
            throw new InvalidBidException(
                "Bid amount '" + amount +" too low, last bid was: " + lastBid.getAmount()
            );
        }
        this.amount = amount;
        this.item = item;
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Item getItem() {
        return item;
    }
}