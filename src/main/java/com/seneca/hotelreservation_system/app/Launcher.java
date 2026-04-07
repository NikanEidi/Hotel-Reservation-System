package com.seneca.hotelreservation_system.app;

import com.seneca.hotelreservation_system.service.AuthService;
import com.seneca.hotelreservation_system.util.JPAUtil;

public class Launcher {
    public static void main(String[] args) {
        try {
            JPAUtil.getEntityManagerFactory();
        } catch (Exception e) {
            System.err.println("Database connection failed: " + e.getMessage());
            return;
        }

        AuthService authService = new AuthService();
        try {
            authService.registerAdmin("nikan", "123456", "Admin");
        } catch (Exception ignored) {
        }

        HotelApp.main(args);
    }
}