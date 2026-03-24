package com.seneca.hotelreservation_system.util;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;


public class JPAUtil {

    private static final String PERSISTENCE_UNIT_NAME = "HotelReservationPU";
    private static EntityManagerFactory factory;

    // Private constructor to prevent instantiation from outside
    private JPAUtil() {
    }


    public static synchronized EntityManagerFactory getEntityManagerFactory() {
        if (factory == null) {
            try {
                factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
                System.out.println("EntityManagerFactory created successfully.");
            } catch (Exception ex) {
                System.err.println("Initial EntityManagerFactory creation failed: " + ex.getMessage());
                throw new ExceptionInInitializerError(ex);
            }
        }
        return factory;
    }


    public static void shutdown() {
        if (factory != null && factory.isOpen()) {
            factory.close();
            System.out.println("EntityManagerFactory closed.");
        }
    }
}