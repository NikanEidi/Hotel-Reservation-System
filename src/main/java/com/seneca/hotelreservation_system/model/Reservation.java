package com.seneca.hotelreservation_system.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "reservation")
@SuppressWarnings({ "unused", "JpaDataSourceORMInspection" })
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long reservationId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "guest_id", nullable = false)
    private Guest guest;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "reservation_room", joinColumns = @JoinColumn(name = "reservation_id"), inverseJoinColumns = @JoinColumn(name = "room_id"))
    private List<Room> rooms;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @Column(name = "adult_count", nullable = false)
    private int adultCount;

    @Column(name = "child_count", nullable = false)
    private int childCount;

    @Column(name = "status", nullable = false)
    private String status;

    public Reservation() {
    }

    public Reservation(Guest guest, List<Room> rooms, LocalDate checkInDate, LocalDate checkOutDate, int adultCount,
                       int childCount) {
        this.guest = guest;
        this.rooms = rooms;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.adultCount = adultCount;
        this.childCount = childCount;
        this.status = "PENDING";
    }

    // Helper method for TableView in AdminController
    public String getGuestName() {
        return (guest != null) ? guest.getFirstName() + " " + guest.getLastName() : "Unknown Guest";
    }


    public double getTotalPrice() {

        return 150.0;
    }

    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }

    public Guest getGuest() { return guest; }
    public void setGuest(Guest guest) { this.guest = guest; }

    public List<Room> getRooms() { return rooms; }
    public void setRooms(List<Room> rooms) { this.rooms = rooms; }

    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }

    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }

    public int getAdultCount() { return adultCount; }
    public void setAdultCount(int adultCount) { this.adultCount = adultCount; }

    public int getChildCount() { return childCount; }
    public void setChildCount(int childCount) { this.childCount = childCount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}