package org.jpwh.test.stateful;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.persistence.UsingDataSet;
import org.jboss.arquillian.testng.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jpwh.model.InvalidBidException;
import org.jpwh.model.ItemBidSummary;
import org.jpwh.shared.util.Exceptions;
import org.jpwh.stateful.AuctionService;
import org.jpwh.stateful.ItemService;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.ejb.EJBException;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.List;

import static org.testng.Assert.*;

@UsingDataSet("testdata.xml")
public class AuctionServiceTest extends Arquillian {

    @Deployment
    public static WebArchive createTestArchive() {
        WebArchive archive = ShrinkWrap.create(WebArchive.class)
            .addPackages(true, "org.jpwh.stateful") // Recursively add all classes in that package
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
    AuctionService auctionService;

    @Inject
    Instance<ItemService> itemServiceInstance;

    @Test
    public void editItemName() throws Exception {

        // The client doesn't have to hold state, it renders a list of DTOs
        List<ItemBidSummary> itemBidSummaries = auctionService.getSummaries();

        // Only identifier values are necessary, get the first in the list
        Long itemId = itemBidSummaries.get(0).getItemId();

        // Change its name in a conversation with a stateful service instance
        ItemService itemService = itemServiceInstance.get();
        itemService.startConversation(itemId);
        itemService.setItemName("Pretty Baseball Glove");

        // Still the old name until we commit and flush the extended persistence context!
        itemBidSummaries = auctionService.getSummaries();
        assertEquals(itemBidSummaries.get(0).getName(), "Baseball Glove");

        // Start concurrent second conversation
        ItemService concurrentItemService = itemServiceInstance.get();
        concurrentItemService.startConversation(itemId);
        concurrentItemService.setItemName("Ugly Baseball Glove");

        // Commit the first conversation
        itemService.commitConversation();

        // Second conversation must fail
        boolean test = false;
        try {
            concurrentItemService.commitConversation();
        } catch (EJBException ex) {
            // TODO: This should be an OptimisticLockException
            if (Exceptions.unwrap(ex) instanceof org.hibernate.StaleObjectStateException)
                test = true;
        }
        assertTrue(test, "StaleObjectStateException should have been thrown");

        // First conversation wins, the new name has been set
        itemBidSummaries = auctionService.getSummaries();
        assertEquals(itemBidSummaries.get(0).getName(), "Pretty Baseball Glove");
    }

    @Test
    public void placeBid() throws Exception {
        // The client doesn't have to hold state, it renders a list of DTOs
        List<ItemBidSummary> itemBidSummaries = auctionService.getSummaries();

        // Get the first DTO
        ItemBidSummary itemBidSummary = itemBidSummaries.get(0);

        // Check the current number of bids and place a new higher bid
        BigDecimal newBidAmount = new BigDecimal(itemBidSummary.getHighestBid().intValue() + 1);

        // Place the bid in a conversation with a stateful service instance
        ItemService itemService = itemServiceInstance.get();
        itemService.startConversation(itemBidSummary.getItemId());
        itemService.placeBid(newBidAmount);

        // Still the old bid until we commit and flush the extended persistence context!
        itemBidSummaries = auctionService.getSummaries();
        assertEquals(itemBidSummaries.get(0).getHighestBid().compareTo(newBidAmount), -1);

        // Start concurrent second conversation
        ItemService concurrentItemService = itemServiceInstance.get();
        concurrentItemService.startConversation(itemBidSummary.getItemId());
        BigDecimal secondNewBid = new BigDecimal(itemBidSummary.getHighestBid().intValue() + 2);
        concurrentItemService.placeBid(secondNewBid);

        // Commit the first conversation
        itemService.commitConversation();

        // Second conversation must fail
        boolean test = false;
        try {
            concurrentItemService.commitConversation();
        } catch (EJBException ex) {
            // TODO: This should be an OptimisticLockException
            if (Exceptions.unwrap(ex) instanceof org.hibernate.StaleObjectStateException)
                test = true;
        }
        assertTrue(test, "StaleObjectStateException should have been thrown");

        BigDecimal lowBidAmount = new BigDecimal(itemBidSummary.getHighestBid().intValue()-1);
        itemService = itemServiceInstance.get();
        itemService.startConversation(itemBidSummary.getItemId());
        test = false;
        try {
            itemService.placeBid(lowBidAmount);
        } catch (InvalidBidException ex) {
            // Pending conversation is never committed, server times out stateful bean
            test = true;
        }
        Assert.assertTrue(test, "InvalidBidException should have been thrown");

        // First conversation wins, the new bid has been made
        itemBidSummaries = auctionService.getSummaries();
        assertEquals(itemBidSummaries.get(0).getHighestBid().compareTo(newBidAmount), 0);
    }
}
