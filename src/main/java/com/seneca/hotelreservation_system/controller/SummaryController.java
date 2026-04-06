package com.seneca.hotelreservation_system.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
    @FXML private Label summaryRoomQuantity;
    @FXML private Label summaryRoomPrice;
    @FXML private HBox summaryWifiRow;
    @FXML private Label summaryWifiLabel;
    @FXML private Label summaryWifiPrice;
    @FXML private HBox summaryBreakfastRow;
    @FXML private Label summaryBreakfastLabel;
    @FXML private Label summaryBreakfastPrice;
    @FXML private HBox summaryParkingRow;
    @FXML private Label summaryParkingLabel;
    @FXML private Label summaryParkingPrice;
    @FXML private HBox summarySpaRow;
    @FXML private Label summarySpaLabel;
    @FXML private Label summarySpaPrice;
    @FXML private Label pricingTypeLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private HBox summaryDiscountRow;
    @FXML private Label summaryDiscount;
    @FXML private Label totalLabel;
    @FXML private Label loyaltyStatusLabel;
    @FXML private Label loyaltyNumberLabel;
    @FXML private VBox loyaltyInfoBox;

    private UIController mainController;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    public void setMainController(UIController controller) {
        this.mainController = controller;
        loadSummaryData();
    }

    private void loadSummaryData() {
        UIController.BookingData data = mainController.getBookingData();
        if (data == null) return;

        summaryGuestName.setText(data.guestName);
        if (data.checkIn != null && data.checkOut != null) {
            summaryCheckIn.setText(data.checkIn.format(dateFormatter) + " (3:00 PM)");
            summaryCheckOut.setText(data.checkOut.format(dateFormatter) + " (11:00 AM)");
            summaryNights.setText(ChronoUnit.DAYS.between(data.checkIn, data.checkOut) + " nights");
        }
        summaryGuests.setText(data.adults + " Adults, " + data.children + " Children");

        summaryRoomType.setText(getRoomTypeName(data.selectedRoomType));
        summaryRoomQuantity.setText("x" + data.roomQuantity);
        summaryRoomPrice.setText("$" + mainController.getRoomPrice());

        setupAddOnsDisplay(data);

        pricingTypeLabel.setText(mainController.getPricingTypeDescription());
        subtotalLabel.setText("$" + mainController.getSubtotal());
        taxLabel.setText("$" + mainController.getTax());

        if (data.loyaltyPointsToRedeem > 0) {
            summaryDiscountRow.setVisible(true);
            summaryDiscount.setText("-$" + calculateLoyaltyDiscount(data));
        }

        totalLabel.setText("$" + mainController.getTotal());

        if (data.enrollLoyalty) {
            loyaltyStatusLabel.setText("✓ Enrolled");
            loyaltyNumberLabel.setText(generateLoyaltyNumber());
            loyaltyInfoBox.setVisible(true);
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

    private void setupAddOnsDisplay(UIController.BookingData data) {
        summaryWifiRow.setVisible(data.hasWifi && data.wifiQuantity > 0);
        summaryWifiLabel.setText("Wi-Fi (" + data.wifiQuantity + " devices, " + data.nights + " nights)");
        if (data.hasWifi && data.wifiQuantity > 0) {
            summaryWifiPrice.setText("$" + (10 * data.nights * data.wifiQuantity));
        }

        summaryBreakfastRow.setVisible(data.hasBreakfast && data.breakfastQuantity > 0);
        summaryBreakfastLabel.setText("Breakfast (" + data.breakfastQuantity + " persons, " + data.nights + " days)");
        if (data.hasBreakfast && data.breakfastQuantity > 0) {
            summaryBreakfastPrice.setText("$" + (15 * data.nights * data.breakfastQuantity));
        }

        summaryParkingRow.setVisible(data.hasParking && data.parkingQuantity > 0);
        summaryParkingLabel.setText("Parking (" + data.parkingQuantity + " cars, " + data.nights + " nights)");
        if (data.hasParking && data.parkingQuantity > 0) {
            summaryParkingPrice.setText("$" + (20 * data.nights * data.parkingQuantity));
        }

        summarySpaRow.setVisible(data.hasSpa && data.spaQuantity > 0);
        summarySpaLabel.setText("Spa (" + data.spaQuantity + " sessions)");
        if (data.hasSpa && data.spaQuantity > 0) {
            summarySpaPrice.setText("$" + (50 * data.spaQuantity));
        }
    }

    private BigDecimal calculateLoyaltyDiscount(UIController.BookingData data) {
        return BigDecimal.valueOf(data.loyaltyPointsToRedeem / 100).multiply(BigDecimal.TEN);
    }

    private String generateLoyaltyNumber() {
        return "LTY-" + System.currentTimeMillis();
    }

    // ========== MS3 ADDITION: CSV EXPORT ==========
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
                // Write header
                writer.println("GRAND PLAZA HOTEL - BOOKING SUMMARY");
                writer.println("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                writer.println();

                // Guest Information
                writer.println("GUEST INFORMATION");
                writer.println("Name," + data.guestName);
                writer.println("Email," + data.guestEmail);
                writer.println("Phone," + data.guestPhone);
                writer.println();

                // Stay Details
                writer.println("STAY DETAILS");
                writer.println("Check-in," + data.checkIn);
                writer.println("Check-out," + data.checkOut);
                writer.println("Nights," + data.nights);
                writer.println("Guests," + data.adults + " Adults, " + data.children + " Children");
                writer.println();

                // Room Details
                writer.println("ROOM DETAILS");
                writer.println("Room Type," + data.selectedRoomType);
                writer.println("Quantity," + data.roomQuantity);
                writer.println("Room Price,$" + mainController.getRoomPrice());
                writer.println();

                // Add-ons
                writer.println("ADD-ONS");
                if (data.hasWifi && data.wifiQuantity > 0) {
                    writer.println("Wi-Fi," + data.wifiQuantity + " device(s),$" + (10 * data.nights * data.wifiQuantity));
                }
                if (data.hasBreakfast && data.breakfastQuantity > 0) {
                    writer.println("Breakfast," + data.breakfastQuantity + " person(s),$" + (15 * data.nights * data.breakfastQuantity));
                }
                if (data.hasParking && data.parkingQuantity > 0) {
                    writer.println("Parking," + data.parkingQuantity + " car(s),$" + (20 * data.nights * data.parkingQuantity));
                }
                if (data.hasSpa && data.spaQuantity > 0) {
                    writer.println("Spa," + data.spaQuantity + " session(s),$" + (50 * data.spaQuantity));
                }
                writer.println();

                // Price Breakdown
                writer.println("PRICE BREAKDOWN");
                writer.println("Pricing Type," + mainController.getPricingTypeDescription());
                writer.println("Subtotal,$" + mainController.getSubtotal());
                writer.println("Tax (10%),$" + mainController.getTax());
                if (data.loyaltyPointsToRedeem > 0) {
                    writer.println("Loyalty Discount,-$" + calculateLoyaltyDiscount(data));
                }
                writer.println("TOTAL,$" + mainController.getTotal());
                writer.println();

                // Loyalty Information
                if (data.enrollLoyalty) {
                    writer.println("LOYALTY PROGRAM");
                    writer.println("Enrolled,Yes");
                    writer.println("Loyalty Number," + generateLoyaltyNumber());
                }

                writer.println();
                writer.println("Thank you for choosing Grand Plaza Hotel!");

                showAlert("Export Successful", "Booking summary exported to:\n" + file.getAbsolutePath());

            } catch (IOException e) {
                showAlert("Export Failed", "Error saving file: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // ========== MS3 ADDITION: PDF EXPORT (Text-based simulation) ==========
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
            // Since full PDF library may not be available, create a text file with .pdf extension
            // In production, use iText or Apache PDFBox
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                writer.println("%PDF-1.4");
                writer.println("%âãÏÓ");
                writer.println();
                writer.println("=" .repeat(60));
                writer.println("                    GRAND PLAZA HOTEL");
                writer.println("=" .repeat(60));
                writer.println();
                writer.println("                    BOOKING SUMMARY");
                writer.println();
                writer.println("=" .repeat(60));
                writer.println();
                writer.println("GUEST INFORMATION");
                writer.println("-".repeat(40));
                writer.println("Name: " + data.guestName);
                writer.println("Email: " + data.guestEmail);
                writer.println("Phone: " + data.guestPhone);
                writer.println();
                writer.println("STAY DETAILS");
                writer.println("-".repeat(40));
                writer.println("Check-in: " + data.checkIn);
                writer.println("Check-out: " + data.checkOut);
                writer.println("Nights: " + data.nights);
                writer.println("Guests: " + data.adults + " Adults, " + data.children + " Children");
                writer.println();
                writer.println("ROOM DETAILS");
                writer.println("-".repeat(40));
                writer.println("Room Type: " + data.selectedRoomType);
                writer.println("Quantity: " + data.roomQuantity);
                writer.println("Room Price: $" + mainController.getRoomPrice());
                writer.println();
                writer.println("ADD-ONS");
                writer.println("-".repeat(40));
                if (data.hasWifi && data.wifiQuantity > 0) {
                    writer.println("Wi-Fi: " + data.wifiQuantity + " device(s) - $" + (10 * data.nights * data.wifiQuantity));
                }
                if (data.hasBreakfast && data.breakfastQuantity > 0) {
                    writer.println("Breakfast: " + data.breakfastQuantity + " person(s) - $" + (15 * data.nights * data.breakfastQuantity));
                }
                if (data.hasParking && data.parkingQuantity > 0) {
                    writer.println("Parking: " + data.parkingQuantity + " car(s) - $" + (20 * data.nights * data.parkingQuantity));
                }
                if (data.hasSpa && data.spaQuantity > 0) {
                    writer.println("Spa: " + data.spaQuantity + " session(s) - $" + (50 * data.spaQuantity));
                }
                writer.println();
                writer.println("PRICE BREAKDOWN");
                writer.println("-".repeat(40));
                writer.println("Pricing Type: " + mainController.getPricingTypeDescription());
                writer.println("Subtotal: $" + mainController.getSubtotal());
                writer.println("Tax (10%): $" + mainController.getTax());
                if (data.loyaltyPointsToRedeem > 0) {
                    writer.println("Loyalty Discount: -$" + calculateLoyaltyDiscount(data));
                }
                writer.println("TOTAL: $" + mainController.getTotal());
                writer.println();
                if (data.enrollLoyalty) {
                    writer.println("LOYALTY PROGRAM");
                    writer.println("-".repeat(40));
                    writer.println("Enrolled: Yes");
                    writer.println("Loyalty Number: " + generateLoyaltyNumber());
                    writer.println();
                }
                writer.println("=" .repeat(60));
                writer.println("         Thank you for choosing Grand Plaza Hotel!");
                writer.println("=" .repeat(60));
                writer.println();
                writer.println("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                showAlert("Export Successful", "Booking summary exported to:\n" + file.getAbsolutePath() + "\n\nNote: This is a text file. For proper PDF, use a PDF library.");

            } catch (IOException e) {
                showAlert("Export Failed", "Error saving file: " + e.getMessage());
                e.printStackTrace();
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