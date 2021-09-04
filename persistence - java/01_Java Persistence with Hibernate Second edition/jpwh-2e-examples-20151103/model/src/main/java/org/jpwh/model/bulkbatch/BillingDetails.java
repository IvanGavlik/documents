package org.jpwh.model.bulkbatch;

import org.jpwh.model.Constants;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class BillingDetails {

    @Id
    @GeneratedValue(generator = Constants.ID_GENERATOR_POOLED)
    protected Long id;

    @NotNull
    @ManyToOne(fetch =  FetchType.LAZY)
    protected User user;

    @NotNull
    protected String owner;

    protected BillingDetails() {
    }

    protected BillingDetails(User user, String owner) {
        this.user = user;
        this.owner = owner;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}