module com.seneca.hotelreservation_system {
    requires javafx.controls;
    requires javafx.fxml;


    requires java.persistence;
    requires org.hibernate.orm.core;


    requires java.sql;
    requires java.naming;

    requires jbcrypt;
    requires com.github.librepdf.openpdf;


    opens com.seneca.hotelreservation_system.controller to javafx.fxml;


    opens com.seneca.hotelreservation_system.model to org.hibernate.orm.core;

    exports com.seneca.hotelreservation_system.app;
    exports com.seneca.hotelreservation_system.controller;

    exports com.seneca.hotelreservation_system.model;
}