package org.jpwh.model.customsql;

import org.jpwh.model.Constants;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedNativeQueries;
import javax.persistence.NamedNativeQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

/* 
   Annotations declare the query to load an instance of <code>User</code>; you
   could also declare it in an XML file (JPA or Hibernate metadata). You can also call
   this named query directly in your data access code when needed.
 */
@NamedNativeQueries({
    @NamedNativeQuery(
        name = "findUserById",
        /* 
           The query must have exactly one parameter placeholder, which Hibernate will
           set as the identifier value of the instance to load. Here, we have a positional
           parameter, but a named parameter would also work.
         */
        query = "select * from USERS where ID = ?",
        /* 
           For this trivial query, you don't need a custom result set mapping.
           The <code>User</code> class already maps all fields returned by the query.
           Hibernate can automatically transform the result.
         */
        resultClass = User.class
    )
})
/* 
   Setting the loader for an entity class to a named query enables the query
   for all operations that retrieve an instance of <code>User</code> from the
   database. Note that there is no indication of the query language or where
   you declared it, this is independent of the loader declaration.
 */
@org.hibernate.annotations.Loader(
    namedQuery = "findUserById"
)
@org.hibernate.annotations.SQLInsert(
    sql = "insert into USERS " +
          "(ACTIVATED, USERNAME, ID) values (?, ?, ?)"
)
@org.hibernate.annotations.SQLUpdate(
    sql = "update USERS set " +
          "ACTIVATED = ?, " +
          "USERNAME = ? " +
          "where ID = ?"
)
@org.hibernate.annotations.SQLDelete(
    sql = "delete from USERS where ID = ?"
)
@Entity
@Table(name = "USERS")
public class User {

    @Id
    @GeneratedValue(generator = Constants.ID_GENERATOR)
    protected Long id;

    @NotNull
    protected String username;

    protected boolean activated  = true;

    public User() {
    }

    public User(String username) {
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

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }
    // ...
}