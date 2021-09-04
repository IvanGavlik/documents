package org.jpwh;

import org.dbunit.JndiDatabaseTester;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.operation.DatabaseOperation;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import java.io.InputStream;

// This bean will only execute when the application is deployed, it's not included in tests
@ApplicationScoped
public class SampleDataImporter {

    public void onStartup(@Observes @Initialized(ApplicationScoped.class) Object event) throws Exception {
        InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream("testdata.xml");
        IDatabaseConnection connection = new JndiDatabaseTester("java:jboss/datasources/ExampleDS").getConnection();
        DatabaseOperation.CLEAN_INSERT.execute(connection, new FlatXmlDataSetBuilder().build(input));
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception ex) {
                ex.printStackTrace(System.err);
            }
        } else {
            System.err.println("Can't import test data, check connection settings!");
        }
    }
}
