package org.jpwh.web.dao;

import org.jpwh.web.model.Image;

import java.io.InputStream;

public interface ImageDAO extends GenericDAO<Image, Long> {

    Image hydrateImage(InputStream inputStream) throws Exception;

}
