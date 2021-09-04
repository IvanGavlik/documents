package org.jpwh.model.customsql;

import org.jpwh.model.Constants;

import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@org.hibernate.annotations.Loader(
    namedQuery = "findItemByIdFetchBids"
)
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
    protected boolean active = true;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    protected Category category;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    protected User seller;


    @OneToMany(mappedBy = "item")
    protected Set<Bid> bids = new HashSet<>();



    @ElementCollection
    @org.hibernate.annotations.Loader(namedQuery = "loadImagesForItem")
    @org.hibernate.annotations.SQLInsert(
        sql = "insert into ITEM_IMAGES " +
              "(ITEM_ID, FILENAME, HEIGHT, WIDTH) " +
              "values (?, ?, ?, ?)"
    )
    @org.hibernate.annotations.SQLDelete(
        sql = "delete from ITEM_IMAGES " +
              "where ITEM_ID = ? and FILENAME = ? and HEIGHT = ? and WIDTH = ?"
    )
    @org.hibernate.annotations.SQLDeleteAll(
        sql = "delete from ITEM_IMAGES where ITEM_ID = ?"
    )
    protected Set<Image> images = new HashSet<Image>();


    public Item() {
    }

    public Item(Category category, String name, Date auctionEnd, User seller) {
        this.category = category;
        this.name = name;
        this.auctionEnd = auctionEnd;
        this.seller = seller;
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
    }

    public Set<Bid> getBids() {
        return bids;
    }

    public void setBids(Set<Bid> bids) {
        this.bids = bids;
    }

    public Set<Image> getImages() {
        return images;
    }

    public void setImages(Set<Image> images) {
        this.images = images;
    }
    // ...
}