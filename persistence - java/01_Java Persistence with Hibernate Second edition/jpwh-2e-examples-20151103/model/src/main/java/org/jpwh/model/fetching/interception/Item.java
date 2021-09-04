package org.jpwh.model.fetching.interception;

import org.hibernate.validator.constraints.Length;
import org.jpwh.model.Constants;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
public class Item {

    @Id
    @GeneratedValue(generator = Constants.ID_GENERATOR)
    protected Long id;

    @NotNull
    protected String name;

    @NotNull
    protected Date auctionEnd;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY) // Has no effect, no User proxy
    @org.hibernate.annotations.LazyToOne( // Requires bytecode enhancement!
       org.hibernate.annotations.LazyToOneOption.NO_PROXY
    )
    protected User seller;

    @NotNull
    @Length(min = 0, max = 4000)
    @Basic(fetch = FetchType.LAZY)
    protected String description;

    public Item() {
    }

    public Item(String name, Date auctionEnd, User seller, String description) {
        this.name = name;
        this.auctionEnd = auctionEnd;
        this.seller = seller;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getAuctionEnd() {
        return auctionEnd;
    }

    public void setAuctionEnd(Date auctionEnd) {
        this.auctionEnd = auctionEnd;
    }

    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // ...
}
