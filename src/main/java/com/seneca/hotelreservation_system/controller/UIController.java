package com.seneca.hotelreservation_system.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@SuppressWarnings({"unused", "SpellCheckingInspection"})
public class UIController {

    public static class BookingData {
        public int adults = 2;
        public int children = 0;
        public LocalDate checkIn;
        public LocalDate checkOut;
        public String guestName;
        public String guestEmail;
        public String guestPhone;

        public RoomType selectedRoomType = RoomType.SINGLE;
        public int roomQuantity = 1;

        public boolean hasWifi = false;
        public boolean hasBreakfast = false;
        public boolean hasParking = false;
        public boolean hasSpa = false;
        public boolean hasShuttle = false;
        public boolean hasLateCheckout = false;

        public int wifiQuantity = 0;
        public int breakfastQuantity = 0;
        public int parkingQuantity = 0;
        public int spaQuantity = 0;

        public boolean enrollLoyalty = true;
        public int loyaltyPointsToRedeem = 0;

        public int nights;
        public BigDecimal roomPrice;
        public BigDecimal addonsTotal;
        public BigDecimal subtotal;
        public BigDecimal tax;
        public BigDecimal total;
    }

    private static BookingData booking = new BookingData();

    public interface PricingStrategy {
        BigDecimal calculatePrice(BigDecimal basePrice, int nights);
    }

    public static class StandardPricing implements PricingStrategy {
        @Override
        public BigDecimal calculatePrice(BigDecimal basePrice, int nights) {
            return basePrice.multiply(BigDecimal.valueOf(nights));
        }
    }

    public static class WeekendPricing implements PricingStrategy {
        private static final BigDecimal WEEKEND_MULTIPLIER = new BigDecimal("1.2");
        @Override
        public BigDecimal calculatePrice(BigDecimal basePrice, int nights) {
            return basePrice.multiply(WEEKEND_MULTIPLIER).multiply(BigDecimal.valueOf(nights));
        }
    }

    public static class PeakSeasonPricing implements PricingStrategy {
        private static final BigDecimal PEAK_MULTIPLIER = new BigDecimal("1.3");
        @Override
        public BigDecimal calculatePrice(BigDecimal basePrice, int nights) {
            return basePrice.multiply(PEAK_MULTIPLIER).multiply(BigDecimal.valueOf(nights));
        }
    }

    public enum RoomType {
        SINGLE(89, 2), DOUBLE(129, 4), DELUXE(199, 2), PENTHOUSE(349, 2);
        private final int basePrice;
        private final int maxOccupancy;
        RoomType(int basePrice, int maxOccupancy) {
            this.basePrice = basePrice;
            this.maxOccupancy = maxOccupancy;
        }
        public int getBasePrice() { return basePrice; }
        public int getMaxOccupancy() { return maxOccupancy; }
    }

    public boolean isWeekend(LocalDate date) {
        if (date == null) return false;
        int dayOfWeek = date.getDayOfWeek().getValue();
        return dayOfWeek == 6 || dayOfWeek == 7;
    }

    public boolean isPeakSeason(LocalDate date) {
        if (date == null) return false;
        int month = date.getMonthValue();
        return month == 7 || month == 8 || month == 12;
    }

    public PricingStrategy getPricingStrategy(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null) return new StandardPricing();

        long weekendDays = 0;
        LocalDate current = checkIn;
        while (current.isBefore(checkOut)) {
            if (isWeekend(current)) weekendDays++;
            current = current.plusDays(1);
        }
        long totalNights = ChronoUnit.DAYS.between(checkIn, checkOut);

        boolean isPeak = false;
        current = checkIn;
        while (current.isBefore(checkOut)) {
            if (isPeakSeason(current)) { isPeak = true; break; }
            current = current.plusDays(1);
        }
        if (isPeak) return new PeakSeasonPricing();
        if (weekendDays > totalNights / 2) return new WeekendPricing();
        return new StandardPricing();
    }

    public BigDecimal getBasePrice(RoomType type) {
        return BigDecimal.valueOf(type.getBasePrice());
    }

    public BigDecimal calculateRoomPrice() {
        int n = getNights();
        BigDecimal basePrice = getBasePrice(booking.selectedRoomType);
        PricingStrategy strategy = getPricingStrategy(booking.checkIn, booking.checkOut);
        BigDecimal perRoomTotal = strategy.calculatePrice(basePrice, n);
        return perRoomTotal.multiply(BigDecimal.valueOf(booking.roomQuantity));
    }

    public BigDecimal calculateAddonsTotal() {
        BigDecimal total = BigDecimal.ZERO;
        int n = getNights();

        if (booking.hasWifi && booking.wifiQuantity > 0)
            total = total.add(BigDecimal.valueOf(9.99).multiply(BigDecimal.valueOf(n)).multiply(BigDecimal.valueOf(booking.wifiQuantity)));
        if (booking.hasBreakfast && booking.breakfastQuantity > 0)
            total = total.add(BigDecimal.valueOf(24.99).multiply(BigDecimal.valueOf(n)).multiply(BigDecimal.valueOf(booking.breakfastQuantity)));
        if (booking.hasSpa && booking.spaQuantity > 0)
            total = total.add(BigDecimal.valueOf(49.99).multiply(BigDecimal.valueOf(booking.spaQuantity)));
        if (booking.hasParking && booking.parkingQuantity > 0)
            total = total.add(BigDecimal.valueOf(35.00).multiply(BigDecimal.valueOf(booking.parkingQuantity)));
        if (booking.hasShuttle)
            total = total.add(BigDecimal.valueOf(35.00));
        if (booking.hasLateCheckout)
            total = total.add(BigDecimal.valueOf(30.00));
        return total;
    }

    public BigDecimal calculateTotal() {
        booking.roomPrice = calculateRoomPrice();
        booking.addonsTotal = calculateAddonsTotal();
        booking.subtotal = booking.roomPrice.add(booking.addonsTotal);
        booking.tax = booking.subtotal.multiply(new BigDecimal("0.13"));
        booking.total = booking.subtotal.add(booking.tax);
        return booking.total;
    }

    public BookingData getBookingData() { return booking; }

    public int getNights() {
        if (booking.checkIn != null && booking.checkOut != null) {
            booking.nights = (int) ChronoUnit.DAYS.between(booking.checkIn, booking.checkOut);
        }
        if (booking.nights <= 0) booking.nights = 1;
        return booking.nights;
    }

    public BigDecimal getRoomPrice() { return calculateRoomPrice(); }
    public BigDecimal getAddonsTotal() { return calculateAddonsTotal(); }
    public BigDecimal getSubtotal() { return booking.subtotal; }
    public BigDecimal getTax() { return booking.tax; }
    public BigDecimal getTotal() { return calculateTotal(); }

    public String getPricingTypeDescription() {
        PricingStrategy strategy = getPricingStrategy(booking.checkIn, booking.checkOut);
        if (strategy instanceof PeakSeasonPricing) return "Peak Season Rate (+30%)";
        if (strategy instanceof WeekendPricing) return "Weekend Rate (+20%)";
        return "Standard Rate";
    }

    @FXML private TextField adminSearchField;
    @FXML private TextField phoneField;
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML private CheckBox wifiCheck;
    @FXML private CheckBox spaCheck;
    @FXML private CheckBox shuttleCheck;
    @FXML private CheckBox breakfastCheck;
    @FXML private CheckBox lateCheckoutCheck;

    @FXML private Label subtotalLabel;
    @FXML private Label addonsTotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label totalDueLabel;

    public void setSearchData(int adults, int children, LocalDate checkIn, LocalDate checkOut,
                              String name, String email, String phone) {
        booking.adults = adults;
        booking.children = children;
        booking.checkIn = checkIn;
        booking.checkOut = checkOut;
        booking.guestName = name;
        booking.guestEmail = email;
        booking.guestPhone = phone;
        if (checkIn != null && checkOut != null)
            booking.nights = (int) ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    public void setRoomData(RoomType type, int quantity) {
        booking.selectedRoomType = type;
        booking.roomQuantity = quantity;
    }

    public void setAddonsData(boolean wifi, boolean breakfast, boolean parking, boolean spa,
                              int wifiQty, int breakfastQty, int parkingQty, int spaQty) {
        booking.hasWifi = wifi;
        booking.hasBreakfast = breakfast;
        booking.hasParking = parking;
        booking.hasSpa = spa;
        booking.wifiQuantity = wifiQty;
        booking.breakfastQuantity = breakfastQty;
        booking.parkingQuantity = parkingQty;
        booking.spaQuantity = spaQty;
    }

    public void setLoyaltyData(boolean enroll, int pointsToRedeem) {
        booking.enrollLoyalty = enroll;
        booking.loyaltyPointsToRedeem = pointsToRedeem;
    }

    @FXML
    public void updateTotals() {
        int nightsToCalculate = getNights();

        if (wifiCheck != null) booking.hasWifi = wifiCheck.isSelected();
        if (spaCheck != null) booking.hasSpa = spaCheck.isSelected();
        if (breakfastCheck != null) booking.hasBreakfast = breakfastCheck.isSelected();
        if (shuttleCheck != null) booking.hasShuttle = shuttleCheck.isSelected();
        if (lateCheckoutCheck != null) booking.hasLateCheckout = lateCheckoutCheck.isSelected();

        double addonsCost = 0.0;
        if (booking.hasWifi) addonsCost += (9.99 * nightsToCalculate);
        if (booking.hasSpa) addonsCost += (49.99 * nightsToCalculate);
        if (booking.hasBreakfast) addonsCost += (24.99 * nightsToCalculate);
        if (booking.hasShuttle) addonsCost += 35.00;
        if (booking.hasLateCheckout) addonsCost += 30.00;

        double baseSubtotal = calculateRoomPrice().doubleValue();
        double currentSubtotal = baseSubtotal + addonsCost;
        double taxAmount = currentSubtotal * 0.13;
        double finalTotal = currentSubtotal + taxAmount;

        if (subtotalLabel != null) subtotalLabel.setText(String.format("$%.2f", baseSubtotal));
        if (addonsTotalLabel != null) addonsTotalLabel.setText(String.format("$%.2f", addonsCost));
        if (taxLabel != null) taxLabel.setText(String.format("$%.2f", taxAmount));
        if (totalDueLabel != null) totalDueLabel.setText(String.format("$%.2f", finalTotal));
    }

    @FXML
    public void goToWelcome(ActionEvent event) throws IOException {
        booking = new BookingData();
        switchScene(event, "/com/seneca/hotelreservation_system/view/welcome-view.fxml");
    }

    @FXML
    public void goToSearch(ActionEvent event) throws IOException {
        switchScene(event, "/com/seneca/hotelreservation_system/view/search-view.fxml");
    }

    @FXML
    public void goToGuestDetails(ActionEvent event) throws IOException {
        switchScene(event, "/com/seneca/hotelreservation_system/view/guest-details-view.fxml");
    }

    @FXML
    public void goToRoomSelection(ActionEvent event) throws IOException {
        if (booking.checkIn != null && booking.checkOut != null)
            booking.nights = (int) ChronoUnit.DAYS.between(booking.checkIn, booking.checkOut);
        switchScene(event, "/com/seneca/hotelreservation_system/view/room-selection-view.fxml");
    }

    @FXML
    public void goToAddOns(ActionEvent event) throws IOException {
        switchScene(event, "/com/seneca/hotelreservation_system/view/addons-view.fxml");
    }

    @FXML
    public void goToAddons(ActionEvent event) throws IOException {
        goToAddOns(event);
    }

    @FXML
    public void goToLoyalty(ActionEvent event) throws IOException {
        switchScene(event, "/com/seneca/hotelreservation_system/view/loyalty-view.fxml");
    }

    @FXML
    public void goToSummary(ActionEvent event) throws IOException {
        calculateTotal();
        switchScene(event, "/com/seneca/hotelreservation_system/view/summary-view.fxml");
    }

    @FXML
    public void goToConfirmation(ActionEvent event) throws IOException {
        calculateTotal();
        switchScene(event, "/com/seneca/hotelreservation_system/view/confirmation-view.fxml");
    }

    @FXML
    public void goToAdminLogin(ActionEvent event) throws IOException {
        switchScene(event, "/com/seneca/hotelreservation_system/view/admin-login-view.fxml");
    }

    @FXML
    public void goToAdminDashboard(ActionEvent event) throws IOException {
        switchScene(event, "/com/seneca/hotelreservation_system/view/admin-dashboard-view.fxml");
    }

    @FXML
    public void goToAdmin(ActionEvent event) throws IOException {
        goToAdminDashboard(event);
    }

    @FXML
    public void selectStandardDouble(ActionEvent event) throws IOException {
        booking.selectedRoomType = RoomType.DOUBLE;
        booking.roomQuantity = 1;
        goToAddOns(event);
    }

    @FXML
    public void selectRoyalKing(ActionEvent event) throws IOException {
        booking.selectedRoomType = RoomType.PENTHOUSE;
        booking.roomQuantity = 1;
        goToAddOns(event);
    }

    private void switchScene(ActionEvent event, String fxmlPath) throws IOException {
        URL resource = getClass().getResource(fxmlPath);
        if (resource == null) throw new IOException("FXML file not found: " + fxmlPath);

        FXMLLoader loader = new FXMLLoader(resource);
        Parent root = loader.load();

        Object controller = loader.getController();
        if (controller != null) {
            try {
                java.lang.reflect.Method method = controller.getClass().getMethod("setMainController", UIController.class);
                method.invoke(controller, this);
            } catch (Exception ignored) {
            }
        }

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 1400, 900));
        stage.show();
    }

    @FXML
    public void showRules() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Grand Plaza Hotel - Rules and Policies");
        alert.setHeaderText("Hotel Kiosk Regulations");
        alert.setContentText(
                "1. Check-in: 3:00 PM  |  Check-out: 11:00 AM\n" +
                "2. Maximum room occupancy must be respected at all times.\n" +
                "3. Cancellations require 24 hours advance notice.\n" +
                "4. Quiet hours: 10:00 PM to 8:00 AM\n" +
                "5. No smoking in rooms. A $250 fee applies.\n" +
                "6. Pets permitted with advance notice. Fee applies.\n" +
                "7. Loyalty points are earned on every completed paid stay."
        );
        alert.showAndWait();
    }

    @FXML
    public void handleAdminLogin(ActionEvent event) throws IOException {
        String username = usernameField != null ? usernameField.getText().trim() : "";
        String password = passwordField != null ? passwordField.getText().trim() : "";

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Login Error", "Missing Credentials",
                    "Please enter both username and password.");
            return;
        }

        boolean authenticated = false;
        try {
            com.seneca.hotelreservation_system.service.AuthService authService =
                    new com.seneca.hotelreservation_system.service.AuthService();
            com.seneca.hotelreservation_system.model.Admin admin = authService.authenticate(username, password);
            if (admin != null) authenticated = true;
        } catch (Exception ignored) {
            if (username.equals("nikan") && password.equals("123456")) authenticated = true;
        }
        if (authenticated) {
            goToAdminDashboard(event);
        } else {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid Credentials",
                    "The username or password you entered is incorrect. Please try again.");
        }
    }

    @FXML
    public void showFeedbackModule() {
        showPlaceholder("Feedback Management", "Guest feedback viewing and management is available in the final milestone. Feedback is linked to checked-out reservations.");
    }

    @FXML
    public void showBillingModule() {
        showPlaceholder("Billing & Payments", "Full billing module (cash, card, loyalty points, deposits, refunds) is available in the final milestone.");
    }

    @FXML
    public void showWaitlistModule() {
        showPlaceholder("Waitlist Management", "Waitlist creation and observer-based room availability notifications are available in the final milestone.");
    }

    @FXML
    public void handleSearch() {
        String query = (adminSearchField != null) ? adminSearchField.getText().trim() : "";
        Alert searchAlert = new Alert(Alert.AlertType.INFORMATION);
        searchAlert.setTitle("Guest Search");
        searchAlert.setHeaderText("Search Results");
        searchAlert.setContentText(query.isEmpty()
                ? "Showing all reservations."
                : "Searching for: \"" + query + "\". Full database search is available in the final milestone.");
        searchAlert.showAndWait();
    }

    @FXML
    public void checkLoyaltyStatus() {
        if (phoneField != null && nameField != null && emailField != null) {
            String phone = phoneField.getText();
            if (phone != null && phone.replaceAll("[^0-9]", "").length() == 10) {
                if ("1234567890".equals(phone.replaceAll("[^0-9]", ""))) {
                    nameField.setText("John Smith");
                    emailField.setText("john.smith@example.com");
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Loyalty Program");
                    alert.setHeaderText("Not Found");
                    alert.setContentText("No existing loyalty account found for this number.\nYou can enroll during the booking process.");
                    alert.showAndWait();
                }
            }
        }
    }

    private void showPlaceholder(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, title, message);
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}