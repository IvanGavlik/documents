package org.jpwh.model.advanced.converter;

import org.jpwh.converter.ZipcodeConverter;
import org.jpwh.model.Constants;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name = "USERS")
public class User implements Serializable {

    @Id
    @GeneratedValue(generator = Constants.ID_GENERATOR)
    protected Long id;

    @NotNull
    protected String username;

    // Group multiple attribute conversions with @Converts
    @Convert(
        converter = ZipcodeConverter.class,
        attributeName = "zipcode" // Or "city.zipcode" for nested embeddables
    )
    protected Address homeAddress;

    public Long getId() {
        return id;
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Address getHomeAddress() {
        return homeAddress;
    }

    public void setHomeAddress(Address homeAddress) {
        this.homeAddress = homeAddress;
    }

    // ...
}
