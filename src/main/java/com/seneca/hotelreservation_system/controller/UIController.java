package com.seneca.hotelreservation_system.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

public class UIController {

    @FXML private TextField adminSearchField;

    // Navigation methods - ActionEvent used here to identify the source stage.
    @FXML public void goToWelcome(ActionEvent event) throws IOException { switchScene(event, "/com/seneca/hotelreservation_system/view/welcome-view.fxml"); }
    @FXML public void goToRoomSelection(ActionEvent event) throws IOException { switchScene(event, "/com/seneca/hotelreservation_system/view/room-selection-view.fxml"); }
    @FXML public void goToGuestDetails(ActionEvent event) throws IOException { switchScene(event, "/com/seneca/hotelreservation_system/view/guest-details-view.fxml"); }
    @FXML public void goToAddons(ActionEvent event) throws IOException { switchScene(event, "/com/seneca/hotelreservation_system/view/addons-view.fxml"); }
    @FXML public void goToAdmin(ActionEvent event) throws IOException { switchScene(event, "/com/seneca/hotelreservation_system/view/admin-dashboard-view.fxml"); }


    @FXML
    public void showRules() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Grand Plaza Rules & Policies");
        alert.setHeaderText("Hotel Kiosk Regulations");
        alert.setContentText("""
                1. Check-in: 3:00 PM | Check-out: 11:00 AM
                2. Maximum occupancy per room must be respected.
                3. Cancellations must be made 24 hours in advance.
                """);
        alert.showAndWait();
    }


    @FXML
    public void handleSearch() {
        String query = (adminSearchField != null) ? adminSearchField.getText() : "None";
        System.out.println("Admin search triggered for: " + query);

        Alert searchAlert = new Alert(Alert.AlertType.INFORMATION);
        searchAlert.setTitle("Admin Search");
        searchAlert.setHeaderText("Searching Records");
        searchAlert.setContentText("Searching for guest: " + query);
        searchAlert.showAndWait();
    }

    private void switchScene(ActionEvent event, String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));

        Scene scene = new Scene(loader.load(), 1400, 900);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
}