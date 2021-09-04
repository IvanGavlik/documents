package org.jpwh.model.collections.mapofembeddables;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class Filename {

    @Column(nullable = false) // Must be NOT NULL, part of PK!
    protected String name;

    @Column(nullable = false) // Must be NOT NULL, part of PK!
    protected String extension;


    public Filename() {
    }

    public Filename(String name, String extension) {
        this.name = name;
        this.extension = extension;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Filename filename = (Filename) o;

        if (!extension.equals(filename.extension)) return false;
        if (!name.equals(filename.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + extension.hashCode();
        return result;
    }

    // ...
}
