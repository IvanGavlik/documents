package org.jpwh.stateful;

import org.hibernate.StaleObjectStateException;
import org.jpwh.model.InvalidBidException;
import org.jpwh.model.ItemBidSummary;
import org.jpwh.shared.util.Exceptions;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.OptimisticLockException;
import javax.validation.ConstraintViolationException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.logging.LogManager;

public class AuctionClient {

    public static void main(String[] args) throws Exception {
        // If this main() method is started with the Maven exec plugin, the JVM is already started and not
        // forked, and it's too late for JUL to pick up this system property automatically. Or something else...
        LogManager.getLogManager().readConfiguration(
            new FileInputStream(new File(System.getProperty("java.util.logging.config.file"))));

        AuctionClient client = new AuctionClient();
        client.startDialog();
    }

    protected final DateFormat dateFormat = new SimpleDateFormat("dd. MMM yyyy HH:mm", Locale.ENGLISH);
    protected final BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));

    protected final RemoteAuctionService auctionService;

    public AuctionClient() throws Exception {
        System.out.println(">>> Starting dialog, connecting to server (press CTRL+C to exit)...");
        this.auctionService = lookupService(RemoteAuctionService.class, AuctionServiceImpl.class, false);
    }

    @SuppressWarnings("InfiniteRecursion")
    public void startDialog() throws Exception {

        // Get a list of DTOs from the service, this is stateless
        List<ItemBidSummary> itemBidSummaries = auctionService.getSummaries();

        // Render the list
        renderItemBidSummary(itemBidSummaries);

        // Let the user pick one item identifier and an action to perform
        ItemBidSummary itemBidSummary = promptItemBidSummary(itemBidSummaries);
        System.out.println("Would you like to rename (n) the item or place a bid (b):");
        String action = userInput.readLine();

        if (action.equals("n")) {

            RemoteItemService itemService = startConversation(itemBidSummary.getItemId());
            itemService.setItemName(promptName());
            commitConversation(itemService);

        } else if (action.equals("b")) {

            RemoteItemService itemService = startConversation(itemBidSummary.getItemId());
            // This operation has some recoverable exceptions, so we encapsulate it
            placeBid(itemService, itemBidSummary);
            commitConversation(itemService);
        }

        startDialog();
    }

    protected void renderItemBidSummary(List<ItemBidSummary> itemBidSummaries) {
        if (itemBidSummaries.size() == 0) {
            System.err.println("No items found, are you sure you imported some test data? Exiting...");
            System.exit(1);
        }
        System.out.println("--------------------------------------------------------------------");
        String format = "%-4s | %-20s | %-20s | %15s %n";
        System.out.printf(format, "ID", "Name", "Auction End", "Highest Bid");
        System.out.println("--------------------------------------------------------------------");
        for (ItemBidSummary itemBidSummary : itemBidSummaries) {
            String highestBidAmount = itemBidSummary.getHighestBid() != null
                ? itemBidSummary.getHighestBid().toString()
                : "-";
            String auctionEnd = dateFormat.format(itemBidSummary.getAuctionEnd());
            System.out.printf(format, itemBidSummary.getItemId(), itemBidSummary.getName(), auctionEnd, highestBidAmount);
        }
        System.out.println("--------------------------------------------------------------------");
    }

    protected ItemBidSummary promptItemBidSummary(List<ItemBidSummary> itemBidSummaries) throws Exception {
        System.out.println("Please enter an item ID:");
        try {
            Long id = Long.valueOf(userInput.readLine());
            for (ItemBidSummary itemBidSummary : itemBidSummaries) {
                if (itemBidSummary.getItemId().equals(id))
                    return itemBidSummary;
            }
        } catch (NumberFormatException ex) {
            // Can't read user input line
            promptItemBidSummary(itemBidSummaries);
            return null;
        }
        System.out.println("Item not found!");
        promptItemBidSummary(itemBidSummaries);
        return null;
    }

    protected String promptName() throws Exception {
        System.out.println("Enter new name:");
        return userInput.readLine();
    }

    protected BigDecimal promptBid(ItemBidSummary itemBidSummary) throws Exception {
        System.out.println("Your bid for item '" + itemBidSummary.getName() + "':");
        return new BigDecimal(userInput.readLine());
    }

    protected void placeBid(RemoteItemService itemService, ItemBidSummary itemBidSummary) throws Exception {
        // Handle some recoverable exceptions which don't kill the conversation
        try {
            itemService.placeBid(promptBid(itemBidSummary));
        } catch (NumberFormatException ex ) { // Thrown by client!
            System.out.println("=> Sorry! Not a number, try again.");
            placeBid(itemService, itemBidSummary);
        } catch (InvalidBidException ex) { // Thrown by server!
            System.out.println("=> Sorry! Invalid bid, try again: " + ex.getMessage());
            placeBid(itemService, itemBidSummary);
        }
    }

    protected RemoteItemService startConversation(Long itemId) throws Exception {
        RemoteItemService itemService = lookupService(RemoteItemService.class, ItemServiceImpl.class, true);
        // OR WITH DAO:
        // RemoteItemService itemService = lookupService(RemoteItemService.class, ItemServiceWithDAOImpl.class, true);

        System.out.println("=> Starting conversation with server");
        itemService.startConversation(itemId);

        return itemService;
    }

    protected void commitConversation(RemoteItemService itemService) throws Exception {
        try {
            itemService.commitConversation();
            System.out.println("=> Conversation committed successfully!");
        } catch (Exception ex) {
            Throwable cause = Exceptions.unwrap(ex);

            // Exceptions on commit kill the conversation, as the stateful session bean on the server
            // is not retained when an exception is thrown in the @Remove commitConversation() method.
            // We know that for certain exceptions we can print user-friendly errors and restart
            // the conversation, we don't have to kill the whole client.
            if (cause instanceof OptimisticLockException || cause instanceof StaleObjectStateException)
                System.err.println(
                    "=> Sorry! The data of your conversation was modified concurrently by another user."
                );
            else if (cause instanceof ConstraintViolationException)
                System.err.println(
                    "=> Sorry! The server reported validation errors: " +
                    ((ConstraintViolationException)cause).getConstraintViolations()
                );
            else
                throw ex;
        }
    }

    protected static <T> T lookupService(Class<T> remoteInterface, Class<? extends T> bean, boolean stateful) throws NamingException {
        // This hurts... but at least some of it is standardized...
        final Hashtable jndiProperties = new Hashtable();
        jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        Context context = new InitialContext(jndiProperties);

        String appName = ""; // Empty with WAR deployment
        String moduleName = "app-stateful-server"; // The name of the WAR (makes sense, right?)
        String distinctName = ""; // Unless you do more configuration magic, this is always empty
        String beanName = bean.getSimpleName(); // That's the implementation of...
        String viewClassName = remoteInterface.getName(); // ... this interface
        String ejbName = "ejb:" + appName + "/" + moduleName + "/" + distinctName + "/" + beanName + "!" + viewClassName;
        if (stateful)
            ejbName = ejbName + "?stateful";
        return (T) context.lookup(ejbName);
    }
}
