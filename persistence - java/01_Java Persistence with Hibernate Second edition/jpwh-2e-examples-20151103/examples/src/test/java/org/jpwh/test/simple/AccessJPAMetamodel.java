package org.jpwh.test.simple;

import org.jpwh.env.JPATest;
import org.jpwh.model.simple.Item;
import org.jpwh.model.simple.Item_;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.Type;
import javax.transaction.UserTransaction;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class AccessJPAMetamodel extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("SimpleXMLCompletePU");
    }

    @Test
    public void accessDynamicMetamodel() throws Exception {
        EntityManagerFactory entityManagerFactory = JPA.getEntityManagerFactory();

        Metamodel mm = entityManagerFactory.getMetamodel();

        Set<ManagedType<?>> managedTypes = mm.getManagedTypes();
        assertEquals(managedTypes.size(), 1);

        ManagedType itemType = managedTypes.iterator().next();
        assertEquals(
            itemType.getPersistenceType(),
            Type.PersistenceType.ENTITY
        );

        SingularAttribute nameAttribute =
            itemType.getSingularAttribute("name");
        assertEquals(
            nameAttribute.getJavaType(),
            String.class
        );
        assertEquals(
            nameAttribute.getPersistentAttributeType(),
            Attribute.PersistentAttributeType.BASIC
        );
        assertFalse(
            nameAttribute.isOptional() // NOT NULL
        );

        SingularAttribute auctionEndAttribute =
            itemType.getSingularAttribute("auctionEnd");
        assertEquals(
            auctionEndAttribute.getJavaType(),
            Date.class
        );
        assertFalse(
            auctionEndAttribute.isCollection()
        );
        assertFalse(
            auctionEndAttribute.isAssociation()
        );
    }

    @Test
    public void accessStaticMetamodel() throws Exception {

        SingularAttribute nameAttribute = Item_.name;

        assertEquals(
            nameAttribute.getJavaType(),
            String.class
        );
    }

    @Test
    public void queryStaticMetamodel() throws Exception {
        UserTransaction tx = TM.getUserTransaction();
        try {
            tx.begin();

            EntityManager entityManager = JPA.createEntityManager();

            Item itemOne = new Item();
            itemOne.setName("This is some item");
            itemOne.setAuctionEnd(new Date(System.currentTimeMillis() + 100000));
            entityManager.persist(itemOne);

            Item itemTwo = new Item();
            itemTwo.setName("Another item");
            itemTwo.setAuctionEnd(new Date(System.currentTimeMillis() + 100000));

            entityManager.persist(itemTwo);

            tx.commit();
            entityManager.close();

            entityManager = JPA.createEntityManager();
            tx.begin();

            CriteriaBuilder cb = entityManager.getCriteriaBuilder();

            // This query is the equivalent of "select i from Item i"
            CriteriaQuery<Item> query = cb.createQuery(Item.class);
            Root<Item> fromItem = query.from(Item.class);
            query.select(fromItem);

            List<Item> items =
                entityManager.createQuery(query)
                    .getResultList();

            assertEquals(items.size(), 2);

            // "where i.name like :pattern"
            Path<String> namePath = fromItem.get("name");
            query.where(
                cb.like(
                    namePath, // Has to be a Path<String> for like() operator!
                    cb.parameter(String.class, "pattern")
                )
            );

            items =
                entityManager.createQuery(query)
                    .setParameter("pattern", "%some item%") // Wildcards!
                    .getResultList();

            assertEquals(items.size(), 1);
            assertEquals(items.iterator().next().getName(), "This is some item");

            query.where(
                cb.like(
                    fromItem.get(Item_.name), // Static Item_ metamodel!
                    cb.parameter(String.class, "pattern")
                )
            );

            items =
                entityManager.createQuery(query)
                    .setParameter("pattern", "%some item%") // Wildcard!
                    .getResultList();

            assertEquals(items.size(), 1);
            assertEquals(items.iterator().next().getName(), "This is some item");

            tx.commit();
            entityManager.close();
        } finally {
            TM.rollback();
        }
    }

}