package com.seneca.hotelreservation_system.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import java.math.BigDecimal;

@Entity
@DiscriminatorValue("Single")
public class SingleRoom extends Room {
    public SingleRoom() {}
    public SingleRoom(String roomNumber, BigDecimal basePrice) {
        super(roomNumber, basePrice, 2);
    }
}