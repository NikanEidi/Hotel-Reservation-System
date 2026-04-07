package com.seneca.hotelreservation_system.controller;

import com.seneca.hotelreservation_system.model.Feedback;
import com.seneca.hotelreservation_system.model.Reservation;
import com.seneca.hotelreservation_system.service.FeedbackService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FeedbackController {

    @FXML private TextField eligibleSearchField;
    @FXML private TextField feedbackSearchField;
    @FXML private ComboBox<String> sentimentFilterCombo;
    @FXML private ComboBox<Integer> minRatingCombo;
    @FXML private ComboBox<Integer> ratingCombo;
    @FXML private TextArea commentArea;
    @FXML private Label selectedReservationLabel;
    @FXML private Label submitStatusLabel;

    @FXML private TableView<Reservation> eligibleTable;
    @FXML private TableColumn<Reservation, String> eligibleRefColumn;
    @FXML private TableColumn<Reservation, String> eligibleGuestColumn;
    @FXML private TableColumn<Reservation, String> eligibleDateColumn;
    @FXML private TableColumn<Reservation, String> eligibleStatusColumn;

    @FXML private TableView<Feedback> feedbackTable;
    @FXML private TableColumn<Feedback, String> feedbackRefColumn;
    @FXML private TableColumn<Feedback, String> feedbackGuestColumn;
    @FXML private TableColumn<Feedback, String> feedbackRatingColumn;
    @FXML private TableColumn<Feedback, String> feedbackSentimentColumn;
    @FXML private TableColumn<Feedback, String> feedbackDateColumn;

    private final FeedbackService feedbackService = new FeedbackService();

    @FXML
    public void initialize() {
        if (sentimentFilterCombo != null) {
            sentimentFilterCombo.setItems(FXCollections.observableArrayList("ALL", "POSITIVE", "NEUTRAL", "NEGATIVE"));
            sentimentFilterCombo.setValue("ALL");
        }

        if (minRatingCombo != null) {
            minRatingCombo.setItems(FXCollections.observableArrayList(0, 1, 2, 3, 4, 5));
            minRatingCombo.setValue(0);
        }

        if (ratingCombo != null) {
            ratingCombo.setItems(FXCollections.observableArrayList(1, 2, 3, 4, 5));
            ratingCombo.setValue(5);
        }

        setupEligibleTable();
        setupFeedbackTable();
        loadEligibleReservations();
        loadFeedbackEntries();
        resetForm();
    }

    @FXML
    public void handleRefresh() {
        loadEligibleReservations();
        loadFeedbackEntries();
    }

    @FXML
    public void handleSubmitFeedback() {
        Reservation selected = getSelectedEligibleReservation();
        if (selected == null) {
            showError("Please select an eligible checked-out reservation.");
            return;
        }

        try {
            feedbackService.submitFeedback(
                    selected.getReservationId(),
                    ratingCombo.getValue(),
                    commentArea.getText()
            );

            submitStatusLabel.setText("Feedback submitted successfully.");
            loadEligibleReservations();
            loadFeedbackEntries();
            resetForm();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    @FXML
    public void goBack() throws IOException {
        Stage stage = (Stage) eligibleTable.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("/com/seneca/hotelreservation_system/view/admin-dashboard-view.fxml"));
        stage.setScene(new Scene(root, 1400, 900));
        stage.show();
    }

    private void setupEligibleTable() {
        eligibleRefColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(String.valueOf(cell.getValue().getReservationId())));
        eligibleGuestColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getGuest().getFullName()));
        eligibleDateColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getCheckInDate() + " to " + cell.getValue().getCheckOutDate()));
        eligibleStatusColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getStatus()));

        eligibleTable.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, selected) -> {
            if (selected != null) {
                selectedReservationLabel.setText("Selected Reservation: #" + selected.getReservationId() + " - " + selected.getGuest().getFullName());
            }
        });
    }

    private void setupFeedbackTable() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        feedbackRefColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(String.valueOf(cell.getValue().getReservation().getReservationId())));
        feedbackGuestColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getGuest().getFullName()));
        feedbackRatingColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(String.valueOf(cell.getValue().getRating())));
        feedbackSentimentColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getSentimentTag()));
        feedbackDateColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getSubmittedAt().format(formatter)));
    }

    private void loadEligibleReservations() {
        String query = eligibleSearchField == null ? "" : eligibleSearchField.getText().trim();
        List<Reservation> reservations = feedbackService.getEligibleReservations(query);
        eligibleTable.setItems(FXCollections.observableArrayList(reservations));
    }

    private void loadFeedbackEntries() {
        String query = feedbackSearchField == null ? "" : feedbackSearchField.getText().trim();
        String sentiment = sentimentFilterCombo == null ? "ALL" : sentimentFilterCombo.getValue();
        Integer minRating = minRatingCombo == null ? 0 : minRatingCombo.getValue();

        List<Feedback> entries = feedbackService.getFeedbackEntries(query, sentiment, minRating);
        feedbackTable.setItems(FXCollections.observableArrayList(entries));
    }

    private Reservation getSelectedEligibleReservation() {
        return eligibleTable == null ? null : eligibleTable.getSelectionModel().getSelectedItem();
    }

    private void resetForm() {
        if (ratingCombo != null) {
            ratingCombo.setValue(5);
        }
        if (commentArea != null) {
            commentArea.clear();
        }
        if (selectedReservationLabel != null) {
            selectedReservationLabel.setText("Selected Reservation: None");
        }
        if (submitStatusLabel != null) {
            submitStatusLabel.setText("");
        }
        if (eligibleTable != null) {
            eligibleTable.getSelectionModel().clearSelection();
        }
    }

    private void showError(String message) {
        if (submitStatusLabel != null) {
            submitStatusLabel.setText(message);
        }
    }
}