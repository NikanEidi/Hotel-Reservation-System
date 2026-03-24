package com.seneca.hotelreservation_system.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

@SuppressWarnings({ "unused", "SpellCheckingInspection" })
public class UIController {

    @FXML
    private TextField adminSearchField;

    @FXML
    private TextField phoneField;

    @FXML
    private TextField nameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    public void goToWelcome(ActionEvent event) throws IOException {
        switchScene(event, "/com/seneca/hotelreservation_system/view/welcome-view.fxml");
    }

    @FXML
    public void goToRoomSelection(ActionEvent event) throws IOException {
        switchScene(event, "/com/seneca/hotelreservation_system/view/room-selection-view.fxml");
    }

    @FXML
    public void goToGuestDetails(ActionEvent event) throws IOException {
        switchScene(event, "/com/seneca/hotelreservation_system/view/guest-details-view.fxml");
    }

    @FXML
    public void goToAddons(ActionEvent event) throws IOException {
        switchScene(event, "/com/seneca/hotelreservation_system/view/addons-view.fxml");
    }

    @FXML
    public void goToSearch(ActionEvent event) throws IOException {
        switchScene(event, "/com/seneca/hotelreservation_system/view/search-view.fxml");
    }

    @FXML
    public void goToSummary(ActionEvent event) throws IOException {
        switchScene(event, "/com/seneca/hotelreservation_system/view/summary-view.fxml");
    }

    @FXML
    public void goToConfirmation(ActionEvent event) throws IOException {
        switchScene(event, "/com/seneca/hotelreservation_system/view/confirmation-view.fxml");
    }

    @FXML
    public void goToAdminLogin(ActionEvent event) throws IOException {
        switchScene(event, "/com/seneca/hotelreservation_system/view/admin-login-view.fxml");
    }

    @FXML
    public void goToAdminDashboard(ActionEvent event) throws IOException {
        switchScene(event, "/com/seneca/hotelreservation_system/view/admin-dashboard-view.fxml");
    }

    @FXML
    public void goToAdmin(ActionEvent event) throws IOException {
        goToAdminDashboard(event);
    }

    @FXML
    public void handleAdminLogin(ActionEvent event) throws IOException {
        String username = usernameField != null ? usernameField.getText().trim() : "";
        String password = passwordField != null ? passwordField.getText().trim() : "";

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(
                    Alert.AlertType.ERROR,
                    "Login Error",
                    "Missing Credentials",
                    "Please enter both username and password."
            );
            return;
        }


        if (username.equals("admin") && password.equals("admin123")) {
            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Login Successful",
                    "Welcome",
                    "Administrator access granted."
            );
            goToAdminDashboard(event);
        } else {
            showAlert(
                    Alert.AlertType.ERROR,
                    "Login Failed",
                    "Invalid Credentials",
                    "Incorrect username or password."
            );
        }
    }

    @FXML
    public void showFeedbackModule() {
        showPlaceholder("Feedback", "Feedback page is part of later milestone functionality.");
    }

    @FXML
    public void showBillingModule() {
        showPlaceholder("Billing", "Billing page is part of later milestone functionality.");
    }

    @FXML
    public void showWaitlistModule() {
        showPlaceholder("Waitlist", "Waitlist page is part of later milestone functionality.");
    }

    @FXML
    public void showRules() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Grand Plaza Rules & Policies");
        alert.setHeaderText("Hotel Kiosk Regulations");
        alert.setContentText(
                "1. Check-in: 3:00 PM | Check-out: 11:00 AM\n" +
                        "2. Maximum occupancy per room must be respected.\n" +
                        "3. Cancellations must be made 24 hours in advance."
        );
        alert.showAndWait();
    }

    @FXML
    public void handleSearch() {
        String query = (adminSearchField != null) ? adminSearchField.getText() : "None";

        Alert searchAlert = new Alert(Alert.AlertType.INFORMATION);
        searchAlert.setTitle("Admin Search");
        searchAlert.setHeaderText("Searching Records");
        searchAlert.setContentText("Searching for guest: " + query);
        searchAlert.showAndWait();
    }

    @FXML
    public void checkLoyaltyStatus() {
        if (phoneField != null && nameField != null && emailField != null) {
            String phone = phoneField.getText();

            if (phone != null && phone.length() == 10) {
                if ("1234567890".equals(phone)) {
                    nameField.setText("Nikan Eidi");
                    emailField.setText("nikaneydi1984@gmail.com");
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Loyalty Member");
                    alert.setHeaderText(null);
                    alert.setContentText("Would you like to become a Loyalty Member?");
                    alert.showAndWait();
                }
            }
        }
    }

    private void switchScene(ActionEvent event, String fxmlPath) throws IOException {
        URL resource = getClass().getResource(fxmlPath);

        if (resource == null) {
            throw new IOException("FXML file not found: " + fxmlPath);
        }

        Parent root = FXMLLoader.load(resource);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 1400, 900));
        stage.show();
    }

    private void showPlaceholder(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, title + " Module", message);
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void setSearchData(int adults, int children,
                              java.time.LocalDate checkIn, java.time.LocalDate checkOut,
                              String name, String email, String phone) {

        // Temporary storage / debug output
        System.out.println("Search Data Received:");
        System.out.println("Adults: " + adults);
        System.out.println("Children: " + children);
        System.out.println("Check-in: " + checkIn);
        System.out.println("Check-out: " + checkOut);
        System.out.println("Name: " + name);
        System.out.println("Email: " + email);
        System.out.println("Phone: " + phone);
    }
}