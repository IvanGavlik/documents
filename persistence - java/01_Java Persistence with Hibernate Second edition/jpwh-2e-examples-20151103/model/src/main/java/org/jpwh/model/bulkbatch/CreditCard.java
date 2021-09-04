package org.jpwh.model.bulkbatch;

import javax.persistence.Entity;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
public class CreditCard extends BillingDetails {

    @NotNull
    protected String cardNumber;

    @NotNull
    protected String expMonth;

    @NotNull
    protected String expYear;

    @NotNull
    protected Date stolenOn = new Date(0);

    public CreditCard() {
        super();
    }

    public CreditCard(User user, String owner, String cardNumber, String expMonth, String expYear) {
        super(user, owner);
        this.cardNumber = cardNumber;
        this.expMonth = expMonth;
        this.expYear = expYear;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getExpMonth() {
        return expMonth;
    }

    public void setExpMonth(String expMonth) {
        this.expMonth = expMonth;
    }

    public String getExpYear() {
        return expYear;
    }

    public void setExpYear(String expYear) {
        this.expYear = expYear;
    }

    public Date getStolenOn() {
        return stolenOn;
    }

    public void setStolenOn(Date stolenOn) {
        this.stolenOn = stolenOn;
    }
}