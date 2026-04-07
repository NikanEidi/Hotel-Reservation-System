package com.seneca.hotelreservation_system.observer;

public interface RoomAvailabilitySubject {
    void addObserver(RoomAvailabilityObserver observer);
    void removeObserver(RoomAvailabilityObserver observer);
    void notifyObservers(String message);
}