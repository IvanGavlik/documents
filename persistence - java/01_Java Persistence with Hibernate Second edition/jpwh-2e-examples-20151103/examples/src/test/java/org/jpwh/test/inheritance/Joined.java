package org.jpwh.test.inheritance;

import org.jpwh.model.inheritance.joined.BankAccount;
import org.jpwh.model.inheritance.joined.CreditCard;
import org.testng.annotations.Test;

public class Joined extends InheritanceCRUD {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("JoinedPU");
    }

    @Override
    protected Object createBankAccount() {
        return new BankAccount(
                "Jane Roe", "445566", "One Percent Bank Inc.", "999"
        );
    }

    @Override
    protected Object createCreditCard() {
        return new CreditCard(
                "John Doe", "1234123412341234", "06", "2015"
        );
    }

    @Test
    public void jdbcBillingDetailsSqlQuery() throws Exception {
        storeLoadBillingDetails();
        doJdbcSqlQuery("inheritance/joined/AllQuery.sql.txt", false, new String[][]{
                {"\\d*", "Jane Roe", null, null, null, "445566", "One Percent Bank Inc.", "999", "2"},
                {"\\d*", "John Doe", "06", "2015", "1234123412341234", null, null, null, "1"}});
    }

    @Test
    public void jdbcCreditCardSqlQuery() throws Exception {
        storeLoadBillingDetails();
        doJdbcSqlQuery("inheritance/joined/CreditCard.sql.txt", false, new String[][]{
                {"\\d*", "John Doe", "06", "2015", "1234123412341234"}});
    }

    @Test
    @Override
    public void storeLoadBillingDetails() throws Exception {
        super.storeLoadBillingDetails();
    }

}
