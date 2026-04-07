package com.seneca.hotelreservation_system.service;

import com.seneca.hotelreservation_system.model.Payment;
import com.seneca.hotelreservation_system.model.Reservation;
import com.seneca.hotelreservation_system.util.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

public class BillingService {

    public List<Reservation> getReservations(String query, String status) {
        if (!JPAUtil.isDatabaseAvailable()) {
            return Collections.emptyList();
        }

        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "select distinct r from Reservation r " +
                            "join fetch r.guest g " +
                            "left join fetch r.rooms rooms " +
                            "where 1=1 "
            );

            if (query != null && !query.trim().isEmpty()) {
                jpql.append("and (lower(g.firstName) like :query or lower(g.lastName) like :query or lower(g.phone) like :query) ");
            }

            if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
                jpql.append("and upper(r.status) = :status ");
            }

            jpql.append("order by r.reservationId desc");

            TypedQuery<Reservation> typedQuery = em.createQuery(jpql.toString(), Reservation.class);

            if (query != null && !query.trim().isEmpty()) {
                typedQuery.setParameter("query", "%" + query.trim().toLowerCase() + "%");
            }

            if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
                typedQuery.setParameter("status", status.trim().toUpperCase());
            }

            return typedQuery.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Payment> getPaymentsForReservation(Long reservationId) {
        if (reservationId == null || !JPAUtil.isDatabaseAvailable()) {
            return Collections.emptyList();
        }

        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                    "select p from Payment p " +
                            "join fetch p.reservation r " +
                            "where r.reservationId = :reservationId " +
                            "order by p.paymentDate desc",
                    Payment.class
            ).setParameter("reservationId", reservationId).getResultList();
        } finally {
            em.close();
        }
    }

    public BigDecimal getBalance(Reservation reservation) {
        if (reservation == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalPaid = getTotalPaid(reservation.getReservationId());
        BigDecimal totalAmount = reservation.getTotalAmount() == null ? BigDecimal.ZERO : reservation.getTotalAmount();
        return totalAmount.subtract(totalPaid).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal getTotalPaid(Long reservationId) {
        if (reservationId == null || !JPAUtil.isDatabaseAvailable()) {
            return BigDecimal.ZERO;
        }

        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            BigDecimal total = em.createQuery(
                    "select coalesce(sum(p.amount), 0) from Payment p where p.reservation.reservationId = :reservationId",
                    BigDecimal.class
            ).setParameter("reservationId", reservationId).getSingleResult();

            return total == null ? BigDecimal.ZERO : total.setScale(2, RoundingMode.HALF_UP);
        } finally {
            em.close();
        }
    }

    public Payment processPayment(Long reservationId, BigDecimal amount, String method, String notes) {
        if (reservationId == null) {
            throw new IllegalArgumentException("Please select a reservation.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than 0.");
        }
        if (method == null || method.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method is required.");
        }

        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();

            Reservation reservation = em.find(Reservation.class, reservationId);
            if (reservation == null) {
                throw new IllegalArgumentException("Reservation not found.");
            }

            BigDecimal balance = getBalance(reservation);
            if (amount.compareTo(balance) > 0) {
                throw new IllegalArgumentException("Payment cannot exceed current balance of $" + balance);
            }

            Payment payment = new Payment(
                    reservation,
                    amount.setScale(2, RoundingMode.HALF_UP),
                    method.trim().toUpperCase(),
                    false,
                    notes
            );

            em.persist(payment);
            em.getTransaction().commit();
            return payment;
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public Payment processRefund(Long paymentId, BigDecimal refundAmount, String notes) {
        if (paymentId == null) {
            throw new IllegalArgumentException("Please select a payment to refund.");
        }
        if (refundAmount == null || refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Refund amount must be greater than 0.");
        }

        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();

            Payment originalPayment = em.find(Payment.class, paymentId);
            if (originalPayment == null) {
                throw new IllegalArgumentException("Payment not found.");
            }

            if (originalPayment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Refund cannot be created from an existing refund row.");
            }

            Payment refund = new Payment(
                    originalPayment.getReservation(),
                    refundAmount.negate().setScale(2, RoundingMode.HALF_UP),
                    originalPayment.getMethod(),
                    true,
                    notes == null || notes.isBlank() ? "Refund for payment #" + originalPayment.getPaymentId() : notes
            );

            em.persist(refund);
            em.getTransaction().commit();
            return refund;
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }
}