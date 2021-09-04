package org.jpwh.model.querying;

import org.jpwh.model.Constants;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@NamedQueries({
    @NamedQuery(
        name = "findItemById",
        query = "select i from Item i where i.id = :id"
    )
    ,
    @NamedQuery(
        name = "findItemByName",
        query = "select i from Item i where i.name like :name",
        hints = {
            @QueryHint(
                name = org.hibernate.annotations.QueryHints.TIMEOUT_JPA,
                value = "60000"),
            @QueryHint(
                name = org.hibernate.annotations.QueryHints.COMMENT,
                value = "Custom SQL comment")
        }
    )
})
@SqlResultSetMappings({
    @SqlResultSetMapping(
        name = "ItemResult",
        entities =
        @EntityResult(
            entityClass = Item.class,
            fields = {
                @FieldResult(name = "id", column = "ID"),
                @FieldResult(name = "name", column = "EXTENDED_NAME"),
                @FieldResult(name = "createdOn", column = "CREATEDON"),
                @FieldResult(name = "auctionEnd", column = "AUCTIONEND"),
                @FieldResult(name = "auctionType", column = "AUCTIONTYPE"),
                @FieldResult(name = "approved", column = "APPROVED"),
                @FieldResult(name = "buyNowPrice", column = "BUYNOWPRICE"),
                @FieldResult(name = "seller", column = "SELLER_ID")
            }
        )
    )
})
@Entity
public class Item {

    @Id
    @GeneratedValue(generator = Constants.ID_GENERATOR)
    protected Long id;

    @NotNull
    protected String name;

    @NotNull
    protected Date createdOn = new Date();

    @NotNull
    protected Date auctionEnd;

    @NotNull
    @Enumerated(EnumType.STRING)
    protected AuctionType auctionType = AuctionType.HIGHEST_BID;

    @NotNull
    protected boolean approved = true;

    protected BigDecimal buyNowPrice;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(foreignKey = @ForeignKey(name = "FK_ITEM_SELLER_ID"))
    protected User seller;

    @ManyToMany(mappedBy = "items")
    protected Set<Category> categories = new HashSet<>();

    @OneToMany(mappedBy = "item")
    protected Set<Bid> bids = new HashSet<>();

    @ElementCollection
    @JoinColumn(foreignKey = @ForeignKey(name = "FK_ITEM_IMAGES_ITEM_ID"))
    protected Set<Image> images = new HashSet<>();

    public Item() {
    }

    public Item(String name, Date auctionEnd, User seller) {
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

    public Date getCreatedOn() {
        return createdOn;
    }

    public Date getAuctionEnd() {
        return auctionEnd;
    }

    public void setAuctionEnd(Date auctionEnd) {
        this.auctionEnd = auctionEnd;
    }

    public AuctionType getAuctionType() {
        return auctionType;
    }

    public void setAuctionType(AuctionType auctionType) {
        this.auctionType = auctionType;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public BigDecimal getBuyNowPrice() {
        return buyNowPrice;
    }

    public void setBuyNowPrice(BigDecimal buyNowPrice) {
        this.buyNowPrice = buyNowPrice;
    }

    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
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