package org.jpwh.model.querying;

import java.util.Date;

public class ItemSummary {

    protected Long itemId;

    protected String name;

    protected Date auctionEnd;

    public ItemSummary() {
    }

    public ItemSummary(Long itemId, String name, Date auctionEnd) {
        this.itemId = itemId;
        this.name = name;
        this.auctionEnd = auctionEnd;
    }

    public Long getItemId() {
        return itemId;
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

    // ...
}