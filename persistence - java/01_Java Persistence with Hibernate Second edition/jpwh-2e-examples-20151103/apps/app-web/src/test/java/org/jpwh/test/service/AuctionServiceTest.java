package org.jpwh.test.service;

import org.jpwh.web.jsf.AuctionService;
import org.testng.annotations.Test;

import javax.inject.Inject;

import static org.testng.Assert.assertEquals;

public class AuctionServiceTest extends IntegrationTest {

    @Inject
    AuctionService service;

    @Test
    public void placeBid() {
        service.setId(1);

        assertEquals(service.getItem().getName(), "Baseball Glove");
        assertEquals(service.getHighestBidAmount().toString(), "13.00");

        /* TODO: This requires JSFUnit, which is currently a beta mess, giving up...

           JSF is difficult to test, your best option is probably UI functional
           testing with tools like Selenium or HTMLUnit.

        // Too low
        service.setNewBidAmount(new BigDecimal("12.00"));
        String outcome = service.placeBid();
        assertTrue(!outcome.contains("redirect"));
        assertTrue(FacesContext.getCurrentInstance().getMessageList().size() > 0);

        // OK
        service.setNewBidAmount(new BigDecimal("14.00"));
        outcome = service.placeBid();
        assertTrue(outcome.contains("redirect"));
        assertTrue(FacesContext.getCurrentInstance().getMessageList().size() == 0);
        */

    }

}
