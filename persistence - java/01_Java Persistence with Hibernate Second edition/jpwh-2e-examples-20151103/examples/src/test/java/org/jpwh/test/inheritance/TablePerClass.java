package org.jpwh.test.inheritance;

import org.jpwh.model.inheritance.tableperclass.BankAccount;
import org.jpwh.model.inheritance.tableperclass.CreditCard;
import org.testng.annotations.Test;

public class TablePerClass extends InheritanceCRUD {

    @Override
    public void configurePersistenceUnit() throws Exception {
        configurePersistenceUnit("TablePerClassPU");
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

    @Test(groups = {"H2", "POSTGRESQL"})
    public void jdbcBillingDetailsSqlQuery() throws Exception {
        storeLoadBillingDetails();
        doJdbcSqlQuery("inheritance/tableperclass/UnionQuery.sql.txt", false, new String[][]{
                {"\\d*", "Jane Roe", null, null, null, "445566", "One Percent Bank Inc.", "999", "2"},
                {"\\d*", "John Doe", "06", "2015", "1234123412341234", null, null, null, "1"}});
    }

    @Test
    @Override
    public void storeLoadBillingDetails() throws Exception {
        super.storeLoadBillingDetails();
    }

}
