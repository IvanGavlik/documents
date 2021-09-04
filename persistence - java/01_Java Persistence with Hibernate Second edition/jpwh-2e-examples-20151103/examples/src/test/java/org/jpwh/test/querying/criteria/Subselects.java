package org.jpwh.test.querying.criteria;

import org.jpwh.model.querying.Bid;
import org.jpwh.model.querying.Item;
import org.jpwh.model.querying.User;
import org.jpwh.test.querying.QueryingTest;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.transaction.UserTransaction;
import java.math.BigDecimal;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class Subselects extends QueryingTest {

    @Test
    public void executeQueries() throws Exception {
        TestDataCategoriesItems testData = storeTestData();

        CriteriaBuilder cb =
            JPA.getEntityManagerFactory().getCriteriaBuilder();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            { // Correlated

                CriteriaQuery criteria = cb.createQuery();
                Root<User> u = criteria.from(User.class);

                Subquery<Long> sq = criteria.subquery(Long.class);
                Root<Item> i = sq.from(Item.class);
                sq.select(cb.count(i))
                    .where(cb.equal(i.get("seller"), u)
                    );

                criteria.select(u);
                criteria.where(cb.greaterThan(sq, 1L));

                Query q = em.createQuery(criteria);
                List<User> result = q.getResultList();
                assertEquals(result.size(), 1);
                User user = result.iterator().next();
                assertEquals(user.getId(), testData.users.getFirstId());
            }
            em.clear();

            { // Uncorrelated

                CriteriaQuery criteria = cb.createQuery();
                Root<Bid> b = criteria.from(Bid.class);

                Subquery<BigDecimal> sq = criteria.subquery(BigDecimal.class);
                Root<Bid> b2 = sq.from(Bid.class);
                sq.select(cb.max(b2.<BigDecimal>get("amount")));

                criteria.select(b);
                criteria.where(
                    cb.greaterThanOrEqualTo(
                        cb.sum(b.<BigDecimal>get("amount"), new BigDecimal(1)),
                        sq
                    )
                );

                Query q = em.createQuery(criteria);
                List<Bid> result = q.getResultList();
                assertEquals(result.size(), 2);
            }
            em.clear();

            { // Exists

                CriteriaQuery criteria = cb.createQuery();
                Root<Item> i = criteria.from(Item.class);

                Subquery<Bid> sq = criteria.subquery(Bid.class);
                Root<Bid> b = sq.from(Bid.class);
                sq.select(b).where(cb.equal(b.get("item"), i));

                criteria.select(i);
                criteria.where(cb.exists(sq));

                Query q = em.createQuery(criteria);
                List<Item> result = q.getResultList();
                assertEquals(result.size(), 2);
            }
            em.clear();

            { // Quantify ALL

                CriteriaQuery criteria = cb.createQuery();
                Root<Item> i = criteria.from(Item.class);

                Subquery<BigDecimal> sq = criteria.subquery(BigDecimal.class);
                Root<Bid> b = sq.from(Bid.class);
                sq.select(b.<BigDecimal>get("amount"));
                sq.where(cb.equal(b.get("item"), i));

                criteria.select(i);
                criteria.where(
                    cb.greaterThanOrEqualTo(
                        cb.literal(new BigDecimal(10)),
                        cb.all(sq)
                    )
                );

                Query q = em.createQuery(criteria);
                List<Item> result = q.getResultList();
                assertEquals(result.size(), 2);
            }
            em.clear();

            {// Quantify ANY

                CriteriaQuery criteria = cb.createQuery();
                Root<Item> i = criteria.from(Item.class);

                Subquery<BigDecimal> sq = criteria.subquery(BigDecimal.class);
                Root<Bid> b = sq.from(Bid.class);
                sq.select(b.<BigDecimal>get("amount"));
                sq.where(cb.equal(b.get("item"), i));

                criteria.select(i);
                criteria.where(
                    cb.equal(
                        cb.literal(new BigDecimal("101.00")),
                        cb.any(sq)
                    )
                );

                Query q = em.createQuery(criteria);
                List<Item> result = q.getResultList();
                assertEquals(result.size(), 1);
            }
            em.clear();

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

}
