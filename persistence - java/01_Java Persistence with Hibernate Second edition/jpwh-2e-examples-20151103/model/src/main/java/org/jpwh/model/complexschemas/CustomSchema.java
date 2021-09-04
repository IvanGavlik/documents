package org.jpwh.model.complexschemas;

import org.hibernate.dialect.Dialect;
import org.hibernate.boot.model.relational.AbstractAuxiliaryDatabaseObject;

public class CustomSchema
    extends AbstractAuxiliaryDatabaseObject {

    public CustomSchema() {
        addDialectScope("org.hibernate.dialect.Oracle9Dialect");
    }

    @Override
    public String[] sqlCreateStrings(Dialect dialect) {
        return new String[]{"[CREATE statement]"};
    }

    @Override
    public String[] sqlDropStrings(Dialect dialect) {
        return new String[]{"[DROP statement]"};
    }
}
