package org.jpwh.model.inheritance.associations.onetomany;

import org.jpwh.model.Constants;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

// Can not be @MappedSuperclass when it's a target class in associations!
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class BillingDetails {

    @Id
    @GeneratedValue(generator = Constants.ID_GENERATOR)
    protected Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    protected User user;

    @NotNull
    protected String owner;

    protected BillingDetails() {
    }

    protected BillingDetails(String owner) {
        this.owner = owner;
    }

    public Long getId() {
        return id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void pay(int amount) {
        // NOOP
    }

    // ...
}
