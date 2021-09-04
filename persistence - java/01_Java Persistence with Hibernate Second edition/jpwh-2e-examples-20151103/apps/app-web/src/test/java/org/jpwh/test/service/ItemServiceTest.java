package org.jpwh.test.service;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.extension.rest.client.ArquillianResteasyResource;
import org.jboss.arquillian.testng.Arquillian;
import org.jpwh.web.jaxrs.EntityReferenceAdapter;
import org.jpwh.web.model.Bid;
import org.jpwh.web.model.EntityReference;
import org.jpwh.web.model.Item;
import org.jpwh.web.model.User;
import org.testng.annotations.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;

import static org.jpwh.Constants.API_VERSION;
import static org.testng.Assert.*;

public class ItemServiceTest extends IntegrationTest {

    @RunAsClient
    @Test(dataProvider = Arquillian.ARQUILLIAN_DATA_PROVIDER)
    public void getAndPutItem(@ArquillianResteasyResource(API_VERSION) WebTarget webTarget) throws Exception {

        WebTarget itemTarget = webTarget.path("/item/1");

        Response response = itemTarget
            .request()
            .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML)
            .get();

        assertEquals(response.getStatus(), 200);
        String xmlData = response.readEntity(String.class);

        // There should be an EntityReference to a User in that XML document, let's find out
        final boolean[] tests = new boolean[1];
        JAXBContext jaxbContext = JAXBContext.newInstance(Item.class, Bid.class, User.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        unmarshaller.setAdapter(EntityReferenceAdapter.class, new EntityReferenceAdapter() {
            @Override
            public Object unmarshal(EntityReference entityReference) throws Exception {
                if (entityReference.type == User.class && entityReference.id == 1l)
                    tests[0] = true;
                return null; // DON'T DO THIS IN A REAL APP! NULL DOESN'T MEAN UNINITIALIZED PROXY!
            }
        });
        unmarshaller.unmarshal(new StringReader(xmlData));
        assertTrue(tests[0]);

        xmlData = xmlData.replaceAll("Baseball Glove", "Pretty Baseball Glove"); // Quick & Dirty

        response = itemTarget
            .request()
            .put(Entity.xml(xmlData));

        assertEquals(response.getStatus(), 204);
    }
}
