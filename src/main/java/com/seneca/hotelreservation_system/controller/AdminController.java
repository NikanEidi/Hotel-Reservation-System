package com.seneca.hotelreservation_system.controller;

import com.seneca.hotelreservation_system.model.AdminRole;
import com.seneca.hotelreservation_system.model.Reservation;
import com.seneca.hotelreservation_system.model.WaitlistEntry;
import com.seneca.hotelreservation_system.observer.AdminNotificationService;
import com.seneca.hotelreservation_system.observer.RoomAvailabilityObserver;
import com.seneca.hotelreservation_system.service.AuthService;
import com.seneca.hotelreservation_system.service.BookingService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class AdminController implements RoomAvailabilityObserver {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML private Label authenticatedLabel;
    @FXML private Label notificationLabel;
    @FXML private Label loyaltyPreviewLabel;

    @FXML private TextField adminSearchField;
    @FXML private ComboBox<String> statusFilterCombo;

    @FXML private TableView<Reservation> reservationTable;
    @FXML private TableColumn<Reservation, String> refIdColumn;
    @FXML private TableColumn<Reservation, String> guestNameColumn;
    @FXML private TableColumn<Reservation, String> checkInColumn;
    @FXML private TableColumn<Reservation, String> statusColumn;
    @FXML private TableColumn<Reservation, String> totalColumn;

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private DatePicker checkInPicker;
    @FXML private DatePicker checkOutPicker;
    @FXML private TextField adultsField;
    @FXML private TextField childrenField;
    @FXML private ComboBox<String> roomTypeCombo;
    @FXML private TextField roomQuantityField;
    @FXML private TextField discountField;
    @FXML private ComboBox<String> paymentMethodCombo;
    @FXML private CheckBox redeemLoyaltyCheck;

    @FXML private TableView<WaitlistEntry> waitlistTable;
    @FXML private TableColumn<WaitlistEntry, String> waitGuestColumn;
    @FXML private TableColumn<WaitlistEntry, String> waitRoomTypeColumn;
    @FXML private TableColumn<WaitlistEntry, String> waitDatesColumn;
    @FXML private TableColumn<WaitlistEntry, String> waitStatusColumn;

    private final AuthService authService = new AuthService();
    private BookingService bookingService;
    private final AdminNotificationService notificationService = AdminNotificationService.getInstance();

    @FXML
    public void initialize() {
        if (statusFilterCombo != null) {
            statusFilterCombo.setItems(FXCollections.observableArrayList("ALL", "CONFIRMED", "CHECKED_OUT", "CANCELLED"));
            statusFilterCombo.setValue("ALL");
        }

        if (roomTypeCombo != null) {
            roomTypeCombo.setItems(FXCollections.observableArrayList("SINGLE", "DOUBLE", "PENTHOUSE"));
            roomTypeCombo.setValue("SINGLE");
        }

        if (paymentMethodCombo != null) {
            paymentMethodCombo.setItems(FXCollections.observableArrayList("CARD", "CASH", "LOYALTY"));
            paymentMethodCombo.setValue("CARD");
        }

        setupReservationTable();
        setupWaitlistTable();
        updateAuthLabel();
        updateNotificationLabel();
        updateLoyaltyLabel();
        notificationService.addObserver(this);

        if (reservationTable != null) {
            tryInitializeBookingService();
            loadReservations();
            loadWaitlist();
        }
    }

    @FXML
    public void handleAdminLogin(ActionEvent event) throws IOException {
        String username = usernameField == null ? "" : usernameField.getText().trim();
        String password = passwordField == null ? "" : passwordField.getText().trim();

        AuthService.AdminSession session = authService.login(username, password);
        if (session == null) {
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid credentials.");
            return;
        }

        switchScene(event, "/com/seneca/hotelreservation_system/view/admin-dashboard-view.fxml");
    }

    @FXML
    public void goToWelcome(ActionEvent event) throws IOException {
        authService.logout();
        switchScene(event, "/com/seneca/hotelreservation_system/view/welcome-view.fxml");
    }

    @FXML
    public void handleSearch() {
        if (!ensureServiceAvailable()) return;
        loadReservations();
    }

    @FXML
    public void handleCreateReservation() {
        if (!ensureServiceAvailable()) return;

        try {
            Reservation reservation = bookingService.createReservation(
                    value(firstNameField),
                    value(lastNameField),
                    value(emailField),
                    value(phoneField),
                    dateValue(checkInPicker),
                    dateValue(checkOutPicker),
                    intValue(adultsField, "Adults"),
                    intValue(childrenField, "Children"),
                    comboValue(roomTypeCombo),
                    intValue(roomQuantityField, "Room quantity"),
                    decimalValue(discountField),
                    redeemLoyaltyCheck != null && redeemLoyaltyCheck.isSelected(),
                    currentRole()
            );

            showAlert(Alert.AlertType.INFORMATION, "Success", "Reservation #" + reservation.getReservationId() + " created.");
            clearForm();
            loadReservations();
            loadWaitlist();
            updateNotificationLabel();
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Create Failed", ex.getMessage());
            loadWaitlist();
            updateNotificationLabel();
        }
    }

    @FXML
    public void handleUpdateReservation() {
        if (!ensureServiceAvailable()) return;

        Reservation selected = getSelectedReservation();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Select a reservation first.");
            return;
        }

        try {
            bookingService.updateReservation(
                    selected.getReservationId(),
                    value(firstNameField),
                    value(lastNameField),
                    value(emailField),
                    value(phoneField),
                    dateValue(checkInPicker),
                    dateValue(checkOutPicker),
                    intValue(adultsField, "Adults"),
                    intValue(childrenField, "Children"),
                    comboValue(roomTypeCombo),
                    intValue(roomQuantityField, "Room quantity"),
                    decimalValue(discountField),
                    redeemLoyaltyCheck != null && redeemLoyaltyCheck.isSelected(),
                    currentRole()
            );

            showAlert(Alert.AlertType.INFORMATION, "Success", "Reservation updated.");
            clearForm();
            loadReservations();
            loadWaitlist();
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Update Failed", ex.getMessage());
        }
    }

    @FXML
    public void handleCancelReservation() {
        if (!ensureServiceAvailable()) return;

        Reservation selected = getSelectedReservation();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Select a reservation first.");
            return;
        }

        try {
            bookingService.cancelReservation(selected.getReservationId());
            showAlert(Alert.AlertType.INFORMATION, "Success", "Reservation cancelled.");
            clearForm();
            loadReservations();
            loadWaitlist();
            updateNotificationLabel();
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Cancel Failed", ex.getMessage());
        }
    }

    @FXML
    public void handleCheckoutReservation() {
        if (!ensureServiceAvailable()) return;

        Reservation selected = getSelectedReservation();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection", "Select a reservation first.");
            return;
        }

        try {
            bookingService.checkoutReservation(
                    selected.getReservationId(),
                    comboValue(paymentMethodCombo),
                    decimalValue(discountField),
                    currentRole()
            );

            showAlert(Alert.AlertType.INFORMATION, "Success", "Checkout completed. Loyalty points earned.");
            clearForm();
            loadReservations();
            loadWaitlist();
            updateNotificationLabel();
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Checkout Failed", ex.getMessage());
        }
    }

    @FXML
    public void handleRefreshData() {
        if (!ensureServiceAvailable()) return;
        loadReservations();
        loadWaitlist();
        updateNotificationLabel();
    }

    @FXML
    public void handleClearForm() {
        clearForm();
    }

    @FXML
    public void showFeedbackModule(ActionEvent event) throws IOException {
        switchScene(event, "/com/seneca/hotelreservation_system/view/feedback-view.fxml");
    }

    @FXML
    public void showBillingModule(ActionEvent event) throws IOException {
        switchScene(event, "/com/seneca/hotelreservation_system/view/billing-view.fxml");
    }

    @FXML
    public void showWaitlistModule() {
        if (!ensureServiceAvailable()) return;
        loadWaitlist();
        showAlert(Alert.AlertType.INFORMATION, "Waitlist", "Waitlist refreshed.");
    }

    @FXML
    public void previewLoyaltyImpact() {
        updateLoyaltyLabel();
    }

    @Override
    public void onRoomAvailabilityChange(String message) {
        updateNotificationLabel();
    }

    private void tryInitializeBookingService() {
        try {
            bookingService = new BookingService();
            bookingService.initializeDatabase();
        } catch (Exception ex) {
            bookingService = null;
            if (notificationLabel != null) {
                notificationLabel.setText("Notification: Database unavailable.");
            }
        }
    }

    private boolean ensureServiceAvailable() {
        if (bookingService == null) {
            tryInitializeBookingService();
        }
        if (bookingService == null) {
            showAlert(Alert.AlertType.ERROR, "Database Error", "Database connection is not available.");
            return false;
        }
        return true;
    }

    private void setupReservationTable() {
        if (refIdColumn == null) return;

        refIdColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(String.valueOf(cell.getValue().getReservationId())));
        guestNameColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getGuest().getFirstName() + " " + cell.getValue().getGuest().getLastName()));
        checkInColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(String.valueOf(cell.getValue().getCheckInDate())));
        statusColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getStatus()));
        totalColumn.setCellValueFactory(cell ->
                new SimpleStringProperty("$" + bookingService.calculateReservationDisplayTotal(cell.getValue())));

        reservationTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            if (selected != null) {
                populateForm(selected);
            }
        });
    }

    private void setupWaitlistTable() {
        if (waitGuestColumn == null) return;

        waitGuestColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getGuestName()));
        waitRoomTypeColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getDesiredRoomType()));
        waitDatesColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getCheckInDate() + " to " + cell.getValue().getCheckOutDate()));
        waitStatusColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getStatus()));
    }

    private void loadReservations() {
        if (reservationTable == null || bookingService == null) return;

        String query = adminSearchField == null ? "" : adminSearchField.getText().trim();
        String status = statusFilterCombo == null ? "ALL" : statusFilterCombo.getValue();

        List<Reservation> reservations = bookingService.searchReservations(query, status);
        reservationTable.setItems(FXCollections.observableArrayList(reservations));
    }

    private void loadWaitlist() {
        if (waitlistTable == null || bookingService == null) return;
        waitlistTable.setItems(FXCollections.observableArrayList(bookingService.getWaitlistEntries()));
    }

    private void populateForm(Reservation reservation) {
        if (reservation == null) return;

        setText(firstNameField, reservation.getGuest().getFirstName());
        setText(lastNameField, reservation.getGuest().getLastName());
        setText(emailField, reservation.getGuest().getEmail());
        setText(phoneField, reservation.getGuest().getPhone());

        if (checkInPicker != null) checkInPicker.setValue(reservation.getCheckInDate());
        if (checkOutPicker != null) checkOutPicker.setValue(reservation.getCheckOutDate());

        setText(adultsField, String.valueOf(reservation.getAdultCount()));
        setText(childrenField, String.valueOf(reservation.getChildCount()));
        setText(roomQuantityField, String.valueOf(reservation.getRooms() == null ? 0 : reservation.getRooms().size()));
        setText(discountField, reservation.getDiscountPercent() == null ? "0" : reservation.getDiscountPercent().stripTrailingZeros().toPlainString());

        if (roomTypeCombo != null && reservation.getRooms() != null && !reservation.getRooms().isEmpty()) {
            roomTypeCombo.setValue(reservation.getRooms().get(0).getRoomType().toUpperCase());
        }

        if (redeemLoyaltyCheck != null) {
            redeemLoyaltyCheck.setSelected(reservation.getLoyaltyPointsRedeemed() > 0);
        }

        updateLoyaltyLabel(reservation);
    }

    private void updateLoyaltyLabel() {
        if (loyaltyPreviewLabel == null) return;

        if (redeemLoyaltyCheck != null && redeemLoyaltyCheck.isSelected()) {
            loyaltyPreviewLabel.setText("Loyalty redemption selected.");
        } else {
            loyaltyPreviewLabel.setText("Loyalty redemption not selected.");
        }
    }

    private void updateLoyaltyLabel(Reservation reservation) {
        if (loyaltyPreviewLabel == null || reservation == null) return;

        loyaltyPreviewLabel.setText(
                "Guest points: " + reservation.getGuest().getLoyaltyPoints() +
                        " | Redeemed: " + reservation.getLoyaltyPointsRedeemed()
        );
    }

    private Reservation getSelectedReservation() {
        return reservationTable == null ? null : reservationTable.getSelectionModel().getSelectedItem();
    }

    private void clearForm() {
        setText(firstNameField, "");
        setText(lastNameField, "");
        setText(emailField, "");
        setText(phoneField, "");
        setText(adultsField, "1");
        setText(childrenField, "0");
        setText(roomQuantityField, "1");
        setText(discountField, "0");

        if (checkInPicker != null) checkInPicker.setValue(null);
        if (checkOutPicker != null) checkOutPicker.setValue(null);
        if (roomTypeCombo != null) roomTypeCombo.setValue("SINGLE");
        if (paymentMethodCombo != null) paymentMethodCombo.setValue("CARD");
        if (redeemLoyaltyCheck != null) redeemLoyaltyCheck.setSelected(false);
        if (reservationTable != null) reservationTable.getSelectionModel().clearSelection();

        updateLoyaltyLabel();
    }

    private void updateAuthLabel() {
        if (authenticatedLabel == null) return;

        AuthService.AdminSession session = authService.getCurrentSession();
        if (session == null) {
            authenticatedLabel.setText("Authenticated: Not Logged In");
        } else {
            authenticatedLabel.setText("Authenticated: " + session.getUsername() + " (" + session.getRole() + ")");
        }
    }

    private void updateNotificationLabel() {
        if (notificationLabel != null) {
            notificationLabel.setText("Notification: " + notificationService.getLatestNotification());
        }
    }

    private AdminRole currentRole() {
        AuthService.AdminSession session = authService.getCurrentSession();
        return session == null ? AdminRole.ADMIN : session.getRole();
    }

    private String value(TextField field) {
        return field == null ? "" : field.getText().trim();
    }

    private LocalDate dateValue(DatePicker picker) {
        return picker == null ? null : picker.getValue();
    }

    private String comboValue(ComboBox<String> combo) {
        return combo == null || combo.getValue() == null ? "" : combo.getValue().trim();
    }

    private int intValue(TextField field, String label) {
        try {
            return Integer.parseInt(value(field));
        } catch (Exception ex) {
            throw new IllegalArgumentException(label + " must be a valid number.");
        }
    }

    private BigDecimal decimalValue(TextField field) {
        String value = value(field);
        if (value.isBlank()) {
            return BigDecimal.ZERO;
        }

        try {
            return new BigDecimal(value);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Discount must be a valid number.");
        }
    }

    private void setText(TextField field, String value) {
        if (field != null) {
            field.setText(value);
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void switchScene(ActionEvent event, String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root, 1400, 900));
        stage.show();
    }
}