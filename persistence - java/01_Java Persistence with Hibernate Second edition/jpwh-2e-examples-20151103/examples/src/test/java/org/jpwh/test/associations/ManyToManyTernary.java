package org.jpwh.test.associations;


import org.jpwh.env.JPATest;
import org.jpwh.model.associations.manytomany.ternary.CategorizedItem;
import org.jpwh.model.associations.manytomany.ternary.Category;
import org.jpwh.model.associations.manytomany.ternary.Item;
import org.jpwh.model.associations.manytomany.ternary.User;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import java.util.List;

import static org.testng.Assert.assertEquals;

public class ManyToManyTernary extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("ManyToManyTernaryPU");
    }

    @Test
    public void storeLoadCategoryItems() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            Category someCategory = new Category("Some Category");
            Category otherCategory = new Category("Other Category");
            em.persist(someCategory);
            em.persist(otherCategory);

            Item someItem = new Item("Some Item");
            Item otherItem = new Item("Other Item");
            em.persist(someItem);
            em.persist(otherItem);

            User someUser = new User("johndoe");
            em.persist(someUser);

            CategorizedItem linkOne = new CategorizedItem(
                someUser, someItem
            );
            someCategory.getCategorizedItems().add(linkOne);

            CategorizedItem linkTwo = new CategorizedItem(
                someUser, otherItem
            );
            someCategory.getCategorizedItems().add(linkTwo);

            CategorizedItem linkThree = new CategorizedItem(
                someUser, someItem
            );
            otherCategory.getCategorizedItems().add(linkThree);

            tx.commit();
            em.close();

            Long CATEGORY_ID = someCategory.getId();
            Long OTHER_CATEGORY_ID = otherCategory.getId();
            Long ITEM_ID = someItem.getId();
            Long USER_ID = someUser.getId();

            tx.begin();
            em = JPA.createEntityManager();

            Category category1 = em.find(Category.class, CATEGORY_ID);
            Category category2 = em.find(Category.class, OTHER_CATEGORY_ID);

            Item item1 = em.find(Item.class, ITEM_ID);

            User user = em.find(User.class, USER_ID);

            assertEquals(category1.getCategorizedItems().size(), 2);

            assertEquals(category2.getCategorizedItems().size(), 1);

            assertEquals(category2.getCategorizedItems().iterator().next().getItem(), item1);
            assertEquals(category2.getCategorizedItems().iterator().next().getAddedBy(), user);

            tx.commit();
            em.close();

            tx.begin();
            em = JPA.createEntityManager();

            Item item = em.find(Item.class, ITEM_ID);

            List<Category> categoriesOfItem =
                em.createQuery(
                    "select c from Category c " +
                        "join c.categorizedItems ci " +
                        "where ci.item = :itemParameter")
                .setParameter("itemParameter", item)
                .getResultList();

            assertEquals(categoriesOfItem.size(), 2);

            tx.commit();
            em.close();

        } finally {
            TM.rollback();
        }
    }

}