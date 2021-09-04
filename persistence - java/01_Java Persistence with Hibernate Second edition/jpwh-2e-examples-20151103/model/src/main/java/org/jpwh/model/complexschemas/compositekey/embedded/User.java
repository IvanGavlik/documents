package org.jpwh.model.complexschemas.compositekey.embedded;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "USERS")
public class User {

    @EmbeddedId
    // Optional: @AttributeOverrides
    protected UserId id;

    public User(UserId id) {
        this.id = id;
    }

    protected User() {
    }

    public UserId getId() {
        return id;
    }

    // ...
}
