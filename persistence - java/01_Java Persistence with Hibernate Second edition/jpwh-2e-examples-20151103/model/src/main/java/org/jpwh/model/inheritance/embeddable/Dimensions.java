package org.jpwh.model.inheritance.embeddable;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Embeddable
@AttributeOverrides({
        @AttributeOverride(name = "name",
                column = @Column(name = "DIMENSIONS_NAME")),
        @AttributeOverride(name = "symbol",
                column = @Column(name = "DIMENSIONS_SYMBOL"))
})
public class Dimensions extends Measurement {

    public static Dimensions centimeters(BigDecimal width, BigDecimal height, BigDecimal depth) {
        return new Dimensions("centimeters", "cm", width, height, depth);
    }

    public static Dimensions inches(BigDecimal width, BigDecimal height, BigDecimal depth) {
        return new Dimensions("inches", "\"", width, height, depth);
    }

    @NotNull
    protected BigDecimal depth;

    @NotNull
    protected BigDecimal height;

    @NotNull
    protected BigDecimal width;

    // ...
    public Dimensions() {
    }

    public Dimensions(String name, String symbol, BigDecimal width, BigDecimal height, BigDecimal depth) {
        super(name, symbol);
        this.height = height;
        this.width = width;
        this.depth = depth;
    }

    public BigDecimal getDepth() {
        return depth;
    }

    public BigDecimal getHeight() {
        return height;
    }

    public BigDecimal getWidth() {
        return width;
    }

    public void setDepth(BigDecimal depth) {
        this.depth = depth;
    }

    public void setHeight(BigDecimal height) {
        this.height = height;
    }

    public void setWidth(BigDecimal width) {
        this.width = width;
    }

    public String toString() {
        return String.format("W:%s%s x H:%s%s x D:%s%s", this.height, this.symbol, this.width, this.symbol, this.depth, this.symbol);
    }
}
