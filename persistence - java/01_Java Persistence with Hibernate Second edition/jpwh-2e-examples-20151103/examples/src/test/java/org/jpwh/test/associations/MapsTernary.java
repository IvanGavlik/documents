package org.jpwh.test.associations;


import org.jpwh.env.JPATest;
import org.jpwh.model.associations.maps.ternary.Category;
import org.jpwh.model.associations.maps.ternary.Item;
import org.jpwh.model.associations.maps.ternary.User;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import static org.testng.Assert.assertEquals;

public class MapsTernary extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("MapsTernaryPU");
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

            someCategory.getItemAddedBy().put(someItem, someUser);
            someCategory.getItemAddedBy().put(otherItem, someUser);
            otherCategory.getItemAddedBy().put(someItem, someUser);

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

            assertEquals(category1.getItemAddedBy().size(), 2);

            assertEquals(category2.getItemAddedBy().size(), 1);

            assertEquals(category2.getItemAddedBy().keySet().iterator().next(), item1);
            assertEquals(category2.getItemAddedBy().values().iterator().next(), user);

            tx.commit();
            em.close();

        } finally {
            TM.rollback();
        }
    }

}