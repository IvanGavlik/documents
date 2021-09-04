package org.jpwh.model.collections.bagofembeddables;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class Image {

    @Column(nullable = true) // Can be null if we have surrogate PK!
    protected String title;

    @Column(nullable = false)
    protected String filename;

    protected int width;

    protected int height;

    public Image() {
    }

    public Image(String title, String filename, int width, int height) {
        this.title = title;
        this.filename = filename;
        this.width = width;
        this.height = height;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    // Whenever value-types are managed in collections, overriding equals/hashCode is a good idea!

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Image other = (Image) o;

        // The title is optional
        if (title != null ? !title.equals(other.title) : other.title != null) return false;
        if (width != other.width) return false;
        if (height != other.height) return false;
        if (!filename.equals(other.filename)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = title != null ? title.hashCode() : 0;
        result = 31 * result + filename.hashCode();
        result = 31 * result + width;
        result = 31 * result + height;
        return result;
    }

    // ...
}
