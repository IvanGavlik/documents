package org.jpwh.env;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

import java.io.*;

/**
 * Starts and stops the JPA environment before/after a test class.
 * <p>
 * Create a subclass to write unit tests. Access the <code>EntityManagerFactory</code>
 * with {@link JPATest#JPA} and create <code>EntityManager</code> instances.
 * </p>
 * <p>
 * Drops and creates the SQL database schema of the persistence unit before and after
 * every test method. This means your database will be cleaned for every test method.
 * </p>
 * <p>
 * Override the {@link #configurePersistenceUnit} method to provide a custom
 * persistence unit name or additional <code>hbm.xml</code> file names to load for
 * your test class.
 * </p>
 * <p>
 * Override the {@link #afterJPABootstrap()} method to execute operations before the
 * test method but after the <code>EntityManagerFactory</code> is ready. At this point
 * you can create an <code>EntityManager</code> or <code>Session#doWork(JDBC)</code>. If
 * cleanup is needed, override the {@link #beforeJPAClose()} method.
 * </p>
 */
public class JPATest extends TransactionManagerTest {

    public String persistenceUnitName;
    public String[] hbmResources;
    public JPASetup JPA;

    @BeforeClass
    public void beforeClass() throws Exception {
        configurePersistenceUnit();
    }

    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit(null);
    }

    public void configurePersistenceUnit(String persistenceUnitName,
                                         String... hbmResources) throws Exception {
        this.persistenceUnitName = persistenceUnitName;
        this.hbmResources = hbmResources;
    }

    @BeforeMethod
    public void beforeMethod() throws Exception {
        JPA = new JPASetup(TM.databaseProduct, persistenceUnitName, hbmResources);
        // Always drop the schema, cleaning up at least some of the artifacts
        // that might be left over from the last run, if it didn't cleanup
        // properly
        JPA.dropSchema();

        JPA.createSchema();
        afterJPABootstrap();
    }

    public void afterJPABootstrap() throws Exception {
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod() throws Exception {
        if (JPA != null) {
            beforeJPAClose();
            if (!"true".equals(System.getProperty("keepSchema"))) {
                JPA.dropSchema();
            }
            JPA.getEntityManagerFactory().close();
        }
    }

    public void beforeJPAClose() throws Exception {

    }

    protected long copy(Reader input, Writer output) throws IOException {
        char[] buffer = new char[4096];
        long count = 0;
        int n;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    protected String getTextResourceAsString(String resource) throws IOException {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(resource);
        if (is == null) {
            throw new IllegalArgumentException("Resource not found: " + resource);
        }
        StringWriter sw = new StringWriter();
        copy(new InputStreamReader(is), sw);
        return sw.toString();
    }

    protected Throwable unwrapRootCause(Throwable throwable) {
        return unwrapCauseOfType(throwable, null);
    }

    protected Throwable unwrapCauseOfType(Throwable throwable, Class<? extends Throwable> type) {
        for (Throwable current = throwable; current != null; current = current.getCause()) {
            if (type != null && type.isAssignableFrom(current.getClass()))
                return current;
            throwable = current;
        }
        return throwable;
    }
}
