package org.jpwh.web.jaxrs;

import org.jpwh.web.dao.ItemDAO;
import org.jpwh.web.model.Image;
import org.jpwh.web.model.Item;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import java.util.List;

import static javax.ws.rs.core.MediaType.APPLICATION_XML;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

/* 
    When the server receives a request with the request path <code>/item</code>, the method on this service handles it.
    By default, the service instance is request-scoped, but you can apply CDI scoping annotations to change that.
 */
@Path("/item")
public class ItemService {

    @Inject
    ItemDAO itemDAO;

    /* 
        An HTTP <code>GET</code> request maps to this method.
     */
    @GET
    /* 
        The container uses the path segment after <code>/item</code>
        as an argument value for the call, e.g. <code>/item/123</code>.
        You map it to a method parameter with <code>@PathParam</code>.
      */
    @Path("{id}")
    /* 
        This method produces XML media; therefore, someone has to
        serialize the method's returned value into XML. Be careful,
        this annotation is not the same producer annotation as in CDI;
        it is in a different package!
     */
    @Produces(APPLICATION_XML)
    public Item get(@PathParam("id") Long id) {
        Item item = itemDAO.findById(id);
        if (item == null)
            throw new WebApplicationException(NOT_FOUND);
        return item;
    }

    @PUT
    @Path("{id}")
    /* 
        This method consumes XML media; therefore, someone has to
        deserialize the XML document and transform it into a
        detached <code>Item</code> instance.
    */
    @Consumes(APPLICATION_XML)
    /* 
        You want to store data in this method, so you must start a
        system transaction and join the persistence context with it.
    */
    @Transactional
    public void put(@PathParam("id") Long id, Item item) {
        itemDAO.joinTransaction();
        itemDAO.makePersistent(item);
    }

    @GET
    @Path("{id}/images")
    @Produces({APPLICATION_XML})
    @Transactional
    public List<Image> getImages(@PathParam("id") Long id) {
        Item item = itemDAO.findById(id);
        if (item == null)
            throw new WebApplicationException(NOT_FOUND);
        return item.getImagesSorted();
    }
}