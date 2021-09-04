package org.jpwh.web.jsf;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ApplicationScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.inject.Named;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.SingularAttribute;

@Named("persistentAttributeConverter")
@ApplicationScoped
public class PersistentAttributeConverter implements Converter {

    @PersistenceUnit
    protected EntityManagerFactory emf;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        return getAttribute(value);
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        if (value instanceof SingularAttribute) {
            return getString((SingularAttribute) value);
        } else {
            throw new ConverterException(new FacesMessage(
                    "Not a JPA metamodel SingularAttribute: " + value
            ));
        }
    }

    public boolean equals(SingularAttribute attribute, String value) {
        return attribute.equals(getAttribute(value));
    }

    public SingularAttribute getAttribute(String value) {
        // TODO: This only supports 'Item_.name', not 'User_.address.city.zipCode'
        String entityName = value.substring(0, value.lastIndexOf("."));
        String attributeName = value.substring(entityName.length()+1);

        SingularAttribute attribute = null;
        for (EntityType<?> entityType : emf.getMetamodel().getEntities()) {
            if (entityType.getName().equals(entityName)) {
                try {
                    attribute = entityType.getSingularAttribute(attributeName);
                } catch (IllegalArgumentException ex) {
                    throw new ConverterException(new FacesMessage(
                            "Persistent entity '" + entityName + "' does not have attribute: " + attributeName
                    ));
                }
            }
        }

        if (attribute == null)
            throw new ConverterException(new FacesMessage(
                    "Persistent attribute not found: " + value
            ));

        return attribute;
    }

    public String getString(SingularAttribute attribute) {
        ManagedType declaringType = attribute.getDeclaringType();
        if (declaringType instanceof EntityType) {
            EntityType type = (EntityType) declaringType;
            return type.getName() + "." + attribute.getName();
            // TODO: Support other managed types
        } else {
            throw new ConverterException(new FacesMessage(
                    "Not a JPA metamodel EntityType: " + declaringType
            ));
        }
    }

}
