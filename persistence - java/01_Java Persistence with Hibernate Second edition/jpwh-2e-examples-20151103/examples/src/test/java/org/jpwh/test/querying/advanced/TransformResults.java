package org.jpwh.test.querying.advanced;

import org.hibernate.Session;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.hibernate.transform.AliasToEntityMapResultTransformer;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.transform.ToListResultTransformer;
import org.jpwh.model.querying.Item;
import org.jpwh.model.querying.ItemSummary;
import org.jpwh.model.querying.ItemSummaryFactory;
import org.jpwh.model.querying.User;
import org.jpwh.test.querying.QueryingTest;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.testng.Assert.assertEquals;

public class TransformResults extends QueryingTest {

    @Test
    public void executeQueries() throws Exception {
        storeTestData();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            Session session = em.unwrap(Session.class);
            org.hibernate.Query query = session.createQuery(
                "select i.id as itemId, i.name as name, i.auctionEnd as auctionEnd from Item i"
            );

            {
                // Access List of Object[]
                List<Object[]> result = query.list();

                for (Object[] tuple : result) {
                    Long itemId = (Long) tuple[0];
                    String name = (String) tuple[1];
                    Date auctionEnd = (Date) tuple[2];
                    // ...
                }
                assertEquals(result.size(), 3);
            }
            em.clear();

            {
                // Transform to List of Lists
                query.setResultTransformer(
                    ToListResultTransformer.INSTANCE
                );

                List<List> result = query.list();

                for (List list : result) {
                    Long itemId = (Long) list.get(0);
                    String name = (String) list.get(1);
                    Date auctionEnd = (Date) list.get(2);
                    // ...
                }
                assertEquals(result.size(), 3);
            }
            em.clear();

            {
                // Transform to List of Maps
                query.setResultTransformer(
                    AliasToEntityMapResultTransformer.INSTANCE
                );

                List<Map> result = query.list();

                // You can access the aliases of the query
                assertEquals(
                    query.getReturnAliases(),
                    new String[]{"itemId", "name", "auctionEnd"}
                );

                for (Map map : result) {
                    Long itemId = (Long) map.get("itemId");
                    String name = (String) map.get("name");
                    Date auctionEnd = (Date) map.get("auctionEnd");
                    // ...
                }
                assertEquals(result.size(), 3);
            }
            em.clear();

            {
                // Transform to List of Maps with entity aliases
                org.hibernate.Query entityQuery = session.createQuery(
                    "select i as item, u as seller from Item i join i.seller u"
                );

                entityQuery.setResultTransformer(
                    AliasToEntityMapResultTransformer.INSTANCE
                );

                List<Map> result = entityQuery.list();

                for (Map map : result) {
                    Item item = (Item) map.get("item");
                    User seller = (User) map.get("seller");

                    assertEquals(item.getSeller(), seller);
                    // ...
                }
                assertEquals(result.size(), 3);
            }
            em.clear();

            {
                // Transform to List of JavaBean calling setters/fields
                query.setResultTransformer(
                    new AliasToBeanResultTransformer(ItemSummary.class)
                );

                List<ItemSummary> result = query.list();

                for (ItemSummary itemSummary : result) {
                    Long itemId = itemSummary.getItemId();
                    String name = itemSummary.getName();
                    Date auctionEnd = itemSummary.getAuctionEnd();
                    // ...
                }
                assertEquals(result.size(), 3);
            }
            em.clear();


            { // Custom ResultTransformer
                query.setResultTransformer(
                    new ResultTransformer() {

                        /**
                         * 
                         * For each result "row", an <code>Object[]</code> tuple has to be transformed into
                         * the desired result value for that row. Here you access each projection element by
                         * index in the tuple array, and then call the <code>ItemSummaryFactory</code> to produce
                         * the query result value. Hibernate passes the method the aliases found in the query, for each
                         * tuple element. You don't need the aliases in this transformer, though.
                         */
                        @Override
                        public Object transformTuple(Object[] tuple, String[] aliases) {

                            Long itemId = (Long) tuple[0];
                            String name = (String) tuple[1];
                            Date auctionEnd = (Date) tuple[2];

                            // You can access the aliases of the query if needed
                            assertEquals(aliases[0], "itemId");
                            assertEquals(aliases[1], "name");
                            assertEquals(aliases[2], "auctionEnd");

                            return ItemSummaryFactory.newItemSummary(
                                itemId, name, auctionEnd
                            );
                        }

                        /**
                         * 
                         * You can wrap or modify the result list after after transforming the tuples.
                         * Here you make the returned <code>List</code> unmodifiable,
                         * ideal for a reporting screen where nothing should change the data.
                         */
                        @Override
                        public List transformList(List collection) {
                            // The "collection" is a List<ItemSummary>
                            return Collections.unmodifiableList(collection);
                        }
                    }
                );

                List<ItemSummary> result = query.list();
                assertEquals(result.size(), 3);
            }
            em.clear();

            /* Hibernate has an internal CriteriaResultTransformer for JPA criteria queries
               TODO https://hibernate.atlassian.net/browse/HHH-8196
            {
                CriteriaBuilder cb = JPA.getEntityManagerFactory().getCriteriaBuilder();

                CriteriaQuery criteria = cb.createQuery();
                Root<Item> i = criteria.from(Item.class);
                i.alias("i");
                criteria.multiselect(
                    i.get("id").alias("itemId"),
                    i.get("name").alias("name"),
                    i.get("auctionEnd").alias("auctionEnd")
                );

                Query query = em.createQuery(criteria);
                org.hibernate.Query hibernateQuery = ((HibernateQuery)query).getHibernateQuery();

                /*
                assertEquals(
                    hibernateQuery.getQueryString(),
                    "select i.id as itemId, i.name as name, i.auctionEnd as auctionEnd from Item as i"
                );
                // Actual: select i.id, i.name, i.auctionEnd from Item as i

                assertEquals(
                    hibernateQuery.getReturnAliases(),
                    new String[] {"itemId", "name", "auctionEnd"}
                );
                // Actual: 0, 1, 2

                // Overrides the internal CriteriaResultTransformer, breaks JPA converters
                hibernateQuery.setResultTransformer(
                    new AliasToBeanResultTransformer(ItemSummary.class)
                );

                List<ItemSummary> result = query.getResultList();
                assertEquals(result.size(), 3);
            }
            em.clear();
            */

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

}
