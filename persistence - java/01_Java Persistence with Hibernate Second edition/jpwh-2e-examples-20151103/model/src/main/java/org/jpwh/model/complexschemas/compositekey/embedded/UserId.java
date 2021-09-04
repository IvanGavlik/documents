package org.jpwh.model.complexschemas.compositekey.embedded;

import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * 
 * This class has to be <code>@Embeddable</code> and <code>Serializable</code>&#8212;any type used as
 * an identifier type in JPA has to be <code>Serializable</code>.
 */
@Embeddable
public class UserId implements Serializable {

    /**
     * 
     * You don't have to mark the properties of the composite key as <code>@NotNull</code>, their
     * database columns will automatically be <code>NOT NULL</code> when embedded as the primary
     * key of an entity.
     */
    protected String username;

    protected String departmentNr;

    /**
     * 
     * The JPA specification requires a public no-argument constructor for
     * an embeddable identifier class, Hibernate accepts protected visibility.
     */
    protected UserId() {
    }

    /**
     * 
     * The only public constructor should have the key values as arguments.
     */
    public UserId(String username, String departmentNr) {
        this.username = username;
        this.departmentNr = departmentNr;
    }

    /**
     * 
     * You have to override the <code>equals()</code> and <code>hashCode()</code> methods, with
     * the same semantics as the composite key has in your database. In this case this is a
     * straightforward comparison of the <code>username</code> and
     * <code>departmentNr</code> values.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserId userId = (UserId) o;
        if (!departmentNr.equals(userId.departmentNr)) return false;
        if (!username.equals(userId.username)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = username.hashCode();
        result = 31 * result + departmentNr.hashCode();
        return result;
    }

    public String getUsername() {
        return username;
    }

    public String getDepartmentNr() {
        return departmentNr;
    }


    // ...
}
