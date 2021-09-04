package org.jpwh.test.querying.sql;

import org.hibernate.Session;
import org.hibernate.transform.AliasToBeanConstructorResultTransformer;
import org.hibernate.type.StandardBasicTypes;
import org.jpwh.model.querying.Bid;
import org.jpwh.model.querying.Category;
import org.jpwh.model.querying.Item;
import org.jpwh.model.querying.ItemSummary;
import org.jpwh.model.querying.User;
import org.jpwh.test.querying.QueryingTest;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.transaction.UserTransaction;
import java.util.Date;
import java.util.List;

import static org.testng.Assert.*;

public class HibernateSQLQueries extends QueryingTest {

    @Test
    public void executeQueries() throws Exception {
        TestDataCategoriesItems testData = storeTestData();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();
            Session session = em.unwrap(Session.class);

            {
                // Simple SQL projection
                org.hibernate.SQLQuery query = session.createSQLQuery(
                    "select NAME, AUCTIONEND from {h-schema}ITEM"
                );
                List<Object[]> result = query.list();

                for (Object[] tuple : result) {
                    assertTrue(tuple[0] instanceof String);
                    assertTrue(tuple[1] instanceof Date);
                }
                assertEquals(result.size(), 3);
            }
            em.clear();
            {
                // Automatic marshaling of resultset to mapped entity class
                org.hibernate.SQLQuery query = session.createSQLQuery(
                    "select * from ITEM"
                );
                query.addEntity(Item.class);

                List<Item> result = query.list();
                assertEquals(result.size(), 3);
                assertNotNull(result.get(0));
            }
            em.clear();
            {
                // Positional parameter binding
                Long ITEM_ID = testData.items.getFirstId();
                org.hibernate.SQLQuery query = session.createSQLQuery(
                    "select * from ITEM where ID = ?"
                );
                query.addEntity(Item.class);
                query.setParameter(0, ITEM_ID); // Starts at zero!

                List<Item> result = query.list();
                assertEquals(result.size(), 1);
                assertEquals(result.get(0).getId(), ITEM_ID);
            }
            em.clear();
            {
                // Named parameter binding
                Long ITEM_ID = testData.items.getFirstId();
                org.hibernate.SQLQuery query = session.createSQLQuery(
                    "select * from ITEM where ID = :id"
                );
                query.addEntity(Item.class);
                query.setParameter("id", ITEM_ID);

                List<Item> result = query.list();
                assertEquals(result.size(), 1);
                assertEquals(result.get(0).getId(), ITEM_ID);
            }
            em.clear();
            {
                // Automatic marshaling of resultset to entity class with aliases
                org.hibernate.SQLQuery query = session.createSQLQuery(
                    "select " +
                        "i.ID as {i.id}, " +
                        "'Auction: ' || i.NAME as {i.name}, " +
                        "i.CREATEDON as {i.createdOn}, " +
                        "i.AUCTIONEND as {i.auctionEnd}, " +
                        "i.AUCTIONTYPE as {i.auctionType}, " +
                        "i.APPROVED as {i.approved}, " +
                        "i.BUYNOWPRICE as {i.buyNowPrice}, " +
                        "i.SELLER_ID as {i.seller} " +
                        "from ITEM i"
                );
                query.addEntity("i", Item.class);

                List<Item> result = query.list();
                assertEquals(result.size(), 3);
                assertNotNull(result.get(0));
            }
            em.clear();
            {
                // Automatic marshaling of resultset to entity class with aliases (alternative)
                org.hibernate.SQLQuery query = session.createSQLQuery(
                    "select " +
                        "i.ID, " +
                        "'Auction: ' || i.NAME as EXTENDED_NAME, " +
                        "i.CREATEDON, " +
                        "i.AUCTIONEND, " +
                        "i.AUCTIONTYPE, " +
                        "i.APPROVED, " +
                        "i.BUYNOWPRICE, " +
                        "i.SELLER_ID " +
                        "from ITEM i"
                );
                query.addRoot("i", Item.class)
                    .addProperty("id", "ID")
                    .addProperty("name", "EXTENDED_NAME")
                    .addProperty("createdOn", "CREATEDON")
                    .addProperty("auctionEnd", "AUCTIONEND")
                    .addProperty("auctionType", "AUCTIONTYPE")
                    .addProperty("approved", "APPROVED")
                    .addProperty("buyNowPrice", "BUYNOWPRICE")
                    .addProperty("seller", "SELLER_ID");

                List<Item> result = query.list();
                assertEquals(result.size(), 3);
                assertNotNull(result.get(0));
            }
            em.clear();
            {
                // Automatic marshaling of resultset to entity class with aliases (using existing mapping)
                org.hibernate.SQLQuery query = session.createSQLQuery(
                    "select " +
                        "i.ID, " +
                        "'Auction: ' || i.NAME as EXTENDED_NAME, " +
                        "i.CREATEDON, " +
                        "i.AUCTIONEND, " +
                        "i.AUCTIONTYPE, " +
                        "i.APPROVED, " +
                        "i.BUYNOWPRICE, " +
                        "i.SELLER_ID " +
                        "from ITEM i"
                );
                query.setResultSetMapping("ItemResult");

                List<Item> result = query.list();
                assertEquals(result.size(), 3);
                assertNotNull(result.get(0));

            }
            em.clear();
            {
                // Automatic marshaling of resultset to several mapped entity classes
                org.hibernate.SQLQuery query = session.createSQLQuery(
                    "select " +
                        "{i.*}, {u.*} " +
                        "from ITEM i join USERS u on u.ID = i.SELLER_ID"
                );
                query.addEntity("i", Item.class);
                query.addEntity("u", User.class);

                List<Object[]> result = query.list();

                for (Object[] tuple : result) {
                    assertTrue(tuple[0] instanceof Item);
                    assertTrue(tuple[1] instanceof User);
                    Item item = (Item) tuple[0];
                    assertTrue(Persistence.getPersistenceUtil().isLoaded(item, "seller"));
                    assertEquals(item.getSeller(), tuple[1]);
                }
                assertEquals(result.size(), 3);
            }
            em.clear();
            {
                // Automatic marshaling of resultset to entity class with component aliases
                org.hibernate.SQLQuery query = session.createSQLQuery(
                    "select " +
                        "u.ID as {u.id}, " +
                        "u.USERNAME as {u.username}, " +
                        "u.FIRSTNAME as {u.firstname}, " +
                        "u.LASTNAME as {u.lastname}, " +
                        "u.ACTIVATED as {u.activated}, " +
                        "u.STREET as {u.homeAddress.street}, " +
                        "u.ZIPCODE as {u.homeAddress.zipcode}, " +
                        "u.CITY as {u.homeAddress.city} " +
                        "from USERS u"
                );
                query.addEntity("u", User.class);

                List<User> result = query.list();
                assertEquals(result.size(), 3);
                assertNotNull(result.get(0));
            }
            em.clear();
            {
                // Automatic marshaling of resultset with eager fetch of collection
                /* 
                   The query (outer) joins the <code>ITEM</code> and <code>BID</code> tables, the projection
                   returns all columns required to construct <code>Item</code> and <code>Bid</code> instances.
                   The query renames duplicate columns such as <code>ID</code> with aliases, so field names are
                   unique in the result.
                 */
                org.hibernate.SQLQuery query = session.createSQLQuery(
                    "select " +
                        "i.ID as ITEM_ID, " +
                        "i.NAME, " +
                        "i.CREATEDON, " +
                        "i.AUCTIONEND, " +
                        "i.AUCTIONTYPE, " +
                        "i.APPROVED, " +
                        "i.BUYNOWPRICE, " +
                        "i.SELLER_ID, " +
                        "b.ID as BID_ID," +
                        "b.ITEM_ID as BID_ITEM_ID, " +
                        "b.AMOUNT, " +
                        "b.BIDDER_ID " +
                        "from ITEM i left outer join BID b on i.ID = b.ITEM_ID"
                );
                /* 
                   Because of the renamed fields, you have to map all columns to their respective
                   entity property.
                 */
                query.addRoot("i", Item.class)
                    .addProperty("id", "ITEM_ID")
                    .addProperty("name", "NAME")
                    .addProperty("createdOn", "CREATEDON")
                    .addProperty("auctionEnd", "AUCTIONEND")
                    .addProperty("auctionType", "AUCTIONTYPE")
                    .addProperty("approved", "APPROVED")
                    .addProperty("buyNowPrice", "BUYNOWPRICE")
                    .addProperty("seller", "SELLER_ID");

                /* 
                   You add a <code>FetchReturn</code> for the <code>bids</code> collection with the alias of the
                   owning entity <code>i</code>, and map the <code>key</code> and <code>element</code> special
                   properties to the foreign key column <code>BID_ITEM_ID</code> and the identifier of the
                   <code>Bid</code>. Then the code maps each property of <code>Bid</code> to a field of the result set.
                   Note that the code maps some fields twice, as required by Hibernate for construction of the
                   collection.
                 */
                query.addFetch("b", "i", "bids")
                    .addProperty("key", "BID_ITEM_ID")
                    .addProperty("element", "BID_ID")
                    .addProperty("element.id", "BID_ID")
                    .addProperty("element.item", "BID_ITEM_ID")
                    .addProperty("element.amount", "AMOUNT")
                    .addProperty("element.bidder", "BIDDER_ID");

                List<Object[]> result = query.list();

                /* 
                   The number of rows in the result set is a product: 1 item has 3 bids, 1 item has 1 bid, and
                   the last item has no bids, for a total of 5 rows in the result.
                 */
                assertEquals(result.size(), 5);

                for (Object[] tuple : result) {
                    /* 
                       The first element of the result tuple is the <code>Item</code> instance; Hibernate initialized
                       the bids collection.
                     */
                    Item item = (Item) tuple[0];
                    assertTrue(Persistence.getPersistenceUtil().isLoaded(item, "bids"));

                    /* 
                       The second element of the result tuple is each <code>Bid</code>.
                     */
                    Bid bid = (Bid) tuple[1];
                    if (bid != null)
                        assertTrue(item.getBids().contains(bid));
                }
            }
            em.clear();
            {
                // Automatic marshaling of resultset with eager fetch of collection (short version)
                org.hibernate.SQLQuery query = session.createSQLQuery(
                    "select " +
                        "{i.*}, " +
                        "{b.*} " +
                        "from ITEM i left outer join BID b on i.ID = b.ITEM_ID"
                );
                query.addEntity("i", Item.class);
                query.addFetch("b", "i", "bids");

                List<Object[]> result = query.list();
                assertEquals(result.size(), 5);
                for (Object[] tuple : result) {
                    Item item = (Item) tuple[0];
                    assertTrue(Persistence.getPersistenceUtil().isLoaded(item, "bids"));
                    Bid bid = (Bid) tuple[1];
                    if (bid != null)
                        assertTrue(item.getBids().contains(bid));
                }
            }
            em.clear();
            {
                // Automatic marshaling of resultset to entity class and additional column
                org.hibernate.SQLQuery query = session.createSQLQuery(
                    "select " +
                        "i.*, " +
                        "count(b.ID) as NUM_OF_BIDS " +
                        "from ITEM i left join BID b on b.ITEM_ID = i.ID " +
                        "group by i.ID, i.NAME, i.CREATEDON, i.AUCTIONEND, " +
                        "i.AUCTIONTYPE, i.APPROVED, i.BUYNOWPRICE, i.SELLER_ID"
                );
                query.addEntity(Item.class);
                query.addScalar("NUM_OF_BIDS");

                List<Object[]> result = query.list();

                for (Object[] tuple : result) {
                    assertTrue(tuple[0] instanceof Item);
                    assertTrue(tuple[1] instanceof Number);
                }
                assertEquals(result.size(), 3);
                assertNotNull(result.get(0));
            }
            em.clear();
            {
                // Automatic marshaling of resultset to data-transfer class constructor
                org.hibernate.SQLQuery query = session.createSQLQuery(
                    "select ID, NAME, AUCTIONEND from ITEM"
                );

                /* 
                   You can use an existing result mapping.
                 */
                // query.setResultSetMapping("ItemSummaryResult");

                /* 
                   Alternatively, you can map the fields returned by the query as scalar values. Without a
                   result transformer, you would simply get an <code>Object[]</code> for each result row.
                 */
                query.addScalar("ID", StandardBasicTypes.LONG);
                query.addScalar("NAME");
                query.addScalar("AUCTIONEND");

                /* 
                   Apply a built-in result transformer to turn the <code>Object[]</code> into instances
                   of <code>ItemSummary</code>.
                 */
                query.setResultTransformer(
                    new AliasToBeanConstructorResultTransformer(
                        ItemSummary.class.getConstructor(
                            Long.class,
                            String.class,
                            Date.class
                        )
                    )
                );

                List<ItemSummary> result = query.list();
                assertNotNull(result.get(0));
                assertEquals(result.size(), 3);
            }
            em.clear();

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

    @Test(groups = {"H2"})
    public void executeRecursiveQueries() throws Exception {
        TestDataCategoriesItems testData = storeTestData();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();
            Session session = em.unwrap(Session.class);
            {
                // Externalized recursive SQL query
                org.hibernate.Query query = session.getNamedQuery("findAllCategoriesHibernate");

                List<Object[]> result = query.list();

                for (Object[] tuple : result) {
                    Category category = (Category) tuple[0];
                    String path = (String) tuple[1];
                    Integer level = (Integer) tuple[2];
                    // ...
                    /*
                    System.out.println("------------------------------------------------");
                    System.out.println("### PATH: " + path);
                    System.out.println("### NAME: " + category.getName());
                    System.out.println("### PARENT: " + (category.getParent() != null ? category.getParent().getName() : "ROOT"));
                    System.out.println("### LEVEL " + level);
                    */
                }
            }
            em.clear();

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }
}
