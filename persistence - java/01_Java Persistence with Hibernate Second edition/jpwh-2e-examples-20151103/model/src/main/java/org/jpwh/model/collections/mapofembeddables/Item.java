package org.jpwh.model.collections.mapofembeddables;

import org.jpwh.model.Constants;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
public class Item {

    @Id
    @GeneratedValue(generator = Constants.ID_GENERATOR)
    protected Long id;

    @ElementCollection
    @CollectionTable(name = "IMAGE")
    protected Map<Filename, Image> images = new HashMap<Filename, Image>();

    public Long getId() {
        return id;
    }

    public Map<Filename, Image> getImages() {
        return images;
    }

    public void setImages(Map<Filename, Image> images) {
        this.images = images;
    }


    // ...
}
