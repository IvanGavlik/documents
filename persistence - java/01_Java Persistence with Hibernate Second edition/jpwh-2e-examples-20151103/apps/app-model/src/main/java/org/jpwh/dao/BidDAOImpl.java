package org.jpwh.dao;

import org.jpwh.model.Bid;

import javax.ejb.Stateless;

@Stateless
public class BidDAOImpl extends GenericDAOImpl<Bid, Long>
    implements BidDAO {

    public BidDAOImpl() {
        super(Bid.class);
    }
}
