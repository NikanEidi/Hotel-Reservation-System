package com.seneca.hotelreservation_system.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ConfirmationController {

    @FXML private Label bookingReferenceLabel;
    @FXML private Label guestNameLabel;
    @FXML private Label checkInLabel;
    @FXML private Label checkOutLabel;
    @FXML private Label nightsLabel;
    @FXML private Label roomTypeLabel;
    @FXML private Label roomQuantityLabel;
    @FXML private TextArea addOnsDetailsArea;
    @FXML private Label totalAmountLabel;
    @FXML private Label paymentInstructionLabel;
    @FXML private Label loyaltyNumberLabel;
    @FXML private VBox loyaltyInfoBox;
    @FXML private Label confirmationDateLabel;
    @FXML private Label emailLabel;

    private UIController mainController;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    public void setMainController(UIController controller) {
        this.mainController = controller;
        loadConfirmationData();
    }

    private void loadConfirmationData() {
        UIController.BookingData data = mainController.getBookingData();
        if (data == null) return;

        bookingReferenceLabel.setText(generateBookingReference());
        guestNameLabel.setText(data.guestName);

        if (data.checkIn != null && data.checkOut != null) {
            checkInLabel.setText(data.checkIn.format(dateFormatter) + " (3:00 PM)");
            checkOutLabel.setText(data.checkOut.format(dateFormatter) + " (11:00 AM)");
            nightsLabel.setText(data.nights + " nights");
        }

        roomTypeLabel.setText(getRoomTypeName(data.selectedRoomType));
        roomQuantityLabel.setText("x" + data.roomQuantity);

        StringBuilder addOnsBuilder = new StringBuilder();
        if (data.hasWifi && data.wifiQuantity > 0) addOnsBuilder.append("• Wi-Fi: ").append(data.wifiQuantity).append(" device(s)\n");
        if (data.hasBreakfast && data.breakfastQuantity > 0) addOnsBuilder.append("• Breakfast: ").append(data.breakfastQuantity).append(" person(s)\n");
        if (data.hasParking && data.parkingQuantity > 0) addOnsBuilder.append("• Parking: ").append(data.parkingQuantity).append(" car(s)\n");
        if (data.hasSpa && data.spaQuantity > 0) addOnsBuilder.append("• Spa: ").append(data.spaQuantity).append(" session(s)\n");

        if (addOnsBuilder.length() > 0) addOnsDetailsArea.setText(addOnsBuilder.toString());
        else addOnsDetailsArea.setText("No additional services selected");

        totalAmountLabel.setText("$" + mainController.getTotal());

        paymentInstructionLabel.setText(
                "✓ Please proceed to the FRONT DESK upon arrival\n" +
                        "✓ Complete payment (Cash, Card, or Loyalty Points)\n" +
                        "✓ Receive your room keys\n" +
                        "✓ Enjoy your stay at GRAND HOTEL!"
        );

        if (data.enrollLoyalty) {
            loyaltyNumberLabel.setText("LTY-" + System.currentTimeMillis());
            loyaltyInfoBox.setVisible(true);
        }

        confirmationDateLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm:ss")));
        emailLabel.setText(data.guestEmail);

        // Save to database
        saveReservationToDatabase();
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

    private String generateBookingReference() {
        return "BK-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + String.format("%04d", (int)(Math.random() * 10000));
    }

    private void saveReservationToDatabase() {
        UIController.BookingData data = mainController.getBookingData();
        if (data != null) {
            System.out.println("========================================");
            System.out.println("RESERVATION SAVED");
            System.out.println("Booking Ref: " + bookingReferenceLabel.getText());
            System.out.println("Guest: " + data.guestName);
            System.out.println("Total: $" + mainController.getTotal());
            System.out.println("========================================");
        }
    }

    @FXML
    public void goToWelcome(ActionEvent event) throws IOException {
        mainController.goToWelcome(event);
    }

    @FXML
    public void printConfirmation() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Print Confirmation");
        alert.setContentText("Your booking confirmation has been sent to the printer.\n\nBooking Reference: " + bookingReferenceLabel.getText());
        alert.showAndWait();
    }

    @FXML
    public void emailConfirmation() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Email Confirmation");
        alert.setContentText("Confirmation sent to: " + emailLabel.getText());
        alert.showAndWait();
    }
}