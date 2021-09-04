package org.jpwh.web.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

/* 
    This CDI annotation declares that only one producer is needed in the
    whole application, there will only ever be one instance of
    <code>EntityManagerProducer</code>.
 */
@javax.enterprise.context.ApplicationScoped
public class EntityManagerProducer {

    /* 
        The Java EE runtime will give you the persistence unit
        configured in your <code>persistence.xml</code>, which is also an
        application-scoped component. (If you use CDI standalone and outside
        a Java EE environment, you can instead use the static
        <code>Persistence.createEntityManagerFactory()</code> bootstrap.)

     */
    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    /* 
        Whenever an <code>EntityManager</code> is needed, the <code>create()</code>
         method is called. The container will re-use the same <code>EntityManager</code>
        during a request handled by our server. (If you forget the
        <code>@RequestScoped</code> annotation on the method, the
        <code>EntityManager</code> would be application-scoped like the producer class!)
     */
    @javax.enterprise.inject.Produces
    @javax.enterprise.context.RequestScoped
    public EntityManager create() {
        return entityManagerFactory.createEntityManager();
    }

    /* 
        When a request is over and the request context is being destroyed, the CDI
        container will call this method to get rid of an <code>EntityManager</code>
        instance. Since you created this application-managed persistence context
        (see <a href="#PersistenceContext"/>), it's your job to close it.
     */
    public void dispose(@javax.enterprise.inject.Disposes
                        EntityManager entityManager) {
        if (entityManager.isOpen())
            entityManager.close();
    }
}