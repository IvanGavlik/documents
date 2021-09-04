package org.jpwh.test.filtering;

import org.hibernate.Session;
import org.jpwh.env.JPATest;
import org.jpwh.model.filtering.dynamic.Category;
import org.jpwh.model.filtering.dynamic.Item;
import org.jpwh.model.filtering.dynamic.User;
import org.jpwh.shared.util.TestData;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.transaction.UserTransaction;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class DynamicFilter extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("FilteringDynamicPU");
    }

    class DynamicFilterTestData {
        TestData categories;
        TestData items;
        TestData users;
    }

    public DynamicFilterTestData storeTestData() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        tx.begin();
        EntityManager em = JPA.createEntityManager();

        DynamicFilterTestData testData = new DynamicFilterTestData();

        testData.users = new TestData(new Long[2]);
        User johndoe = new User("johndoe");
        em.persist(johndoe);
        testData.users.identifiers[0] = johndoe.getId();
        User janeroe = new User("janeroe", 100);
        em.persist(janeroe);
        testData.users.identifiers[1] = janeroe.getId();

        testData.categories = new TestData(new Long[2]);
        Category categoryOne = new Category("One");
        em.persist(categoryOne);
        testData.categories.identifiers[0] = categoryOne.getId();
        Category categoryTwo = new Category("Two");
        em.persist(categoryTwo);
        testData.categories.identifiers[1] = categoryTwo.getId();

        testData.items = new TestData(new Long[3]);
        Item itemFoo = new Item("Foo", categoryOne, johndoe);
        em.persist(itemFoo);
        testData.items.identifiers[0] = itemFoo.getId();
        Item itemBar = new Item("Bar", categoryOne, janeroe);
        em.persist(itemBar);
        testData.items.identifiers[1] = itemBar.getId();
        Item itemBaz = new Item("Baz", categoryTwo, janeroe);
        em.persist(itemBaz);
        testData.items.identifiers[2] = itemBaz.getId();

        tx.commit();
        em.close();
        return testData;
    }

    @Test
    public void filterItems() throws Throwable {
        DynamicFilterTestData testData = storeTestData();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            {

                org.hibernate.Filter filter = em.unwrap(Session.class)
                    .enableFilter("limitByUserRank");

                filter.setParameter("currentUserRank", 0);

                {
                    List<Item> items = em.createQuery("select i from Item i").getResultList();
                    // select * from ITEM where 0 >=
                    //  (select u.RANK from USERS u  where u.ID = SELLER_ID)
                    assertEquals(items.size(), 1);
                }
                em.clear();
                {
                    CriteriaBuilder cb = em.getCriteriaBuilder();
                    CriteriaQuery criteria = cb.createQuery();
                    criteria.select(criteria.from(Item.class));
                    List<Item> items = em.createQuery(criteria).getResultList();
                    // select * from ITEM where 0 >=
                    //  (select u.RANK from USERS u  where u.ID = SELLER_ID)
                    assertEquals(items.size(), 1);
                }
                em.clear();

                filter.setParameter("currentUserRank", 100);
                List<Item >items =
                    em.createQuery("select i from Item i")
                        .getResultList();
                assertEquals(items.size(), 3);
            }
            em.clear();

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

    @Test
    public void filterCollection() throws Throwable {
        DynamicFilterTestData testData = storeTestData();

        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            Long CATEGORY_ID = testData.categories.getFirstId();
            {

                org.hibernate.Filter filter = em.unwrap(Session.class)
                    .enableFilter("limitByUserRank");

                filter.setParameter("currentUserRank", 0);
                Category category = em.find(Category.class, CATEGORY_ID);
                assertEquals(category.getItems().size(), 1);

                em.clear();

                filter.setParameter("currentUserRank", 100);
                category = em.find(Category.class, CATEGORY_ID);
                assertEquals(category.getItems().size(), 2);
            }
            em.clear();

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

}
