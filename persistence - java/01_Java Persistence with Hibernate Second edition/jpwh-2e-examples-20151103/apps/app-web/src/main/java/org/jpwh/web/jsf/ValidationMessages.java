package org.jpwh.web.jsf;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.util.ResourceBundle;

public class ValidationMessages {

    public static void addFacesMessage(String key) {
        FacesContext.getCurrentInstance().addMessage(null,
           new FacesMessage(ValidationMessages.get(key)));
    }

    public static String get(String key) {
        String messageBundleName = FacesContext.getCurrentInstance().getApplication().getMessageBundle();
        ResourceBundle validationMessages = ResourceBundle.getBundle(messageBundleName);
        return validationMessages.getString(key);
    }

}
