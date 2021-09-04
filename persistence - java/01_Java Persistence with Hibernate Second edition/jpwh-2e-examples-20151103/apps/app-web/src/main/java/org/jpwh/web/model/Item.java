package org.jpwh.web.model;

import org.jpwh.Constants;
import org.jpwh.web.jaxrs.EntityReferenceAdapter;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.FetchType.LAZY;

@Entity
/* 
    An <code>Item</code> instance maps to an <code>&lt;item></code> XML element.
    This annotation effectively enables JAXB on the class.
 */
@XmlRootElement
/* 
    When serializing or deserializing an instance, JAXB should call the
    fields directly and not the getter or setter methods. The reasoning
    behind this is the same as for JPA, freedom in method design.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Item implements Serializable {

    @Id
    @GeneratedValue(generator = Constants.ID_GENERATOR)
    @XmlAttribute
    protected Long id;

    @NotNull
    @Version
    protected long version;

    @NotNull
    @Size(min = 2, max = 255)
    protected String name;

    @NotNull
    @Size(min = 10, max = 4000)
    protected String description;

    @NotNull
    @DecimalMin("0")
    protected BigDecimal initialPrice = new BigDecimal(0);

    @NotNull
    @Future(message = "{Item.auctionEnd.Future}")
    @XmlAttribute
    protected Date auctionEnd;

    @NotNull
    @ManyToOne(fetch = LAZY)
    @XmlJavaTypeAdapter(EntityReferenceAdapter.class)
    protected User seller;

    @OneToMany(mappedBy = "item", cascade = MERGE)
    @XmlTransient
    protected Set<Image> images = new HashSet<>();

    @OneToMany(mappedBy = "item")
    @XmlElementWrapper(name = "bids")
    @XmlElement(name = "bid")
    protected Set<Bid> bids = new HashSet<>();

    @org.hibernate.annotations.Formula(
        "coalesce((select max(b.AMOUNT) from BID b where b.ITEM_ID = ID), 0)"
    )
    protected BigDecimal maxBidAmount;

    public Item() {
    }

    public Long getId() { // Optional but useful
        return id;
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

    public BigDecimal getInitialPrice() {
        return initialPrice;
    }

    public void setInitialPrice(BigDecimal initialPrice) {
        this.initialPrice = initialPrice;
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

    public Set<Bid> getBids() {
        return bids;
    }

    public Set<Image> getImages() {
        return images;
    }

    public void setImages(Set<Image> images) {
        this.images = images;
    }

    public List<Image> getImagesSorted() {
        List<Image> list = new ArrayList<>(getImages());
        Collections.sort(list);
        return list;
    }

    public boolean isValidBidAmount(BigDecimal highestBidAmount, BigDecimal newBidAmount) {
        return newBidAmount != null && newBidAmount.compareTo(getInitialPrice()) == 1
            && (highestBidAmount == null || newBidAmount.compareTo(highestBidAmount) == 1);
    }

    // ...
}
