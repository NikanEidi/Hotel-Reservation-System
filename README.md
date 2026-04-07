Grand Plaza Hotel - Reservation System
A self-service kiosk and administration portal for the Grand Plaza Hotel in Toronto, Ontario. The system digitizes the hotel reservation process, replacing a manual paper-based workflow with a JavaFX desktop application backed by a MySQL database.

This project was developed for Seneca College APD course, Milestone 3.


Overview

The application serves two types of users. Guests interact with a self-service kiosk to create reservations, select rooms, add services, enroll in the loyalty program, and receive a booking confirmation. Staff access an administration portal to view reservations, search guest records, and manage the hotel waitlist and billing.

The system implements a full three-tier architecture with a JavaFX presentation layer, a service-oriented business logic layer, and a JPA-based persistence layer connected to a MySQL database.


Features

Self-service kiosk with a guided multi-step reservation flow
Seven room types managed by a factory pattern: Single, Double, Deluxe, and Penthouse
Occupancy validation enforcing maximum guest limits per room type
Dynamic pricing with Standard, Weekend, and Peak Season strategies
Decorator-based add-on services including WiFi, Breakfast, Airport Shuttle, and Spa
Loyalty program enrollment with point tracking
Booking summary with CSV and plain-text export
Administrator login with BCrypt password authentication
Admin dashboard with reservation search and management modules
JPA and Hibernate ORM for all database operations
Handler-based audit logging with 1MB rotation and 10-file limit


Technology Stack

Language: Java 17
UI Framework: JavaFX 17 with FXML
Database: MySQL 8
ORM: Hibernate 5.6 with JPA 2.2
Security: jBCrypt 0.4
Export: OpenCSV 5.8, OpenPDF 1.3
Build: Apache Maven via Maven Wrapper
JDK: GraalVM CE 25 (compatible with Java 17)


Architecture

The application follows a three-tier MVC pattern.

Presentation Layer: FXML view files define the UI structure. Each view has a corresponding controller class that handles user events, validates input, and delegates to the service layer. Navigation between views is managed by UIController, which also holds the BookingData session object.

Business Logic Layer: Service classes (AuthService, BookingService, PricingService) contain the business rules. Design patterns are used throughout: Strategy for pricing, Decorator for add-on billing, Factory for room creation, Observer for waitlist notifications, and Singleton for the JPA entity manager factory.

Data Layer: JPA entities (Guest, Reservation, Room, Payment, Admin) are mapped to MySQL tables using Hibernate annotations. JPAUtil implements the Singleton pattern to manage the EntityManagerFactory lifecycle. The persistence unit is defined in META-INF/persistence.xml.


Prerequisites

Java 17 or higher (GraalVM or OpenJDK)
MySQL 8 running locally on port 3306
Maven (or use the included Maven Wrapper)


Database Setup

Create a MySQL user with access to the application database, or use the root account. The database and tables are created automatically by Hibernate on first launch.

Update the connection settings in src/main/resources/META-INF/persistence.xml to match your MySQL credentials.

Default connection:
  URL: jdbc:mysql://localhost:3306/hotel_reservation_db
  Username: root
  Password: (your MySQL root password)


Running the Application

./mvnw compile
./mvnw exec:java

Or run directly from IntelliJ IDEA using the Launcher class as the main entry point.

The application will connect to MySQL, create tables if they do not exist, register the default admin user, and launch the JavaFX kiosk UI.


Default Credentials

Admin username: nikan
Admin password: 123456

These credentials are registered automatically on first launch if no admin record exists.


Project Structure

src/main/java/com/seneca/hotelreservation_system/
  app/          Application entry point and JavaFX bootstrap
  controller/   FXML controllers for all views
  model/        JPA entity classes
  service/      Business logic and authentication
  util/         JPAUtil singleton, LoggerUtil handler
  security/     Reserved for future security utilities
  repository/   Reserved for future DAO implementations

src/main/resources/com/seneca/hotelreservation_system/
  view/         FXML layout files for all kiosk and admin screens

src/main/resources/META-INF/
  persistence.xml  JPA configuration and entity registration


Kiosk Navigation Flow

Welcome - Begin a reservation or access the admin login
Search - Enter dates, adult and children count, and guest information
Room Selection - Choose from Single, Double, Deluxe, or Penthouse rooms with quantity selection and occupancy validation
Add-ons - Select optional services with per-unit quantity spinners
Loyalty - Enroll in the Grand Plaza loyalty rewards program
Summary - Review the full booking and pricing breakdown, export a report
Confirmation - Receive the booking reference and proceed to front desk payment


Business Rules

Occupancy: Single, Deluxe, and Penthouse rooms accept a maximum of 2 guests. Double rooms accept a maximum of 4 guests. The room selection screen enforces this with inline validation.

Pricing: Standard rate applies by default. Weekend pricing adds 20 percent. Peak season pricing (July, August, December) adds 30 percent. The applicable rate is determined from the selected check-in and check-out dates.

Tax: 13 percent HST is applied to the subtotal of room and add-on charges.

Loyalty: Guests earn 1 point per $10 spent. 100 points equal a $10 discount. Points are tracked per phone number.


Design Patterns Used

Singleton: JPAUtil.getEntityManagerFactory() returns a single shared instance of EntityManagerFactory throughout the application lifecycle.

Factory: RoomFactory creates Room subclass instances (SingleRoom, DoubleRoom, PenthouseRoom) based on the selected room type.

Strategy: UIController.PricingStrategy interface is implemented by StandardPricing, WeekendPricing, and PeakSeasonPricing. The correct strategy is selected at runtime based on dates.

Decorator: AddOnsController.BillComponent interface wraps the base room cost with optional add-on charges. Each add-on is a separate decorator class that adds its cost to the total.

Observer: Planned for the waitlist notification system. When a room becomes available, registered observers (waiting guests) are notified.


Security and Logging

Admin passwords are hashed using BCrypt before being stored in the database. Authentication verifies the plain-text input against the stored hash.

The logging system uses java.util.logging.FileHandler with a rotating file pattern (system_logs.%g.log). Each log file is limited to 1 MB. Up to 10 rotated files are retained. Log entries include timestamps, severity levels, and source class references.


Export

From the booking summary screen, guests can export their reservation details in two formats:

CSV: A structured comma-separated file with all booking fields grouped by section.
PDF: A plain-text formatted document styled as a formal hotel receipt.

Both formats are saved to a location chosen by the guest using a file chooser dialog.


Version History

v1.0.0  Initial milestone release with full kiosk flow and admin dashboard
v1.1.0  UI redesign with dark luxury theme and dynamic pricing
v1.2.0  Full FXML and controller synchronization, bug fixes, documentation


License

This project was developed for academic purposes at Seneca College. Not for commercial distribution.