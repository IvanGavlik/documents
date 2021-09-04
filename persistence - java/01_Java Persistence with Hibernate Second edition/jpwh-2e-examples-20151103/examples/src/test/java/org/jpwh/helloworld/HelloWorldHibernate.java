package org.jpwh.helloworld;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;
import org.hibernate.resource.transaction.backend.jta.internal.JtaTransactionCoordinatorBuilderImpl;
import org.hibernate.service.ServiceRegistry;
import org.jpwh.env.TransactionManagerTest;
import org.jpwh.model.helloworld.Message;
import org.testng.annotations.Test;

import javax.transaction.UserTransaction;
import java.util.List;

import static org.testng.Assert.*;

public class HelloWorldHibernate extends TransactionManagerTest {

    protected void unusedSimpleBoot() {
        SessionFactory sessionFactory = new MetadataSources(
            new StandardServiceRegistryBuilder()
                .configure("hibernate.cfg.xml").build()
        ).buildMetadata().buildSessionFactory();
    }

    protected SessionFactory createSessionFactory() {

        /* 
            This builder helps you create the immutable service registry with
            chained method calls.
         */
        StandardServiceRegistryBuilder serviceRegistryBuilder =
            new StandardServiceRegistryBuilder();

        /* 
            Configure the services registry by applying settings.
         */
        serviceRegistryBuilder
            .applySetting("hibernate.connection.datasource", "myDS")
            .applySetting("hibernate.format_sql", "true")
            .applySetting("hibernate.use_sql_comments", "true")
            .applySetting("hibernate.hbm2ddl.auto", "create-drop");

        // Enable JTA (this is a bit crude because Hibernate devs still believe that JTA is
        // used only in monstrous application servers and you'll never see this code).
        serviceRegistryBuilder.applySetting(
            Environment.TRANSACTION_COORDINATOR_STRATEGY,
            JtaTransactionCoordinatorBuilderImpl.class
        );
        ServiceRegistry serviceRegistry = serviceRegistryBuilder.build();

        /* 
            You can only enter this configuration stage with an existing service registry.
         */
        MetadataSources metadataSources = new MetadataSources(serviceRegistry);

        /* 
            Add your persistent classes to the (mapping) metadata sources.
         */
        metadataSources.addAnnotatedClass(
            org.jpwh.model.helloworld.Message.class
        );

        // Add hbm.xml mapping files
        // metadataSources.addFile(...);

        // Read all hbm.xml mapping files from a JAR
        // metadataSources.addJar(...)

        MetadataBuilder metadataBuilder = metadataSources.getMetadataBuilder();

        Metadata metadata = metadataBuilder.build();

        assertEquals(metadata.getEntityBindings().size(), 1);

        SessionFactory sessionFactory = metadata.buildSessionFactory();

        return sessionFactory;
    }

    @Test
    public void storeLoadMessage() throws Exception {
        SessionFactory sessionFactory = createSessionFactory();
        try {
            {
                /* 
                    Get access to the standard transaction API <code>UserTransaction</code> and
                    begin a transaction on this thread of execution.
                 */
                UserTransaction tx = TM.getUserTransaction();
                tx.begin();

                /* 
                    Whenever you call <code>getCurrentSession()</code> in the same thread you get
                    the same <code>org.hibernate.Session</code>. It's bound automatically to the
                    ongoing transaction and is closed for you automatically when that transaction
                    commits or rolls back.
                 */
                Session session = sessionFactory.getCurrentSession();

                Message message = new Message();
                message.setText("Hello World!");

                /* 
                    The native Hibernate API is very similar to the standard Java Persistence API and most methods
                    have the same name.
                 */
                session.persist(message);

                /* 
                    Hibernate synchronizes the session with the database and closes the "current"
                    session on commit of the bound transaction automatically.
                 */
                tx.commit();
                // INSERT into MESSAGE (ID, TEXT) values (1, 'Hello World!')
            }

            {
                UserTransaction tx = TM.getUserTransaction();
                tx.begin();

                /* 
                    A Hibernate criteria query is a type-safe programmatic way to express queries,
                    automatically translated into SQL.
                 */
                List<Message> messages =
                    sessionFactory.getCurrentSession().createCriteria(
                        Message.class
                    ).list();
                // SELECT * from MESSAGE

                assertEquals(messages.size(), 1);
                assertEquals(messages.get(0).getText(), "Hello World!");

                tx.commit();
            }

        } finally {
            TM.rollback();
        }
    }
}
