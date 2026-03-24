package com.seneca.hotelreservation_system.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.math.BigDecimal;

@Entity
@DiscriminatorValue("Penthouse")
public class PenthouseRoom extends Room {
    public PenthouseRoom() {}
    public PenthouseRoom(String roomNumber, BigDecimal basePrice) {
        super(roomNumber, basePrice, 2);
    }
}