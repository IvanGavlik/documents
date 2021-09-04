package org.jpwh.model.complexschemas.custom;

import org.jpwh.model.Constants;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(
    name = "USERS",
    uniqueConstraints =
        @UniqueConstraint(
            name = "UNQ_USERNAME_EMAIL",
            columnNames = { "USERNAME", "EMAIL" }
        )
    ,
    indexes = {
        @Index(
            name = "IDX_USERNAME",
            columnList = "USERNAME"
        ),
        @Index(
            name = "IDX_USERNAME_EMAIL",
            columnList = "USERNAME, EMAIL"
        )
    }
)
public class User {

    @Id
    @GeneratedValue(generator = Constants.ID_GENERATOR)
    protected Long id;

    @Column(
        nullable = false,                       // Column constraint
        unique = true,                          // Table multi-row constraint
        columnDefinition = "EMAIL_ADDRESS(255)" // Applying domain constraint
    )
    protected String email;

    @Column(columnDefinition =
        "varchar(15) not null unique" +
        " check (not substring(lower(USERNAME), 0, 5) = 'admin')"
    )
    // @org.hibernate.annotations.Check currently not supported on properties!
    protected String username;

    public User() {
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // ...
}