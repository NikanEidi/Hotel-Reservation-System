module com.seneca.hotelreservation_system {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.seneca.hotelreservation_system.controller to javafx.fxml;


    exports com.seneca.hotelreservation_system.app;
    exports com.seneca.hotelreservation_system.controller;
}