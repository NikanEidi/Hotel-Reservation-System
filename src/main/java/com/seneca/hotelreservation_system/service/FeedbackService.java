package com.seneca.hotelreservation_system.service;

import com.seneca.hotelreservation_system.model.Feedback;
import com.seneca.hotelreservation_system.model.Reservation;
import com.seneca.hotelreservation_system.util.JPAUtil;

import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;

public class FeedbackService {

    public List<Reservation> getEligibleReservations(String query) {
        if (!JPAUtil.isDatabaseAvailable()) {
            return Collections.emptyList();
        }

        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "select distinct r from Reservation r " +
                            "join fetch r.guest g " +
                            "left join fetch r.rooms rooms " +
                            "where upper(r.status) = 'CHECKED_OUT' " +
                            "and not exists (select f.feedbackId from Feedback f where f.reservation = r) "
            );

            if (query != null && !query.trim().isEmpty()) {
                jpql.append("and (lower(g.firstName) like :query or lower(g.lastName) like :query or lower(g.phone) like :query) ");
            }

            jpql.append("order by r.reservationId desc");

            var typedQuery = em.createQuery(jpql.toString(), Reservation.class);

            if (query != null && !query.trim().isEmpty()) {
                typedQuery.setParameter("query", "%" + query.trim().toLowerCase() + "%");
            }

            List<Reservation> reservations = typedQuery.getResultList();
            return reservations.stream().filter(this::isBalanceSettled).toList();
        } finally {
            em.close();
        }
    }

    public List<Feedback> getFeedbackEntries(String query, String sentiment, Integer minRating) {
        if (!JPAUtil.isDatabaseAvailable()) {
            return Collections.emptyList();
        }

        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                    "select f from Feedback f " +
                            "join fetch f.guest g " +
                            "join fetch f.reservation r " +
                            "where 1=1 "
            );

            if (query != null && !query.trim().isEmpty()) {
                jpql.append("and (lower(g.firstName) like :query or lower(g.lastName) like :query or cast(r.reservationId as string) like :query) ");
            }

            if (sentiment != null && !sentiment.trim().isEmpty() && !"ALL".equalsIgnoreCase(sentiment)) {
                jpql.append("and upper(f.sentimentTag) = :sentiment ");
            }

            if (minRating != null && minRating > 0) {
                jpql.append("and f.rating >= :minRating ");
            }

            jpql.append("order by f.submittedAt desc");

            var typedQuery = em.createQuery(jpql.toString(), Feedback.class);

            if (query != null && !query.trim().isEmpty()) {
                typedQuery.setParameter("query", "%" + query.trim().toLowerCase() + "%");
            }

            if (sentiment != null && !sentiment.trim().isEmpty() && !"ALL".equalsIgnoreCase(sentiment)) {
                typedQuery.setParameter("sentiment", sentiment.trim().toUpperCase());
            }

            if (minRating != null && minRating > 0) {
                typedQuery.setParameter("minRating", minRating);
            }

            return typedQuery.getResultList();
        } finally {
            em.close();
        }
    }

    public Feedback submitFeedback(Long reservationId, int rating, String comment) {
        if (reservationId == null) {
            throw new IllegalArgumentException("Please select a reservation.");
        }
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }
        if (comment == null || comment.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment is required.");
        }
        if (comment.trim().length() > 500) {
            throw new IllegalArgumentException("Comment must be 500 characters or less.");
        }

        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();

            Reservation reservation = em.find(Reservation.class, reservationId);
            if (reservation == null) {
                throw new IllegalArgumentException("Reservation not found.");
            }

            if (!"CHECKED_OUT".equalsIgnoreCase(reservation.getStatus())) {
                throw new IllegalArgumentException("Feedback can only be submitted after checkout.");
            }

            if (!isBalanceSettled(reservation)) {
                throw new IllegalArgumentException("Feedback can only be submitted after the balance is fully settled.");
            }

            Long existingCount = em.createQuery(
                    "select count(f) from Feedback f where f.reservation.reservationId = :reservationId",
                    Long.class
            ).setParameter("reservationId", reservationId).getSingleResult();

            if (existingCount != null && existingCount > 0) {
                throw new IllegalArgumentException("Feedback already exists for this reservation.");
            }

            Feedback feedback = new Feedback(
                    reservation,
                    reservation.getGuest(),
                    rating,
                    comment.trim(),
                    determineSentimentTag(rating, comment)
            );

            em.persist(feedback);
            em.getTransaction().commit();
            return feedback;
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    private boolean isBalanceSettled(Reservation reservation) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            BigDecimal totalPayments = em.createQuery(
                    "select coalesce(sum(p.amount), 0) from Payment p where p.reservation.reservationId = :reservationId",
                    BigDecimal.class
            ).setParameter("reservationId", reservation.getReservationId()).getSingleResult();

            BigDecimal paid = totalPayments == null ? BigDecimal.ZERO : totalPayments;
            BigDecimal due = reservation.getTotalAmount() == null ? BigDecimal.ZERO : reservation.getTotalAmount();
            BigDecimal balance = due.subtract(paid).setScale(2, RoundingMode.HALF_UP);
            return balance.compareTo(BigDecimal.ZERO) <= 0;
        } finally {
            em.close();
        }
    }

    private String determineSentimentTag(int rating, String comment) {
        String normalized = comment == null ? "" : comment.toLowerCase();

        if (rating >= 4) {
            return "POSITIVE";
        }
        if (rating <= 2) {
            return "NEGATIVE";
        }
        if (normalized.contains("slow") || normalized.contains("dirty") || normalized.contains("late")) {
            return "NEGATIVE";
        }
        return "NEUTRAL";
    }
}