package org.jpwh.test.filtering;

import org.hibernate.HibernateException;
import org.hibernate.event.internal.DefaultLoadEventListener;
import org.hibernate.event.spi.LoadEvent;
import org.hibernate.event.spi.LoadEventListener;

import java.io.Serializable;

public class SecurityLoadListener extends DefaultLoadEventListener {

    public void onLoad(LoadEvent event, LoadEventListener.LoadType loadType)
        throws HibernateException {

        boolean authorized =
            MySecurity.isAuthorized(
                event.getEntityClassName(), event.getEntityId()
            );

        if (!authorized)
            throw new MySecurityException("Unauthorized access");

        super.onLoad(event, loadType);
    }

    static public class MySecurity {
        static boolean isAuthorized(String entityName, Serializable entityId) {
           return true;
        }
    }

    static public class MySecurityException extends RuntimeException {
        public MySecurityException(String message) {
            super(message);
        }
    }
}

