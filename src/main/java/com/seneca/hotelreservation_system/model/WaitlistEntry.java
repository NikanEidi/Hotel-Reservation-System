package com.seneca.hotelreservation_system.model;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "waitlist_entry")
@SuppressWarnings({ "unused", "JpaDataSourceORMInspection" })
public class WaitlistEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "waitlist_id")
    private Long waitlistId;

    @Column(name = "guest_name", nullable = false)
    private String guestName;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "desired_room_type", nullable = false)
    private String desiredRoomType;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @Column(name = "status", nullable = false)
    private String status = "WAITING";

    @Column(name = "notified", nullable = false)
    private boolean notified = false;

    public WaitlistEntry() {
    }

    public WaitlistEntry(String guestName, String phone, String desiredRoomType,
                         LocalDate checkInDate, LocalDate checkOutDate) {
        this.guestName = guestName;
        this.phone = phone;
        this.desiredRoomType = desiredRoomType;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.status = "WAITING";
        this.notified = false;
    }

    public Long getWaitlistId() {
        return waitlistId;
    }

    public void setWaitlistId(Long waitlistId) {
        this.waitlistId = waitlistId;
    }

    public String getGuestName() {
        return guestName;
    }

    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDesiredRoomType() {
        return desiredRoomType;
    }

    public void setDesiredRoomType(String desiredRoomType) {
        this.desiredRoomType = desiredRoomType;
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isNotified() {
        return notified;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }
}