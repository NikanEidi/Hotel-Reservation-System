package com.seneca.hotelreservation_system.model;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Column(name = "method", nullable = false, length = 30)
    private String method;

    @Column(name = "is_refunded", nullable = false)
    private boolean refunded;

    @Column(name = "notes", length = 255)
    private String notes;

    public Payment() {
    }

    public Payment(Reservation reservation, BigDecimal amount, String method) {
        this.reservation = reservation;
        this.amount = amount;
        this.method = method;
        this.paymentDate = LocalDateTime.now();
        this.refunded = false;
        this.notes = "";
    }

    public Payment(Reservation reservation, BigDecimal amount, String method, boolean refunded, String notes) {
        this.reservation = reservation;
        this.amount = amount;
        this.method = method;
        this.paymentDate = LocalDateTime.now();
        this.refunded = refunded;
        this.notes = notes == null ? "" : notes.trim();
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public boolean isRefunded() {
        return refunded;
    }

    public void setRefunded(boolean refunded) {
        this.refunded = refunded;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}