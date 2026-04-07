package com.seneca.hotelreservation_system.observer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdminNotificationService implements RoomAvailabilitySubject {

    private static final AdminNotificationService INSTANCE = new AdminNotificationService();

    private final List<RoomAvailabilityObserver> observers = new ArrayList<>();
    private final List<String> notifications = new ArrayList<>();

    private AdminNotificationService() {
    }

    public static AdminNotificationService getInstance() {
        return INSTANCE;
    }

    @Override
    public void addObserver(RoomAvailabilityObserver observer) {
        if (observer != null && !observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(RoomAvailabilityObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(String message) {
        notifications.add(0, message);
        for (RoomAvailabilityObserver observer : observers) {
            observer.onRoomAvailabilityChange(message);
        }
    }

    public List<String> getNotifications() {
        return Collections.unmodifiableList(notifications);
    }

    public String getLatestNotification() {
        return notifications.isEmpty() ? "No new notifications." : notifications.get(0);
    }
}