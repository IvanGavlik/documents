package org.jpwh.model.bulkbatch;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class StolenCreditCard {

    @Id // Application-assigned!
    public Long id;
    public String owner;
    public String cardNumber;
    public String expMonth;
    public String expYear;
    public Long userId;
    public String username;

    public StolenCreditCard() {
    }

    public StolenCreditCard(Long id,
                            String owner, String cardNumber,
                            String expMonth, String expYear,
                            Long userId, String username) {
        this.id = id;
        this.owner = owner;
        this.cardNumber = cardNumber;
        this.expMonth = expMonth;
        this.expYear = expYear;
        this.userId = userId;
        this.username = username;
    }
}
