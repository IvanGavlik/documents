package org.jpwh.model.customsql;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

@Embeddable
public class Image {

    @NotNull
    @Column(nullable = false)
    protected String filename;

    @NotNull
    protected int width;

    @NotNull
    protected int height;

    public Image() {
    }

    public Image(String filename, int width, int height) {
        this.filename = filename;
        this.width = width;
        this.height = height;
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

    public void setWidth(int sizeX) {
        this.width = sizeX;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int sizeY) {
        this.height = sizeY;
    }

    // Whenever value-types are managed in collections, overriding equals/hashCode is a good idea!

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Image image = (Image) o;

        if (width != image.width) return false;
        if (height != image.height) return false;
        if (!filename.equals(image.filename)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = filename.hashCode();
        result = 31 * result + width;
        result = 31 * result + height;
        return result;
    }
    // ...
}
