package com.seneca.hotelreservation_system.app;

import com.seneca.hotelreservation_system.service.AuthService;
import com.seneca.hotelreservation_system.util.JPAUtil;

public class Launcher {
    public static void main(String[] args) {
        System.out.println("Booting Up System...");

        try {
            JPAUtil.getEntityManagerFactory();
            System.out.println("Database connected successfully.");
        } catch (Exception e) {
            System.err.println("Critical Error: Failed to connect to the database.");
            e.printStackTrace();
            return;
        }

        AuthService authService = new AuthService();
        try {
            authService.registerAdmin("nikan", "123456", "Admin");
            System.out.println("Security: Initial admin user 'nikan' registered with password '123456'.");
        } catch (Exception e) {
            System.out.println("Security: Admin user already exists. Skipping registration.");
        }

        System.out.println("Launching User Interface...");
        HotelApp.main(args);
    }
}