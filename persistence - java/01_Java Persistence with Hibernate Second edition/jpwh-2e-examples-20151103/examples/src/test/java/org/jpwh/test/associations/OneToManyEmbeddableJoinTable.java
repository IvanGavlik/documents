package org.jpwh.test.associations;


import org.jpwh.env.JPATest;
import org.jpwh.model.associations.onetomany.embeddablejointable.Address;
import org.jpwh.model.associations.onetomany.embeddablejointable.Shipment;
import org.jpwh.model.associations.onetomany.embeddablejointable.User;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import static org.testng.Assert.assertEquals;

public class OneToManyEmbeddableJoinTable extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("OneToManyEmbeddableJoinTablePU");
    }

    @Test
    public void storeAndLoadUsersShipments() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            User user = new User("johndoe");
            Address deliveryAddress = new Address("Some Street", "12345", "Some City");
            user.setShippingAddress(deliveryAddress);
            em.persist(user);

            Shipment firstShipment = new Shipment();
            deliveryAddress.getDeliveries().add(firstShipment);
            em.persist(firstShipment);

            Shipment secondShipment = new Shipment();
            deliveryAddress.getDeliveries().add(secondShipment);
            em.persist(secondShipment);

            tx.commit();
            em.close();

            tx.begin();
            em = JPA.createEntityManager();

            Long USER_ID = user.getId();

            User johndoe = em.find(User.class, USER_ID);
            assertEquals(johndoe.getShippingAddress().getDeliveries().size(), 2);

            tx.commit();
            em.close();

        } finally {
            TM.rollback();
        }
    }

}