package org.jpwh.model.complexschemas.compositekey.readonly;

import javax.persistence.*;

@Entity
@Table(name = "USERS")
public class User {

    @EmbeddedId
    protected UserId id;

    @ManyToOne
    @JoinColumn(
        name = "DEPARTMENTID", // Defaults to DEPARTMENT_ID
        insertable = false, updatable = false // Make it read-only!
    )
    protected Department department;

    public User(UserId id) {
        this.id = id;
    }

    public User(String username, Department department) {
        if (department.getId() == null)
            throw new IllegalStateException(
                "Department is transient: " + department
            );
        this.id = new UserId(username, department.getId());
        this.department = department;
    }

    protected User() {
    }

    public UserId getId() {
        return id;
    }

    public Department getDepartment() {
        return department;
    }

    // ...
}