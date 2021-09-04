package org.jpwh.test.querying.advanced;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.hibernate.sql.JoinType;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.jpwh.model.querying.Address;
import org.jpwh.model.querying.Bid;
import org.jpwh.model.querying.Item;
import org.jpwh.model.querying.User;
import org.jpwh.test.querying.QueryingTest;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.testng.Assert.*;

public class HibernateCriteria extends QueryingTest {

    @Test
    public void foo() throws Exception {
        TestDataCategoriesItems testData = storeTestData();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();
            Session session = em.unwrap(Session.class);

            {
                List<Object[]> result =
                    session.createCriteria(Bid.class, "b")
                        .createAlias("item", "i", JoinType.RIGHT_OUTER_JOIN)
                        .add(Restrictions.or(
                            Restrictions.isNull("b.id"),
                            Restrictions.gt("amount", new BigDecimal(100))
                        ))
                        // Simple projection of aliases as result, not a list
                        // of root entity instances
                        .setResultTransformer(Criteria.PROJECTION)
                        .list();

                assertEquals(result.size(), 2);

                // Criteria quirk: the root entity alias is always last in the result tuple
                assertTrue(result.get(0)[0] instanceof Item);
                assertTrue(result.get(0)[1] instanceof Bid);

                assertTrue(result.get(1)[0] instanceof Item);
                assertEquals(result.get(1)[1], null);
            }
            em.clear();
            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }


    @Test
    public void executeQueries() throws Exception {
        TestDataCategoriesItems testData = storeTestData();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();
            Session session = em.unwrap(Session.class);

            {
                // Selection
                org.hibernate.Criteria criteria = session.createCriteria(Item.class);
                List<Item> items = criteria.list();
                assertEquals(items.size(), 3);
            }
            em.clear();

            {
                DetachedCriteria criteria = DetachedCriteria.forClass(Item.class);

                List<Item> items = criteria.getExecutableCriteria(session).list();
                assertEquals(items.size(), 3);
            }
            em.clear();

            {
                // Ordering
                List<User> users =
                    session.createCriteria(User.class)
                        .addOrder(Order.asc("firstname"))
                        .addOrder(Order.asc("lastname"))
                        .list();

                assertEquals(users.size(), 3);
                assertEquals(users.get(0).getFirstname(), "Jane");
                assertEquals(users.get(1).getFirstname(), "John");
                assertEquals(users.get(2).getFirstname(), "Robert");
            }
            em.clear();

            {
                // Restriction
                List<Item> items =
                    session.createCriteria(Item.class)
                        .add(Restrictions.eq("name", "Foo"))
                        .list();

                assertEquals(items.size(), 1);
            }
            em.clear();

            {
                List<User> users =
                    session.createCriteria(User.class)
                        .add(Restrictions.like("username", "j", MatchMode.START).ignoreCase())
                        .list();

                assertEquals(users.size(), 2);
            }
            em.clear();

            {
                List<User> users =
                    session.createCriteria(User.class)
                        .add(Restrictions.eq("homeAddress.city", "Some City"))
                        .list();

                assertEquals(users.size(), 1);
                assertEquals(users.get(0).getUsername(), "johndoe");
            }
            em.clear();


            {
                List<User> users =
                    session.createCriteria(User.class)
                        .add(Restrictions.sqlRestriction(
                            "length({alias}.USERNAME) < ?",
                            8,
                            StandardBasicTypes.INTEGER
                        )).list();

                assertEquals(users.size(), 2);
            }
            em.clear();

            {
                // Projection
                List<Object[]> result =
                    session.createCriteria(User.class)
                        .setProjection(Projections.projectionList()
                            .add(Projections.property("id"))
                            .add(Projections.property("username"))
                            .add(Projections.property("homeAddress"))
                        ).list();

                assertEquals(result.size(), 3);
                for (Object[] tuple : result) {
                    assertTrue(tuple[0] instanceof Long);
                    assertTrue(tuple[1] instanceof String);
                    assertTrue(tuple[2] == null || tuple[2] instanceof Address);
                }
            }
            em.clear();

            {
                List<String> result =
                    session.createCriteria(Item.class)
                        .setProjection(Projections.projectionList()
                            .add(Projections.sqlProjection(
                                "NAME || ':' || AUCTIONEND as RESULT",
                                new String[]{"RESULT"},
                                new Type[]{StandardBasicTypes.STRING}
                            ))
                        ).list();

                assertEquals(result.size(), 3);
            }
            em.clear();

            {
                List<Object[]> result =
                    session.createCriteria(User.class)
                        .setProjection(Projections.projectionList()
                            .add(Projections.groupProperty("lastname"))
                            .add(Projections.rowCount())
                        ).list();

                assertEquals(result.size(), 2);
                for (Object[] tuple : result) {
                    assertTrue(tuple[0] instanceof String);
                    assertTrue(tuple[1] instanceof Long);
                }
            }
            em.clear();

            {
                List<Object[]> result =
                    session.createCriteria(Bid.class)
                        .setProjection(Projections.projectionList()
                            .add(Projections.groupProperty("item"))
                            .add(Projections.avg("amount"))
                        ).list();

                assertEquals(result.size(), 2);
                for (Object[] tuple : result) {
                    assertTrue(tuple[0] instanceof Item);
                    assertTrue(tuple[1] instanceof Double);
                }
            }
            em.clear();

            {
                // Joins
                List<Bid> result =
                    session.createCriteria(Bid.class)
                        .createCriteria("item") // Inner join
                        .add(Restrictions.like(
                            "name", "Fo", MatchMode.START
                        ))
                        .list();

                assertEquals(result.size(), 3);
                for (Bid bid : result) {
                    assertEquals(bid.getItem().getId(), testData.items.getFirstId());
                }
            }
            em.clear();

            {
                List<Bid> result =
                    session.createCriteria(Bid.class)
                        .createCriteria("item") // Inner join
                        .add(Restrictions.isNotNull("buyNowPrice"))
                        .createCriteria("seller") // Inner join
                        .add(Restrictions.eq("username", "johndoe"))
                        .list();

                assertEquals(result.size(), 3);
            }
            em.clear();

            {
                List<Object[]> result =
                    session.createCriteria(Bid.class, "b")
                        .createAlias("item", "i", JoinType.RIGHT_OUTER_JOIN)
                        .add(Restrictions.or(
                            Restrictions.isNull("b.id"),
                            Restrictions.gt("amount", new BigDecimal(100))
                        ))
                        // Simple projection of aliases as result, not a list
                        // of root entity instances
                        .setResultTransformer(Criteria.PROJECTION)
                        .list();

                assertEquals(result.size(), 2);

                // Criteria quirk: the root entity alias is always last in the result tuple
                assertTrue(result.get(0)[0] instanceof Item);
                assertTrue(result.get(0)[1] instanceof Bid);

                assertTrue(result.get(1)[0] instanceof Item);
                assertEquals(result.get(1)[1], null);
            }
            em.clear();

            {
                List<Bid> result =
                    session.createCriteria(Bid.class)
                        .createCriteria("item") // Inner join
                        .createAlias("seller", "s") // Inner join
                        .add(Restrictions.and(
                            Restrictions.eq("s.username", "johndoe"),
                            Restrictions.isNotNull("buyNowPrice")
                        ))
                        .list();

                assertEquals(result.size(), 3);
            }
            em.clear();

            {
                // Fetching
                List<Item> result =
                    session.createCriteria(Item.class)
                        .setFetchMode("bids", FetchMode.JOIN)
                        .list();

                assertEquals(result.size(), 5); // 3 items, 4 bids, 5 "rows" in result!
                Set<Item> distinctResult = new LinkedHashSet<Item>(result); // In-memory "distinct"
                assertEquals(distinctResult.size(), 3); // It was only three items all along...

                boolean haveBids = false;
                for (Item item : result) {
                    em.detach(item); // No more lazy loading!
                    if (item.getBids().size() > 0) {
                        haveBids = true;
                        break;
                    }
                }
                assertTrue(haveBids);
            }
            em.clear();

            {
                List<Item> result =
                    session.createCriteria(Item.class)
                        .setFetchMode("bids", FetchMode.JOIN)
                        .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
                        .list();

                assertEquals(result.size(), 3); // Hibernate filtered it for us in-memory
                boolean haveBids = false;
                for (Item item : result) {
                    em.detach(item); // No more lazy loading!
                    if (item.getBids().size() > 0) {
                        haveBids = true;
                        break;
                    }
                }
                assertTrue(haveBids);
            }
            em.clear();

            {
                List<Item> result =
                    session.createCriteria(Item.class)
                        .createAlias("bids", "b", JoinType.LEFT_OUTER_JOIN)
                        .setFetchMode("b", FetchMode.JOIN)
                        .createAlias("b.bidder", "bdr", JoinType.INNER_JOIN)
                        .setFetchMode("bdr", FetchMode.JOIN)
                        .createAlias("seller", "s", JoinType.LEFT_OUTER_JOIN)
                        .setFetchMode("s", FetchMode.JOIN)
                        .list();

                result = new ArrayList<Item>(new LinkedHashSet<Item>(result));
                assertEquals(result.size(), 2);
                boolean haveBids = false;
                boolean haveBidder = false;
                boolean haveSeller = false;
                for (Item item : result) {
                    em.detach(item); // No more lazy loading!
                    if (item.getBids().size() > 0) {
                        haveBids = true;
                        Bid bid = item.getBids().iterator().next();
                        if (bid.getBidder() != null && bid.getBidder().getUsername() != null) {
                            haveBidder = true;
                        }
                    }
                    if (item.getSeller() != null && item.getSeller().getUsername() != null)
                        haveSeller = true;
                }
                assertTrue(haveBids);
                assertTrue(haveBidder);
                assertTrue(haveSeller);
            }
            em.clear();

            {
                // Subquery
                DetachedCriteria sq = DetachedCriteria.forClass(Item.class, "i");
                sq.add(Restrictions.eqProperty("i.seller.id", "u.id"));
                sq.setProjection(Projections.rowCount());

                List<User> result =
                    session.createCriteria(User.class, "u")
                        .add(Subqueries.lt(1l, sq))
                        .list();

                assertEquals(result.size(), 1);
                User user = result.iterator().next();
                assertEquals(user.getId(), testData.users.getFirstId());
            }
            em.clear();

            {
                DetachedCriteria sq = DetachedCriteria.forClass(Bid.class, "b");
                sq.add(Restrictions.eqProperty("b.item.id", "i.id"));
                sq.setProjection(Projections.property("amount"));

                List<Item> result =
                    session.createCriteria(Item.class, "i")
                        .add(Subqueries.geAll(new BigDecimal(10), sq))
                        .list();

                assertEquals(result.size(), 2);
            }
            em.clear();

            {
                // Query by example: Find all users with last name "Doe"
                /* 
                   First, create an "empty" instance of <code>User</code> as a template for your
                   search and set the property values you are looking for. You want to retrieve
                   people with the last name "Doe".
                 */
                User template = new User();
                template.setLastname("Doe");

                /* 
                   Create an instance of <code>Example</code> with the template, this API allows
                   you to fine-tune the search. You want the case of the last name to be ignored,
                   and a substring search, so "Doe", "DoeX", or "Doe Y" would match. In addition, the
                   <code>User</code> class has a <code>boolean</code> property called
                   <code>activated</code>. As a primitive, it can't be <code>null</code> and its
                   default value is <code>false</code>, so Hibernate would include it in the search
                   and only return users that aren't activated. As you want all users, you tell
                   Hibernate to ignore that property when building the search query.
                 */
                org.hibernate.criterion.Example example = Example.create(template);
                example.ignoreCase();
                example.enableLike(MatchMode.START);
                example.excludeProperty("activated");

                /* 
                   The <code>Example</code> is added to a <code>Criteria</code> as a restriction.
                 */
                List<User> users =
                    session.createCriteria(User.class)
                        .add(example)
                        .list();

                assertEquals(users.size(), 2);
            }
            em.clear();


            {
                // Find all items which have a name starting with "B" or "b", and a
                // seller with the last name "Doe"
                Item itemTemplate = new Item();
                itemTemplate.setName("B");

                Example exampleItem = Example.create(itemTemplate);
                exampleItem.ignoreCase();
                exampleItem.enableLike(MatchMode.START);
                exampleItem.excludeProperty("auctionType");
                exampleItem.excludeProperty("createdOn");

                User userTemplate = new User();
                userTemplate.setLastname("Doe");

                Example exampleUser = Example.create(userTemplate);
                exampleUser.excludeProperty("activated");

                List<Item> items =
                    session
                        .createCriteria(Item.class)
                        .add(exampleItem)
                        .createCriteria("seller").add(exampleUser)
                        .list();

                assertEquals(items.size(), 1);
                assertTrue(items.get(0).getName().startsWith("B"));
                assertEquals(items.get(0).getSeller().getLastname(), "Doe");

            }
            em.clear();

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

    class ExcludeBooleanExample extends Example {
        ExcludeBooleanExample(Object template) {
            super(template, new PropertySelector() {
                @Override
                public boolean include(Object propertyValue,
                                       String propertyName,
                                       Type type) {
                    return propertyValue != null
                        && !type.equals(StandardBasicTypes.BOOLEAN);
                }
            });
        }
    }

}
