package org.jpwh.model.associations.onetoone.sharedprimarykey;

import javax.persistence.*;

@Entity
@Table(name = "USERS")
public class User {

    @Id
    protected Long id;


    protected String username;

    @OneToOne(
        fetch = FetchType.LAZY,  // Defaults to EAGER
        optional = false // Required for lazy loading with proxies!
    )
    @PrimaryKeyJoinColumn
    protected Address shippingAddress;


    protected User() {
    }

    public User(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Address getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(Address shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
    // ...
}
