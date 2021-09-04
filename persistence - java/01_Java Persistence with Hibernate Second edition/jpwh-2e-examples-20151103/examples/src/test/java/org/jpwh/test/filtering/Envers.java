package org.jpwh.test.filtering;

import org.hibernate.ReplicationMode;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.DefaultRevisionEntity;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.jpwh.env.JPATest;
import org.jpwh.model.filtering.envers.Item;
import org.jpwh.model.filtering.envers.User;
import org.testng.annotations.Test;

import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import java.util.Date;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class Envers extends JPATest {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("FilteringEnversPU");
    }

    @Test
    public void auditLogging() throws Throwable {

        UserTransaction tx = TM.getUserTransaction();
        try {

            Long ITEM_ID;
            Long USER_ID;
            {
                // Create
                tx.begin();
                EntityManager em = JPA.createEntityManager();

                User user = new User("johndoe");
                em.persist(user);

                Item item = new Item("Foo", user);
                em.persist(item);

                tx.commit();
                em.close();

                ITEM_ID = item.getId();
                USER_ID = user.getId();
            }
            Date TIMESTAMP_CREATE = new Date();

            {
                // Update
                tx.begin();
                EntityManager em = JPA.createEntityManager();

                Item item = em.find(Item.class, ITEM_ID);
                item.setName("Bar");
                item.getSeller().setUsername("doejohn");

                tx.commit();
                em.close();
            }
            Date TIMESTAMP_UPDATE = new Date();

            {
                // Delete
                tx.begin();
                EntityManager em = JPA.createEntityManager();

                Item item = em.find(Item.class, ITEM_ID);
                em.remove(item);

                tx.commit();
                em.close();
            }
            Date TIMESTAMP_DELETE = new Date();

            {
                tx.begin();
                EntityManager em = JPA.createEntityManager();
                /* 
                   The main Envers API is the <code>AuditReader</code>, it can be accessed with
                   an <code>EntityManager</code>.
                 */
                AuditReader auditReader = AuditReaderFactory.get(em);

                /* 
                   Given a timestamp, you can find the revision number of a change set, made
                   before or on that timestamp.
                 */
                Number revisionCreate = auditReader.getRevisionNumberForDate(TIMESTAMP_CREATE);
                Number revisionUpdate = auditReader.getRevisionNumberForDate(TIMESTAMP_UPDATE);
                Number revisionDelete  = auditReader.getRevisionNumberForDate(TIMESTAMP_DELETE);

                /* 
                   If you don't have a timestamp, you can get all revision numbers in which a
                   particular audited entity instance was involved. This operation finds all
                   change sets where the given <code>Item</code> was created, modified, or
                   deleted. In our example, we created, modified, and then deleted the
                   <code>Item</code>. Hence, we have three revisions.
                 */
                List<Number> itemRevisions = auditReader.getRevisions(Item.class, ITEM_ID);
                assertEquals(itemRevisions.size(), 3);
                for (Number itemRevision : itemRevisions) {
                    /* 
                       If you have a revision number, you can get the timestamp when Envers
                       logged the change set.
                     */
                    Date itemRevisionTimestamp = auditReader.getRevisionDate(itemRevision);
                    // ...
                }

                /* 
                   We created and modified the <code>User</code>, so there are two revisions.
                 */
                List<Number> userRevisions = auditReader.getRevisions(User.class, USER_ID);
                assertEquals(userRevisions.size(), 2);

                em.clear();
                {
                    /* 
                       If you don't know modification timestamps or revision numbers, you can write
                       a query with <code>forRevisionsOfEntity()</code> to obtain all audit trail
                       details of a particular entity.
                     */
                    AuditQuery query = auditReader.createQuery()
                        .forRevisionsOfEntity(Item.class, false, false);

                    /* 
                       This query returns the audit trail details as a <code>List</code> of
                       <code>Object[]</code>.
                     */
                    List<Object[]> result = query.getResultList();
                    for (Object[] tuple : result) {

                        /* 
                           Each result tuple contains the entity instance for a particular revision, the
                           revision details (including revision number and timestamp), as well as the
                           revision type.
                         */
                        Item item = (Item) tuple[0];
                        DefaultRevisionEntity revision = (DefaultRevisionEntity)tuple[1];
                        RevisionType revisionType = (RevisionType)tuple[2];

                        /* 
                           The revision type indicates why Envers created the revision, because
                           the entity instance was inserted, modified, or deleted in the database.
                         */
                        if (revision.getId() == 1) {
                            assertEquals(revisionType, RevisionType.ADD);
                            assertEquals(item.getName(), "Foo");
                        } else if (revision.getId() == 2) {
                            assertEquals(revisionType, RevisionType.MOD);
                            assertEquals(item.getName(), "Bar");
                        } else if (revision.getId() == 3) {
                            assertEquals(revisionType, RevisionType.DEL);
                            assertNull(item);
                        }
                    }
                }
                em.clear();
                {
                    /* 
                       The <code>find()</code> method returns an audited entity instance version,
                       given a revision. This operation loads the <code>Item</code> as it was after
                       creation.
                     */
                    Item item = auditReader.find(Item.class, ITEM_ID, revisionCreate);
                    assertEquals(item.getName(), "Foo");
                    assertEquals(item.getSeller().getUsername(), "johndoe");

                    /* 
                       This operation loads the <code>Item</code> after it was updated. Note how
                       the modified <code>seller</code> of this change set was also retrieved
                       automatically.
                     */
                    Item modifiedItem = auditReader.find(Item.class, ITEM_ID, revisionUpdate);
                    assertEquals(modifiedItem.getName(), "Bar");
                    assertEquals(modifiedItem.getSeller().getUsername(), "doejohn");

                    /* 
                       In this revision, the <code>Item</code> was deleted, so <code>find()</code>
                       returns <code>null</code>.
                     */
                    Item deletedItem = auditReader.find(Item.class, ITEM_ID, revisionDelete);
                    assertNull(deletedItem);

                    /* 
                       However, the example did not modify the <code>User</code> in this revision,
                       so Envers returns its closest historical revision.
                     */
                    User user = auditReader.find(User.class, USER_ID, revisionDelete);
                    assertEquals(user.getUsername(), "doejohn");
                }
                em.clear();
                {
                    /* 
                       This query returns <code>Item</code> instances restricted to a
                       particular revision and change set.
                     */
                    AuditQuery query = auditReader.createQuery()
                        .forEntitiesAtRevision(Item.class, revisionUpdate);

                    /* 
                       You can add further restrictions to the query; here the <code>Item#name</code>
                       must start with "Ba".
                     */
                    query.add(
                        AuditEntity.property("name").like("Ba", MatchMode.START)
                    );

                    /* 
                       Restrictions can include entity associations, for example, we are looking for
                       the revision of an <code>Item</code> sold by a particular <code>User</code>.
                     */
                    query.add(
                        AuditEntity.relatedId("seller").eq(USER_ID)
                    );

                    /* 
                       You can order query results.
                     */
                    query.addOrder(
                        AuditEntity.property("name").desc()
                    );

                    /* 
                       You can paginate through large results.
                     */
                    query.setFirstResult(0);
                    query.setMaxResults(10);

                    assertEquals(query.getResultList().size(), 1);
                    Item result = (Item)query.getResultList().get(0);
                    assertEquals(result.getSeller().getUsername(), "doejohn");
                }
                em.clear();
                {
                    AuditQuery query = auditReader.createQuery()
                        .forEntitiesAtRevision(Item.class, revisionUpdate);

                    query.addProjection(
                        AuditEntity.property("name")
                    );

                    assertEquals(query.getResultList().size(), 1);
                    String result = (String)query.getSingleResult();
                    assertEquals(result, "Bar");
                }
                em.clear();
                {
                    // TODO This fails with instrumented bytecode, Hibernate bug HHH-8600
                    /*
                    User user = auditReader.find(User.class, USER_ID, revisionCreate);

                    em.unwrap(Session.class)
                        .replicate(user, ReplicationMode.OVERWRITE);
                    em.flush();
                    em.clear();

                    user = em.find(User.class, USER_ID);
                    assertEquals(user.getUsername(), "johndoe");
                    */
                }
                tx.commit();
                em.close();
            }
        } finally {
            TM.rollback();
        }
    }

}
