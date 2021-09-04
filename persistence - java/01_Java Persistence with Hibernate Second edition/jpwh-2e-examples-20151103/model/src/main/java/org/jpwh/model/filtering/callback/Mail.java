package org.jpwh.model.filtering.callback;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Singleton
 */
public class Mail extends ArrayList<String> {

    public static final Mail INSTANCE = new Mail();

    private Mail(int initialCapacity) {
        super(initialCapacity);
    }

    private Mail() {
    }

    private Mail(Collection<? extends String> c) {
        super(c);
    }

    public void send(String message) {
        add(message);
    }
}
