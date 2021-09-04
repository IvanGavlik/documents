package org.jpwh.model.advanced;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Currency;

/* 
   This value-typed class should be <code>java.io.Serializable</code>: When Hibernate stores entity
   instance data in the shared second-level cache (see <a href="#Caching"/>), it <em>disassembles</em>
   the entity's state. If an entity has a <code>MonetaryAmount</code> property, the serialized
   representation of the property value will be stored in the second-level cache region. When entity
   data is retrieved from the cache region, the property value will be deserialized and reassembled.
 */
public class MonetaryAmount implements Serializable {

    /*
 The class does not need a special constructor, you can make it immutable, even with
        <code>final</code> fields, as your code will be the only place an instance is created.
     */
    protected final BigDecimal value;
    protected final Currency currency;

    public MonetaryAmount(BigDecimal value, Currency currency) {
        this.value = value;
        this.currency = currency;
    }

    public BigDecimal getValue() {
        return value;
    }

    public Currency getCurrency() {
        return currency;
    }

    /*
 You should implement the <code>equals()</code> and <code>hashCode()</code>
        methods, and compare monetary amounts "by value".
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MonetaryAmount)) return false;

        final MonetaryAmount monetaryAmount = (MonetaryAmount) o;

        if (!value.equals(monetaryAmount.value)) return false;
        if (!currency.equals(monetaryAmount.currency)) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = value.hashCode();
        result = 29 * result + currency.hashCode();
        return result;
    }

    /*
 You will need a <code>String</code> representation of a monetary
        amount. Implement the <code>toString()</code> method and a static method to
        create an instance from a <code>String</code>.
     */
    public String toString() {
        return getValue() + " " + getCurrency();
    }

    public static MonetaryAmount fromString(String s) {
        String[] split = s.split(" ");
        return new MonetaryAmount(
            new BigDecimal(split[0]),
            Currency.getInstance(split[1])
        );
    }
}

