package com.seneca.hotelreservation_system.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.math.BigDecimal;

public class LoyaltyController {

    @FXML private ToggleGroup enrollGroup;
    @FXML private RadioButton enrollYesBtn;
    @FXML private RadioButton enrollNoBtn;
    @FXML private Label loyaltyNumberLabel;
    @FXML private VBox loyaltyNumberBox;
    @FXML private Label bookingTotalLabel;
    @FXML private Label pointsToEarnLabel;
    @FXML private TextField pointsToRedeemField;
    @FXML private Label discountAmountLabel;
    @FXML private Label newTotalLabel;
    @FXML private Button applyPointsBtn;

    private UIController mainController;
    private BigDecimal originalTotal;
    private int earnedPoints;

    public void setMainController(UIController controller) {
        this.mainController = controller;
        loadBookingData();
        calculatePoints();
        setupEnrollmentDisplay();
        setupPointsRedeemField();
    }

    private void loadBookingData() {
        UIController.BookingData data = mainController.getBookingData();
        if (data != null) {
            originalTotal = data.total != null ? data.total : BigDecimal.ZERO;
            bookingTotalLabel.setText("$" + originalTotal);
            enrollYesBtn.setSelected(data.enrollLoyalty);
            enrollNoBtn.setSelected(!data.enrollLoyalty);
        }
    }

    private void calculatePoints() {
        earnedPoints = originalTotal.intValue();
        pointsToEarnLabel.setText(String.valueOf(earnedPoints));
    }

    private void setupEnrollmentDisplay() {
        String loyaltyNumber = "LTY-" + System.currentTimeMillis();
        loyaltyNumberLabel.setText(loyaltyNumber);

        loyaltyNumberBox.setVisible(enrollYesBtn.isSelected());
        loyaltyNumberBox.setManaged(enrollYesBtn.isSelected());

        enrollGroup.selectedToggleProperty().addListener((obs, old, val) -> {
            boolean isEnrolled = val == enrollYesBtn;
            loyaltyNumberBox.setVisible(isEnrolled);
            loyaltyNumberBox.setManaged(isEnrolled);
        });
    }

    private void setupPointsRedeemField() {
        pointsToRedeemField.textProperty().addListener((obs, old, val) -> validatePointsInput(val));
        applyPointsBtn.setOnAction(e -> applyPoints());
    }

    private void validatePointsInput(String input) {
        try {
            if (input == null || input.isEmpty()) return;
            int points = Integer.parseInt(input);
            if (points < 0) pointsToRedeemField.setText("0");
            else if (points > earnedPoints) pointsToRedeemField.setText(String.valueOf(earnedPoints));
        } catch (NumberFormatException e) {
            pointsToRedeemField.setText("0");
        }
    }

    private void applyPoints() {
        try {
            int points = Integer.parseInt(pointsToRedeemField.getText().trim());
            BigDecimal discount = BigDecimal.valueOf(points / 100).multiply(BigDecimal.TEN);
            BigDecimal maxDiscount = originalTotal.multiply(new BigDecimal("0.30"));

            if (discount.compareTo(maxDiscount) > 0) discount = maxDiscount;

            BigDecimal newTotal = originalTotal.subtract(discount);
            if (newTotal.compareTo(BigDecimal.ZERO) < 0) newTotal = BigDecimal.ZERO;

            discountAmountLabel.setText("-$" + discount);
            newTotalLabel.setText("$" + newTotal);
        } catch (NumberFormatException e) {
            discountAmountLabel.setText("-$0");
            newTotalLabel.setText("$" + originalTotal);
        }
    }

    @FXML
    public void goToSummary(ActionEvent event) throws IOException {
        boolean enrollLoyalty = enrollYesBtn.isSelected();
        int pointsToRedeem = 0;
        try {
            pointsToRedeem = Integer.parseInt(pointsToRedeemField.getText().trim());
        } catch (NumberFormatException e) {}

        mainController.setLoyaltyData(enrollLoyalty, pointsToRedeem);

        // Update total with discount
        UIController.BookingData data = mainController.getBookingData();
        if (data != null) {
            int points = pointsToRedeem;
            BigDecimal discount = BigDecimal.valueOf(points / 100).multiply(BigDecimal.TEN);
            data.total = originalTotal.subtract(discount);
        }

        mainController.goToSummary(event);
    }

    @FXML
    public void goToAddOns(ActionEvent event) throws IOException {
        mainController.goToAddOns(event);
    }
}