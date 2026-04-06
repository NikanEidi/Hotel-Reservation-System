package com.seneca.hotelreservation_system.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class SummaryController {

    @FXML private Label summaryGuestName;
    @FXML private Label summaryCheckIn;
    @FXML private Label summaryCheckOut;
    @FXML private Label summaryNights;
    @FXML private Label summaryGuests;
    @FXML private Label summaryRoomType;
    @FXML private Label summaryRoomPrice;
    @FXML private Label pricingTypeLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label totalLabel;

    private UIController mainController;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    public void setMainController(UIController controller) {
        this.mainController = controller;
        loadSummaryData();
    }

    private void loadSummaryData() {
        UIController.BookingData data = mainController.getBookingData();
        if (data == null) return;

        System.out.println("=== Loading Summary Data ===");

        summaryGuestName.setText(data.guestName != null ? data.guestName : "Guest");

        if (data.checkIn != null && data.checkOut != null) {
            summaryCheckIn.setText(data.checkIn.format(dateFormatter) + " (3:00 PM)");
            summaryCheckOut.setText(data.checkOut.format(dateFormatter) + " (11:00 AM)");
            long nights = ChronoUnit.DAYS.between(data.checkIn, data.checkOut);
            summaryNights.setText(nights + " nights");
        }

        summaryGuests.setText(data.adults + " Adults, " + data.children + " Children");
        summaryRoomType.setText(getRoomTypeName(data.selectedRoomType));

        // Force recalculation
        BigDecimal roomPrice = mainController.getRoomPrice();
        BigDecimal subtotal = mainController.getSubtotal();
        BigDecimal tax = mainController.getTax();
        BigDecimal total = mainController.getTotal();

        System.out.println("Room Price: $" + roomPrice);
        System.out.println("Subtotal: $" + subtotal);
        System.out.println("Tax: $" + tax);
        System.out.println("Total: $" + total);

        summaryRoomPrice.setText("$" + roomPrice);
        pricingTypeLabel.setText(mainController.getPricingTypeDescription());
        subtotalLabel.setText("$" + subtotal);
        taxLabel.setText("$" + tax);
        totalLabel.setText("$" + total);
    }

    private String getRoomTypeName(UIController.RoomType type) {
        switch (type) {
            case SINGLE: return "Single Room (1 Queen Bed)";
            case DOUBLE: return "Double Room (2 Queen Beds)";
            case DELUXE: return "Deluxe Room (King Bed + City View)";
            case PENTHOUSE: return "Penthouse Suite (Panoramic View + Jacuzzi)";
            default: return "Standard Room";
        }
    }

    @FXML
    public void exportToCSV() {
        UIController.BookingData data = mainController.getBookingData();
        if (data == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Booking Summary as CSV");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("booking_summary_" + System.currentTimeMillis() + ".csv");

        Stage stage = (Stage) summaryGuestName.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("GRAND PLAZA HOTEL - BOOKING SUMMARY");
                writer.println("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                writer.println();
                writer.println("GUEST INFORMATION");
                writer.println("Name," + data.guestName);
                writer.println("Email," + data.guestEmail);
                writer.println("Phone," + data.guestPhone);
                writer.println();
                writer.println("STAY DETAILS");
                writer.println("Check-in," + data.checkIn);
                writer.println("Check-out," + data.checkOut);
                writer.println("Nights," + data.nights);
                writer.println("Guests," + data.adults + " Adults, " + data.children + " Children");
                writer.println();
                writer.println("PRICE BREAKDOWN");
                writer.println("Room Type," + data.selectedRoomType);
                writer.println("Room Price,$" + mainController.getRoomPrice());
                writer.println("Subtotal,$" + mainController.getSubtotal());
                writer.println("Tax (10%),$" + mainController.getTax());
                writer.println("TOTAL,$" + mainController.getTotal());

                showAlert("Export Successful", "Booking summary exported to:\n" + file.getAbsolutePath());
            } catch (IOException e) {
                showAlert("Export Failed", "Error saving file: " + e.getMessage());
            }
        }
    }

    @FXML
    public void exportToPDF() {
        UIController.BookingData data = mainController.getBookingData();
        if (data == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Booking Summary as PDF");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
        );
        fileChooser.setInitialFileName("booking_summary_" + System.currentTimeMillis() + ".pdf");

        Stage stage = (Stage) summaryGuestName.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("=".repeat(50));
                writer.println("         GRAND PLAZA HOTEL");
                writer.println("=".repeat(50));
                writer.println();
                writer.println("              BOOKING SUMMARY");
                writer.println();
                writer.println("Guest: " + data.guestName);
                writer.println("Email: " + data.guestEmail);
                writer.println("Phone: " + data.guestPhone);
                writer.println();
                writer.println("Check-in: " + data.checkIn);
                writer.println("Check-out: " + data.checkOut);
                writer.println("Nights: " + data.nights);
                writer.println("Guests: " + data.adults + " Adults, " + data.children + " Children");
                writer.println();
                writer.println("Room: " + data.selectedRoomType);
                writer.println("Room Price: $" + mainController.getRoomPrice());
                writer.println("Subtotal: $" + mainController.getSubtotal());
                writer.println("Tax (10%): $" + mainController.getTax());
                writer.println("TOTAL: $" + mainController.getTotal());
                writer.println();
                writer.println("=".repeat(50));
                writer.println("Thank you for choosing Grand Plaza Hotel!");

                showAlert("Export Successful", "Booking summary exported to:\n" + file.getAbsolutePath());
            } catch (IOException e) {
                showAlert("Export Failed", "Error saving file: " + e.getMessage());
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    public void goToConfirmation(ActionEvent event) throws IOException {
        mainController.goToConfirmation(event);
    }

    @FXML
    public void goToLoyalty(ActionEvent event) throws IOException {
        mainController.goToLoyalty(event);
    }
}