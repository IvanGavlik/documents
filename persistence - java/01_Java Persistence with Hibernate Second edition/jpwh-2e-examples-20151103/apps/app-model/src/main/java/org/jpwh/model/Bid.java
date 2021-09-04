package org.jpwh.model;

import org.jpwh.Constants;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import static javax.persistence.CascadeType.MERGE;

@Entity
@org.hibernate.annotations.Immutable
public class Bid implements Serializable, Comparable<Bid> {

    @Id
    @GeneratedValue(generator = Constants.ID_GENERATOR)
    protected Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY, cascade = MERGE)
    protected Item item;

    @NotNull
    @Column(updatable = false)
    protected BigDecimal amount;

    @NotNull
    @Column(updatable = false)
    protected Date createdOn = new Date();

    public Bid() {
    }

    public Bid(BigDecimal amount, Item item) {
        this.amount = amount;
        this.item = item;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Date createdOn) {
        this.createdOn = createdOn;
    }

    @Override
    public int compareTo(Bid that) {
        if (this.getAmount() == null || that.getAmount() == null) return 0;
        return that.getAmount().compareTo(this.getAmount());
    }
}