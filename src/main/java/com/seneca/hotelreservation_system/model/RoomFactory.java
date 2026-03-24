package com.seneca.hotelreservation_system.model;

import java.math.BigDecimal;


public class RoomFactory {

    public static Room createRoom(String type, String roomNumber, BigDecimal basePrice) {
        if (type == null) {
            return null;
        }

        return switch (type.toLowerCase()) {
            case "single" -> new SingleRoom(roomNumber, basePrice);
            case "double" -> new DoubleRoom(roomNumber, basePrice);
            case "penthouse" -> new PenthouseRoom(roomNumber, basePrice);
            default -> throw new IllegalArgumentException("Unknown room type: " + type);
        };
    }
}