package org.jpwh.web.jsf;

import org.jpwh.web.dao.ItemDAO;
import org.jpwh.web.dao.Page;
import org.jpwh.web.model.ItemBidSummary;

import javax.inject.Inject;
import java.util.List;

public abstract class CatalogService<P extends Page> {

    @Inject
    ItemDAO itemDAO;

    List<ItemBidSummary> itemBidSummaries;
    P page;

    public List<ItemBidSummary> getItemBidSummaries() {
        if (itemBidSummaries == null)
            itemBidSummaries =
                itemDAO.getItemBidSummaries(getPage());
        return itemBidSummaries;
    }

    public P getPage() {
        if (page == null)
            page = createPage();
        return page;
    }

    abstract protected P createPage();
}
