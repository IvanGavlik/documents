package org.jpwh.stateful;

import org.jpwh.model.InvalidBidException;

import java.math.BigDecimal;

public interface RemoteItemService {

    void startConversation(Long itemId);

    void setItemName(String newName);

    void placeBid(BigDecimal amount) throws InvalidBidException;

    void commitConversation();
}
