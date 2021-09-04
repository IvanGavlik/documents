package org.jpwh.web.jaxrs;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static javax.ws.rs.core.MediaType.*;

@Provider
@Consumes(APPLICATION_XML)
@RequestScoped
public class EntityReferenceXMLReader implements MessageBodyReader {

    @Inject
    EntityManager em;

    @Override
    public boolean isReadable(Class type,
                              Type genericType,
                              Annotation[] annotations,
                              MediaType mediaType) {
        return type.isAnnotationPresent(XmlRootElement.class);
    }

    @Override
    public Object readFrom(Class type, Type genericType,
                           Annotation[] annotations,
                           MediaType mediaType,
                           MultivaluedMap httpHeaders,
                           InputStream entityStream)
        throws IOException, WebApplicationException {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(type);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            unmarshaller.setAdapter(
                EntityReferenceAdapter.class,
                new EntityReferenceAdapter(em)
            );
            return unmarshaller.unmarshal(entityStream);
        } catch (JAXBException ex) {
            throw new IOException(ex);
        }
    }
}
