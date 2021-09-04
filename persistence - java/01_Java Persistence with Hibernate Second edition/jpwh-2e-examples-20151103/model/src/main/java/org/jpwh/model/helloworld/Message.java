package org.jpwh.model.helloworld;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/* 
    Every persistent entity class must have at least the <code>@Entity</code> annotation.
    Hibernate maps this class to a table called <code>MESSAGE</code>.
 */
@Entity
public class Message {

    /* 
        Every persistent entity class must have an identifier
        attribute annotated with <code>@Id</code>. Hibernate maps
        this attribute to a column named <code>ID</code>.
     */
    @Id
    /* 
        Someone must generate identifier values; this annotation enables
        automatic generation of IDs.
     */
    @GeneratedValue
    private Long id;

    /* 
        You usually implement regular attributes of a persistent class with private
        or protected fields, and public getter/setter method pairs. Hibernate maps
        this attribute to a column called <code>TEXT</code>.
     */
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
