# Grand Plaza Hotel Reservation System

## 1. Project Overview
The Grand Plaza Hotel Reservation System is a fully functional, desktop-based management application designed to transition manual hotel booking operations into a digital environment. It aims to eliminate physical paperwork, secure guest financial data via hash operations, and streamline the administration flow. 

### Key Features
- **Kiosk Mode Booking**: Interactive 4-step wizard allowing guests to select check-in dates, discover available rooms, attach add-ons (Breakfast, Parking), and finalize bookings.
- **Admin Dashboard**: Real-time analytical UI displaying active bookings, pending check-ins, and occupied rooms.
- **Secure Authentication**: Administrators authenticate using cryptographically hashed credentials (BCrypt).
- **Billing & Invoice Module**: Dynamically generated pricing models that instantly reflect in PDF exported invoices.
- **Waitlist Logic**: Built-in observation sequence tracking overflow requests when occupancy limits peak.

### Technologies Used
- **Core Strategy**: Java 17, JavaFX (Desktop UI)
- **Database & ORM**: MySQL 8.0, Hibernate JPA (5.6)
- **Security**: jBCrypt (Password Hashing)
- **Tooling**: Maven, OpenPDF

## 2. Architecture Summary
The application is governed by a strict 3-tier architecture separating the Presentation (UI), Business Logic (Services), and Data (Database/ORM) layers.

- **Presentation Layer (JavaFX)**: Divided into View FXMLs and Controllers using precise MVC routing logic. Components communicate dynamically using JavaFX Properties.
- **Business Logic Layer**: `BookingService` and `AuthService` isolate all domain instructions. They process state validation, handle logic calculation, and prevent the UI from executing raw SQL.
- **Data Access Layer / ORM**: The application utilizes standard `EntityManager` commands built on top of `Hibernate` mapping Entity records (Guest, Reservation, Room) directly to relational tables. 
- **Cross-Cutting Concerns**: A centralized exception catching system operates via custom Alert builders to intercept SQL constraint violations and UI layer errors dynamically.

## 3. Design Artifacts & Patterns
The codebase extensively enforces modern GoF patterns.
- **MVC (Model-View-Controller)**: Every FXML file operates securely behind designated controllers, shielding data models.
- **Singleton**: The `WaitlistManager` is enforced as a strict Singleton to guarantee thread-safe global queue injection no matter what GUI node triggers it.
- **Domain Mapping Object**: Standardized POJOs bound accurately to standard `@Table` and `@Entity` architectures.

## 4. UI/UX Journey
- Flow controls sequentially block users from progressing if guest identities aren't supplied or room capacities exceed.
- A newly integrated **Global Style** (CSS) breathes a modernized shadow-glow behavior into the UI frames, generating an interactive illusion for scaling elements mapping a seamless user experience.

## Installation & Deployment
1. Pull the package into locally configured IntelliJ settings.
2. Initialize MySQL Server utilizing port `3306` with an active schema named `hotel_reservation_db`. Update `hibernate.cfg.xml` username and password references matching your setup.
3. Establish the runtime build through Maven Wrappers:
   ```bash
   ./mvnw clean package
   ./mvnw javafx:run
   ```
4. Test the Kiosk. For administrative bypass, route to the top right lock and login with:
   - **User**: `nikan`
   - **Password**: `123456`

## Documentation & Code Delivery
Please observe the companion `ProjectDocumentation_GrandPlaza.docx` inside the `/docs/` repository branch acting as our official formal report wrapper.

---
**Developed By Students**: Nikan Eidi, nkhanal6, Yahya Osman
