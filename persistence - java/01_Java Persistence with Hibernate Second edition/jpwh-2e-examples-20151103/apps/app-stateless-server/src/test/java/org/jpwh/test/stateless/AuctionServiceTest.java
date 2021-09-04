package org.jpwh.test.stateless;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jpwh.model.Bid;
import org.jpwh.model.InvalidBidException;
import org.jpwh.model.Item;
import org.jpwh.stateless.AuctionService;
import org.testng.annotations.Test;

import javax.ejb.EJBException;
import javax.inject.Inject;
import javax.persistence.OptimisticLockException;
import java.math.BigDecimal;
import java.util.List;

import static org.testng.Assert.*;

@UsingDataSet("testdata.xml")
public class AuctionServiceTest extends Arquillian {

    @Deployment
    public static WebArchive createTestArchive() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class)
            .addPackages(true, "org.jpwh.stateless") // Recursively add all classes in that package
            .addAsLibraries(Maven.resolver() // Place Maven dependencies in WEB-INF/lib
                .loadPomFromFile("pom.xml")
                .importRuntimeDependencies()
                .resolve()
                .withTransitivity()
                .asFile());

        System.out.println("=> Deploying integration test virtual WAR:");
        System.out.println(archive.toString(true));
        return archive;
    }

    @Inject
    AuctionService service;

    @Test
    public void editItemName() throws Exception {
        // The client will have to manage application state, a list of items
        List<Item> items;

        // Get all items in detached state
        items = service.getItems(true); // Load the bids as well
        assertEquals(items.size(), 5);

        // Pick the first one and change its name
        Item detachedItem = items.get(0);
        assertEquals(detachedItem.getName(), "Baseball Glove");
        detachedItem.setName("Pretty Baseball Glove");

        // Call the service and make the change permanent, returns current Item instance
        detachedItem = service.storeItem(detachedItem);

        // The old instance is outdated, whoever is using it will fail to store
        Item oldItem = items.get(0);
        oldItem.setName("Other Name");
        boolean test = false;
        try {
            service.storeItem(oldItem);
        } catch (EJBException ex) {
            if (ex.getCause() instanceof OptimisticLockException)
                test = true;
        }
        assertTrue(test, "OptimisticLockException should have been thrown");

        List<Item> testItems = service.getItems(false);
        assertEquals(testItems.get(0).getName(), "Pretty Baseball Glove");
    }

    @Test
    public void placeBidFailure() throws Exception {
        // The client will have to manage application state, a list of items
        List<Item> items;

        // Get all items in detached state
        items = service.getItems(true); // Load the bids as well

        // Pick the first item
        Item item = items.get(0);

        // Check the current number of bids and place a new higher bid
        assertEquals(item.getBids().size(), 3);
        BigDecimal highestAmount = item.getHighestBid().getAmount();
        Bid newBid = new Bid(new BigDecimal(highestAmount.intValue() + 1), item);

        // A naive client might attempt something you don't support, document it!
        item.getBids().add(newBid);

        // "Maybe this service has cascading persistence on the Item#bids @OneToMany?"
        item = service.storeItem(item);
        // ... nothing happens, the bid won't be stored!

        List<Item> testItems = service.getItems(true);
        assertEquals(testItems.get(0).getBids().size(), 3);
        assertEquals(testItems.get(0).getHighestBid().getAmount(), highestAmount); // Still the old value
    }

    @Test
    public void placeBid() throws Exception {
        // The client will have to manage application state, a list of items
        List<Item> items;

        // Get all items in detached state
        items = service.getItems(true); // Load the bids as well

        // Pick the first item
        Item item = items.get(0);

        // Check the current number of bids and place a new higher bid
        assertEquals(item.getBids().size(), 3);
        BigDecimal highestAmount = item.getHighestBid().getAmount();
        Bid newBid = new Bid(new BigDecimal(highestAmount.intValue() + 1), item);

        // Call the service and place the bid, returns a current Item instance
        item = service.placeBid(newBid);

        // The old instance is outdated, whoever is using it will fail to place a bid
        Item oldItem = items.get(0);
        newBid = new Bid(new BigDecimal(newBid.getAmount().intValue() + 1), oldItem);
        boolean test = false;
        try {
            service.placeBid(newBid);
        } catch (EJBException ex) {
            if (ex.getCause() instanceof OptimisticLockException)
                test = true;
        }
        assertTrue(test, "OptimisticLockException should have been thrown");

        newBid = new Bid(newBid.getAmount().subtract(new BigDecimal(1)), item); // Amount too low
        test = false;
        try {
            service.placeBid(newBid);
        } catch (InvalidBidException ex) {
            test = true;
        }
        assertTrue(test, "InvalidBidException should have been thrown");

        List<Item> testItems = service.getItems(true);
        assertEquals(testItems.get(0).getBids().size(), 4);
    }

}
