package org.jpwh.test.associations;


import org.jpwh.env.JPATest;
import org.jpwh.model.associations.manytomany.linkentity.CategorizedItem;
import org.jpwh.model.associations.manytomany.linkentity.Category;
import org.jpwh.model.associations.manytomany.linkentity.Item;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import static org.testng.Assert.assertEquals;

public class ManyToManyLinkEntity extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("ManyToManyLinkEntityPU");
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

            CategorizedItem linkOne = new CategorizedItem(
                "johndoe", someCategory, someItem
            );

            CategorizedItem linkTwo = new CategorizedItem(
                "johndoe", someCategory, otherItem
            );

            CategorizedItem linkThree = new CategorizedItem(
                "johndoe", otherCategory, someItem
            );

            em.persist(linkOne);
            em.persist(linkTwo);
            em.persist(linkThree);

            tx.commit();
            em.close();

            Long CATEGORY_ID = someCategory.getId();
            Long OTHER_CATEGORY_ID = otherCategory.getId();
            Long ITEM_ID = someItem.getId();
            Long OTHER_ITEM_ID = otherItem.getId();

            tx.begin();
            em = JPA.createEntityManager();

            Category category1 = em.find(Category.class, CATEGORY_ID);
            Category category2 = em.find(Category.class, OTHER_CATEGORY_ID);

            Item item1 = em.find(Item.class, ITEM_ID);
            Item item2 = em.find(Item.class, OTHER_ITEM_ID);

            assertEquals(category1.getCategorizedItems().size(), 2);
            assertEquals(item1.getCategorizedItems().size(), 2);

            assertEquals(category2.getCategorizedItems().size(), 1);
            assertEquals(item2.getCategorizedItems().size(), 1);

            assertEquals(category2.getCategorizedItems().iterator().next().getItem(), item1);
            assertEquals(item2.getCategorizedItems().iterator().next().getCategory(), category1);

            tx.commit();
            em.close();

        } finally {
            TM.rollback();
        }
    }

}