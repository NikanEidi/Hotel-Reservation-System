package com.seneca.hotelreservation_system.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class HotelApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/seneca/hotelreservation_system/view/welcome-view.fxml"));

        Scene scene = new Scene(loader.load(), 1400, 900);

        stage.setTitle("Hotel Reservation System - Grand Plaza");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}