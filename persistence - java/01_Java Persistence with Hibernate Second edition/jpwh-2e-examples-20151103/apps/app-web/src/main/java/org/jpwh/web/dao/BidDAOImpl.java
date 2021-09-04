package org.jpwh.web.dao;

import org.jpwh.web.model.Bid;

import javax.inject.Inject;
import javax.persistence.EntityManager;

public class BidDAOImpl extends GenericDAOImpl<Bid, Long> implements BidDAO {

    @Inject
    public BidDAOImpl(EntityManager em) {
        super(em, Bid.class);
    }
}
