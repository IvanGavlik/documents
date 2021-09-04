package org.jpwh.model.complexschemas.naturalforeignkey;

import org.jpwh.model.Constants;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

// TODO User class must be serializable, Hibernate bug HHH-7668
@Entity
@Table(name = "USERS")
public class User implements Serializable {

    @Id
    @GeneratedValue(generator = Constants.ID_GENERATOR)
    protected Long id;

    @NotNull
    @Column(unique = true)
    protected String customerNr;

    protected User() {
    }

    public User(String customerNr) {
        this.customerNr = customerNr;
    }

    public Long getId() {
        return id;
    }

    public String getCustomerNr() {
        return customerNr;
    }

    public void setCustomerNr(String customerNr) {
        this.customerNr = customerNr;
    }

    // ...
}