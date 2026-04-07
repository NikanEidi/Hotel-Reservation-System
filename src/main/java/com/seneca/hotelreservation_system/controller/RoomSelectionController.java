package com.seneca.hotelreservation_system.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
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
    @FXML private VBox singleRoomCard;
    @FXML private VBox doubleRoomCard;
    @FXML private VBox deluxeRoomCard;
    @FXML private VBox penthouseCard;

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
                case SINGLE:    singleRoomRadio.setSelected(true); selectedType = UIController.RoomType.SINGLE; break;
                case DOUBLE:    doubleRoomRadio.setSelected(true); selectedType = UIController.RoomType.DOUBLE; break;
                case DELUXE:    deluxeRoomRadio.setSelected(true); selectedType = UIController.RoomType.DELUXE; break;
                case PENTHOUSE: penthouseRadio.setSelected(true); selectedType = UIController.RoomType.PENTHOUSE; break;
                default:        singleRoomRadio.setSelected(true); selectedType = UIController.RoomType.SINGLE;
            }
            selectedQuantity = Math.max(data.roomQuantity, 1);
        } else {
            singleRoomRadio.setSelected(true);
            selectedType = UIController.RoomType.SINGLE;
            selectedQuantity = 1;
        }
        initializeComboBoxes();
    }

    private void initializeComboBoxes() {
        singleRoomQty.getItems().addAll(1, 2, 3, 4);
        doubleRoomQty.getItems().addAll(1, 2, 3, 4);
        deluxeRoomQty.getItems().addAll(1, 2);
        penthouseQty.getItems().addAll(1, 2);

        singleRoomQty.setValue(selectedType == UIController.RoomType.SINGLE ? selectedQuantity : 1);
        doubleRoomQty.setValue(selectedType == UIController.RoomType.DOUBLE ? selectedQuantity : 1);
        deluxeRoomQty.setValue(selectedType == UIController.RoomType.DELUXE ? selectedQuantity : 1);
        penthouseQty.setValue(selectedType == UIController.RoomType.PENTHOUSE ? selectedQuantity : 1);

        singleRoomQty.valueProperty().addListener((obs, old, val) -> {
            if (val != null && singleRoomRadio.isSelected()) { selectedQuantity = val; updateCapacityDisplay(); }
        });
        doubleRoomQty.valueProperty().addListener((obs, old, val) -> {
            if (val != null && doubleRoomRadio.isSelected()) { selectedQuantity = val; updateCapacityDisplay(); }
        });
        deluxeRoomQty.valueProperty().addListener((obs, old, val) -> {
            if (val != null && deluxeRoomRadio.isSelected()) { selectedQuantity = val; updateCapacityDisplay(); }
        });
        penthouseQty.valueProperty().addListener((obs, old, val) -> {
            if (val != null && penthouseRadio.isSelected()) { selectedQuantity = val; updateCapacityDisplay(); }
        });
    }

    private void updateSearchSummary() {
        if (searchSummaryLabel == null) return;
        UIController.BookingData data = mainController.getBookingData();
        if (data != null && data.checkIn != null && data.checkOut != null) {
            long nights = ChronoUnit.DAYS.between(data.checkIn, data.checkOut);
            searchSummaryLabel.setText(String.format(
                    "%d Adult%s%s  •  %s → %s  (%d night%s)",
                    data.adults, data.adults != 1 ? "s" : "",
                    data.children > 0 ? ", " + data.children + " Child" + (data.children != 1 ? "ren" : "") : "",
                    data.checkIn, data.checkOut, nights, nights != 1 ? "s" : ""));
        } else {
            searchSummaryLabel.setText("Step 3  •  Choose your suite");
        }
    }

    private void updateCapacityDisplay() {
        if (capacityLabel == null || capacityBar == null) return;
        UIController.BookingData data = mainController.getBookingData();
        if (data == null) return;

        int totalGuests = data.adults + data.children;
        int maxCapacity = getCurrentRoomMaxCapacity();
        int totalCapacity = maxCapacity * selectedQuantity;

        if (totalGuests <= totalCapacity) {
            capacityLabel.setText(String.format("CAPACITY OK  -  %d / %d guests", totalGuests, totalCapacity));
            capacityLabel.setStyle("-fx-text-fill: #22c55e; -fx-font-size: 13px; -fx-font-weight: bold;");
            capacityBar.setStyle("-fx-accent: #22c55e;");
        } else {
            capacityLabel.setText(String.format("EXCEEDS CAPACITY  -  %d guests exceed %d capacity", totalGuests, totalCapacity));
            capacityLabel.setStyle("-fx-text-fill: #e55; -fx-font-size: 13px; -fx-font-weight: bold;");
            capacityBar.setStyle("-fx-accent: #e55;");
        }
        double pct = totalCapacity > 0 ? Math.min(1.0, (double) totalGuests / totalCapacity) : 0;
        capacityBar.setProgress(pct);
    }

    private int getCurrentRoomMaxCapacity() {
        if (singleRoomRadio != null && singleRoomRadio.isSelected()) return SINGLE_MAX;
        if (doubleRoomRadio != null && doubleRoomRadio.isSelected()) return DOUBLE_MAX;
        if (deluxeRoomRadio != null && deluxeRoomRadio.isSelected()) return DELUXE_MAX;
        if (penthouseRadio != null && penthouseRadio.isSelected()) return PENTHOUSE_MAX;
        return SINGLE_MAX;
    }

    @FXML
    public void onRoomSelected() {
        if (singleRoomRadio.isSelected()) {
            selectedType = UIController.RoomType.SINGLE;
            selectedQuantity = singleRoomQty.getValue() != null ? singleRoomQty.getValue() : 1;
        } else if (doubleRoomRadio.isSelected()) {
            selectedType = UIController.RoomType.DOUBLE;
            selectedQuantity = doubleRoomQty.getValue() != null ? doubleRoomQty.getValue() : 1;
        } else if (deluxeRoomRadio.isSelected()) {
            selectedType = UIController.RoomType.DELUXE;
            selectedQuantity = deluxeRoomQty.getValue() != null ? deluxeRoomQty.getValue() : 1;
        } else if (penthouseRadio.isSelected()) {
            selectedType = UIController.RoomType.PENTHOUSE;
            selectedQuantity = penthouseQty.getValue() != null ? penthouseQty.getValue() : 1;
        }
        updateCapacityDisplay();
    }

    @FXML
    public void selectStandardDouble(ActionEvent event) throws IOException {
        selectedType = UIController.RoomType.DOUBLE;
        selectedQuantity = doubleRoomQty.getValue() != null ? doubleRoomQty.getValue() : 1;
        goToAddOns(event);
    }

    @FXML
    public void selectRoyalKing(ActionEvent event) throws IOException {
        selectedType = UIController.RoomType.PENTHOUSE;
        selectedQuantity = penthouseQty.getValue() != null ? penthouseQty.getValue() : 1;
        goToAddOns(event);
    }

    @FXML
    public void selectSingle(ActionEvent event) throws IOException {
        selectedType = UIController.RoomType.SINGLE;
        selectedQuantity = singleRoomQty.getValue() != null ? singleRoomQty.getValue() : 1;
        goToAddOns(event);
    }

    @FXML
    public void selectDeluxe(ActionEvent event) throws IOException {
        selectedType = UIController.RoomType.DELUXE;
        selectedQuantity = deluxeRoomQty.getValue() != null ? deluxeRoomQty.getValue() : 1;
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
        if (data == null) {
            showError("Please complete guest information first.");
            return false;
        }

        if (selectedQuantity <= 0) {
            showError("Please select at least one room.");
            return false;
        }

        int totalGuests = data.adults + data.children;
        int maxCapacity = getCurrentRoomMaxCapacity();
        int totalCapacity = maxCapacity * selectedQuantity;

        if (totalGuests > totalCapacity) {
            showError(String.format(
                    "Occupancy exceeded: %d guests cannot fit in %d %s room(s) (max %d per room, %d total capacity).",
                    totalGuests, selectedQuantity,
                    selectedType.name().toLowerCase(),
                    maxCapacity, totalCapacity));
            return false;
        }
        return true;
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Room Selection");
        alert.setHeaderText("Invalid Selection");
        alert.setContentText(message);
        alert.showAndWait();
    }
}