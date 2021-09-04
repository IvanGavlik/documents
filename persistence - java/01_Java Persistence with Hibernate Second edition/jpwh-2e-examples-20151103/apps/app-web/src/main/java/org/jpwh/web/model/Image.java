package org.jpwh.web.model;

import org.jpwh.Constants;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.*;
import java.io.Serializable;

@Entity
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Image implements Comparable<Image>, Serializable {

    @Id
    @GeneratedValue(generator = Constants.ID_GENERATOR)
    @XmlAttribute
    protected Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ITEM_ID", nullable = false)
    @XmlTransient
    protected Item item;

    @NotNull
    @Size(
       min = 2,
       max = 255,
       message = "Image name is required, minimum 2, maximum 255 characters."
    )
    protected String name;

    @NotNull
    @Basic(fetch = FetchType.LAZY) // Lazy loaded if instrumented
    @Column(length = 1048576) // 1M maximum for the picture
    @XmlTransient
    protected byte[] data; // Maps to SQL VARBINARY type

    @NotNull
    protected String contentType;

    public Long getId() {
        return id;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public int compareTo(Image that) {
        if (this.getId() == null || that.getId() == null) return 0;
        // In lists, oldest image (lower id value) is shown first
        return this.getId().compareTo(that.getId());
    }
}
