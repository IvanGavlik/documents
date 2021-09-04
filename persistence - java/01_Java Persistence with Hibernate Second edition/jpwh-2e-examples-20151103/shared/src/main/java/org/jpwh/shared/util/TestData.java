package org.jpwh.shared.util;

public class TestData {
    public final Long[] identifiers;

    public TestData(Long[] identifiers) {
        this.identifiers = identifiers;
    }

    public Long getFirstId() {
        return identifiers.length > 0 ? identifiers[0] : null;
    }

    public Long getLastId() {
        return identifiers.length > 0 ? identifiers[identifiers.length - 1] : null;
    }
}
