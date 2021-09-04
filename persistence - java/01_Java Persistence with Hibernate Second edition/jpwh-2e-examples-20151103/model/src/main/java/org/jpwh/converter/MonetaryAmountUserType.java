package org.jpwh.converter;

import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.hibernate.usertype.CompositeUserType;
import org.hibernate.usertype.DynamicParameterizedType;
import org.jpwh.model.advanced.MonetaryAmount;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Currency;
import java.util.Properties;

public class MonetaryAmountUserType
    implements CompositeUserType, DynamicParameterizedType {

    protected Currency convertTo;

    public void setParameterValues(Properties parameters) {

        /**
         * You can access some dynamic parameters here, such as the
         * name of the mapped columns, the mapped (entity) table, or even the
         * annotations on the field/getter of the mapped property. We won't need
         * them in this example though.
         */
        ParameterType parameterType =
            (ParameterType) parameters.get(PARAMETER_TYPE);
        String[] columns = parameterType.getColumns();
        String table = parameterType.getTable();
        Annotation[] annotations = parameterType.getAnnotationsMethod();

        /**
         * We only use the <code>convertTo</code> parameter to
         * determine the target currency when saving a value into the database.
         * If the parameter hasn't been set, we default to US Dollar.
         */
        String convertToParameter = parameters.getProperty("convertTo");
        this.convertTo = Currency.getInstance(
            convertToParameter != null ? convertToParameter : "USD"
        );
    }

    /**
     * The method <code>returnedClass</code> adapts the given class, in this case
     * <code>MonetaryAmount</code>.
     */
    public Class returnedClass() {
        return MonetaryAmount.class;
    }

    /**
     * Hibernate can enable some optimizations if it knows
     * that <code>MonetaryAmount</code> is immutable.
     */
    public boolean isMutable() {
        return false;
    }

    /**
     * If Hibernate has to make a copy of the value, it will call
     * this method. For simple immutable classes like <code>MonetaryAmount</code>,
     * you can return the given instance.
     */
    public Object deepCopy(Object value) {
        return value;
    }

    /**
     * Hibernate calls <code>disassemble</code> when it stores a value in the global shared second-level
     * cache. You need to return a <code>Serializable</code> representation. For <code>MonetaryAmount</code>,
     * a <code>String</code> representation is an easy solution. Or, because <code>MonetaryAmount</code> is actually
     * <code>Serializable</code>, you could return it directly.
     */
    public Serializable disassemble(Object value,
                                    SessionImplementor session) {
        return value.toString();
    }

    /**
     * Hibernate calls this method when it reads the serialized
     * representation from the global shared second-level cache. We create a
     * <code>MonetaryAmount</code> instance from the <code>String</code>
     * representation. Or, if have stored a serialized <code>MonetaryAmount</code>,
     * you could return it directly.
     */
    public Object assemble(Serializable cached,
                           SessionImplementor session, Object owner) {
        return MonetaryAmount.fromString((String) cached);
    }

    /**
     * Called during <code>EntityManager#merge()</code> operations, you
     * need to return a copy of the <code>original</code>. Or, if your value type is
     * immutable, like <code>MonetaryAmount</code>, you can simply return the original.
     */
    public Object replace(Object original, Object target,
                          SessionImplementor session, Object owner) {
        return original;
    }

    /**
     * Hibernate will use value equality to determine whether the value
     * was changed, and the database needs to be updated. We rely on the equality
     * routine we have already written on the <code>MonetaryAmount</code> class.
     */
    public boolean equals(Object x, Object y) {
        return x == y || !(x == null || y == null) && x.equals(y);
    }

    public int hashCode(Object x) {
        return x.hashCode();
    }

    /**
     * Called to read the <code>ResultSet</code>, when a
     * <code>MonetaryAmount</code> value has to be retrieved from the database.
     * We take the <code>amount</code> and <code>currency</code> values as given
     * in the query result, and create a new instance of <code>MonetaryAmount</code>.
     */
    public Object nullSafeGet(ResultSet resultSet,
                              String[] names,
                              SessionImplementor session,
                              Object owner) throws SQLException {

        BigDecimal amount = resultSet.getBigDecimal(names[0]);
        if (resultSet.wasNull())
            return null;
        Currency currency =
            Currency.getInstance(resultSet.getString(names[1]));
        return new MonetaryAmount(amount, currency);
    }

    /**
     * Called when a <code>MonetaryAmount</code> value has
     * to be stored in the database. We convert the value to the target currency,
     * then set the <code>amount</code> and <code>currency</code> on the
     * provided <code>PreparedStatement</code>. (Unless the <code>MonetaryAmount</code>
     * was <code>null</code>, in that case, we call <code>setNull()</code> to
     * prepare the statement.)
     */
    public void nullSafeSet(PreparedStatement statement,
                            Object value,
                            int index,
                            SessionImplementor session) throws SQLException {

        if (value == null) {
            statement.setNull(
                index,
                StandardBasicTypes.BIG_DECIMAL.sqlType());
            statement.setNull(
                index + 1,
                StandardBasicTypes.CURRENCY.sqlType());
        } else {
            MonetaryAmount amount = (MonetaryAmount) value;
            // When saving, convert to target currency
            MonetaryAmount dbAmount = convert(amount, convertTo);
            statement.setBigDecimal(index, dbAmount.getValue());
            statement.setString(index + 1, convertTo.getCurrencyCode());
        }
    }

    /**
     * Here you can implement whatever currency conversion routine
     * you need. For the sake of the example, we simply double the value so we
     * can easily test if conversion was successful. You'll have to replace this
     * code with a real currency converter in a real application. It's not a
     * method of the Hibernate <code>UserType</code> API.
     */
    protected MonetaryAmount convert(MonetaryAmount amount,
                                     Currency toCurrency) {
        return new MonetaryAmount(
            amount.getValue().multiply(new BigDecimal(2)),
            toCurrency
        );
    }

    public String[] getPropertyNames() {
        return new String[]{"value", "currency"};
    }

    public Type[] getPropertyTypes() {
        return new Type[]{
            StandardBasicTypes.BIG_DECIMAL,
            StandardBasicTypes.CURRENCY
        };
    }

    public Object getPropertyValue(Object component,
                                   int property) {
        MonetaryAmount monetaryAmount = (MonetaryAmount) component;
        if (property == 0)
            return monetaryAmount.getValue();
        else
            return monetaryAmount.getCurrency();
    }

    public void setPropertyValue(Object component,
                                 int property,
                                 Object value) {
        throw new UnsupportedOperationException(
            "MonetaryAmount is immutable"
        );
    }

    // ...
}
