package org.jpwh.test.complexschemas;

import org.jpwh.env.JPATest;
import org.jpwh.model.complexschemas.compositekey.manytoone.Item;
import org.jpwh.model.complexschemas.compositekey.manytoone.User;
import org.jpwh.model.complexschemas.compositekey.manytoone.UserId;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import static org.testng.Assert.assertEquals;

public class CompositeKeyManyToOne extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("CompositeKeyManyToOnePU");
    }

    @Test
    public void storeLoad() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            {
                UserId id = new UserId("johndoe", "123");
                User user = new User(id);
                em.persist(user);

                Item item = new Item("Some Item");
                item.setSeller(user);
                em.persist(item);
            }

            tx.commit();
            em.close();

            tx.begin();
            em = JPA.createEntityManager();

            {
                UserId id = new UserId("johndoe", "123");
                User user = em.find(User.class, id);
                assertEquals(user.getId().getDepartmentNr(), "123");

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
