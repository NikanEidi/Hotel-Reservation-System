package com.seneca.hotelreservation_system.util;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public final class JPAUtil {

    private static final String PERSISTENCE_UNIT_NAME = "HotelReservationPU";
    private static EntityManagerFactory factory;

    private JPAUtil() {
    }

    public static synchronized EntityManagerFactory getEntityManagerFactory() {
        if (factory == null || !factory.isOpen()) {
            factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
        }
        return factory;
    }

    public static synchronized boolean isDatabaseAvailable() {
        try {
            EntityManagerFactory emf = getEntityManagerFactory();
            return emf != null && emf.isOpen();
        } catch (Exception ex) {
            return false;
        }
    }

    public static synchronized void shutdown() {
        if (factory != null && factory.isOpen()) {
            factory.close();
        }
    }
}