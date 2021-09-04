package org.jpwh.test.filtering;

import org.hibernate.CallbackException;
import org.hibernate.EmptyInterceptor;
import org.hibernate.Session;
import org.hibernate.type.Type;
import org.jpwh.model.filtering.interceptor.AuditLogRecord;
import org.jpwh.model.filtering.interceptor.Auditable;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class AuditLogInterceptor extends EmptyInterceptor {

    /* 
       You need to access the database to write the audit log, so this interceptor
       needs a Hibernate <code>Session</code>. You also want to store the identifier
       of the currently logged-in user in each audit log record. The <code>inserts</code>
       and <code>updates</code> instance variables are collections where this interceptor
       will hold its internal state.
     */
    protected Session currentSession;
    protected Long currentUserId;
    protected Set<Auditable> inserts = new HashSet<Auditable>();
    protected Set<Auditable> updates = new HashSet<Auditable>();

    public void setCurrentSession(Session session) {
        this.currentSession = session;
    }

    public void setCurrentUserId(Long currentUserId) {
        this.currentUserId = currentUserId;
    }

    /* 
       This method is called when an entity instance is made persistent.
     */
    public boolean onSave(Object entity, Serializable id,
                          Object[] state, String[] propertyNames, Type[] types)
        throws CallbackException {

        if (entity instanceof Auditable)
            inserts.add((Auditable)entity);

        return false; // We didn't modify the state
    }

    /* 
       This method is called when an entity instance is detected as dirty
       during flushing of the persistence context.
     */
    public boolean onFlushDirty(Object entity, Serializable id,
                                Object[] currentState, Object[] previousState,
                                String[] propertyNames, Type[] types)
        throws CallbackException {

        if (entity instanceof Auditable)
            updates.add((Auditable)entity);

        return false; // We didn't modify the currentState
    }

    /* 
       This method is called after flushing of the persistence context is complete.
       Here, you write the audit log records for all insertions and updates you
       collected earlier.
     */
    public void postFlush(Iterator iterator) throws CallbackException {

        /* 
           You are not allowed to access the original persistence context, the
           <code>Session</code> that is currently executing this interceptor.
           The <code>Session</code> is in a fragile state during interceptor calls.
           Hibernate allows you to create a new <code>Session</code> that
           inherits some information from the original <code>Session</code> with
           the <code>sessionWithOptions()</code> method. Here the new temporary
           <code>Session</code> works with the same transaction and database
           connection as the original <code>Session</code>.
         */
        Session tempSession =
            currentSession.sessionWithOptions()
                .transactionContext()
                .connection()
                .openSession();

        try {
            /* 
               You store a new <code>AuditLogRecord</code> for each insertion and
               update using the temporary <code>Session</code>.
             */
            for (Auditable entity : inserts) {
                tempSession.persist(
                    new AuditLogRecord("insert", entity, currentUserId)
                );
            }
            for (Auditable entity : updates) {
                tempSession.persist(
                    new AuditLogRecord("update", entity, currentUserId)
                );
            }

            /* 
               You flush and close the temporary <code>Session</code>
               independently from the original <code>Session</code>.
             */
            tempSession.flush();
        } finally {
            tempSession.close();
            inserts.clear();
            updates.clear();
        }
    }
}
