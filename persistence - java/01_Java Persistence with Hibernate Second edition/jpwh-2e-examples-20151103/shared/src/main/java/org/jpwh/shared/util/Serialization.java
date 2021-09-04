package org.jpwh.shared.util;

import java.io.*;

public class Serialization {

    static public byte[] serialize(Object value) {
        try (ByteArrayOutputStream bo = new ByteArrayOutputStream();
             ObjectOutputStream so = new ObjectOutputStream(bo)) {
            so.writeObject(value);
            so.flush();
            return bo.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    static public Object deserialize(byte[] serialized) {
        try (ByteArrayInputStream bi = new ByteArrayInputStream(serialized);
             ObjectInputStream si = new ObjectInputStream(bi)) {
            return si.readObject();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    static public <T> T copy(T o) {
        return (T)deserialize(serialize(o));
    }
}
