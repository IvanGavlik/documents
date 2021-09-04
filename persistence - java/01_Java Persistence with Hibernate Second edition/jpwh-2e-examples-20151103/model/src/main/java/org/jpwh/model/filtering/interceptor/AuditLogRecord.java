package org.jpwh.model.filtering.interceptor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
public class AuditLogRecord {

    @Id
    @GeneratedValue(generator = "ID_GENERATOR")
    protected Long id;

    @NotNull
    protected String message;

    @NotNull
    protected Long entityId;

    @NotNull
    protected Class entityClass;

    @NotNull
    protected Long userId;

    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    protected Date createdOn = new Date();

    protected AuditLogRecord() {
    }

    public AuditLogRecord(String message,
                          Auditable entityInstance,
                          Long userId) {
        this.message = message;
        this.entityId = entityInstance.getId();
        this.entityClass = entityInstance.getClass();
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public Long getEntityId() {
        return entityId;
    }

    public Class getEntityClass() {
        return entityClass;
    }

    public Long getUserId() {
        return userId;
    }

    public Date getCreatedOn() {
        return createdOn;
    }

    // ...
}
