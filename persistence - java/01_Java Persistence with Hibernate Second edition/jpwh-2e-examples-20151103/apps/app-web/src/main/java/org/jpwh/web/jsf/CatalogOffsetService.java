package org.jpwh.web.jsf;

import org.jpwh.web.dao.OffsetPage;
import org.jpwh.web.dao.Page;
import org.jpwh.web.model.Item_;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

@Named
@RequestScoped
public class CatalogOffsetService extends CatalogService<OffsetPage> {

    @Override
    protected OffsetPage createPage() {
        return new OffsetPage(
            3,
            itemDAO.getCount(),
            Item_.name, Page.SortDirection.ASC,
            Item_.name, Item_.auctionEnd, Item_.maxBidAmount);
    }
}
