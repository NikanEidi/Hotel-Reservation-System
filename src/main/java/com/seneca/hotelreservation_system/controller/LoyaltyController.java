package com.seneca.hotelreservation_system.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import java.io.IOException;

public class LoyaltyController {

    @FXML private RadioButton enrollYesBtn;
    @FXML private RadioButton enrollNoBtn;
    @FXML private ToggleGroup enrollGroup;

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
        } else {
            enrollYesBtn.setSelected(true);
        }
    }

    @FXML
    public void goToSummary(ActionEvent event) throws IOException {
        boolean enrollLoyalty = enrollYesBtn.isSelected();
        mainController.setLoyaltyData(enrollLoyalty, 0);
        mainController.goToSummary(event);
    }

    @FXML
    public void goToAddOns(ActionEvent event) throws IOException {
        mainController.goToAddOns(event);
    }

    @FXML
    public void showRules() {
        mainController.showRules();
    }
}