package com.seneca.hotelreservation_system.controller;

import com.seneca.hotelreservation_system.model.Admin;
import com.seneca.hotelreservation_system.model.Reservation;
import com.seneca.hotelreservation_system.service.BookingService;
import com.seneca.hotelreservation_system.util.LoggerUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.PrintWriter;
import java.util.List;

public class AdminController {

    @FXML private TableView<Reservation> reservationTable;
    @FXML private TableColumn<Reservation, Long> colId;
    @FXML private TableColumn<Reservation, String> colGuest;
    @FXML private TableColumn<Reservation, String> colStatus;
    @FXML private TextField searchField;
    @FXML private Label adminNameLabel;
    @FXML private Label roleLabel;

    private final BookingService bookingService;
    private Admin currentAdmin;

    public AdminController() {
        this.bookingService = new BookingService();
    }

    public void setAdmin(Admin admin) {
        this.currentAdmin = admin;
        adminNameLabel.setText(admin.getUsername());
        roleLabel.setText(admin.getRole());
        LoggerUtil.logAction(admin.getUsername(), "LOGIN_SUCCESS", "ADMIN",
                admin.getAdminId().toString(), "Admin entered dashboard");
    }

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("reservationId"));
        colGuest.setCellValueFactory(new PropertyValueFactory<>("guestName"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        refreshTable();
    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String query = searchField.getText();
        List<Reservation> results = bookingService.searchReservations(query);
        reservationTable.setItems(FXCollections.observableArrayList(results));
        LoggerUtil.logAction(currentAdmin.getUsername(), "SEARCH", "RESERVATION",
                "QUERY:" + query, "Performed reservation search");
    }

    @FXML
    private void handleExportCSV(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("Reservation ID,Guest,Status,Amount");
                for (Reservation res : reservationTable.getItems()) {
                    writer.printf("%d,%s,%s,%.2f%n",
                            res.getReservationId(), res.getGuest().getFirstName(),
                            res.getStatus(), res.getTotalPrice());
                }
                LoggerUtil.logAction(currentAdmin.getUsername(), "EXPORT_CSV", "REPORT",
                        file.getName(), "Exported revenue report to CSV");
            } catch (Exception e) {
                LoggerUtil.logError("Failed to export CSV", e);
            }
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        LoggerUtil.logAction(currentAdmin.getUsername(), "LOGOUT", "ADMIN",
                currentAdmin.getAdminId().toString(), "Admin logged out");
        System.exit(0);
    }

    private void refreshTable() {
        List<Reservation> allReservations = bookingService.getAllReservations();
        reservationTable.setItems(FXCollections.observableArrayList(allReservations));
    }
}