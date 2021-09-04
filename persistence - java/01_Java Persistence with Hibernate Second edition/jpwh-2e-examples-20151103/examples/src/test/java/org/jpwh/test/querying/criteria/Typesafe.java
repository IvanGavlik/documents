package org.jpwh.test.querying.criteria;

import org.jpwh.model.querying.Address_;
import org.jpwh.model.querying.Bid;
import org.jpwh.model.querying.Bid_;
import org.jpwh.model.querying.Item;
import org.jpwh.model.querying.Item_;
import org.jpwh.model.querying.User;
import org.jpwh.model.querying.User_;
import org.jpwh.test.querying.QueryingTest;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class Typesafe extends QueryingTest {

    @Test
    public void executeQueries() throws Exception {
        TestDataCategoriesItems testData = storeTestData();

        CriteriaBuilder cb =
           JPA.getEntityManagerFactory().getCriteriaBuilder();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();
            { // Typesafe path navigation

                CriteriaQuery<User> criteria = cb.createQuery(User.class);
                Root<User> u = criteria.from(User.class);
                criteria.select(u).where(
                   cb.equal(
                      u.get(User_.homeAddress).get(Address_.city),
                      "Some City"));

                TypedQuery<User> q = em.createQuery(criteria);
                User user = q.getSingleResult();
                assertEquals(user.getId(), testData.users.getFirstId());
            }
            em.clear();
            { // Typesafe operands for joins and restriction

                CriteriaQuery<Item> criteria = cb.createQuery(Item.class);
                Root<Item> i = criteria.from(Item.class);
                Join<Item, Bid> b = i.join(Item_.bids);
                criteria.select(i).where(
                   cb.gt(b.get(Bid_.amount), new BigDecimal(100)));
                   // cb.gt(b.get(Bid_.amount), "100")); // Wouldn't compile!

                TypedQuery<Item> q = em.createQuery(criteria);
                List<Item> result = q.getResultList();
                assertEquals(result.size(), 1);
                assertEquals(result.get(0).getId(), testData.items.getFirstId());
            }
            em.clear();

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

}
