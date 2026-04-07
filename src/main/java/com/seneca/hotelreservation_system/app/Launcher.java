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

        // MS3 Requirement: Multithreaded server for admin sessions
        new Thread(() -> {
            try (java.net.ServerSocket serverSocket = new java.net.ServerSocket(8081)) {
                System.out.println("Multithreaded Admin Server started on port 8081");
                while (true) {
                    java.net.Socket clientSocket = serverSocket.accept();
                    new Thread(new AdminSessionHandler(clientSocket)).start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        HotelApp.main(args);
    }

    private static class AdminSessionHandler implements Runnable {
        private final java.net.Socket socket;
        public AdminSessionHandler(java.net.Socket socket) {
            this.socket = socket;
        }
        @Override
        public void run() {
            try {
                java.io.PrintWriter out = new java.io.PrintWriter(socket.getOutputStream(), true);
                out.println("Connected to Grand Plaza Admin Server (MS3 feature)");
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}