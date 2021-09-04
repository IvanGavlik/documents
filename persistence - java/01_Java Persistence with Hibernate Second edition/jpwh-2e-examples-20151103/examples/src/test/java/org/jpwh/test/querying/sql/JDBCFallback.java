package org.jpwh.test.querying.sql;

import org.hibernate.Session;
import org.jpwh.test.querying.QueryingTest;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.testng.Assert.assertEquals;

public class JDBCFallback extends QueryingTest {

    public class QueryItemWork implements org.hibernate.jdbc.Work {

        /* 
           For this "work", an item identifier is needed, enforced with the final field and the constructor parameter.
         */
        final protected Long itemId;

        public QueryItemWork(Long itemId) {
            this.itemId = itemId;
        }

        /* 
            The <code>execute()</code> method is called by Hibernate with a JDBC <code>Connection</code>. You do not
            have to close the connection when you are done.
         */
        @Override
        public void execute(Connection connection) throws SQLException {
            PreparedStatement statement = null;
            ResultSet result = null;
            try {
                statement = connection.prepareStatement(
                    "select * from ITEM where ID = ?"
                );
                statement.setLong(1, itemId);

                result = statement.executeQuery();

                while (result.next()) {
                    String itemName = result.getString("NAME");
                    BigDecimal itemPrice = result.getBigDecimal("BUYNOWPRICE");
                    // ...
                    assertEquals(Long.valueOf(result.getLong("ID")), itemId);
                }
            } finally {
                // Close things you opened, results, statements
                /* 
                    You have to close and release other resources you have obtained though, such as the
                    <code>PreparedStatement</code> and <code>ResultSet</code>.
                 */
                if (result != null)
                    result.close();
                if (statement != null)
                    statement.close();
            }
        }
    }

    @Test
    public void queryItems() throws Exception {
        Long ITEM_ID = storeTestData().items.getFirstId();

        UserTransaction tx = TM.getUserTransaction();
        tx.begin();
        EntityManager em = JPA.createEntityManager();

        Session session = em.unwrap(Session.class);
        session.doWork(new QueryItemWork(ITEM_ID));

        tx.commit();
        em.close();
    }
}
