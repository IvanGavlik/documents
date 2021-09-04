package org.jpwh.model.filtering.callback;

import javax.persistence.PostPersist;

/** 
 * An entity listener class must have either no constructor or a public no-argument
 * constructor. It doesn't have to implement any special interfaces. An entity listener
 * is stateless; the JPA engine automatically creates and destroys it.
 */
public class PersistEntityListener {

    /* 
       You may annotate any method of an entity listener class as a callback method
       for persistence life cycle events. The <code>notifyAdmin()</code> method is
       invoked after a new entity instance is stored in the database.
     */
    @PostPersist
    public void notifyAdmin(Object entityInstance) {

        /* 
           Because event listener classes are stateless, it can be difficult to get
           more contextual information when you need it. Here, you want the currently
           logged in user, and access to the email system to send a notification. A primitive
           solution is to use thread-local variables and singletons;  you can find the source for
           <code>CurrentUser</code> and <code>Mail</code> in the example code.
         */
        User currentUser = CurrentUser.INSTANCE.get();
        Mail mail = Mail.INSTANCE;

        mail.send(
            "Entity instance persisted by "
                + currentUser.getUsername()
                + ": "
                + entityInstance
        );
    }

}
