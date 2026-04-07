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
import javafx.scene.layout.HBox;

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

    // Add-ons Labels
    @FXML private Label summaryWifiLabel;
    @FXML private Label summaryWifiPrice;
    @FXML private Label summaryBreakfastLabel;
    @FXML private Label summaryBreakfastPrice;
    @FXML private Label summaryShuttleLabel;
    @FXML private Label summaryShuttlePrice;
    @FXML private Label summarySpaLabel;
    @FXML private Label summarySpaPrice;

    // Add-ons Rows (HBox for visibility control)
    @FXML private HBox summaryWifiRow;
    @FXML private HBox summaryBreakfastRow;
    @FXML private HBox summaryShuttleRow;
    @FXML private HBox summarySpaRow;

    // Add-ons Header
    @FXML private Label addonsHeader;

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

        // Guest Information
        summaryGuestName.setText(data.guestName != null ? data.guestName : "Guest");

        // Stay Details
        if (data.checkIn != null && data.checkOut != null) {
            summaryCheckIn.setText(data.checkIn.format(dateFormatter) + " (3:00 PM)");
            summaryCheckOut.setText(data.checkOut.format(dateFormatter) + " (11:00 AM)");
            long nights = ChronoUnit.DAYS.between(data.checkIn, data.checkOut);
            summaryNights.setText(nights + " nights");
        }

        summaryGuests.setText(data.adults + " Adults, " + data.children + " Children");
        summaryRoomType.setText(getRoomTypeName(data.selectedRoomType));

        // Force recalculation of all prices
        BigDecimal roomPrice = mainController.getRoomPrice();
        BigDecimal addonsTotal = mainController.getAddonsTotal();
        BigDecimal subtotal = roomPrice.add(addonsTotal);
        BigDecimal tax = subtotal.multiply(new BigDecimal("0.13")); // 13% tax
        BigDecimal total = subtotal.add(tax);

        System.out.println("Room Price: $" + roomPrice);
        System.out.println("Add-ons Total: $" + addonsTotal);
        System.out.println("Subtotal: $" + subtotal);
        System.out.println("Tax (13%): $" + tax);
        System.out.println("Total: $" + total);

        // Update labels
        summaryRoomPrice.setText("Room: $" + roomPrice);

        // IMPORTANT: Update the total labels - these MUST match your FXML fx:id
        subtotalLabel.setText("Subtotal: $" + subtotal);
        taxLabel.setText("Tax (13%): $" + tax);
        totalLabel.setText("TOTAL: $" + total);

        pricingTypeLabel.setText("Pricing: " + mainController.getPricingTypeDescription());

        // Display Add-ons
        boolean hasAddons = false;

        if (data.hasWifi && data.wifiQuantity > 0) {
            summaryWifiRow.setVisible(true);
            summaryWifiRow.setManaged(true);
            summaryWifiLabel.setText(data.wifiQuantity + " device(s), " + data.nights + " nights");
            summaryWifiPrice.setText("$" + (10 * data.nights * data.wifiQuantity));
            hasAddons = true;
        } else {
            summaryWifiRow.setVisible(false);
            summaryWifiRow.setManaged(false);
        }

        if (data.hasBreakfast && data.breakfastQuantity > 0) {
            summaryBreakfastRow.setVisible(true);
            summaryBreakfastRow.setManaged(true);
            summaryBreakfastLabel.setText(data.breakfastQuantity + " person(s), " + data.nights + " days");
            summaryBreakfastPrice.setText("$" + (15 * data.nights * data.breakfastQuantity));
            hasAddons = true;
        } else {
            summaryBreakfastRow.setVisible(false);
            summaryBreakfastRow.setManaged(false);
        }

        if (data.hasParking && data.parkingQuantity > 0) {
            summaryShuttleRow.setVisible(true);
            summaryShuttleRow.setManaged(true);
            summaryShuttleLabel.setText(data.parkingQuantity + " trip(s)");
            summaryShuttlePrice.setText("$" + (35 * data.parkingQuantity));
            hasAddons = true;
        } else {
            summaryShuttleRow.setVisible(false);
            summaryShuttleRow.setManaged(false);
        }

        if (data.hasSpa && data.spaQuantity > 0) {
            summarySpaRow.setVisible(true);
            summarySpaRow.setManaged(true);
            summarySpaLabel.setText(data.spaQuantity + " session(s)");
            summarySpaPrice.setText("$" + (50 * data.spaQuantity));
            hasAddons = true;
        } else {
            summarySpaRow.setVisible(false);
            summarySpaRow.setManaged(false);
        }
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
                writer.println("ROOM DETAILS");
                writer.println("Room Type," + data.selectedRoomType);
                writer.println("Room Price,$" + mainController.getRoomPrice());
                writer.println();
                writer.println("ADD-ONS");
                writer.println("Wi-Fi," + (data.hasWifi ? data.wifiQuantity : 0));
                writer.println("Breakfast," + (data.hasBreakfast ? data.breakfastQuantity : 0));
                writer.println("Airport Shuttle," + (data.hasParking ? data.parkingQuantity : 0));
                writer.println("Spa," + (data.hasSpa ? data.spaQuantity : 0));
                writer.println();
                writer.println("PRICE BREAKDOWN");
                writer.println("Room Price,$" + mainController.getRoomPrice());
                writer.println("Add-ons Total,$" + mainController.getAddonsTotal());
                writer.println("Subtotal,$" + mainController.getRoomPrice().add(mainController.getAddonsTotal()));
                writer.println("Tax (13%),$" + mainController.getRoomPrice().add(mainController.getAddonsTotal()).multiply(new BigDecimal("0.13")));
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
                writer.println();
                writer.println("ADD-ONS:");
                writer.println("  WiFi: " + (data.hasWifi ? data.wifiQuantity : 0));
                writer.println("  Breakfast: " + (data.hasBreakfast ? data.breakfastQuantity : 0));
                writer.println("  Airport Shuttle: " + (data.hasParking ? data.parkingQuantity : 0));
                writer.println("  Spa: " + (data.hasSpa ? data.spaQuantity : 0));
                writer.println();
                writer.println("PRICE BREAKDOWN");
                writer.println("Room Price: $" + mainController.getRoomPrice());
                writer.println("Add-ons Total: $" + mainController.getAddonsTotal());
                writer.println("Subtotal: $" + mainController.getRoomPrice().add(mainController.getAddonsTotal()));
                writer.println("Tax (13%): $" + mainController.getRoomPrice().add(mainController.getAddonsTotal()).multiply(new BigDecimal("0.13")));
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