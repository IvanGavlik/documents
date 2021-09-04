package org.jpwh.test.customsql;

import org.jpwh.env.JPATest;
import org.jpwh.model.customsql.procedures.User;
import org.jpwh.shared.FetchTestLoadEventListener;
import org.jpwh.shared.util.TestData;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import static org.testng.Assert.*;

public class CRUDProcedures extends JPATest {

    FetchTestLoadEventListener loadEventListener;

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("CRUDProceduresPU", "customsql/CRUDProcedures.hbm.xml");
    }

    @Override
    public void afterJPABootstrap() throws Exception {
        loadEventListener = new FetchTestLoadEventListener(JPA.getEntityManagerFactory());
    }

    class CustomSQLTestData {
        TestData users;
    }

    public CustomSQLTestData create() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        tx.begin();
        EntityManager em = JPA.createEntityManager();

        CustomSQLTestData testData = new CustomSQLTestData();
        testData.users = new TestData(new Long[2]);

        User johndoe = new User("johndoe");
        em.persist(johndoe);
        testData.users.identifiers[0] = johndoe.getId();

        tx.commit();
        em.close();

        return testData;
    }

    @Test(groups = "MYSQL")
    public void read() throws Exception {
        CustomSQLTestData testData = create();
        Long USER_ID = testData.users.getFirstId();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            {
                User user = em.find(User.class, USER_ID);
                assertEquals(loadEventListener.getLoadCount(User.class), 1);
                assertEquals(user.getId(), USER_ID);
            }
            em.clear();
            loadEventListener.reset();

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

    @Test(groups = "MYSQL")
    public void update() throws Exception {
        CustomSQLTestData testData = create();
        Long USER_ID = testData.users.getFirstId();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            {
                User user = em.find(User.class, USER_ID);
                user.setUsername("jdoe");
                em.flush();

            }
            em.clear();
            loadEventListener.reset();

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

    @Test(groups = "MYSQL")
    public void delete() throws Exception {
        CustomSQLTestData testData = create();
        Long USER_ID = testData.users.getFirstId();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            {
                User user = em.find(User.class, USER_ID);
                em.remove(user);
                em.flush();
                em.clear();

                assertNull(em.find(User.class, USER_ID));
            }
            em.clear();
            loadEventListener.reset();

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }
}
