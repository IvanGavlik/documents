package org.jpwh.test.associations;


import org.jpwh.env.JPATest;
import org.jpwh.model.associations.manytomany.bidirectional.Category;
import org.jpwh.model.associations.manytomany.bidirectional.Item;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import static org.testng.Assert.assertEquals;

public class ManyToManyBidirectional extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("ManyToManyBidirectionalPU");
    }

    @Test
    public void storeLoadCategoryItems() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            Category someCategory = new Category("Some Category");
            Category otherCategory = new Category("Other Category");

            Item someItem = new Item("Some Item");
            Item otherItem = new Item("Other Item");

            someCategory.getItems().add(someItem);
            someItem.getCategories().add(someCategory);

            someCategory.getItems().add(otherItem);
            otherItem.getCategories().add(someCategory);

            otherCategory.getItems().add(someItem);
            someItem.getCategories().add(otherCategory);

            em.persist(someCategory);
            em.persist(otherCategory);

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

            System.out.println("\nCat" + category1.getItems());
            System.out.println("Item" + item1.getCategories()+ "\n");


            assertEquals(category1.getItems().size(), 2);
            assertEquals(item1.getCategories().size(), 2);

            assertEquals(category2.getItems().size(), 1);
            assertEquals(item2.getCategories().size(), 1);

            assertEquals(category2.getItems().iterator().next(), item1);
            assertEquals(item2.getCategories().iterator().next(), category1);

            tx.commit();
            em.close();

        } finally {
            TM.rollback();
        }
    }

}
