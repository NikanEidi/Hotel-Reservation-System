package com.seneca.hotelreservation_system.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.math.BigDecimal;

@Entity
@DiscriminatorValue("Double")
public class DoubleRoom extends Room {
    public DoubleRoom() {}
    public DoubleRoom(String roomNumber, BigDecimal basePrice) {
        super(roomNumber, basePrice, 4);
    }
}