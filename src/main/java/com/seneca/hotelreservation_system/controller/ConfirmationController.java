package com.seneca.hotelreservation_system.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.io.IOException;
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
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    public void setMainController(UIController controller) {
        this.mainController = controller;
        loadConfirmationData();
    }

    private void loadConfirmationData() {
        UIController.BookingData data = mainController.getBookingData();
        if (data == null) return;

        bookingReferenceLabel.setText(generateBookingReference());
        if (guestNameLabel != null) guestNameLabel.setText(data.guestName != null ? data.guestName : "—");
        if (emailLabel != null) emailLabel.setText(data.guestEmail != null ? data.guestEmail : "—");

        if (data.checkIn != null && data.checkOut != null) {
            if (checkInLabel != null) checkInLabel.setText(data.checkIn.format(dateFormatter) + "  (3:00 PM)");
            if (checkOutLabel != null) checkOutLabel.setText(data.checkOut.format(dateFormatter) + "  (11:00 AM)");
        }
        if (nightsLabel != null) nightsLabel.setText(data.nights + " night" + (data.nights != 1 ? "s" : ""));

        if (roomTypeLabel != null) roomTypeLabel.setText(getRoomTypeName(data.selectedRoomType));
        if (roomQuantityLabel != null) roomQuantityLabel.setText("×" + data.roomQuantity);

        StringBuilder addOnsBuilder = new StringBuilder();
        if (data.hasWifi && data.wifiQuantity > 0)
            addOnsBuilder.append("• Wi-Fi: ").append(data.wifiQuantity).append(" device(s)\n");
        if (data.hasBreakfast && data.breakfastQuantity > 0)
            addOnsBuilder.append("• Breakfast: ").append(data.breakfastQuantity).append(" person(s)\n");
        if (data.hasParking && data.parkingQuantity > 0)
            addOnsBuilder.append("• Airport Shuttle: ").append(data.parkingQuantity).append(" trip(s)\n");
        if (data.hasSpa && data.spaQuantity > 0)
            addOnsBuilder.append("• Spa: ").append(data.spaQuantity).append(" session(s)\n");

        if (addOnsDetailsArea != null) {
            addOnsDetailsArea.setText(addOnsBuilder.length() > 0
                    ? addOnsBuilder.toString().trim()
                    : "No additional services selected.");
        }

        if (totalAmountLabel != null)
            totalAmountLabel.setText(String.format("$%.2f", mainController.getTotal().doubleValue()));

        if (paymentInstructionLabel != null) {
            paymentInstructionLabel.setText(
                    "✓  Please proceed to the FRONT DESK upon arrival.\n" +
                    "✓  Complete payment (Cash, Card, or Loyalty Points).\n" +
                    "✓  Receive your room keys and enjoy your stay!"
            );
        }

        if (data.enrollLoyalty) {
            if (loyaltyNumberLabel != null)
                loyaltyNumberLabel.setText("LTY-" + System.currentTimeMillis() % 1000000);
            if (loyaltyInfoBox != null) {
                loyaltyInfoBox.setVisible(true);
                loyaltyInfoBox.setManaged(true);
            }
        }

        if (confirmationDateLabel != null)
            confirmationDateLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy  HH:mm")));
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

    private String generateBookingReference() {
        return "BK-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) +
                "-" + String.format("%04d", (int) (Math.random() * 10000));
    }

    @FXML
    public void goToWelcome(ActionEvent event) throws IOException {
        mainController.goToWelcome(event);
    }

    @FXML
    public void printConfirmation() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Print Confirmation");
        alert.setHeaderText("Sending to Printer");
        alert.setContentText("Your booking confirmation has been sent to the printer.\n\nBooking Reference: " + bookingReferenceLabel.getText());
        alert.showAndWait();
    }

    @FXML
    public void emailConfirmation() {
        String email = emailLabel != null ? emailLabel.getText() : "—";
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Email Confirmation");
        alert.setHeaderText("Confirmation Sent");
        alert.setContentText("A confirmation email has been sent to:\n" + email);
        alert.showAndWait();
    }
}