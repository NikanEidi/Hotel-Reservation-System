package com.seneca.hotelreservation_system.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import java.io.IOException;

public class LoyaltyController {

    @FXML private RadioButton enrollYesBtn;
    @FXML private RadioButton enrollNoBtn;

    private UIController mainController;

    public void setMainController(UIController controller) {
        this.mainController = controller;
        loadExistingData();
    }

    private void loadExistingData() {
        UIController.BookingData data = mainController.getBookingData();
        if (data != null) {
            enrollYesBtn.setSelected(data.enrollLoyalty);
            enrollNoBtn.setSelected(!data.enrollLoyalty);
        }
    }

    @FXML
    public void goToSummary(ActionEvent event) throws IOException {
        boolean enrollLoyalty = enrollYesBtn.isSelected();
        int pointsToRedeem = 0;  // No points redemption in simple version

        mainController.setLoyaltyData(enrollLoyalty, pointsToRedeem);
        mainController.goToSummary(event);
    }

    @FXML
    public void goToAddOns(ActionEvent event) throws IOException {
        mainController.goToAddOns(event);
    }
}