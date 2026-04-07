package com.seneca.hotelreservation_system.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.io.IOException;
import java.time.LocalDate;

public class SearchController {

    @FXML private Label adultCountLabel;
    @FXML private Label childCountLabel;
    @FXML private DatePicker checkInDatePicker;
    @FXML private DatePicker checkOutDatePicker;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private Label errorLabel;

    private UIController mainController;
    private int adults = 2;
    private int children = 0;

    public void setMainController(UIController controller) {
        this.mainController = controller;
        UIController.BookingData data = mainController.getBookingData();
        if (data != null) {
            adults = data.adults;
            children = data.children;
            adultCountLabel.setText(String.valueOf(adults));
            childCountLabel.setText(String.valueOf(children));
            if (data.checkIn != null) checkInDatePicker.setValue(data.checkIn);
            if (data.checkOut != null) checkOutDatePicker.setValue(data.checkOut);
            if (data.guestName != null) fullNameField.setText(data.guestName);
            if (data.guestEmail != null) emailField.setText(data.guestEmail);
            if (data.guestPhone != null) phoneField.setText(data.guestPhone);
        }
    }

    @FXML
    public void increaseAdults() {
        if (adults < 8) {
            adults++;
            adultCountLabel.setText(String.valueOf(adults));
        }
    }

    @FXML
    public void decreaseAdults() {
        if (adults > 1) {
            adults--;
            adultCountLabel.setText(String.valueOf(adults));
        }
    }

    @FXML
    public void increaseChildren() {
        if (children < 6) {
            children++;
            childCountLabel.setText(String.valueOf(children));
        }
    }

    @FXML
    public void decreaseChildren() {
        if (children > 0) {
            children--;
            childCountLabel.setText(String.valueOf(children));
        }
    }

    @FXML
    public void goToRoomSelection(ActionEvent event) throws IOException {
        if (mainController == null) {
            showError("System error. Please restart.");
            return;
        }

        if (!validateInputs()) return;

        mainController.setSearchData(
                adults,
                children,
                checkInDatePicker.getValue(),
                checkOutDatePicker.getValue(),
                fullNameField.getText().trim(),
                emailField.getText().trim(),
                phoneField.getText().trim()
        );

        mainController.goToRoomSelection(event);
    }

    @FXML
    public void goToWelcome(ActionEvent event) throws IOException {
        mainController.goToWelcome(event);
    }

    @FXML
    public void showRules() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Grand Plaza Rules & Policies");
        alert.setHeaderText("Hotel Kiosk Regulations");
        alert.setContentText(
                "1. Check-in: 3:00 PM | Check-out: 11:00 AM\n" +
                "2. Maximum occupancy per room must be respected.\n" +
                "3. Cancellations must be made 24 hours in advance.\n" +
                "4. Quiet hours: 10:00 PM - 8:00 AM\n" +
                "5. No smoking in rooms ($250 fine)"
        );
        alert.showAndWait();
    }

    private boolean validateInputs() {
        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }

        LocalDate checkIn = checkInDatePicker.getValue();
        LocalDate checkOut = checkOutDatePicker.getValue();

        if (checkIn == null) {
            showError("Please select a check-in date.");
            return false;
        }
        if (checkOut == null) {
            showError("Please select a check-out date.");
            return false;
        }
        if (!checkIn.isBefore(LocalDate.now())) {
            if (checkIn.isBefore(LocalDate.now())) {
                showError("Check-in date cannot be in the past.");
                return false;
            }
        }
        if (!checkOut.isAfter(checkIn)) {
            showError("Check-out date must be after check-in date.");
            return false;
        }

        String name = fullNameField.getText().trim();
        if (name.isEmpty()) {
            showError("Please enter your full name.");
            return false;
        }
        if (name.length() < 2) {
            showError("Name must be at least 2 characters.");
            return false;
        }

        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showError("Please enter your email address.");
            return false;
        }
        if (!email.contains("@") || !email.contains(".")) {
            showError("Please enter a valid email address.");
            return false;
        }

        String phone = phoneField.getText().trim();
        if (phone.isEmpty()) {
            showError("Please enter your phone number.");
            return false;
        }
        String phoneDigits = phone.replaceAll("[^0-9]", "");
        if (phoneDigits.length() != 10) {
            showError("Phone number must be exactly 10 digits.");
            return false;
        }

        return true;
    }

    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText("⚠  " + message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        }
    }
}