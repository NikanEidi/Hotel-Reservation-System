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
    @FXML private CheckBox shuttleCheck;
    @FXML private CheckBox spaCheck;
    @FXML private Spinner<Integer> wifiSpinner;
    @FXML private Spinner<Integer> breakfastSpinner;
    @FXML private Spinner<Integer> shuttleSpinner;
    @FXML private Spinner<Integer> spaSpinner;
    @FXML private Label addonsTotalLabel;
    @FXML private Label nightsLabel;
    @FXML private Label guestsLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label totalLabel;

    private UIController mainController;
    private int nights;
    private int totalGuests;

    public interface BillComponent {
        BigDecimal getCost();
        String getDescription();
    }

    public static class BaseBill implements BillComponent {
        private BigDecimal roomCost;
        private String roomDescription;

        public BaseBill(BigDecimal roomCost, String roomDescription) {
            this.roomCost = roomCost;
            this.roomDescription = roomDescription;
        }

        @Override
        public BigDecimal getCost() {
            return roomCost;
        }

        @Override
        public String getDescription() {
            return roomDescription;
        }
    }

    public static class WiFiDecorator implements BillComponent {
        private BillComponent wrapped;
        private int quantity;
        private int nights;

        public WiFiDecorator(BillComponent wrapped, int quantity, int nights) {
            this.wrapped = wrapped;
            this.quantity = quantity;
            this.nights = nights;
        }

        @Override
        public BigDecimal getCost() {
            BigDecimal wifiCost = BigDecimal.valueOf(10)
                    .multiply(BigDecimal.valueOf(nights))
                    .multiply(BigDecimal.valueOf(quantity));
            return wrapped.getCost().add(wifiCost);
        }

        @Override
        public String getDescription() {
            if (quantity > 0) {
                return wrapped.getDescription() + " + Wi-Fi (" + quantity + " device(s), " + nights + " nights)";
            }
            return wrapped.getDescription();
        }
    }

    public static class BreakfastDecorator implements BillComponent {
        private BillComponent wrapped;
        private int quantity;
        private int nights;

        public BreakfastDecorator(BillComponent wrapped, int quantity, int nights) {
            this.wrapped = wrapped;
            this.quantity = quantity;
            this.nights = nights;
        }

        @Override
        public BigDecimal getCost() {
            BigDecimal breakfastCost = BigDecimal.valueOf(15)
                    .multiply(BigDecimal.valueOf(nights))
                    .multiply(BigDecimal.valueOf(quantity));
            return wrapped.getCost().add(breakfastCost);
        }

        @Override
        public String getDescription() {
            if (quantity > 0) {
                return wrapped.getDescription() + " + Breakfast (" + quantity + " person(s), " + nights + " days)";
            }
            return wrapped.getDescription();
        }
    }

    public static class ShuttleDecorator implements BillComponent {
        private BillComponent wrapped;
        private int quantity;

        public ShuttleDecorator(BillComponent wrapped, int quantity) {
            this.wrapped = wrapped;
            this.quantity = quantity;
        }

        @Override
        public BigDecimal getCost() {
            BigDecimal shuttleCost = BigDecimal.valueOf(35).multiply(BigDecimal.valueOf(quantity));
            return wrapped.getCost().add(shuttleCost);
        }

        @Override
        public String getDescription() {
            if (quantity > 0) {
                return wrapped.getDescription() + " + Airport Shuttle (" + quantity + " trip(s))";
            }
            return wrapped.getDescription();
        }
    }

    public static class SpaDecorator implements BillComponent {
        private BillComponent wrapped;
        private int quantity;

        public SpaDecorator(BillComponent wrapped, int quantity) {
            this.wrapped = wrapped;
            this.quantity = quantity;
        }

        @Override
        public BigDecimal getCost() {
            BigDecimal spaCost = BigDecimal.valueOf(50).multiply(BigDecimal.valueOf(quantity));
            return wrapped.getCost().add(spaCost);
        }

        @Override
        public String getDescription() {
            if (quantity > 0) {
                return wrapped.getDescription() + " + Spa (" + quantity + " session(s))";
            }
            return wrapped.getDescription();
        }
    }

    private BillComponent buildBill() {
        UIController.BookingData data = mainController.getBookingData();
        BigDecimal roomCost = mainController.getRoomPrice();
        String roomDesc = data.selectedRoomType.toString() + " Room x" + data.roomQuantity;

        BillComponent bill = new BaseBill(roomCost, roomDesc);

        if (wifiCheck.isSelected() && wifiSpinner.getValue() > 0) {
            bill = new WiFiDecorator(bill, wifiSpinner.getValue(), nights);
        }

        if (breakfastCheck.isSelected() && breakfastSpinner.getValue() > 0) {
            bill = new BreakfastDecorator(bill, breakfastSpinner.getValue(), nights);
        }

        if (shuttleCheck.isSelected() && shuttleSpinner.getValue() > 0) {
            bill = new ShuttleDecorator(bill, shuttleSpinner.getValue());
        }

        if (spaCheck.isSelected() && spaSpinner.getValue() > 0) {
            bill = new SpaDecorator(bill, spaSpinner.getValue());
        }

        return bill;
    }

    private BigDecimal calculateTotalWithDecorator() {
        BillComponent bill = buildBill();
        BigDecimal roomCost = mainController.getRoomPrice();
        return bill.getCost().subtract(roomCost);
    }

    public String getFullBillDescription() {
        BillComponent bill = buildBill();
        return bill.getDescription();
    }

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
            shuttleCheck.setSelected(data.hasParking);
            spaCheck.setSelected(data.hasSpa);
            wifiSpinner.getValueFactory().setValue(data.wifiQuantity);
            breakfastSpinner.getValueFactory().setValue(data.breakfastQuantity);
            shuttleSpinner.getValueFactory().setValue(data.parkingQuantity);
            spaSpinner.getValueFactory().setValue(data.spaQuantity);
        }
    }

    private void setupSpinners() {
        wifiSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 5, 0));
        breakfastSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, totalGuests, 0));
        shuttleSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 3, 0));
        spaSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 10, 0));

        wifiSpinner.getValueFactory().setValue(0);
        breakfastSpinner.getValueFactory().setValue(0);
        shuttleSpinner.getValueFactory().setValue(0);
        spaSpinner.getValueFactory().setValue(0);

        wifiSpinner.disableProperty().bind(wifiCheck.selectedProperty().not());
        breakfastSpinner.disableProperty().bind(breakfastCheck.selectedProperty().not());
        shuttleSpinner.disableProperty().bind(shuttleCheck.selectedProperty().not());
        spaSpinner.disableProperty().bind(spaCheck.selectedProperty().not());

        wifiSpinner.valueProperty().addListener((obs, old, val) -> updateTotalDisplay());
        breakfastSpinner.valueProperty().addListener((obs, old, val) -> updateTotalDisplay());
        shuttleSpinner.valueProperty().addListener((obs, old, val) -> updateTotalDisplay());
        spaSpinner.valueProperty().addListener((obs, old, val) -> updateTotalDisplay());
    }

    private void setupCheckboxListeners() {
        wifiCheck.selectedProperty().addListener((obs, old, val) -> {
            if (!val) wifiSpinner.getValueFactory().setValue(0);
            updateTotalDisplay();
        });
        breakfastCheck.selectedProperty().addListener((obs, old, val) -> {
            if (!val) breakfastSpinner.getValueFactory().setValue(0);
            updateTotalDisplay();
        });
        shuttleCheck.selectedProperty().addListener((obs, old, val) -> {
            if (!val) shuttleSpinner.getValueFactory().setValue(0);
            updateTotalDisplay();
        });
        spaCheck.selectedProperty().addListener((obs, old, val) -> {
            if (!val) spaSpinner.getValueFactory().setValue(0);
            updateTotalDisplay();
        });
    }

    private void updateTotalDisplay() {
        BigDecimal total = calculateTotalWithDecorator();
        addonsTotalLabel.setText("$" + total);

        UIController.BookingData data = mainController.getBookingData();
        if (data != null) {
            data.addonsTotal = total;
            mainController.calculateTotal();

            if (subtotalLabel != null) {
                subtotalLabel.setText("$" + data.subtotal);
            }
            if (taxLabel != null) {
                taxLabel.setText("$" + data.tax);
            }
            if (totalLabel != null) {
                totalLabel.setText("$" + data.total);
            }
        }
    }

    @FXML
    public void goToLoyalty(ActionEvent event) throws IOException {
        System.out.println("=== STEP 1: goToLoyalty CALLED in AddOnsController! ===");

        if (mainController == null) {
            System.out.println("ERROR: mainController is NULL!");
            return;
        }

        System.out.println("STEP 2: Saving add-ons data...");

        int wifiQty = wifiSpinner.getValue() != null ? wifiSpinner.getValue() : 0;
        int breakfastQty = breakfastSpinner.getValue() != null ? breakfastSpinner.getValue() : 0;
        int shuttleQty = shuttleSpinner.getValue() != null ? shuttleSpinner.getValue() : 0;
        int spaQty = spaSpinner.getValue() != null ? spaSpinner.getValue() : 0;

        mainController.setAddonsData(
                wifiCheck.isSelected(), breakfastCheck.isSelected(),
                shuttleCheck.isSelected(), spaCheck.isSelected(),
                wifiQty, breakfastQty, shuttleQty, spaQty
        );

        System.out.println("STEP 3: Calling mainController.goToLoyalty()...");
        mainController.goToLoyalty(event);
    }

    @FXML
    public void goToRoomSelection(ActionEvent event) throws IOException {
        mainController.goToRoomSelection(event);
    }

    @FXML
    public void showRules() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Grand Plaza Rules & Policies");
        alert.setHeaderText("Hotel Kiosk Regulations");
        alert.setContentText(
                "1. Check-in: 3:00 PM | Check-out: 11:00 AM\n" +
                        "2. Maximum occupancy per room must be respected.\n" +
                        "3. Cancellations must be made 24 hours in advance.\n" +
                        "4. Quiet hours: 10:00 PM - 8:00 AM\n" +
                        "5. No smoking in rooms ($250 fine)"
        );
        alert.showAndWait();
    }
}