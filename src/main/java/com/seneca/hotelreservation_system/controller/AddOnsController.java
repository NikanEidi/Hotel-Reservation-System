package com.seneca.hotelreservation_system.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;

public class AddOnsController {

    @FXML private CheckBox wifiCheck;
    @FXML private CheckBox breakfastCheck;
    @FXML private CheckBox parkingCheck;
    @FXML private CheckBox spaCheck;
    @FXML private Spinner<Integer> wifiSpinner;
    @FXML private Spinner<Integer> breakfastSpinner;
    @FXML private Spinner<Integer> parkingSpinner;
    @FXML private Spinner<Integer> spaSpinner;
    @FXML private Label addonsTotalLabel;
    @FXML private Label nightsLabel;
    @FXML private Label guestsLabel;

    private UIController mainController;
    private int nights;
    private int totalGuests;

    public void setMainController(UIController controller) {
        this.mainController = controller;
        loadBookingInfo();
        loadExistingData();
        setupSpinners();
        setupCheckboxListeners();
        updateTotalDisplay();
    }

    private void loadBookingInfo() {
        UIController.BookingData data = mainController.getBookingData();
        if (data != null && data.checkIn != null && data.checkOut != null) {
            nights = (int) ChronoUnit.DAYS.between(data.checkIn, data.checkOut);
            totalGuests = data.adults + data.children;
            nightsLabel.setText("for " + nights + " nights");
            guestsLabel.setText(totalGuests + " guest(s)");
        }
    }

    private void loadExistingData() {
        UIController.BookingData data = mainController.getBookingData();
        if (data != null) {
            wifiCheck.setSelected(data.hasWifi);
            breakfastCheck.setSelected(data.hasBreakfast);
            parkingCheck.setSelected(data.hasParking);
            spaCheck.setSelected(data.hasSpa);
        }
    }

    private void setupSpinners() {
        wifiSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5, 0));
        breakfastSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, totalGuests, 0));
        parkingSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 3, 0));
        spaSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0));

        wifiSpinner.disableProperty().bind(wifiCheck.selectedProperty().not());
        breakfastSpinner.disableProperty().bind(breakfastCheck.selectedProperty().not());
        parkingSpinner.disableProperty().bind(parkingCheck.selectedProperty().not());
        spaSpinner.disableProperty().bind(spaCheck.selectedProperty().not());

        wifiSpinner.valueProperty().addListener((obs, old, val) -> updateTotalDisplay());
        breakfastSpinner.valueProperty().addListener((obs, old, val) -> updateTotalDisplay());
        parkingSpinner.valueProperty().addListener((obs, old, val) -> updateTotalDisplay());
        spaSpinner.valueProperty().addListener((obs, old, val) -> updateTotalDisplay());
    }

    private void setupCheckboxListeners() {
        wifiCheck.selectedProperty().addListener((obs, old, val) -> { if (!val) wifiSpinner.getValueFactory().setValue(0); updateTotalDisplay(); });
        breakfastCheck.selectedProperty().addListener((obs, old, val) -> { if (!val) breakfastSpinner.getValueFactory().setValue(0); updateTotalDisplay(); });
        parkingCheck.selectedProperty().addListener((obs, old, val) -> { if (!val) parkingSpinner.getValueFactory().setValue(0); updateTotalDisplay(); });
        spaCheck.selectedProperty().addListener((obs, old, val) -> { if (!val) spaSpinner.getValueFactory().setValue(0); updateTotalDisplay(); });
    }

    private void updateTotalDisplay() {
        BigDecimal total = calculateTotal();
        addonsTotalLabel.setText("$" + total);
    }

    private BigDecimal calculateTotal() {
        BigDecimal total = BigDecimal.ZERO;
        if (wifiCheck.isSelected()) total = total.add(BigDecimal.valueOf(10).multiply(BigDecimal.valueOf(nights)).multiply(BigDecimal.valueOf(wifiSpinner.getValue())));
        if (breakfastCheck.isSelected()) total = total.add(BigDecimal.valueOf(15).multiply(BigDecimal.valueOf(nights)).multiply(BigDecimal.valueOf(breakfastSpinner.getValue())));
        if (parkingCheck.isSelected()) total = total.add(BigDecimal.valueOf(20).multiply(BigDecimal.valueOf(nights)).multiply(BigDecimal.valueOf(parkingSpinner.getValue())));
        if (spaCheck.isSelected()) total = total.add(BigDecimal.valueOf(50).multiply(BigDecimal.valueOf(spaSpinner.getValue())));
        return total;
    }

    @FXML
    public void goToLoyalty(ActionEvent event) throws IOException {
        mainController.setAddonsData(
                wifiCheck.isSelected(), breakfastCheck.isSelected(),
                parkingCheck.isSelected(), spaCheck.isSelected(),
                wifiSpinner.getValue(), breakfastSpinner.getValue(),
                parkingSpinner.getValue(), spaSpinner.getValue()
        );
        mainController.goToLoyalty(event);
    }

    @FXML
    public void goToRoomSelection(ActionEvent event) throws IOException {
        mainController.goToRoomSelection(event);
    }
}