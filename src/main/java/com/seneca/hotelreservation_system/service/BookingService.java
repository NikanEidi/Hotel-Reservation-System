package com.seneca.hotelreservation_system.service;

import com.seneca.hotelreservation_system.model.Guest;
import com.seneca.hotelreservation_system.model.Reservation;
import com.seneca.hotelreservation_system.util.JPAUtil;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

public class BookingService {

    public List<Reservation> getAllReservations() {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Reservation> query = em.createQuery("SELECT r FROM Reservation r", Reservation.class);
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public List<Reservation> searchReservations(String keyword) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            TypedQuery<Reservation> query = em.createQuery(
                    "SELECT r FROM Reservation r WHERE r.guest.firstName LIKE :kw " +
                            "OR r.guest.lastName LIKE :kw OR r.status LIKE :kw",
                    Reservation.class);
            query.setParameter("kw", "%" + keyword + "%");
            return query.getResultList();
        } finally {
            em.close();
        }
    }

    public void updateReservationStatus(Long reservationId, String newStatus) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            Reservation reservation = em.find(Reservation.class, reservationId);
            if (reservation != null) {
                reservation.setStatus(newStatus);
            }
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
    
    public void saveReservation(Guest guest, Reservation reservation) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();
            
            // Check if guest exists by email
            TypedQuery<Guest> gq = em.createQuery("SELECT g FROM Guest g WHERE g.email = :e", Guest.class);
            gq.setParameter("e", guest.getEmail());
            List<Guest> existingGuests = gq.getResultList();
            
            Guest persistedGuest;
            if (!existingGuests.isEmpty()) {
                persistedGuest = existingGuests.get(0);
                // Update properties
                persistedGuest.setFirstName(guest.getFirstName());
                persistedGuest.setLastName(guest.getLastName());
                persistedGuest.setPhone(guest.getPhone());
                if (guest.getLoyaltyPoints() > 0) {
                    persistedGuest.setLoyaltyPoints(persistedGuest.getLoyaltyPoints() + guest.getLoyaltyPoints());
                }
                em.merge(persistedGuest);
            } else {
                em.persist(guest);
                persistedGuest = guest;
            }
            
            reservation.setGuest(persistedGuest);
            em.persist(reservation);
            
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
    
    public Object[] getDashboardStatistics() {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            Long totalRes = em.createQuery("SELECT COUNT(r) FROM Reservation r", Long.class).getSingleResult();
            if (totalRes == 0) {
                seedMockData(em);
                totalRes = em.createQuery("SELECT COUNT(r) FROM Reservation r", Long.class).getSingleResult();
            }
            Long roomsOcc = em.createQuery("SELECT COUNT(r) FROM Reservation r WHERE r.status = 'ACTIVE'", Long.class).getSingleResult();
            Long pending = em.createQuery("SELECT COUNT(r) FROM Reservation r WHERE r.status = 'PENDING'", Long.class).getSingleResult();
            
            Long waitlistCount = (long) com.seneca.hotelreservation_system.model.WaitlistManager.getInstance().getWaitlist().size();
            
            return new Object[]{totalRes, roomsOcc, pending, waitlistCount};
        } catch (Exception e) {
            e.printStackTrace();
            return new Object[]{0L, 0L, 0L, 0L};
        } finally {
            em.close();
        }
    }

    private void seedMockData(EntityManager em) {
        em.getTransaction().begin();
        for (int i = 1; i <= 5; i++) {
            com.seneca.hotelreservation_system.model.Guest g = new com.seneca.hotelreservation_system.model.Guest();
            g.setFirstName("Demo");
            g.setLastName("Guest " + i);
            g.setEmail("guest" + i + "@demo.com");
            g.setPhone("555-000" + i);
            g.setLoyaltyPoints(100 * i);
            em.persist(g);
            
            com.seneca.hotelreservation_system.model.Reservation r = new com.seneca.hotelreservation_system.model.Reservation();
            r.setGuest(g);
            r.setCheckInDate(java.time.LocalDate.now().plusDays(i));
            r.setCheckOutDate(java.time.LocalDate.now().plusDays(i + 2));
            r.setAdultCount(2);
            r.setChildCount(0);
            r.setStatus(i % 2 == 0 ? "ACTIVE" : "PENDING");
            em.persist(r);
        }
        em.getTransaction().commit();
        
        com.seneca.hotelreservation_system.model.WaitlistManager.getInstance().addToWaitlist("Waitlist Demo 1", "wait1@demo.com");
        com.seneca.hotelreservation_system.model.WaitlistManager.getInstance().addToWaitlist("Waitlist Demo 2", "wait2@demo.com");
    }
}
