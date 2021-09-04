package org.jpwh.test.querying.jpql;

import org.jpwh.model.querying.Category;
import org.jpwh.model.querying.Item;
import org.jpwh.test.querying.QueryingTest;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.UserTransaction;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class Restriction extends QueryingTest {

    @Test
    public void executeQueries() throws Exception {
        TestDataCategoriesItems testData = storeTestData();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();
            {
                Query q = em.createNamedQuery("equalsString");
                assertEquals(q.getResultList().size(), 1);
            }
            em.clear();
            {
                Query q = em.createNamedQuery("equalsBoolean");
                assertEquals(q.getResultList().size(), 2);
            }
            em.clear();
            {
                Query q = em.createNamedQuery("between");
                assertEquals(q.getResultList().size(), 3);
            }
            em.clear();
            {
                Query q = em.createNamedQuery("greaterThan");
                assertEquals(q.getResultList().size(), 1);
            }
            em.clear();
            {
                Query q = em.createNamedQuery("inList");
                assertEquals(q.getResultList().size(), 2);
            }
            em.clear();
            {
                Query q = em.createNamedQuery("tupleComparison");
                assertEquals(q.getResultList().size(), 2);
            }
            em.clear();
            {
                Query q = em.createNamedQuery("enum");
                assertEquals(q.getResultList().size(), 3);
            }
            em.clear();
            {
                Query q = em.createNamedQuery("isNull");
                assertEquals(q.getResultList().size(), 2);
            }
            em.clear();
            {
                Query q = em.createNamedQuery("isNotNull");
                assertEquals(q.getResultList().size(), 1);
            }
            em.clear();
            {
                Query q = em.createNamedQuery("likeEnd");
                assertEquals(q.getResultList().size(), 1);
            }
            em.clear();
            {
                Query q = em.createNamedQuery("likeEndNot");
                assertEquals(q.getResultList().size(), 2);
            }
            em.clear();
            {
                Query q = em.createNamedQuery("likeSubstring");
                assertEquals(q.getResultList().size(), 3);
            }
            em.clear();
            {
                Query query = em.createNamedQuery("likeEscape");
                query.setParameter("escapeChar", "\\");
                assertEquals(query.getResultList().size(), 0);
            }
            em.clear();
            {
                Query q = em.createNamedQuery("arithmetic");
                assertEquals(q.getResultList().size(), 2);
            }
            em.clear();
            {
                Query q = em.createNamedQuery("logicalGroups");
                assertEquals(q.getResultList().size(), 2);
            }
            em.clear();
            {
                Query q = em.createNamedQuery("functionsLower");
                assertEquals(q.getResultList().size(), 2);
            }
            {
                Query q = em.createNamedQuery("collectionNotEmpty");
                assertEquals(q.getResultList().size(), 2);
            }
            em.clear();
            {
                Query q = em.createNamedQuery("memberOf");
                Item item = em.find(Item.class, testData.items.getFirstId());
                q.setParameter("item", item);
                List<Category> result = q.getResultList();
                assertEquals(result.size(), 1);
            }
            em.clear();
            {
                Query q = em.createNamedQuery("collectionSize");
                assertEquals(q.getResultList().size(), 1);
            }
            em.clear();
            {
                Query q = em.createNamedQuery("orderbyUsername");
                assertEquals(q.getResultList().size(), 3);
            }
            em.clear();
            {
                Query q = em.createNamedQuery("orderbyUsernameDesc");
                assertEquals(q.getResultList().size(), 3);
            }
            em.clear();
            {
                Query q = em.createNamedQuery("orderby");
                assertEquals(q.getResultList().size(), 3);
            }
            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

    @Test(groups = {"H2"})
    public void executeQueriesWithFunctions() throws Exception {
        TestDataCategoriesItems testData = storeTestData();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();
            {
                Query q = em.createNamedQuery("functionsDateDiff");
                assertEquals(q.getResultList().size(), 1);
            }
            em.clear();
            {
                Query q = em.createNamedQuery("functionsDateDiffHibernate");
                assertEquals(q.getResultList().size(), 1);
            }
            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }
}
