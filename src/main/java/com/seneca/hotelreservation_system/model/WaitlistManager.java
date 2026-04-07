package com.seneca.hotelreservation_system.model;

import java.util.ArrayList;
import java.util.List;

public class WaitlistManager {

    public interface Observer {
        void update(String message);
    }

    private static WaitlistManager instance;
    private final List<Observer> observers = new ArrayList<>();
    private final List<String> waitlist = new ArrayList<>();

    private WaitlistManager() {}

    public static synchronized WaitlistManager getInstance() {
        if (instance == null) {
            instance = new WaitlistManager();
        }
        return instance;
    }

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    private void notifyObservers(String message) {
        for (Observer observer : observers) {
            observer.update(message);
        }
    }

    public void addToWaitlist(String guestName, String email) {
        String entry = guestName + " (" + email + ")";
        waitlist.add(entry);
        notifyObservers("Guest added to waitlist: " + entry);
    }

    public List<String> getWaitlist() {
        return new ArrayList<>(waitlist);
    }
}
