package org.jpwh.test.associations;


import org.jpwh.env.JPATest;
import org.jpwh.model.associations.onetomany.jointable.Item;
import org.jpwh.model.associations.onetomany.jointable.User;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import static org.testng.Assert.*;

public class OneToManyJoinTable extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("OneToManyJoinTablePU");
    }

    @Test
    public void storeAndLoadItemUsers() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            Item someItem = new Item("Some Item");
            em.persist(someItem);
            Item otherItem = new Item("Other Item");
            em.persist(otherItem);

            User someUser = new User("johndoe");
            someUser.getBoughtItems().add(someItem); // Link
            someItem.setBuyer(someUser); // Link
            someUser.getBoughtItems().add(otherItem);
            otherItem.setBuyer(someUser);
            em.persist(someUser);

            Item unsoldItem = new Item("Unsold Item");
            em.persist(unsoldItem);

            tx.commit();
            em.close();

            Long ITEM_ID = someItem.getId();
            Long UNSOLD_ITEM_ID = unsoldItem.getId();

            tx.begin();
            em = JPA.createEntityManager();

            Item item = em.find(Item.class, ITEM_ID);
            assertEquals(item.getBuyer().getUsername(), "johndoe");
            assertTrue(item.getBuyer().getBoughtItems().contains(item));

            Item item2 = em.find(Item.class, UNSOLD_ITEM_ID);
            assertNull(item2.getBuyer());

            tx.commit();
            em.close();

        } finally {
            TM.rollback();
        }
    }

}