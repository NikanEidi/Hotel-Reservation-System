package com.seneca.hotelreservation_system.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.math.BigDecimal;
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

        summaryBreakfastRow.setVisible(data.hasBreakfast && data.breakfastQuantity > 0);
        summaryBreakfastLabel.setText("Breakfast (" + data.breakfastQuantity + " persons, " + data.nights + " days)");

        summaryParkingRow.setVisible(data.hasParking && data.parkingQuantity > 0);
        summaryParkingLabel.setText("Parking (" + data.parkingQuantity + " cars, " + data.nights + " nights)");

        summarySpaRow.setVisible(data.hasSpa && data.spaQuantity > 0);
        summarySpaLabel.setText("Spa (" + data.spaQuantity + " sessions)");
    }

    private BigDecimal calculateLoyaltyDiscount(UIController.BookingData data) {
        return BigDecimal.valueOf(data.loyaltyPointsToRedeem / 100).multiply(BigDecimal.TEN);
    }

    private String generateLoyaltyNumber() {
        return "LTY-" + System.currentTimeMillis();
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