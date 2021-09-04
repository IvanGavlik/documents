package org.jpwh.test.inheritance;

import org.jpwh.model.inheritance.mappedsuperclass.BankAccount;
import org.jpwh.model.inheritance.mappedsuperclass.BillingDetails;
import org.jpwh.model.inheritance.mappedsuperclass.CreditCard;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class MappedSuperclass extends InheritanceCRUD {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("MappedSuperclassPU");
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

    @Override
    protected String getBillingDetailsQuery() {
        // Hibernate however supports fully polymorphic queries, as long as you
        // name a class/interface fully qualified (doesn't have to be mapped)
        return "select bd from " + BillingDetails.class.getName() + " bd";
    }

    @Test
    public void jdbcSqlQueryBankAccount() throws Exception {
        storeLoadBillingDetails();
        doJdbcSqlQuery("inheritance/mappedsuperclass/BankAccount.sql.txt",
                true, new String[]{"\\d*", "Jane Roe", "445566", "One Percent Bank Inc.", "999"});
    }

    @Test
    public void jdbcSqlQueryCreditCard() throws Exception {
        storeLoadBillingDetails();
        doJdbcSqlQuery("inheritance/mappedsuperclass/CreditCard.sql.txt",
                true, new String[]{"\\d*", "John Doe", "1234123412341234", "06", "2015"});
    }

    @Test
    @Override
    public void storeLoadBillingDetails() throws Exception {
        super.storeLoadBillingDetails();
    }

}
