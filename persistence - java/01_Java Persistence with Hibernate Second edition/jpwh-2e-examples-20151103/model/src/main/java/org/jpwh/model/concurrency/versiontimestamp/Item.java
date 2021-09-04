package org.jpwh.model.concurrency.versiontimestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
public class Item {

    @Id
    @GeneratedValue(generator = "ID_GENERATOR")
    protected Long id;

    @Version
    // Optional: @org.hibernate.annotations.Type(type = "dbtimestamp")
    protected Date lastUpdated;

    @NotNull
    protected String name;

    public Item() {
    }

    public Long getId() { // Optional but useful
        return id;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    // ...
}
