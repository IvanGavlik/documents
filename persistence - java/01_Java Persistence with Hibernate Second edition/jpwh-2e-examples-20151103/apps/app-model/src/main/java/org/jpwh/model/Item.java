package org.jpwh.model;

import org.jpwh.Constants;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Version;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
public class Item implements Serializable {

    @Id
    @GeneratedValue(generator = Constants.ID_GENERATOR)
    protected Long id;

    @NotNull
    @Version
    protected long version;

    @NotNull
    @Size(
       min = 2,
       max = 255,
       message = "Name is required, minimum 2, maximum 255 characters."
    )
    protected String name;

    @NotNull
    @Size(
       min = 10,
       max = 4000,
       message = "Description is required, minimum 10, maximum 4000 characters."
    )
    protected String description;

    @NotNull(message = "Auction end must be a future date and time.")
    @Future(message = "Auction end must be a future date and time.")
    protected Date auctionEnd;

    @OneToMany(mappedBy = "item")
    protected Set<Bid> bids = new HashSet<>();

    public Item() {
    }

    public Item(String name, String description, Date auctionEnd) {
        this.name = name;
        this.description = description;
        this.auctionEnd = auctionEnd;
    }

    public Long getId() { // Optional but useful
        return id;
    }

    public long getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getAuctionEnd() {
        return auctionEnd;
    }

    public void setAuctionEnd(Date auctionEnd) {
        this.auctionEnd = auctionEnd;
    }

    public Set<Bid> getBids() {
        return bids;
    }

    public void setBids(Set<Bid> bids) {
        this.bids = bids;
    }

    @Override
    public String toString() {
        return "ITEM ID: " + getId() + " NAME: " + getName();
    }

    public boolean isValidBid(Bid newBid) {
        Bid highestBid = getHighestBid();
        if (newBid == null)
            return false;
        if (newBid.getAmount().compareTo(new BigDecimal("0")) != 1)
            return false;
        if (highestBid == null)
            return true;
        if (newBid.getAmount().compareTo(highestBid.getAmount()) == 1)
            return true;
        return false;
    }

    public Bid getHighestBid() {
        return getBids().size() > 0
           ? getBidsHighestFirst().get(0) : null;
    }

    public List<Bid> getBidsHighestFirst() {
        List<Bid> list = new ArrayList<>(getBids());
        Collections.sort(list);
        return list;
    }

    public boolean isValidBid(Bid newBid,
                              Bid currentHighestBid,
                              Bid currentLowestBid) {
        throw new UnsupportedOperationException("Not implemented, example for a more flexible design");
    }

}