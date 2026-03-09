# Grand Plaza Hotel Reservation System
### Milestone 1 — Design & Blueprinting
**Seneca Polytechnic | Software Development Project**

---

## Table of Contents

1. [Project Overview & Scope](#1-project-overview--scope)
2. [Architecture & Design Artifacts](#2-architecture--design-artifacts)
3. [Core Domain Models](#3-core-domain-models-ms1-requirements)
4. [The 7-Screen Kiosk Flow](#4-the-7-screen-kiosk-flow)
5. [Admin Module Features](#5-admin-module-features)
6. [Design Patterns Applied](#6-design-patterns-applied)
7. [Execution Instructions](#7-execution-instructions)

---

## 1. Project Overview & Scope

The Grand Plaza Hotel Reservation System is a college-level software engineering project developed at Seneca Polytechnic. Its primary objective is to replace the hotel's existing manual, paper-based reservation workflow with a fully computerized, scalable JavaFX desktop application. Previously, front-desk staff managed all guest bookings, room assignments, payments, and loyalty tracking through physical ledgers and printed forms — a process prone to human error, data loss, and inefficiency during peak occupancy periods.

This system digitizes the entire reservation lifecycle, from initial room search through payment confirmation, while simultaneously providing administrative staff with real-time control over hotel operations.

The application is divided into two primary functional modules:

- **Self-Service Guest Kiosk** — A 7-screen guided interface that allows guests to independently search for available rooms, enter personal details, select add-on amenities, review a complete pricing summary, and finalize their reservation without staff intervention. The kiosk operates at a fixed resolution of 1400×900 pixels and is designed for touchscreen-friendly interaction.

- **Admin Dashboard** — A secured staff-facing portal enabling hotel administrators to manage guest records, review reservation statuses, process billing adjustments, monitor the waitlist, and access guest feedback. Access is protected through BCrypt-hashed credential verification.

**Technology Stack:**

| Component | Technology |
|---|---|
| Language | Java 17 |
| UI Framework | JavaFX |
| Build Tool | Apache Maven |
| ORM / Persistence | Hibernate / JPA |
| Database | H2 (Embedded) |
| UI Resolution | 1400 × 900 px |

---

## 2. Architecture & Design Artifacts

The system follows a strict **3-Tier Architecture**, separating concerns across the Presentation, Business, and Data layers. This enforces maintainability, testability, and scalability across the codebase.

```
com.grandplaza.hotel
│
├── presentation/          # Tier 1 — Presentation Layer
│   ├── views/             # FXML layout files for all screens
│   └── controllers/       # UIController classes bound to each FXML view
│
├── business/              # Tier 2 — Business / Service Layer
│   ├── BookingService.java
│   ├── PricingService.java
│   └── AuthService.java
│
├── data/                  # Tier 3 — Data / Persistence Layer
│   ├── entities/          # JPA-annotated domain model classes
│   └── repositories/      # Repository interfaces and implementations
│
└── util/                  # Shared utilities (e.g., JPAUtil singleton)
```

### Presentation Tier

The Presentation Tier is built entirely with JavaFX, using FXML files to define the visual layout of each screen and dedicated `UIController` classes to handle user input events. Each controller is responsible for exactly one screen and delegates all business decisions to the service layer — it contains no business logic of its own. This enforces a clean separation that allows the UI to be redesigned without impacting underlying functionality.

### Business Tier

The Business Tier encapsulates all application logic and acts as the intermediary between user-facing controllers and the database layer.

- **`BookingService`** — Orchestrates the end-to-end reservation process. It validates room availability based on requested dates and guest count, creates and persists `Reservation` entities, and applies loyalty point deductions where applicable. It also handles cancellation logic and triggers waitlist notifications through the Observer pattern.

- **`PricingService`** — Computes the final billable amount for a reservation. It applies the room's base nightly rate, multiplies by the number of nights, calculates the 13% tax, and applies any applicable loyalty discounts. Dynamic pricing strategies — such as weekend surcharges and seasonal rate adjustments — are injected via the Strategy pattern, allowing billing behaviour to be swapped without modifying the service class.

- **`AuthService`** — Manages administrator authentication. It retrieves hashed credentials from the database and uses BCrypt to verify password input before granting access to the Admin Dashboard.

### Data Tier

The Data Tier uses **Hibernate as the JPA provider** backed by an **H2 embedded database**, making the application self-contained and environment-independent during development. JPA-annotated entity classes map directly to database tables, and repository classes abstract all CRUD operations using the `EntityManager` API. The `EntityManagerFactory` is managed as a Singleton through `JPAUtil` to ensure a single shared connection pool across the application lifecycle.

---

## 3. Core Domain Models (MS1 Requirements)

The following entities represent the core domain model of the system. Each class is annotated with `@Entity` and managed by Hibernate under the `com.grandplaza.hotel.data.entities` package.

### `Guest`
Represents a registered or walk-in hotel guest.

| Attribute | Type | Description |
|---|---|---|
| `guestId` | `Long` | Auto-generated primary key |
| `firstName` | `String` | Guest's first name |
| `lastName` | `String` | Guest's last name |
| `email` | `String` | Contact email address |
| `phone` | `String` | Phone number (used for loyalty lookup) |
| `loyaltyPoints` | `int` | Accumulated reward points balance |

---

### `Room`
Represents a physical hotel room and its current availability state.

| Attribute | Type | Description |
|---|---|---|
| `roomId` | `Long` | Auto-generated primary key |
| `roomNumber` | `String` | Human-readable room identifier (e.g., `302A`) |
| `roomType` | `String` | Category: Standard, Deluxe, or Suite |
| `basePrice` | `double` | Nightly rate before tax and add-ons |
| `maxCapacity` | `int` | Maximum number of guests permitted |
| `isAvailable` | `boolean` | Real-time availability flag |

---

### `Reservation`
Links a `Guest` to a `Room` for a specified date range and tracks booking status.

| Attribute | Type | Description |
|---|---|---|
| `reservationId` | `Long` | Auto-generated primary key |
| `checkInDate` | `LocalDate` | Requested check-in date |
| `checkOutDate` | `LocalDate` | Requested check-out date |
| `adultCount` | `int` | Number of adult guests |
| `childCount` | `int` | Number of child guests |
| `status` | `String` | Booking state: `CONFIRMED`, `CANCELLED`, or `WAITLISTED` |

---

### `Payment`
Records the financial transaction associated with a confirmed reservation.

| Attribute | Type | Description |
|---|---|---|
| `paymentId` | `Long` | Auto-generated primary key |
| `amount` | `double` | Total charged amount including tax |
| `paymentDate` | `LocalDate` | Date the transaction was processed |
| `method` | `String` | Payment method: `CASH`, `CREDIT`, or `DEBIT` |
| `isRefunded` | `boolean` | Indicates whether a refund has been issued |

---

## 4. The 7-Screen Kiosk Flow

The Self-Service Kiosk guides guests through a linear, seven-step booking journey. Each screen maps to a dedicated FXML view and its corresponding `UIController`. Navigation is sequential, with validation checks at each step before the guest can proceed.

---

### Screen 1 — Welcome

The entry point of the kiosk experience. This screen displays the Grand Plaza Hotel branding and presents the guest with two options: begin a new reservation or view the hotel's **Rules & Policies**. The Rules & Policies panel opens as a modal overlay and covers cancellation terms, occupancy limits, check-in/check-out times, and pet policies. No data is collected on this screen.

---

### Screen 2 — Search

Guests select their desired **check-in date**, **check-out date**, **number of adults**, and **number of children** using date-picker controls and numeric steppers. Upon submission, `BookingService` queries the repository for all rooms where `isAvailable = true` and `maxCapacity >= (adultCount + childCount)` within the specified date range. If no rooms are available, the guest is offered the option to join the waitlist.

---

### Screen 3 — Room Selection

Displays all qualifying rooms as visual cards, each presenting the room number, type, a representative image, maximum capacity, and nightly base price. Rooms are grouped by category (Standard, Deluxe, Suite) and sorted by ascending price. Selecting a card highlights it and stores the chosen `Room` entity in the session state, enabling the next screen.

---

### Screen 4 — Guest Details

Collects the personal information required to create or retrieve a `Guest` record.

**Loyalty Auto-Fill Logic:** When a guest enters their phone number and focus leaves the field, `BookingService` performs a lookup against the `Guest` repository by phone number. If a match is found, the **First Name**, **Last Name**, and **Email** fields are automatically populated with the stored values, and the guest's current loyalty points balance is displayed. This behaviour is demonstrated with the test phone number `1234567890`, which resolves to a pre-seeded guest record in the H2 database.

New guests who do not have an existing record complete all fields manually, and a new `Guest` entity is persisted upon reservation confirmation.

---

### Screen 5 — Add-ons

Guests may optionally select from a curated list of amenities to enhance their stay. Each add-on is presented with a checkbox, a brief description, and its per-night cost. Selecting or deselecting any amenity immediately recalculates and updates the running price subtotal displayed at the bottom of the screen.

Available add-ons include:

| Add-on | Cost |
|---|---|
| High-Speed WiFi | $9.99 / night |
| Spa & Wellness Access | $49.99 / night |
| Airport Shuttle | $35.00 / trip |
| Daily Breakfast Package | $24.99 / night |
| Late Check-Out (2:00 PM) | $30.00 flat |

The cost of selected add-ons is applied using the **Decorator pattern**, wrapping the base billing object with successive cost layers before the final total is computed.

---

### Screen 6 — Summary

Presents a complete, itemized pricing breakdown before the guest commits to the reservation. No further input is required on this screen; its sole purpose is transparency and confirmation.

The summary includes:

- **Room:** Type, number, and nightly rate
- **Duration:** Number of nights
- **Subtotal:** Base room cost + add-on costs
- **Tax (13% HST):** Applied to the subtotal
- **Loyalty Discount:** Deducted if the guest has sufficient points and opts to redeem them
- **Total Owing:** Final payable amount

The guest may navigate back to any previous screen to adjust their selections. Proceeding from this screen triggers the persistence of the `Reservation` entity and moves to confirmation.

---

### Screen 7 — Confirmation

The final screen confirms that the reservation has been successfully created. It displays:

- A success status banner
- The system-generated **Reservation ID**
- The guest's name and selected room details
- Check-in and check-out dates
- Payment instructions (advising the guest to proceed to the front desk or designated payment terminal)

A **Print Receipt** button allows the guest to export a summary, and a **New Reservation** button resets the kiosk to Screen 1.

---

## 5. Admin Module Features

The Admin Dashboard is accessible exclusively to hotel staff following successful authentication. It provides a centralized interface for managing all operational aspects of the reservation system.

### Secure Login

Administrator login is handled by `AuthService`, which retrieves the stored BCrypt hash for the entered username from the database and uses the `BCrypt.checkpw()` method to verify the plaintext password input. No plain-text passwords are stored at any point. Failed login attempts are logged, and repeated failures lock the account for a configurable cooldown period.

### Management Dashboard

Once authenticated, staff have access to the following management panels:

- **Guest Search** — Locate any guest by name, phone number, email, or reservation ID. The detail view displays the guest's full profile, loyalty points balance, and complete reservation history.

- **Feedback Review** — View guest-submitted feedback entries associated with completed stays. Staff can mark items as reviewed or escalate them to management. Feedback is timestamped and linked to the originating reservation.

- **Billing Management** — Access the payment record for any reservation. Administrators can process manual payment entries, issue refunds (toggling `isRefunded` to `true` on the associated `Payment` entity), and generate billing summaries for specified date ranges.

- **Waitlist Management** — View the current waitlist queue of guests awaiting room availability. When a cancellation occurs, `BookingService` evaluates the waitlist in order of entry and triggers an availability notification to the next eligible guest via the Observer pattern. Administrators may also manually promote or remove waitlist entries.

---

## 6. Design Patterns Applied

The system integrates five Gang of Four (GoF) design patterns to address recurring architectural challenges. Each pattern is applied to a specific, well-defined problem within the codebase rather than adopted superficially.

### Singleton — `EntityManagerFactory`

**Problem:** Creating a new `EntityManagerFactory` on each database operation is computationally expensive and leads to connection pool exhaustion under concurrent use.

**Solution:** The `JPAUtil` class maintains a single static instance of `EntityManagerFactory`, initialized once when the application starts and shared across all repository classes for the application's lifetime. Thread-safe lazy initialization ensures the instance is only created when first requested.

```java
public class JPAUtil {
    private static EntityManagerFactory emf;

    public static synchronized EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("grandplaza-pu");
        }
        return emf;
    }
}
```

---

### Factory — Room Instantiation

**Problem:** Room objects are created in multiple contexts (seeding, booking, admin creation), and the construction logic — including type validation and default value assignment — must not be duplicated across calling code.

**Solution:** A `RoomFactory` class centralizes all `Room` object construction. Callers supply the room type as a string, and the factory returns a fully initialized `Room` instance with appropriate defaults (e.g., base price and capacity constraints per room category). This ensures all `Room` objects are created consistently and makes it trivial to introduce new room types without modifying any existing creation sites.

---

### Strategy — Dynamic Billing

**Problem:** The billing rate for a room varies depending on conditions such as weekend stays, holiday periods, and seasonal peaks. Embedding these rules directly in `PricingService` via conditional logic would make the class difficult to extend and impossible to test in isolation.

**Solution:** A `BillingStrategy` interface defines a single `calculateRate(Room room, LocalDate checkIn, LocalDate checkOut)` method. Concrete strategy implementations — `WeekendPricingStrategy` and `SeasonalPricingStrategy` — encapsulate their respective rules independently. `PricingService` holds a reference to the active `BillingStrategy`, which is injected at runtime, allowing pricing behaviour to be swapped or composed without altering the service class.

---

### Decorator — Amenity Cost Calculation

**Problem:** The total reservation cost is the sum of the base room price and a variable set of guest-selected add-ons. The combination of selected amenities differs per booking, making static subclassing impractical.

**Solution:** A `BillComponent` interface exposes a `getCost()` method. The base `RoomBill` class implements this interface and returns the room's nightly rate multiplied by the number of nights. Each amenity — `WiFiDecorator`, `SpaDecorator`, `BreakfastDecorator`, and so on — wraps an existing `BillComponent` and adds its own cost on top when `getCost()` is called. The final total is obtained by calling `getCost()` on the outermost decorator in the chain, producing the cumulative bill without any conditional logic.

---

### Observer — Waitlist & Availability Notifications

**Problem:** When a reservation is cancelled and a room becomes available, all guests currently on the waitlist for that room type need to be notified promptly. Polling-based checks would introduce unnecessary load and latency.

**Solution:** `BookingService` acts as the Subject (Observable) and maintains a list of registered `WaitlistObserver` instances. Each observer represents a waitlisted guest entry. When a cancellation triggers a room's `isAvailable` flag to return to `true`, `BookingService` calls `notifyObservers()`, iterating through all registered observers and invoking their `update()` method. The notification surfaces as an alert in the Admin Dashboard's Waitlist panel and, in a future milestone, may be extended to trigger an email notification.

---

## 7. Execution Instructions

Ensure the following prerequisites are met before running the application:

- **Java 17 JDK** installed and configured on `PATH`
- **Apache Maven 3.8+** installed and configured on `PATH`
- No external database configuration required — H2 runs embedded

### Step 1 — Clean and Compile

```bash
mvn clean compile
```

This command removes any previously compiled artifacts from the `target/` directory, resolves all declared Maven dependencies (including JavaFX modules, Hibernate, H2, and BCrypt), and compiles the full source tree. Verify that the build output ends with `BUILD SUCCESS` before proceeding.

### Step 2 — Run the Application

```bash
mvn javafx:run
```

This command launches the JavaFX application at the configured 1400×900 resolution using the `javafx-maven-plugin`. The application entry point is `com.grandplaza.hotel.MainApp`, which initializes `JPAUtil`, seeds the H2 database with sample data (including the test guest record for phone `1234567890`), and loads the Welcome screen as the primary stage.

---

*Grand Plaza Hotel Reservation System — Milestone 1 | Seneca Polytechnic*