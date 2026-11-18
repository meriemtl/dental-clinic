package com.dentalclinic.model;

import java.math.BigDecimal;

/** An entry of the clinic's price list (consultation, filling, crown...). */
public class TreatmentType {
    private int typeId;
    private String name;
    private String description;
    private BigDecimal defaultPrice = BigDecimal.ZERO;
    private boolean active = true;

    public TreatmentType() {
    }

    public int getTypeId() { return typeId; }
    public void setTypeId(int typeId) { this.typeId = typeId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getDefaultPrice() { return defaultPrice; }
    public void setDefaultPrice(BigDecimal defaultPrice) { this.defaultPrice = defaultPrice; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return name;
    }
}
