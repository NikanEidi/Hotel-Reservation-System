package com.seneca.hotelreservation_system.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import java.io.IOException;
import java.time.temporal.ChronoUnit;

public class RoomSelectionController {

    @FXML private Label searchSummaryLabel;
    @FXML private Label capacityLabel;
    @FXML private ProgressBar capacityBar;
    @FXML private RadioButton singleRoomRadio;
    @FXML private RadioButton doubleRoomRadio;
    @FXML private RadioButton deluxeRoomRadio;
    @FXML private RadioButton penthouseRadio;
    @FXML private ToggleGroup roomToggleGroup;
    @FXML private ComboBox<Integer> singleRoomQty;
    @FXML private ComboBox<Integer> doubleRoomQty;
    @FXML private ComboBox<Integer> deluxeRoomQty;
    @FXML private ComboBox<Integer> penthouseQty;
    @FXML private HBox singleRoomCard;
    @FXML private HBox doubleRoomCard;
    @FXML private HBox deluxeRoomCard;
    @FXML private HBox penthouseCard;

    private UIController mainController;
    private UIController.RoomType selectedType;
    private int selectedQuantity = 1;

    private static final int SINGLE_MAX = 2;
    private static final int DOUBLE_MAX = 4;
    private static final int DELUXE_MAX = 2;
    private static final int PENTHOUSE_MAX = 2;

    public void setMainController(UIController controller) {
        this.mainController = controller;
        loadExistingData();
        updateSearchSummary();
        updateCapacityDisplay();
    }

    private void loadExistingData() {
        UIController.BookingData data = mainController.getBookingData();
        if (data != null) {
            switch (data.selectedRoomType) {
                case SINGLE: singleRoomRadio.setSelected(true); selectedType = UIController.RoomType.SINGLE; break;
                case DOUBLE: doubleRoomRadio.setSelected(true); selectedType = UIController.RoomType.DOUBLE; break;
                case DELUXE: deluxeRoomRadio.setSelected(true); selectedType = UIController.RoomType.DELUXE; break;
                case PENTHOUSE: penthouseRadio.setSelected(true); selectedType = UIController.RoomType.PENTHOUSE; break;
                default: singleRoomRadio.setSelected(true); selectedType = UIController.RoomType.SINGLE;
            }
            selectedQuantity = data.roomQuantity;
        } else {
            singleRoomRadio.setSelected(true);
            selectedType = UIController.RoomType.SINGLE;
            selectedQuantity = 1;
        }
        initializeComboBoxes();
    }

    private void initializeComboBoxes() {
        singleRoomQty.getItems().addAll(0, 1, 2, 3, 4);
        doubleRoomQty.getItems().addAll(0, 1, 2, 3, 4);
        deluxeRoomQty.getItems().addAll(0, 1, 2);
        penthouseQty.getItems().addAll(0, 1, 2);

        singleRoomQty.setValue(selectedType == UIController.RoomType.SINGLE ? selectedQuantity : 0);
        doubleRoomQty.setValue(selectedType == UIController.RoomType.DOUBLE ? selectedQuantity : 0);
        deluxeRoomQty.setValue(selectedType == UIController.RoomType.DELUXE ? selectedQuantity : 0);
        penthouseQty.setValue(selectedType == UIController.RoomType.PENTHOUSE ? selectedQuantity : 0);

        singleRoomQty.valueProperty().addListener((obs, old, val) -> { if (val != null && val > 0 && singleRoomRadio.isSelected()) { selectedQuantity = val; updateCapacityDisplay(); } });
        doubleRoomQty.valueProperty().addListener((obs, old, val) -> { if (val != null && val > 0 && doubleRoomRadio.isSelected()) { selectedQuantity = val; updateCapacityDisplay(); } });
        deluxeRoomQty.valueProperty().addListener((obs, old, val) -> { if (val != null && val > 0 && deluxeRoomRadio.isSelected()) { selectedQuantity = val; updateCapacityDisplay(); } });
        penthouseQty.valueProperty().addListener((obs, old, val) -> { if (val != null && val > 0 && penthouseRadio.isSelected()) { selectedQuantity = val; updateCapacityDisplay(); } });
    }

    private void updateSearchSummary() {
        UIController.BookingData data = mainController.getBookingData();
        if (data != null && data.checkIn != null && data.checkOut != null) {
            long nights = ChronoUnit.DAYS.between(data.checkIn, data.checkOut);
            searchSummaryLabel.setText(String.format("%d Adults, %d Children • %s to %s (%d nights)",
                    data.adults, data.children, data.checkIn, data.checkOut, nights));
        }
    }

    private void updateCapacityDisplay() {
        UIController.BookingData data = mainController.getBookingData();
        if (data == null) return;

        int totalGuests = data.adults + data.children;
        int maxCapacity = getCurrentRoomMaxCapacity();
        int totalCapacity = maxCapacity * selectedQuantity;

        if (totalGuests <= totalCapacity) {
            capacityLabel.setText(String.format("✓ Total Occupancy: %d/%d persons", totalGuests, totalCapacity));
            capacityBar.setProgress((double) totalGuests / totalCapacity);
            capacityLabel.setStyle("-fx-text-fill: #28a745; -fx-font-weight: bold;");
        } else {
            capacityLabel.setText(String.format("⚠️ OVER CAPACITY: %d guests exceed %d capacity", totalGuests, totalCapacity));
            capacityBar.setProgress(1.0);
            capacityLabel.setStyle("-fx-text-fill: #dc3545; -fx-font-weight: bold;");
        }
    }

    private int getCurrentRoomMaxCapacity() {
        if (singleRoomRadio.isSelected()) return SINGLE_MAX;
        if (doubleRoomRadio.isSelected()) return DOUBLE_MAX;
        if (deluxeRoomRadio.isSelected()) return DELUXE_MAX;
        if (penthouseRadio.isSelected()) return PENTHOUSE_MAX;
        return SINGLE_MAX;
    }

    @FXML
    public void onRoomSelected() {
        if (singleRoomRadio.isSelected()) { selectedType = UIController.RoomType.SINGLE; selectedQuantity = singleRoomQty.getValue(); }
        else if (doubleRoomRadio.isSelected()) { selectedType = UIController.RoomType.DOUBLE; selectedQuantity = doubleRoomQty.getValue(); }
        else if (deluxeRoomRadio.isSelected()) { selectedType = UIController.RoomType.DELUXE; selectedQuantity = deluxeRoomQty.getValue(); }
        else if (penthouseRadio.isSelected()) { selectedType = UIController.RoomType.PENTHOUSE; selectedQuantity = penthouseQty.getValue(); }
        updateCapacityDisplay();
    }

    @FXML
    public void selectStandardDouble(ActionEvent event) throws IOException {
        selectedType = UIController.RoomType.DOUBLE;
        selectedQuantity = 1;
        goToAddOns(event);
    }

    @FXML
    public void selectRoyalKing(ActionEvent event) throws IOException {
        selectedType = UIController.RoomType.PENTHOUSE;
        selectedQuantity = 1;
        goToAddOns(event);
    }

    @FXML
    public void goToAddOns(ActionEvent event) throws IOException {
        if (!validateRoomSelection()) return;
        mainController.setRoomData(selectedType, selectedQuantity);
        mainController.goToAddOns(event);
    }

    @FXML
    public void goToSearch(ActionEvent event) throws IOException {
        mainController.goToSearch(event);
    }

    @FXML
    public void goToWelcome(ActionEvent event) throws IOException {
        mainController.goToWelcome(event);
    }

    @FXML
    public void showRules() {
        mainController.showRules();
    }

    private boolean validateRoomSelection() {
        UIController.BookingData data = mainController.getBookingData();
        if (data == null) { showError("Please complete guest information first."); return false; }

        int totalGuests = data.adults + data.children;
        int totalCapacity = getCurrentRoomMaxCapacity() * selectedQuantity;

        if (selectedQuantity == 0) { showError("Please select at least one room."); return false; }
        if (totalGuests > totalCapacity) { showError("Occupancy exceeds capacity!"); return false; }
        return true;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Room Selection Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}