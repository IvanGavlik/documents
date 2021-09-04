package org.jpwh.web.jsf;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.inject.Named;
import javax.mail.internet.MimeUtility;
import java.io.*;

@Named("base64Converter")
@ApplicationScoped
public class Base64Converter implements Converter {

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        byte b[] = value.getBytes();
        try (ByteArrayInputStream bi = new ByteArrayInputStream(b);
             ObjectInputStream si = new ObjectInputStream(MimeUtility.decode(bi, "base64"))) {
            return si.readObject();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        return getString(value);
    }

    public String getString(Object value) {
        if (!(value instanceof Serializable))
            throw new ConverterException(new FacesMessage(
                    "Must be java.io.Serializable: " + value
            ));
        try (ByteArrayOutputStream bo = new ByteArrayOutputStream();
             ObjectOutputStream so = new ObjectOutputStream(MimeUtility.encode(bo, "base64"))) {
            so.writeObject(value);
            so.flush();
            return bo.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
