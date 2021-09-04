package org.jpwh.test.complexschemas;

import org.jpwh.env.JPATest;
import org.jpwh.model.complexschemas.compositekey.readonly.Department;
import org.jpwh.model.complexschemas.compositekey.readonly.User;
import org.jpwh.model.complexschemas.compositekey.readonly.UserId;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

public class CompositeKeyReadOnly extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("CompositeKeyReadOnlyPU");
    }

    @Test
    public void storeLoad() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            Long DEPARTMENT_ID;
            {
                Department department = new Department("Sales");
                em.persist(department); // Assign PK value

                UserId id = new UserId("johndoe", department.getId()); // Required!
                User user = new User(id);
                em.persist(user);

                assertNull(user.getDepartment()); // Careful!

                DEPARTMENT_ID = department.getId();
            }

            tx.commit();
            em.close();

            tx.begin();
            em = JPA.createEntityManager();

            {
                UserId id = new UserId("johndoe", DEPARTMENT_ID);
                User user = em.find(User.class, id);
                assertEquals(user.getDepartment().getName(), "Sales");
            }

            tx.commit();
            em.close();
        } finally {
            TM.rollback();
        }
    }

}
