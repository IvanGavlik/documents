package org.jpwh.converter;

import org.jpwh.model.advanced.converter.GermanZipcode;
import org.jpwh.model.advanced.converter.SwissZipcode;
import org.jpwh.model.advanced.converter.Zipcode;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class ZipcodeConverter
    implements AttributeConverter<Zipcode, String> {

    @Override
    public String convertToDatabaseColumn(Zipcode attribute) {
        return attribute.getValue();
    }

    @Override
    public Zipcode convertToEntityAttribute(String s) {
        if (s.length() == 5)
            return new GermanZipcode(s);
        else if (s.length() == 4)
            return new SwissZipcode(s);

        // If you get to this point, you should consider
        // cleaning up your database... or you can create
        // an InvalidZipCode subclass and return it here.

        throw new IllegalArgumentException(
            "Unsupported zipcode in database: " + s
        );
    }
}
