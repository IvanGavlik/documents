package org.jpwh.test.complexschemas;

import org.jpwh.env.JPATest;
import org.jpwh.model.complexschemas.naturalforeignkey.Item;
import org.jpwh.model.complexschemas.naturalforeignkey.User;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import static org.testng.Assert.assertEquals;

public class NaturalForeignKey extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("NaturalForeignKeyPU");
    }

    @Test
    public void storeLoad() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            Long USER_ID;
            {
                User user = new User("1234");
                em.persist(user);
                
                Item item = new Item("Some Item");
                item.setSeller(user);
                em.persist(item);
                USER_ID = user.getId();
            }

            tx.commit();
            em.close();

            tx.begin();
            em = JPA.createEntityManager();

            {
                User user = em.find(User.class, USER_ID);
                
                Item item = (Item)em.createQuery(
                    "select i from Item i where i.seller = :u"
                ).setParameter("u", user).getSingleResult();
                
                assertEquals(item.getName(), "Some Item");
            }

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

}
