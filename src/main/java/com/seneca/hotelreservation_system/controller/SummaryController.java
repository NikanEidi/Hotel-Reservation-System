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

    @FXML private Label summaryWifiLabel;
    @FXML private Label summaryWifiPrice;
    @FXML private Label summaryBreakfastLabel;
    @FXML private Label summaryBreakfastPrice;
    @FXML private Label summaryShuttleLabel;
    @FXML private Label summaryShuttlePrice;
    @FXML private Label summarySpaLabel;
    @FXML private Label summarySpaPrice;

    @FXML private HBox summaryWifiRow;
    @FXML private HBox summaryBreakfastRow;
    @FXML private HBox summaryShuttleRow;
    @FXML private HBox summarySpaRow;

    private UIController mainController;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    public void setMainController(UIController controller) {
        this.mainController = controller;
        loadSummaryData();
    }

    private void loadSummaryData() {
        UIController.BookingData data = mainController.getBookingData();
        if (data == null) return;

        summaryGuestName.setText("Guest: " + (data.guestName != null ? data.guestName : "—"));

        if (data.checkIn != null && data.checkOut != null) {
            summaryCheckIn.setText(data.checkIn.format(dateFormatter) + "  (3:00 PM)");
            summaryCheckOut.setText(data.checkOut.format(dateFormatter) + "  (11:00 AM)");
            long nights = ChronoUnit.DAYS.between(data.checkIn, data.checkOut);
            summaryNights.setText(nights + " night" + (nights != 1 ? "s" : ""));
        }

        summaryGuests.setText(data.adults + " Adult" + (data.adults != 1 ? "s" : "") +
                (data.children > 0 ? ", " + data.children + " Child" + (data.children != 1 ? "ren" : "") : ""));
        summaryRoomType.setText(getRoomTypeName(data.selectedRoomType) +
                (data.roomQuantity > 1 ? "  ×" + data.roomQuantity : ""));

        BigDecimal roomPrice = mainController.getRoomPrice();
        BigDecimal addonsTotal = mainController.getAddonsTotal();
        BigDecimal subtotal = roomPrice.add(addonsTotal);
        BigDecimal tax = subtotal.multiply(new BigDecimal("0.13"));
        BigDecimal total = subtotal.add(tax);

        summaryRoomPrice.setText(String.format("$%.2f", roomPrice.doubleValue()));
        subtotalLabel.setText(String.format("$%.2f", subtotal.doubleValue()));
        taxLabel.setText(String.format("$%.2f", tax.doubleValue()));
        totalLabel.setText(String.format("$%.2f", total.doubleValue()));
        pricingTypeLabel.setText(mainController.getPricingTypeDescription());

        if (data.hasWifi && data.wifiQuantity > 0) {
            summaryWifiRow.setVisible(true);
            summaryWifiRow.setManaged(true);
            summaryWifiLabel.setText(data.wifiQuantity + " device(s), " + data.nights + " nights");
            summaryWifiPrice.setText(String.format("$%.2f", 9.99 * data.nights * data.wifiQuantity));
        } else {
            summaryWifiRow.setVisible(false);
            summaryWifiRow.setManaged(false);
        }

        if (data.hasBreakfast && data.breakfastQuantity > 0) {
            summaryBreakfastRow.setVisible(true);
            summaryBreakfastRow.setManaged(true);
            summaryBreakfastLabel.setText(data.breakfastQuantity + " person(s), " + data.nights + " days");
            summaryBreakfastPrice.setText(String.format("$%.2f", 24.99 * data.nights * data.breakfastQuantity));
        } else {
            summaryBreakfastRow.setVisible(false);
            summaryBreakfastRow.setManaged(false);
        }

        if (data.hasParking && data.parkingQuantity > 0) {
            summaryShuttleRow.setVisible(true);
            summaryShuttleRow.setManaged(true);
            summaryShuttleLabel.setText(data.parkingQuantity + " trip(s)");
            summaryShuttlePrice.setText(String.format("$%.2f", 35.0 * data.parkingQuantity));
        } else {
            summaryShuttleRow.setVisible(false);
            summaryShuttleRow.setManaged(false);
        }

        if (data.hasSpa && data.spaQuantity > 0) {
            summarySpaRow.setVisible(true);
            summarySpaRow.setManaged(true);
            summarySpaLabel.setText(data.spaQuantity + " session(s)");
            summarySpaPrice.setText(String.format("$%.2f", 49.99 * data.spaQuantity));
        } else {
            summarySpaRow.setVisible(false);
            summarySpaRow.setManaged(false);
        }
    }

    private String getRoomTypeName(UIController.RoomType type) {
        switch (type) {
            case SINGLE:    return "Single Room";
            case DOUBLE:    return "Double Room";
            case DELUXE:    return "Deluxe Room";
            case PENTHOUSE: return "Penthouse Suite";
            default:        return "Standard Room";
        }
    }

    @FXML
    public void exportToCSV() {
        UIController.BookingData data = mainController.getBookingData();
        if (data == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Booking Summary as CSV");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fileChooser.setInitialFileName("booking_summary_" + System.currentTimeMillis() + ".csv");

        Stage stage = (Stage) summaryGuestName.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("GRAND PLAZA HOTEL - BOOKING SUMMARY");
                writer.println("Generated," + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
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
                writer.println("Adults," + data.adults);
                writer.println("Children," + data.children);
                writer.println();
                writer.println("ROOM DETAILS");
                writer.println("Room Type," + data.selectedRoomType);
                writer.println("Quantity," + data.roomQuantity);
                writer.println("Room Price,$" + String.format("%.2f", mainController.getRoomPrice().doubleValue()));
                writer.println("Pricing Type," + mainController.getPricingTypeDescription());
                writer.println();
                writer.println("ADD-ONS");
                writer.println("Wi-Fi," + (data.hasWifi ? data.wifiQuantity : 0));
                writer.println("Breakfast," + (data.hasBreakfast ? data.breakfastQuantity : 0));
                writer.println("Airport Shuttle," + (data.hasParking ? data.parkingQuantity : 0));
                writer.println("Spa," + (data.hasSpa ? data.spaQuantity : 0));
                writer.println();
                writer.println("PRICE BREAKDOWN");
                BigDecimal room = mainController.getRoomPrice();
                BigDecimal addons = mainController.getAddonsTotal();
                BigDecimal sub = room.add(addons);
                BigDecimal tax = sub.multiply(new BigDecimal("0.13"));
                writer.println("Room Price,$" + String.format("%.2f", room.doubleValue()));
                writer.println("Add-ons Total,$" + String.format("%.2f", addons.doubleValue()));
                writer.println("Subtotal,$" + String.format("%.2f", sub.doubleValue()));
                writer.println("Tax (13%),$" + String.format("%.2f", tax.doubleValue()));
                writer.println("TOTAL,$" + String.format("%.2f", mainController.getTotal().doubleValue()));

                showAlert("Export Successful", "Booking summary saved to:\n" + file.getAbsolutePath());
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
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("booking_summary_" + System.currentTimeMillis() + ".pdf");

        Stage stage = (Stage) summaryGuestName.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("=".repeat(55));
                writer.println("           GRAND PLAZA HOTEL");
                writer.println("        TORONTO  •  EST. 1985");
                writer.println("=".repeat(55));
                writer.println();
                writer.println("                BOOKING SUMMARY");
                writer.println();
                writer.println("-".repeat(55));
                writer.println("GUEST INFORMATION");
                writer.println("-".repeat(55));
                writer.printf("Name:       %s%n", data.guestName);
                writer.printf("Email:      %s%n", data.guestEmail);
                writer.printf("Phone:      %s%n", data.guestPhone);
                writer.println();
                writer.println("-".repeat(55));
                writer.println("STAY DETAILS");
                writer.println("-".repeat(55));
                writer.printf("Check-in:   %s (3:00 PM)%n", data.checkIn);
                writer.printf("Check-out:  %s (11:00 AM)%n", data.checkOut);
                writer.printf("Nights:     %d%n", data.nights);
                writer.printf("Guests:     %d adult(s), %d child(ren)%n", data.adults, data.children);
                writer.println();
                writer.println("-".repeat(55));
                writer.println("ROOM");
                writer.println("-".repeat(55));
                writer.printf("Type:       %s x%d%n", data.selectedRoomType, data.roomQuantity);
                writer.printf("Rate:       $%.2f / night%n", mainController.getBasePrice(data.selectedRoomType).doubleValue());
                writer.printf("Pricing:    %s%n", mainController.getPricingTypeDescription());
                writer.println();
                writer.println("-".repeat(55));
                writer.println("ADD-ONS");
                writer.println("-".repeat(55));
                if (data.hasWifi && data.wifiQuantity > 0) writer.printf("Wi-Fi:      %d device(s)%n", data.wifiQuantity);
                if (data.hasBreakfast && data.breakfastQuantity > 0) writer.printf("Breakfast:  %d person(s)%n", data.breakfastQuantity);
                if (data.hasParking && data.parkingQuantity > 0) writer.printf("Shuttle:    %d trip(s)%n", data.parkingQuantity);
                if (data.hasSpa && data.spaQuantity > 0) writer.printf("Spa:        %d session(s)%n", data.spaQuantity);
                writer.println();
                writer.println("-".repeat(55));
                writer.println("PRICE BREAKDOWN");
                writer.println("-".repeat(55));
                BigDecimal room = mainController.getRoomPrice();
                BigDecimal addons = mainController.getAddonsTotal();
                BigDecimal sub = room.add(addons);
                BigDecimal tax = sub.multiply(new BigDecimal("0.13"));
                writer.printf("Room:       $%.2f%n", room.doubleValue());
                writer.printf("Add-ons:    $%.2f%n", addons.doubleValue());
                writer.printf("Subtotal:   $%.2f%n", sub.doubleValue());
                writer.printf("Tax (13%%):  $%.2f%n", tax.doubleValue());
                writer.println("-".repeat(55));
                writer.printf("TOTAL DUE:  $%.2f%n", mainController.getTotal().doubleValue());
                writer.println("=".repeat(55));
                writer.println("Thank you for choosing Grand Plaza Hotel!");
                writer.println("Payment will be collected at the front desk.");

                showAlert("Export Successful", "Booking summary saved to:\n" + file.getAbsolutePath());
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