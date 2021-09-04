package org.jpwh.model;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class InvalidBidException extends Exception {

    public InvalidBidException() {
    }

    public InvalidBidException(String s) {
        super(s);
    }

    public InvalidBidException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public InvalidBidException(Throwable throwable) {
        super(throwable);
    }
}
