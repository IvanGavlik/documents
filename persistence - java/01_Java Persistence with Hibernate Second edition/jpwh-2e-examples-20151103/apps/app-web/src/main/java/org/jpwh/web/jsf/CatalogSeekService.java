package org.jpwh.web.jsf;

import org.jpwh.web.dao.Page;
import org.jpwh.web.dao.SeekPage;
import org.jpwh.web.model.Item_;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

@Named
@RequestScoped
public class CatalogSeekService extends CatalogService<SeekPage> {

    @Override
    protected SeekPage createPage() {
        return new SeekPage(
            3,
            itemDAO.getCount(),
            Item_.name, Page.SortDirection.ASC,
            Item_.id,
            Item_.name, Item_.auctionEnd, Item_.maxBidAmount);
    }
}
