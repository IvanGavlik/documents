package org.jpwh.model.advanced;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

@Embeddable
public class City {

    @NotNull
    @Column(nullable = false, length = 5) // Override VARCHAR(255)
    protected String zipcode;

    @NotNull
    @Column(nullable =  false)
    protected String name;

    @NotNull
    @Column(nullable =  false)
    protected String country;

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(String zipcode) {
        this.zipcode = zipcode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    // ...
}