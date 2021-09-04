package org.jpwh.test.associations;


import org.jpwh.env.JPATest;
import org.jpwh.model.associations.onetoone.jointable.Item;
import org.jpwh.model.associations.onetoone.jointable.Shipment;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class OneToOneJoinTable extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("OneToOneJoinTablePU");
    }

    @Test
    public void storeAndLoadUserAddress() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            Shipment someShipment = new Shipment();
            em.persist(someShipment);

            Item someItem = new Item("Some Item");
            em.persist(someItem);

            Shipment auctionShipment = new Shipment(someItem);
            em.persist(auctionShipment);

            tx.commit();
            em.close();

            Long ITEM_ID = someItem.getId();
            Long SHIPMENT_ID = someShipment.getId();
            Long AUCTION_SHIPMENT_ID = auctionShipment.getId();

            tx.begin();
            em = JPA.createEntityManager();

            Item item = em.find(Item.class, ITEM_ID);
            Shipment shipment1 = em.find(Shipment.class, SHIPMENT_ID);
            Shipment shipment2 = em.find(Shipment.class, AUCTION_SHIPMENT_ID);

            assertNull(shipment1.getAuction());
            assertEquals(shipment2.getAuction(), item);

            tx.commit();
            em.close();

        } finally {
            TM.rollback();
        }
    }

    @Test(expectedExceptions = org.hibernate.exception.ConstraintViolationException.class)
    public void storeNonUniqueRelationship() throws Throwable {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();
            EntityManager em = JPA.createEntityManager();

            Item someItem = new Item("Some Item");
            em.persist(someItem);

            Shipment shipment1 = new Shipment(someItem);
            em.persist(shipment1);

            Shipment shipment2 = new Shipment(someItem);
            em.persist(shipment2);

            try {
                // Hibernate tries the INSERT but fails
                em.flush();
            } catch (Exception ex) {
                throw unwrapCauseOfType(ex, org.hibernate.exception.ConstraintViolationException.class);
            }
        } finally {
            TM.rollback();
        }
    }
}