package org.jpwh.stateless;

import org.jpwh.model.Bid;
import org.jpwh.model.InvalidBidException;
import org.jpwh.model.Item;
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
        this.auctionService = lookupService(RemoteAuctionService.class, AuctionServiceImpl.class);
    }

    @SuppressWarnings("InfiniteRecursion")
    public void startDialog() throws Exception {

        // Get a list of items from the service
        List<Item> items = auctionService.getItems(true);

        // Render the list
        renderItemBidSummary(items);

        // Let the user pick one (detached state) item and an action to perform
        Item item = promptItem(items);
        System.out.println("Would you like to rename (n) the item or place a bid (b):");
        String action = userInput.readLine();
        if (action.equals("n")) {
            changeName(item);
        } else if (action.equals("b")) {
            placeBid(item);
        }

        startDialog();
    }

    protected void renderItemBidSummary(List<Item> items) {
        if (items.size() == 0) {
            System.err.println("No items found, are you sure you imported some test data? Exiting...");
            System.exit(1);
        }
        System.out.println("--------------------------------------------------------------------");
        String format = "%-4s | %-20s | %-20s | %15s %n";
        System.out.printf(format, "ID", "Name", "Auction End", "Highest Bid");
        System.out.println("--------------------------------------------------------------------");
        for (Item item : items) {
            Bid highestBid = item.getHighestBid();
            String highestBidAmount = highestBid != null ? highestBid.getAmount().toString() : "-";
            String auctionEnd = dateFormat.format(item.getAuctionEnd());
            System.out.printf(format, item.getId(), item.getName(), auctionEnd, highestBidAmount);
        }
        System.out.println("--------------------------------------------------------------------");
    }

    protected void changeName(Item item) throws Exception {
        String name = promptName(item);
        item.setName(name);
        try {
            Item updatedItem = auctionService.storeItem(item);
            // The old item is now outdated, but this doesn't really
            // matter because we are going to restart the dialog anyway
            // after this method returns!
            System.out.println("=> Item name changed successfully!");
        } catch (Exception ex) {
            Throwable cause = Exceptions.unwrap(ex);

            if (cause instanceof OptimisticLockException)
                System.err.println(
                    "=> Sorry! The item you were working on was modified by another user."
                );
            else if (cause instanceof ConstraintViolationException)
                System.err.println(
                    "=> Please correct the validation errors reported by the server: " +
                        ((ConstraintViolationException) cause).getConstraintViolations()
                );
            else
                throw ex;
        }
    }

    protected void placeBid(Item item) throws Exception {
        Bid bid = promptBid(item); // Item is in detached state, no lazy loading!
        try {
            Item updatedItem = auctionService.placeBid(bid);
            // The old item is now outdated, but this doesn't really
            // matter because we are going to restart the dialog anyway
            // after this method returns!
            System.out.println("=> Bid placed successfully!");
        } catch (InvalidBidException ex) {
            System.out.println("Invalid bid: " + ex.getMessage());
            placeBid(item);
        } catch (Exception ex) {
            Throwable cause = Exceptions.unwrap(ex);
            if (cause instanceof OptimisticLockException)
                System.err.println(
                    "=> Sorry! The item was modified by another user or another bid was made."
                );
            else
                throw ex;
        }
    }

    protected Item promptItem(List<Item> items) throws Exception {
        System.out.println("Please enter an item ID:");
        try {
            Long id = Long.valueOf(userInput.readLine());
            for (Item item : items) {
                if (item.getId().equals(id))
                    return item;
            }
        } catch (NumberFormatException ex) {
            // Can't read user input line
            promptItem(items);
            return null;
        }
        System.out.println("Item not found!");
        promptItem(items);
        return null;
    }

    protected String promptName(Item item) throws Exception {
        System.out.println("New name for item '" + item.getName() + "':");
        // A real client would of course transform and validate the input here...
        return userInput.readLine();
    }

    protected Bid promptBid(Item item) throws Exception {
        System.out.println("Your bid for item '" + item.getName() + "':");
        BigDecimal amount = new BigDecimal(userInput.readLine());
        return new Bid(amount, item);
    }

    protected static <T> T lookupService(Class<T> remoteInterface, Class<? extends T> bean) throws NamingException {
        // This hurts... but at least some of it is standardized...
        final Hashtable jndiProperties = new Hashtable();
        jndiProperties.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        Context context = new InitialContext(jndiProperties);

        String appName = ""; // Empty with WAR deployment
        String moduleName = "app-stateless-server"; // The name of the WAR (makes sense, right?)
        String distinctName = ""; // Unless you do more configuration magic, this is always empty
        String beanName = bean.getSimpleName(); // That's the implementation of...
        String viewClassName = remoteInterface.getName(); // ... this interface
        String ejbName = "ejb:" + appName + "/" + moduleName + "/" + distinctName + "/" + beanName + "!" + viewClassName;
        return (T) context.lookup(ejbName);
    }
}