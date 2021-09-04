package org.jpwh.test.filtering;

import org.hibernate.Session;
import org.hibernate.engine.spi.SessionImplementor;
import org.jpwh.env.JPATest;
import org.jpwh.model.filtering.interceptor.AuditLogRecord;
import org.jpwh.model.filtering.interceptor.Item;
import org.jpwh.model.filtering.interceptor.User;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class AuditLogging extends JPATest {

    final private static Logger log = Logger.getLogger(AuditLogging.class.getName());

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("FilteringInterceptorPU");
    }

    @Test
    public void writeAuditLog() throws Throwable {
        UserTransaction tx = TM.getUserTransaction();
        try {

            Long CURRENT_USER_ID;
            {
                tx.begin();
                EntityManager em = JPA.createEntityManager();
                User currentUser = new User("johndoe");
                em.persist(currentUser);
                tx.commit();
                em.close();
                CURRENT_USER_ID = currentUser.getId();
            }

            EntityManagerFactory emf = JPA.getEntityManagerFactory();

            Map<String, String> properties = new HashMap<String, String>();
            properties.put(
                org.hibernate.jpa.AvailableSettings.SESSION_INTERCEPTOR,
                AuditLogInterceptor.class.getName()
            );

            EntityManager em = emf.createEntityManager(properties);

            Session session = em.unwrap(Session.class);
            AuditLogInterceptor interceptor =
                (AuditLogInterceptor) ((SessionImplementor) session).getInterceptor();
            interceptor.setCurrentSession(session);
            interceptor.setCurrentUserId(CURRENT_USER_ID);

            tx.begin();
            em.joinTransaction();
            Item item = new Item("Foo");
            em.persist(item);
            tx.commit();
            em.clear();

            tx.begin();
            em.joinTransaction();
            List<AuditLogRecord> logs = em.createQuery(
                "select lr from AuditLogRecord lr",
                AuditLogRecord.class
            ).getResultList();
            assertEquals(logs.size(), 1);
            assertEquals(logs.get(0).getMessage(), "insert");
            assertEquals(logs.get(0).getEntityClass(), Item.class);
            assertEquals(logs.get(0).getEntityId(), item.getId());
            assertEquals(logs.get(0).getUserId(), CURRENT_USER_ID);
            em.createQuery("delete AuditLogRecord").executeUpdate();
            tx.commit();
            em.clear();

            tx.begin();
            em.joinTransaction();
            item = em.find(Item.class, item.getId());
            item.setName("Bar");
            tx.commit();
            em.clear();

            tx.begin();
            em.joinTransaction();
            logs = em.createQuery(
                "select lr from AuditLogRecord lr",
                AuditLogRecord.class
            ).getResultList();
            assertEquals(logs.size(), 1);
            assertEquals(logs.get(0).getMessage(), "update");
            assertEquals(logs.get(0).getEntityClass(), Item.class);
            assertEquals(logs.get(0).getEntityId(), item.getId());
            assertEquals(logs.get(0).getUserId(), CURRENT_USER_ID);
            tx.commit();
            em.close();

        } finally {
            TM.rollback();
        }
    }

}
