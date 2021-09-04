package org.jpwh.test.inheritance;

import org.jpwh.model.inheritance.singletable.BankAccount;
import org.jpwh.model.inheritance.singletable.CreditCard;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SingleTable extends InheritanceCRUD {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("SingleTablePU");
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
        doJdbcSqlQuery("inheritance/singletable/AllQuery.sql.txt", false, new String[][]{
                {"\\d*", "Jane Roe", null, null, null, "445566", "One Percent Bank Inc.", "999", "BA"},
                {"\\d*", "John Doe", "06", "2015", "1234123412341234", null, null, null, "CC"}});
    }

    @Test
    public void jdbcCreditCardSqlQuery() throws Exception {
        storeLoadBillingDetails();
        doJdbcSqlQuery("inheritance/singletable/CreditCard.sql.txt", false, new String[][]{
                {"\\d*", "John Doe", "06", "2015", "1234123412341234"}});
    }

    @Test
    @Override
    public void storeLoadBillingDetails() throws Exception {
        super.storeLoadBillingDetails();
    }

}
