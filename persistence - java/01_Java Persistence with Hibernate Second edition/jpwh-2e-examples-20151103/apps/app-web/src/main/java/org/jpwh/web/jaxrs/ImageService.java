package org.jpwh.web.jaxrs;

import org.jpwh.web.dao.ImageDAO;
import org.jpwh.web.model.ImageLookup;
import org.jpwh.web.model.Image;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

@Path("/image")
public class ImageService {

    @Inject
    ImageDAO imageDAO;

    @Inject
    Event<ImageLookup> imageLookupEvent;

    @GET
    @Path("{id}")
    @Transactional
    public Response getImage(@PathParam("id") Long id) throws Exception {
        if (id == null)
            throw new WebApplicationException(Response.Status.NOT_FOUND);

        // Can any observer provide that image?
        ImageLookup imageLookup = new ImageLookup(id);
        imageLookupEvent.fire(imageLookup);
        Image image = imageLookup.getImage();

        // If not, we try to look it up in the database
        if (image == null)
            image = imageDAO.findById(id);

        if (image == null)
            throw new WebApplicationException(Response.Status.NOT_FOUND);

        return Response
            .ok(image.getData())
            .type(image.getContentType())
            .build();
    }
}