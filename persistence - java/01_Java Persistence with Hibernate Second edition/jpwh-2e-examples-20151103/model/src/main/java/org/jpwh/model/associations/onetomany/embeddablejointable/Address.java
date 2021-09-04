package org.jpwh.model.associations.onetomany.embeddablejointable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

@Embeddable
public class Address {

    @NotNull
    @Column(nullable = false)
    protected String street;

    @NotNull
    @Column(nullable = false, length = 5)
    protected String zipcode;

    @NotNull
    @Column(nullable = false)
    protected String city;

    @OneToMany
    @JoinTable(
        name = "DELIVERIES", // Defaults to USERS_SHIPMENT
        joinColumns =
        @JoinColumn(name = "USER_ID"), // Defaults to USERS_ID
        inverseJoinColumns =
        @JoinColumn(name = "SHIPMENT_ID") // Defaults to SHIPMENTS_ID
    )
    protected Set<Shipment> deliveries = new HashSet<Shipment>();

    protected Address() {
    }

    public Address(String street, String zipcode, String city) {
        this.street = street;
        this.zipcode = zipcode;
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Set<Shipment> getDeliveries() {
        return deliveries;
    }

    public void setDeliveries(Set<Shipment> deliveries) {
        this.deliveries = deliveries;
    }
}
