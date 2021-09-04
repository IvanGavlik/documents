package org.jpwh.web.jaxrs;

import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.HibernateProxyHelper;
import org.hibernate.proxy.LazyInitializer;
import org.jpwh.web.model.EntityReference;

import javax.persistence.EntityManager;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.lang.reflect.Field;

public class EntityReferenceAdapter
    extends XmlAdapter<EntityReference, Object> {

    EntityManager em;

    /* 
        JAXB calls this constructor when it generates an XML document.
        In which case, you don't need an <code>EntityManager</code>,
        the proxy contains all the information you need to write
        an <code>EntityReference</code>.
     */
    public EntityReferenceAdapter() {
    }

    /* 
        JAXB must call this constructor when it reads an XML document.
        You need an <code>EntityManager</code> to get
        a Hibernate proxy from an <code>EntityReference</code>.
     */
    public EntityReferenceAdapter(EntityManager em) {
        this.em = em;
    }

    @Override
    public EntityReference marshal(Object entityInstance)
        throws Exception {

        /* 
            When writing an XML document, take the Hibernate proxy
            and create serializable representation. This calls some internal
            Hibernate methods that we haven't shown here.
         */
        Class type = getType(entityInstance);
        Long id = getId(type, entityInstance);
        return new EntityReference(type, id);
    }

    @Override
    public Object unmarshal(EntityReference entityReference)
        throws Exception {
        if (em == null)
            throw new IllegalStateException(
                "Call Unmarshaller#setAdapter() and " +
                    "provide an EntityManager"
            );

        /* 
           When reading an XML document, take the serialized representation
           and create a Hibernate proxy attached to the current persistence
           context.
         */
        return em.getReference(
            entityReference.type,
            entityReference.id
        );
    }

    // This is all Hibernate proprietary, and we assume that JPA entities are
    // mapped with field access ('id' field check). You can easily rewrite this
    // to use getter/setter methods for the identifier.

    protected Class getType(Object o) throws Exception {
        return HibernateProxyHelper.getClassWithoutInitializingProxy(o);
    }

    protected Long getId(Class type, Object entityInstance) throws Exception {
        if (entityInstance instanceof HibernateProxy) {
            LazyInitializer lazyInitializer =
                ((HibernateProxy) entityInstance).getHibernateLazyInitializer();
            return (Long) lazyInitializer.getIdentifier();
        }
        return (Long) getIdField(type).get(entityInstance);
    }

    protected Field getIdField(Class type) throws Exception {
        Field idField = type.getDeclaredField("id");
        if (idField == null || idField.getType() != Long.class)
            throw new IllegalArgumentException("Missing 'id' field of type Long on: " + type);
        return idField;
    }
}
