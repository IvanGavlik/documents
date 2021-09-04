package org.jpwh.test.querying.sql;

import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.hibernate.procedure.ParameterRegistration;
import org.jpwh.model.querying.Item;
import org.jpwh.test.querying.QueryingTest;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureQuery;
import javax.transaction.UserTransaction;
import java.sql.*;
import java.util.List;

import static org.testng.Assert.*;

@Test(groups = "MYSQL")
public class CallStoredProcedures extends QueryingTest {

    @Test(groups = "MYSQL")
    public void callReturningResultSet() throws Exception {
        storeTestData();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            /*
            em.unwrap(Session.class).doWork(
                new CallProcedureAndPrintResult() {
                    @Override
                    protected CallableStatement prepare(Connection connection) throws SQLException {
                        return connection.prepareCall("{call FIND_ITEMS()}");
                    }
                }
            );
            */

            StoredProcedureQuery query = em.createStoredProcedureQuery(
                "FIND_ITEMS",
                Item.class // Or name of result set mapping
            );

            List<Item> result = query.getResultList();
            for (Item item : result) {
                // ...
            }
            assertEquals(result.size(), 3);

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

    @Test(groups = "MYSQL")
    public void callReturningResultSetNative() throws Exception {
        storeTestData();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            Session session = em.unwrap(Session.class);
            org.hibernate.procedure.ProcedureCall call =
                session.createStoredProcedureCall("FIND_ITEMS", Item.class);

            org.hibernate.result.ResultSetOutput resultSetOutput =
                (org.hibernate.result.ResultSetOutput) call.getOutputs().getCurrent();

            List<Item> result = resultSetOutput.getResultList();
            for (Item item : result) {
                // ...
            }
            assertEquals(result.size(), 3);

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

    @Test(groups = "MYSQL")
    public void callReturningMultipleResults() throws Exception {
        storeTestData();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            /*
            em.unwrap(Session.class).doWork(
                new CallProcedureAndPrintResult() {
                    @Override
                    protected CallableStatement prepare(Connection connection) throws SQLException {
                        return connection.prepareCall("{call APPROVE_ITEMS()}");
                    }
                }
            );
            */

            StoredProcedureQuery query = em.createStoredProcedureQuery(
                "APPROVE_ITEMS",
                Item.class // Or name of result set mapping
            );

            boolean[] expectedResults = new boolean[3];
            int i = 0;

            /* 
               First, execute the procedure call with <code>execute()</code>. This method returns <code>true</code> if
               the first result of the call is a result set and <code>false</code> if the first result is an update
               count.
             */
            boolean isCurrentReturnResultSet = query.execute();
            /* 
               Next, process all results of the call in a loop. You stop looping when there are
               no more results available, which is always indicated by <code>hasMoreResults()</code>
               returning <code>false</code> and <code>getUpdateCount()</code> returning <code>-1</code>.
             */
            while (true) {
                /* 
                   If the current result is a result set, read and process it. Hibernate will map the
                   columns in each result set to managed instances of the <code>Item</code> class.
                   Alternatively, provide a result set mapping name applicable to all result sets
                   returned by the call.
                 */
                if (isCurrentReturnResultSet) {
                    List<Item> result = query.getResultList();
                    // ...
                    // We retrieve two result sets, the first has
                    // one row, the second with two rows
                    if (i == 0) {
                        assertEquals(result.size(), 1);
                    } else if (i == 1) {
                        assertEquals(result.size(), 2);
                    }
                    expectedResults[i] = true;
                } else {
                    /* 
                       If the current result is an update count, <code>getUpdateCount()</code> returns a value
                       greater than <code>-1</code>.
                     */
                    int updateCount = query.getUpdateCount();
                    if (updateCount > -1) {
                        // ...
                        // We also updated one row in the procedure
                        assertEquals(updateCount, 1);
                        expectedResults[i] = true;
                    } else {
                        break; // No more update counts, exit loop
                    }
                }

                /* 
                   The <code>hasMoreResults()</code> method advances to the next result and indicates the
                   type of that result.
                 */
                isCurrentReturnResultSet = query.hasMoreResults();
                i++;
            }

            for (boolean haveResult : expectedResults) {
                assertTrue(haveResult);
            }

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

    @Test(groups = "MYSQL")
    public void callReturningMultipleResultsNative() throws Exception {
        storeTestData();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            {
                Session session = em.unwrap(Session.class);
                org.hibernate.procedure.ProcedureCall call =
                    session.createStoredProcedureCall("APPROVE_ITEMS", Item.class);

                org.hibernate.procedure.ProcedureOutputs callOutputs = call.getOutputs();

                boolean[] expectedResults = new boolean[3];
                int i = 0;

                org.hibernate.result.Output output;
                /* 
                   As long as <code>getCurrent()</code> doesn't return <code>null</code>, there
                   are more outputs to process.
                 */
                while ((output = callOutputs.getCurrent()) != null) {
                    /* 
                       An output might be a result set, test and cast it.
                     */
                    if (output.isResultSet()) {
                        List<Item> result =
                            ((org.hibernate.result.ResultSetOutput) output)
                                .getResultList();
                        // ...
                        // We retrieve two result sets, the first has
                        // one row, the second with two rows
                        if (i == 0) {
                            assertEquals(result.size(), 1);
                        } else if (i == 1) {
                            assertEquals(result.size(), 2);
                        }
                        expectedResults[i] = true;
                    } else {
                        /* 
                           If an output is not a result set, it's an update count.
                         */
                        int updateCount =
                            ((org.hibernate.result.UpdateCountOutput) output)
                                .getUpdateCount();
                        // ...
                        // We also updated one row in the procedure
                        assertEquals(updateCount, 1);
                        expectedResults[i] = true;
                    }
                    /* 
                       Proceed with the next output, if there is any.
                     */
                    if (!callOutputs.goToNext())
                        break;
                    i++;
                }

                for (boolean expectedResult : expectedResults) {
                    assertTrue(expectedResult);
                }
            }
            em.clear();

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

    @Test(groups = "MYSQL")
    public void callWithInOutParameters() throws Exception {
        TestDataCategoriesItems testDataCategoriesItems = storeTestData();
        final Long ITEM_ID = testDataCategoriesItems.items.getFirstId();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            /*
            em.unwrap(Session.class).doWork(
                new CallProcedureAndPrintResult() {
                    @Override
                    protected CallableStatement prepare(Connection connection) throws SQLException {
                        CallableStatement statement = connection.prepareCall("{call FIND_ITEM_TOTAL(?, ?)}");
                        statement.setLong(1, ITEM_ID);
                        statement.registerOutParameter(2, Types.VARCHAR);
                        return statement;
                    }

                    @Override
                    protected ResultSet execute(CallableStatement statement) throws SQLException {
                        statement.execute();
                        System.out.println("### TOTAL NR. OF ITEMS: " + statement.getLong(2));
                        return statement.getResultSet();
                    }
                }
            );
            */

            StoredProcedureQuery query = em.createStoredProcedureQuery(
                "FIND_ITEM_TOTAL",
                Item.class
            );

            /* 
               Register all parameters by position (starting at 1) and their type.
             */
            query.registerStoredProcedureParameter(1, Long.class, ParameterMode.IN);
            query.registerStoredProcedureParameter(2, Long.class, ParameterMode.OUT);

            /* 
               Bind values to the input parameters.
             */
            query.setParameter(1, ITEM_ID);

            /* 
               Retrieve the result set returned by the procedure.
             */
            List<Item> result = query.getResultList();
            for (Item item : result) {
                // ...
                assertEquals(item.getId(), ITEM_ID);
            }

            /* 
               After you've retrieved the result sets, you can access the output parameter values.
             */
            Long totalNumberOfItems = (Long) query.getOutputParameterValue(2);

            assertEquals(result.size(), 1);
            assertEquals(totalNumberOfItems, new Long(3));

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

    @Test(groups = "MYSQL")
    public void callWithInOutParametersNative() throws Exception {
        TestDataCategoriesItems testDataCategoriesItems = storeTestData();
        final Long ITEM_ID = testDataCategoriesItems.items.getFirstId();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            Session session = em.unwrap(Session.class);

            org.hibernate.procedure.ProcedureCall call =
                session.createStoredProcedureCall("FIND_ITEM_TOTAL", Item.class);

            /* 
               Register all parameters; you can bind input values directly.
             */
            call.registerParameter(1, Long.class, ParameterMode.IN)
                .bindValue(ITEM_ID);

            /* 
               Output parameter registrations can be reused later to read the output value.
             */
            ParameterRegistration<Long> totalParameter =
                call.registerParameter(2, Long.class, ParameterMode.OUT);

            org.hibernate.procedure.ProcedureOutputs callOutputs = call.getOutputs();

            boolean expectedResult = false;
            /* 
               Process all returned result sets before you access any output parameters.
             */
            org.hibernate.result.Output output;
            while ((output = callOutputs.getCurrent()) != null) {
                if (output.isResultSet()) {
                    org.hibernate.result.ResultSetOutput resultSetOutput =
                        (org.hibernate.result.ResultSetOutput) output;
                    List<Item> result = resultSetOutput.getResultList();
                    for (Item item : result) {
                        // ...
                        assertEquals(item.getId(), ITEM_ID);
                    }
                    assertEquals(result.size(), 1);
                    expectedResult = true;
                }
                if (!callOutputs.goToNext())
                    break;
            }

            /* 
               Access the output parameter value through the registration.
             */
            Long totalNumberOfItems = callOutputs.getOutputParameterValue(totalParameter);

            assertTrue(expectedResult);
            assertEquals(totalNumberOfItems, new Long(3));

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

    @Test(groups = "MYSQL")
    public void callUpdateCount() throws Exception {
        TestDataCategoriesItems testDataCategoriesItems = storeTestData();
        final Long ITEM_ID = testDataCategoriesItems.items.getFirstId();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            /*
            em.unwrap(Session.class).doWork(
                new CallProcedureAndPrintResult() {
                    @Override
                    protected CallableStatement prepare(Connection connection) throws SQLException {
                        CallableStatement statement = connection.prepareCall("{call UPDATE_ITEM(?, ?)}");
                        statement.setLong(1, ITEM_ID);
                        statement.setString(2, "New Name");
                        return statement;
                    }

                    @Override
                    protected ResultSet execute(CallableStatement statement) throws SQLException {
                        // MySQL executes ROW_COUNT() when executeUpdate() is called, so the
                        // update count depends on the last INSERT/UPDATE statement in the procedure!
                        int updateCount = statement.executeUpdate();
                        System.out.println("### UPDATE COUNT: " + updateCount);
                        return null;
                    }
                }
            );
            */

            StoredProcedureQuery query = em.createStoredProcedureQuery(
                "UPDATE_ITEM"
            );

            query.registerStoredProcedureParameter("itemId", Long.class, ParameterMode.IN);
            query.registerStoredProcedureParameter("name", String.class, ParameterMode.IN);
            query.setParameter("itemId", ITEM_ID);
            query.setParameter("name", "New Item Name");

            assertEquals(query.executeUpdate(), 1); // Update count is 1

            // Alternative:
            // assertFalse(query.execute()); // First result is NOT a result set
            // assertEquals(query.getUpdateCount(), 1);

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

    @Test(groups = "MYSQL")
    public void callUpdateCountNative() throws Exception {
        TestDataCategoriesItems testDataCategoriesItems = storeTestData();
        final Long ITEM_ID = testDataCategoriesItems.items.getFirstId();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            Session session = em.unwrap(Session.class);
            org.hibernate.procedure.ProcedureCall call =
                session.createStoredProcedureCall("UPDATE_ITEM");

            call.registerParameter(1, Long.class, ParameterMode.IN)
                .bindValue(ITEM_ID);

            call.registerParameter(2, String.class, ParameterMode.IN)
                .bindValue("New Item Name");

            org.hibernate.result.UpdateCountOutput updateCountOutput =
                (org.hibernate.result.UpdateCountOutput) call.getOutputs().getCurrent();

            assertEquals(updateCountOutput.getUpdateCount(), 1);

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

    @Test(groups = "POSTGRESQL")
    public void callReturningRefCursor() throws Exception {
        storeTestData();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            /*
            em.unwrap(Session.class).doWork(
                new CallProcedureAndPrintResult() {
                    @Override
                    protected CallableStatement prepare(Connection connection) throws SQLException {
                        CallableStatement statement = connection.prepareCall("{? = call FIND_ITEMS()}");
                        statement.registerOutParameter(1, Types.OTHER);
                        return statement;
                    }

                    @Override
                    protected ResultSet execute(CallableStatement statement) throws SQLException {
                        statement.execute();
                        return (ResultSet)statement.getObject(1);
                    }
                }
            );
            */

            StoredProcedureQuery query = em.createStoredProcedureQuery(
                "FIND_ITEMS",
                Item.class
            );

            query.registerStoredProcedureParameter(
                1,
                void.class,
                ParameterMode.REF_CURSOR
            );

            List<Item> result = query.getResultList();
            for (Item item : result) {
                // ...
            }
            assertEquals(result.size(), 3);

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

    @Test(groups = "POSTGRESQL")
    public void callReturningRefCursorNative() throws Exception {
        storeTestData();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            {
                Session session = em.unwrap(Session.class);

                org.hibernate.procedure.ProcedureCall call =
                    session.createStoredProcedureCall("FIND_ITEMS", Item.class);

                call.registerParameter(1, void.class, ParameterMode.REF_CURSOR);

                org.hibernate.result.ResultSetOutput resultSetOutput =
                    (org.hibernate.result.ResultSetOutput) call.getOutputs().getCurrent();

                List<Item> result = resultSetOutput.getResultList();
                for (Item item : result) {
                    // ...
                }
                assertEquals(result.size(), 3);
            }

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

    /* ######################################################################################### */

    public abstract class CallProcedureAndPrintResult implements Work {

        @Override
        public void execute(Connection connection) throws SQLException {
            CallableStatement statement = null;
            ResultSet result = null;
            try {
                statement = prepare(connection);
                result = execute(statement);
                if (result != null && !result.isClosed())
                    printResultSet(result);
                System.out.println("### STATEMENT UPDATE COUNT: " + statement.getUpdateCount());
            } finally {
                if (result != null)
                    result.close();
                if (statement != null)
                    statement.close();
            }

        }

        protected void printResultSet(ResultSet result) throws SQLException {
            ResultSetMetaData meta = result.getMetaData();

            System.out.println("##################################################################");
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                String label = meta.getColumnLabel(i);
                System.out.println(label + " (" + meta.getColumnTypeName(i) + ")");
            }
            System.out.println("##################################################################");
            while (result.next()) {
                System.out.println("---------------------------------------------------");
                for (int i = 1; i <= meta.getColumnCount(); i++) {
                    String label = meta.getColumnLabel(i);
                    System.out.print(result.getString(label) + ", ");
                }
                System.out.println();
            }
            System.out.println("##################################################################");
        }

        protected abstract CallableStatement prepare(Connection connection) throws SQLException;

        protected ResultSet execute(CallableStatement statement) throws SQLException {
            statement.execute();
            ResultSet currentResultSet = statement.getResultSet();
            printResultSet(currentResultSet);
            while (statement.getMoreResults()) {
                System.out.println("======================== NEXT RESULTSET ===========================");
                currentResultSet = statement.getResultSet();
                printResultSet(currentResultSet);
            }
            return currentResultSet;
        }
    }
}
