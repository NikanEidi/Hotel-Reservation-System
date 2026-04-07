package com.seneca.hotelreservation_system.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@SuppressWarnings({ "unused", "SpellCheckingInspection" })
public class UIController {

    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger("HotelReservationSystem");

    static {
        try {
            java.util.logging.FileHandler fileHandler = new java.util.logging.FileHandler(
                    "system_logs.%g.log", 1024 * 1024, 10, true
            );
            java.util.logging.SimpleFormatter formatter = new java.util.logging.SimpleFormatter();
            fileHandler.setFormatter(formatter);
            LOGGER.addHandler(fileHandler);
            LOGGER.setUseParentHandlers(false);
        } catch (IOException e) {
            LOGGER.severe("Failed to initialize logger: " + e.getMessage());
        }
    }

    private void logKioskAction(String action, String details) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        LOGGER.info(String.format("[%s] KIOSK | %s | %s", timestamp, action, details));
    }

    public static class BookingData {
        public int adults = 2;
        public int children = 1;
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
        public int wifiQuantity = 0;
        public int breakfastQuantity = 0;
        public int parkingQuantity = 0;
        public int spaQuantity = 0;

        public boolean enrollLoyalty = true;
        public int loyaltyPointsToRedeem = 0;

        // MS3 Addition: Decorator pattern support
        public String addonsDescription = "";

        public int nights;
        public BigDecimal roomPrice;
        public BigDecimal addonsTotal;
        public BigDecimal subtotal;
        public BigDecimal tax;
        public BigDecimal total;

        public void setAddonsData(boolean wifi, boolean breakfast, boolean shuttle, boolean spa,
                                  int wifiQty, int breakfastQty, int shuttleQty, int spaQty) {
            booking.hasWifi = wifi;
            booking.hasBreakfast = breakfast;
            booking.hasParking = shuttle;  // shuttle stored in parking field
            booking.hasSpa = spa;
            booking.wifiQuantity = wifiQty;
            booking.breakfastQuantity = breakfastQty;
            booking.parkingQuantity = shuttleQty;
            booking.spaQuantity = spaQty;

            System.out.println("=== SETADDONSDATA CALLED ===");
            System.out.println("WiFi: " + wifi + " qty:" + wifiQty);
            System.out.println("Breakfast: " + breakfast + " qty:" + breakfastQty);
            System.out.println("Shuttle: " + shuttle + " qty:" + shuttleQty);
            System.out.println("Spa: " + spa + " qty:" + spaQty);
        }
    }

    private static BookingData booking = new BookingData();



    // ========== STRATEGY PATTERN FOR PRICING ==========
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

    // ========== PRICING HELPER METHODS ==========
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
        BigDecimal basePrice = getBasePrice(booking.selectedRoomType);
        PricingStrategy strategy = getPricingStrategy(booking.checkIn, booking.checkOut);
        BigDecimal perRoomTotal = strategy.calculatePrice(basePrice, booking.nights);
        return perRoomTotal.multiply(BigDecimal.valueOf(booking.roomQuantity));
    }

    public BigDecimal calculateAddonsTotal() {
        BigDecimal total = BigDecimal.ZERO;

        // WiFi: $10 per night per device
        if (booking.hasWifi && booking.wifiQuantity > 0) {
            total = total.add(BigDecimal.valueOf(10)
                    .multiply(BigDecimal.valueOf(booking.nights))
                    .multiply(BigDecimal.valueOf(booking.wifiQuantity)));
        }

        // Breakfast: $15 per night per person
        if (booking.hasBreakfast && booking.breakfastQuantity > 0) {
            total = total.add(BigDecimal.valueOf(15)
                    .multiply(BigDecimal.valueOf(booking.nights))
                    .multiply(BigDecimal.valueOf(booking.breakfastQuantity)));
        }

        // Shuttle: $35 per trip
        if (booking.hasParking && booking.parkingQuantity > 0) {
            total = total.add(BigDecimal.valueOf(35)
                    .multiply(BigDecimal.valueOf(booking.parkingQuantity)));
        }

        // Spa: $50 per session
        if (booking.hasSpa && booking.spaQuantity > 0) {
            total = total.add(BigDecimal.valueOf(50)
                    .multiply(BigDecimal.valueOf(booking.spaQuantity)));
        }

        System.out.println("Add-ons Total: $" + total);
        return total;
    }

    public BigDecimal calculateTotal() {
        booking.roomPrice = calculateRoomPrice();
        booking.addonsTotal = calculateAddonsTotal();
        booking.subtotal = booking.roomPrice.add(booking.addonsTotal);
        booking.tax = booking.subtotal.multiply(new BigDecimal("0.13"));  // 13% tax
        booking.total = booking.subtotal.add(booking.tax);

        System.out.println("=== CALCULATE TOTAL ===");
        System.out.println("Room Price: $" + booking.roomPrice);
        System.out.println("Add-ons Total: $" + booking.addonsTotal);
        System.out.println("Subtotal: $" + booking.subtotal);
        System.out.println("Tax (13%): $" + booking.tax);
        System.out.println("Total: $" + booking.total);

        return booking.total;
    }

    public BookingData getBookingData() { return booking; }

    public int getNights() {
        if (booking.checkIn != null && booking.checkOut != null) {
            booking.nights = (int) ChronoUnit.DAYS.between(booking.checkIn, booking.checkOut);
        }
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
    // ========== METHODS TO UPDATE BOOKING DATA ==========
    public void setSearchData(int adults, int children, LocalDate checkIn, LocalDate checkOut,
                              String name, String email, String phone) {
        booking.adults = adults;
        booking.children = children;
        booking.checkIn = checkIn;
        booking.checkOut = checkOut;
        booking.guestName = name;
        booking.guestEmail = email;
        booking.guestPhone = phone;
        if (checkIn != null && checkOut != null) {
            booking.nights = (int) ChronoUnit.DAYS.between(checkIn, checkOut);
        }
        logKioskAction("SEARCH", "Guest: " + name + ", " + adults + " adults, " + children + " children, " + checkIn + " to " + checkOut);
        System.out.println("Search Data Saved: " + name);
    }

    public void setRoomData(RoomType type, int quantity) {
        booking.selectedRoomType = type;
        booking.roomQuantity = quantity;
        logKioskAction("ROOM_SELECTION", type + " x" + quantity);
        System.out.println("Room Data Saved: " + type + " x" + quantity);
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
        logKioskAction("ADDONS", "WiFi:" + wifiQty + ", Breakfast:" + breakfastQty + ", Parking:" + parkingQty + ", Spa:" + spaQty);
        System.out.println("Add-ons Saved");
    }

    public void setLoyaltyData(boolean enroll, int pointsToRedeem) {
        booking.enrollLoyalty = enroll;
        booking.loyaltyPointsToRedeem = pointsToRedeem;
        logKioskAction("LOYALTY", "Enrolled: " + enroll + ", Points Redeemed: " + pointsToRedeem);
        System.out.println("Loyalty Saved");
    }

    // MS3 Addition: Decorator pattern support
    public void setAddonsDescription(String description) {
        booking.addonsDescription = description;
    }

    public String getAddonsDescription() {
        return booking.addonsDescription;
    }

    // ========== NAVIGATION METHODS ==========
    @FXML
    public void goToWelcome(ActionEvent event) throws IOException {
        logKioskAction("NAVIGATION", "Returning to Welcome screen - Booking reset");
        booking = new BookingData();
        switchScene(event, "/com/seneca/hotelreservation_system/view/welcome-view.fxml");
    }

    @FXML
    public void goToSearch(ActionEvent event) throws IOException {
        logKioskAction("NAVIGATION", "Moving to Search screen");
        switchScene(event, "/com/seneca/hotelreservation_system/view/search-view.fxml");
    }

    @FXML
    public void goToGuestDetails(ActionEvent event) throws IOException {
        logKioskAction("NAVIGATION", "Moving to Guest Details screen");
        switchScene(event, "/com/seneca/hotelreservation_system/view/guest-details-view.fxml");
    }

    @FXML
    public void goToRoomSelection(ActionEvent event) throws IOException {
        if (booking.checkIn != null && booking.checkOut != null) {
            booking.nights = (int) ChronoUnit.DAYS.between(booking.checkIn, booking.checkOut);
        }
        logKioskAction("NAVIGATION", "Moving to Room Selection screen - Nights: " + booking.nights);
        switchScene(event, "/com/seneca/hotelreservation_system/view/room-selection-view.fxml");
    }

    @FXML
    public void goToAddOns(ActionEvent event) throws IOException {
        logKioskAction("NAVIGATION", "Moving to Add-ons screen");
        switchScene(event, "/com/seneca/hotelreservation_system/view/addons-view.fxml");
    }

    @FXML
    public void goToLoyalty(ActionEvent event) throws IOException {
        logKioskAction("NAVIGATION", "Moving to Loyalty screen");
        switchScene(event, "/com/seneca/hotelreservation_system/view/loyalty-view.fxml");
    }

    @FXML
    public void goToSummary(ActionEvent event) throws IOException {
        calculateTotal();
        logKioskAction("NAVIGATION", "Moving to Summary screen - Total: $" + booking.total);
        switchScene(event, "/com/seneca/hotelreservation_system/view/summary-view.fxml");
    }

    @FXML
    public void goToConfirmation(ActionEvent event) throws IOException {
        calculateTotal();
        logKioskAction("RESERVATION_COMPLETE", "Guest: " + booking.guestName + ", Room: " + booking.selectedRoomType +
                ", Nights: " + booking.nights + ", Total: $" + booking.total);
        System.out.println("Reservation saved! Total: $" + booking.total);
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

    // ========== ROOM SELECTION BUTTON HANDLERS ==========
    @FXML
    public void selectStandardDouble(ActionEvent event) throws IOException {
        logKioskAction("ROOM_SELECTION", "Standard Double Suite selected");
        System.out.println("=== selectStandardDouble called ===");
        booking.selectedRoomType = RoomType.DOUBLE;
        booking.roomQuantity = 1;
        goToAddOns(event);
    }

    @FXML
    public void selectRoyalKing(ActionEvent event) throws IOException {
        logKioskAction("ROOM_SELECTION", "Royal King Suite selected");
        System.out.println("=== selectRoyalKing called ===");
        booking.selectedRoomType = RoomType.PENTHOUSE;
        booking.roomQuantity = 1;
        goToAddOns(event);
    }
    @FXML
    public void selectSingle(ActionEvent event) throws IOException {
        logKioskAction("ROOM_SELECTION", "Single Room selected");
        System.out.println("=== selectSingle called ===");
        booking.selectedRoomType = RoomType.SINGLE;
        booking.roomQuantity = 1;
        goToAddOns(event);
    }

    @FXML
    public void selectDeluxe(ActionEvent event) throws IOException {
        logKioskAction("ROOM_SELECTION", "Deluxe Room selected");
        System.out.println("=== selectDeluxe called ===");
        booking.selectedRoomType = RoomType.DELUXE;
        booking.roomQuantity = 1;
        goToAddOns(event);
    }

    // ========== HELPER METHODS ==========
    private void switchScene(ActionEvent event, String fxmlPath) throws IOException {
        URL resource = getClass().getResource(fxmlPath);

        if (resource == null) {
            LOGGER.severe("FXML file not found: " + fxmlPath);
            throw new IOException("FXML file not found: " + fxmlPath);
        }

        FXMLLoader loader = new FXMLLoader(resource);
        Parent root = loader.load();

        Object controller = loader.getController();
        System.out.println("=== SWITCH SCENE DEBUG ===");
        System.out.println("FXML: " + fxmlPath);
        System.out.println("Controller class: " + (controller != null ? controller.getClass().getSimpleName() : "NULL"));

        if (controller != null) {
            try {
                java.lang.reflect.Method method = controller.getClass().getMethod("setMainController", UIController.class);
                System.out.println("Found setMainController method in " + controller.getClass().getSimpleName());
                method.invoke(controller, this);
            } catch (NoSuchMethodException e) {
                System.out.println("ERROR: No setMainController method in " + controller.getClass().getSimpleName());
            } catch (Exception e) {
                System.out.println("ERROR calling setMainController: " + e.getMessage());
            }
        }

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 1400, 900));
        stage.show();
    }

    @FXML
    public void showRules() {
        logKioskAction("RULES_VIEWED", "Guest viewed hotel rules");
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

    // ========== EXISTING UI ELEMENTS ==========
    @FXML
    private TextField adminSearchField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;

    @FXML
    public void handleAdminLogin(ActionEvent event) throws IOException {
        String username = usernameField != null ? usernameField.getText().trim() : "";
        String password = passwordField != null ? passwordField.getText().trim() : "";

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(
                    Alert.AlertType.ERROR,
                    "Login Error",
                    "Missing Credentials",
                    "Please enter both username and password."
            );
            return;
        }

        if (username.equals("admin") && password.equals("admin123")) {
            LOGGER.info("Admin login successful: " + username);
            showAlert(
                    Alert.AlertType.INFORMATION,
                    "Login Successful",
                    "Welcome",
                    "Administrator access granted."
            );
            goToAdminDashboard(event);
        } else {
            LOGGER.warning("Failed admin login attempt: " + username);
            showAlert(
                    Alert.AlertType.ERROR,
                    "Login Failed",
                    "Invalid Credentials",
                    "Incorrect username or password."
            );
        }
    }

    @FXML
    public void showFeedbackModule() {
        showPlaceholder("Feedback", "Feedback page is part of later milestone functionality.");
    }

    @FXML
    public void showBillingModule() {
        showPlaceholder("Billing", "Billing page is part of later milestone functionality.");
    }

    @FXML
    public void showWaitlistModule() {
        showPlaceholder("Waitlist", "Waitlist page is part of later milestone functionality.");
    }

    @FXML
    public void handleSearch() {
        String query = (adminSearchField != null) ? adminSearchField.getText() : "None";
        LOGGER.info("Admin searched for: " + query);
        Alert searchAlert = new Alert(Alert.AlertType.INFORMATION);
        searchAlert.setTitle("Admin Search");
        searchAlert.setHeaderText("Searching Records");
        searchAlert.setContentText("Searching for guest: " + query);
        searchAlert.showAndWait();
    }

    @FXML
    public void checkLoyaltyStatus() {
        if (phoneField != null && nameField != null && emailField != null) {
            String phone = phoneField.getText();

            if (phone != null && phone.length() == 10) {
                if ("1234567890".equals(phone)) {
                    nameField.setText("Nikan Eidi");
                    emailField.setText("nikaneydi1984@gmail.com");
                    LOGGER.info("Loyalty lookup found: Nikan Eidi (phone: " + phone + ")");
                } else {
                    LOGGER.info("Loyalty lookup: No member found for phone: " + phone);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Loyalty Member");
                    alert.setHeaderText(null);
                    alert.setContentText("Would you like to become a Loyalty Member?");
                    alert.showAndWait();
                }
            }
        }
    }

    private void showPlaceholder(String title, String message) {
        showAlert(Alert.AlertType.INFORMATION, title, title + " Module", message);
    }

    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}