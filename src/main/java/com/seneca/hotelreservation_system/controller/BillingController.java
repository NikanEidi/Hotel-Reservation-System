package com.seneca.hotelreservation_system.controller;

import com.seneca.hotelreservation_system.model.Payment;
import com.seneca.hotelreservation_system.model.Reservation;
import com.seneca.hotelreservation_system.service.BillingService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BillingController {

    @FXML private TextField reservationSearchField;
    @FXML private ComboBox<String> billingStatusFilterCombo;
    @FXML private ComboBox<String> paymentMethodCombo;
    @FXML private TextField amountField;
    @FXML private TextArea notesArea;

    @FXML private Label selectedReservationLabel;
    @FXML private Label balanceLabel;
    @FXML private Label totalPaidLabel;
    @FXML private Label billingStatusLabel;

    @FXML private TableView<Reservation> reservationTable;
    @FXML private TableColumn<Reservation, String> reservationRefColumn;
    @FXML private TableColumn<Reservation, String> reservationGuestColumn;
    @FXML private TableColumn<Reservation, String> reservationStatusColumn;
    @FXML private TableColumn<Reservation, String> reservationTotalColumn;
    @FXML private TableColumn<Reservation, String> reservationBalanceColumn;

    @FXML private TableView<Payment> paymentTable;
    @FXML private TableColumn<Payment, String> paymentIdColumn;
    @FXML private TableColumn<Payment, String> paymentDateColumn;
    @FXML private TableColumn<Payment, String> paymentMethodColumn;
    @FXML private TableColumn<Payment, String> paymentAmountColumn;
    @FXML private TableColumn<Payment, String> paymentRefundColumn;

    private final BillingService billingService = new BillingService();

    @FXML
    public void initialize() {
        if (billingStatusFilterCombo != null) {
            billingStatusFilterCombo.setItems(FXCollections.observableArrayList("ALL", "CONFIRMED", "CHECKED_OUT", "CANCELLED"));
            billingStatusFilterCombo.setValue("ALL");
        }

        if (paymentMethodCombo != null) {
            paymentMethodCombo.setItems(FXCollections.observableArrayList("CARD", "CASH", "LOYALTY"));
            paymentMethodCombo.setValue("CARD");
        }

        setupReservationTable();
        setupPaymentTable();
        loadReservations();
        clearBillingSummary();
    }

    @FXML
    public void handleRefresh() {
        loadReservations();
        loadPayments();
    }

    @FXML
    public void handleProcessPayment() {
        Reservation reservation = getSelectedReservation();
        if (reservation == null) {
            showStatus("Please select a reservation first.");
            return;
        }

        try {
            billingService.processPayment(
                    reservation.getReservationId(),
                    decimalValue(amountField),
                    comboValue(paymentMethodCombo),
                    notesArea == null ? "" : notesArea.getText()
            );

            showStatus("Payment recorded successfully.");
            loadReservations();
            loadPayments();
            clearEntryFields();
        } catch (Exception ex) {
            showStatus(ex.getMessage());
        }
    }

    @FXML
    public void handleProcessRefund() {
        Payment payment = getSelectedPayment();
        if (payment == null) {
            showStatus("Please select a payment row to refund.");
            return;
        }

        try {
            billingService.processRefund(
                    payment.getPaymentId(),
                    decimalValue(amountField),
                    notesArea == null ? "" : notesArea.getText()
            );

            showStatus("Refund recorded successfully.");
            loadReservations();
            loadPayments();
            clearEntryFields();
        } catch (Exception ex) {
            showStatus(ex.getMessage());
        }
    }

    @FXML
    public void goBack() throws IOException {
        Stage stage = (Stage) reservationTable.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("/com/seneca/hotelreservation_system/view/admin-dashboard-view.fxml"));
        stage.setScene(new Scene(root, 1400, 900));
        stage.show();
    }

    private void setupReservationTable() {
        reservationRefColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(String.valueOf(cell.getValue().getReservationId())));
        reservationGuestColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getGuest().getFullName()));
        reservationStatusColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getStatus()));
        reservationTotalColumn.setCellValueFactory(cell ->
                new SimpleStringProperty("$" + cell.getValue().getTotalAmount()));
        reservationBalanceColumn.setCellValueFactory(cell ->
                new SimpleStringProperty("$" + billingService.getBalance(cell.getValue())));

        reservationTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            if (selected != null) {
                updateBillingSummary(selected);
                loadPayments();
            }
        });
    }

    private void setupPaymentTable() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        paymentIdColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(String.valueOf(cell.getValue().getPaymentId())));
        paymentDateColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getPaymentDate().format(formatter)));
        paymentMethodColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getMethod()));
        paymentAmountColumn.setCellValueFactory(cell ->
                new SimpleStringProperty("$" + cell.getValue().getAmount()));
        paymentRefundColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().isRefunded() ? "YES" : "NO"));
    }

    private void loadReservations() {
        String query = reservationSearchField == null ? "" : reservationSearchField.getText().trim();
        String status = billingStatusFilterCombo == null ? "ALL" : billingStatusFilterCombo.getValue();
        List<Reservation> reservations = billingService.getReservations(query, status);
        reservationTable.setItems(FXCollections.observableArrayList(reservations));
    }

    private void loadPayments() {
        Reservation reservation = getSelectedReservation();
        if (reservation == null) {
            paymentTable.setItems(FXCollections.observableArrayList());
            return;
        }

        List<Payment> payments = billingService.getPaymentsForReservation(reservation.getReservationId());
        paymentTable.setItems(FXCollections.observableArrayList(payments));
        updateBillingSummary(reservation);
    }

    private Reservation getSelectedReservation() {
        return reservationTable == null ? null : reservationTable.getSelectionModel().getSelectedItem();
    }

    private Payment getSelectedPayment() {
        return paymentTable == null ? null : paymentTable.getSelectionModel().getSelectedItem();
    }

    private void updateBillingSummary(Reservation reservation) {
        if (reservation == null) {
            clearBillingSummary();
            return;
        }

        BigDecimal totalPaid = billingService.getTotalPaid(reservation.getReservationId());
        BigDecimal balance = billingService.getBalance(reservation);

        selectedReservationLabel.setText("Selected Reservation: #" + reservation.getReservationId() + " - " + reservation.getGuest().getFullName());
        totalPaidLabel.setText("Total Paid: $" + totalPaid);
        balanceLabel.setText("Outstanding Balance: $" + balance);
    }

    private void clearBillingSummary() {
        if (selectedReservationLabel != null) {
            selectedReservationLabel.setText("Selected Reservation: None");
        }
        if (totalPaidLabel != null) {
            totalPaidLabel.setText("Total Paid: $0.00");
        }
        if (balanceLabel != null) {
            balanceLabel.setText("Outstanding Balance: $0.00");
        }
        if (billingStatusLabel != null) {
            billingStatusLabel.setText("");
        }
    }

    private void clearEntryFields() {
        if (amountField != null) {
            amountField.clear();
        }
        if (notesArea != null) {
            notesArea.clear();
        }
        if (paymentMethodCombo != null) {
            paymentMethodCombo.setValue("CARD");
        }
    }

    private void showStatus(String message) {
        if (billingStatusLabel != null) {
            billingStatusLabel.setText(message);
        }
    }

    private BigDecimal decimalValue(TextField field) {
        String value = field == null ? "" : field.getText().trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Amount is required.");
        }

        try {
            return new BigDecimal(value);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Amount must be a valid number.");
        }
    }

    private String comboValue(ComboBox<String> combo) {
        return combo == null || combo.getValue() == null ? "" : combo.getValue().trim();
    }
}