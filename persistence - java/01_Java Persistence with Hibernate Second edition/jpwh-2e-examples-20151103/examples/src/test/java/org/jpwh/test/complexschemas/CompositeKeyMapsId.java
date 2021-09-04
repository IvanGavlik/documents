package org.jpwh.test.complexschemas;

import org.jpwh.env.JPATest;
import org.jpwh.model.complexschemas.compositekey.mapsid.Department;
import org.jpwh.model.complexschemas.compositekey.mapsid.User;
import org.jpwh.model.complexschemas.compositekey.mapsid.UserId;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import static org.testng.Assert.assertEquals;

public class CompositeKeyMapsId extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("CompositeKeyMapsIdPU");
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
                em.persist(department);

                UserId id = new UserId("johndoe", null); // Null?
                User user = new User(id);
                user.setDepartment(department); // Required!
                em.persist(user);

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
