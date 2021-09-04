package org.jpwh.model.associations.manytomany.ternary;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Embeddable
public class CategorizedItem {

    @ManyToOne
    @JoinColumn(
        name = "ITEM_ID",
        nullable = false, updatable = false
    )
    protected Item item;

    @ManyToOne
    @JoinColumn(
        name = "USER_ID",
        updatable = false
    )
    @NotNull // Doesn't generate SQL constraint, so not part of the PK!
    protected User addedBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(updatable = false)
    @NotNull // Doesn't generate SQL constraint, so not part of the PK!
    protected Date addedOn = new Date();

    protected CategorizedItem() {
    }

    public CategorizedItem(User addedBy,
                           Item item) {
        this.addedBy = addedBy;
        this.item = item;
    }

    public Item getItem() {
        return item;
    }

    public User getAddedBy() {
        return addedBy;
    }

    public Date getAddedOn() {
        return addedOn;
    }

    // Careful! Equality as shown here is not 'detached' safe!
    // Don't put detached instances into a HashSet! Or, if you
    // really have to compare detached instances, make sure they
    // were all loaded in the same persistence context.

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CategorizedItem that = (CategorizedItem) o;

        // We are comparing instances by Java identity, not by primary key
        // equality. The scope where Java identity is the same as primary
        // key equality is the persistence context, not when instances are
        // in detached state!
        if (!addedBy.equals(that.addedBy)) return false;
        if (!addedOn.equals(that.addedOn)) return false;
        if (!item.equals(that.item)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = item.hashCode();
        result = 31 * result + addedBy.hashCode();
        result = 31 * result + addedOn.hashCode();
        return result;
    }

    // ...
}
