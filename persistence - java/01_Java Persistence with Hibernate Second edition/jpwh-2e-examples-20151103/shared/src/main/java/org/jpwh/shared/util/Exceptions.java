package org.jpwh.shared.util;

public class Exceptions {

    public static Throwable unwrap(Throwable throwable) {
        Throwable last;
        do {
            last = throwable;
            throwable = getCause(throwable);
        }
        while (throwable != null);
        return last;
    }

    public static <T extends Throwable> T unwrap(Throwable throwable, Class<T> type) {
        if (type == null)
            return null;
        do {
            if (type.isInstance(throwable))
                return (T) throwable;
            throwable = getCause(throwable);
        }
        while (throwable != null);
        return null;
    }

    protected static Throwable getCause(Throwable throwable) {
        // Prevent endless loop when t == t.getCause()
        if (throwable != null && throwable.getCause() != null && throwable != throwable.getCause())
            return throwable.getCause();
        return null;
    }

    public static void main(String[] args) {
        Exception ex = new Exception("WRAPPER",
                new IllegalArgumentException("CAUSE1",
                        new IllegalArgumentException("CAUSE2",
                                new UnsupportedOperationException("CAUSE3"))));

        assert unwrap(ex, Exception.class).getMessage().equals("WRAPPER");
        assert unwrap(ex, IllegalArgumentException.class).getMessage().equals("CAUSE1");
        assert unwrap(ex, UnsupportedOperationException.class).getMessage().equals("CAUSE3");
        assert unwrap(ex, IndexOutOfBoundsException.class) == null;

        assert unwrap(ex).getMessage().equals("CAUSE3");

        assert unwrap(null) == null;
        assert unwrap(null, null) == null;
        assert unwrap(null, IndexOutOfBoundsException.class) == null;
        assert unwrap(ex, null) == null;
    }

}
