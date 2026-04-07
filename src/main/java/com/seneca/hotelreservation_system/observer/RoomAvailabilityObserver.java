package com.seneca.hotelreservation_system.observer;

public interface RoomAvailabilityObserver {
    void onRoomAvailabilityChange(String message);
}