package org.jpwh.test.advanced;

import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.jpwh.env.JPATest;
import org.jpwh.model.advanced.Bid;
import org.jpwh.model.advanced.Item;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class TransformingColumns extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("AdvancedPU");
    }

    @Test
    public void storeLoadTransform() throws Exception {
        final long ITEM_ID = storeItemAndBids();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            {
                Item item = em.find(Item.class, ITEM_ID);
                assertEquals(item.getMetricWeight(), 2.0);

                final boolean[] tests = new boolean[1];
                em.unwrap(Session.class).doWork(new Work() {
                    @Override
                    public void execute(Connection connection) throws SQLException {
                        PreparedStatement statement = null;
                        ResultSet result = null;
                        try {
                            statement = connection.prepareStatement(
                                "select IMPERIALWEIGHT from ITEM where ID = ?"
                            );
                            statement.setLong(1, ITEM_ID);
                            result = statement.executeQuery();
                            while (result.next()) {
                                Double imperialWeight = result.getDouble("IMPERIALWEIGHT");
                                assertEquals(imperialWeight, 4.40924);
                                tests[0] = true;
                            }
                        } finally {
                            if (result != null)
                                result.close();
                            if (statement != null)
                                statement.close();
                        }
                    }
                });
                assertTrue(tests[0]);
            }
            em.clear();

            {
                List<Item> result =
                    em.createQuery("select i from Item i where i.metricWeight = :w")
                        .setParameter("w", 2.0)
                        .getResultList();
                assertEquals(result.size(), 1);
            }
            em.clear();

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

    public Long storeItemAndBids() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        tx.begin();
        EntityManager em = JPA.createEntityManager();
        Item item = new Item();
        item.setName("Some item");
        item.setMetricWeight(2);
        item.setDescription("This is some description.");
        em.persist(item);
        for (int i = 1; i <= 3; i++) {
            Bid bid = new Bid();
            bid.setAmount(new BigDecimal(10 + i));
            bid.setItem(item);
            em.persist(bid);
        }
        tx.commit();
        em.close();
        return item.getId();
    }

}
