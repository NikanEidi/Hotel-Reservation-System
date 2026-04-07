package com.seneca.hotelreservation_system.service;

import com.seneca.hotelreservation_system.model.*;
import com.seneca.hotelreservation_system.observer.AdminNotificationService;
import com.seneca.hotelreservation_system.util.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BookingService {

    private final PricingService pricingService = new PricingService();
    private final AdminNotificationService notificationService = AdminNotificationService.getInstance();

    public void initializeDatabase() {
        if (!JPAUtil.isDatabaseAvailable()) {
            throw new IllegalStateException("Database connection is not available.");
        }
        seedRoomsIfNeeded();
    }

    public List<Reservation> searchReservations(String query, String status) {
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

    public List<WaitlistEntry> getWaitlistEntries() {
        if (!JPAUtil.isDatabaseAvailable()) {
            return Collections.emptyList();
        }

        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.createQuery(
                    "select w from WaitlistEntry w order by w.waitlistId desc",
                    WaitlistEntry.class
            ).getResultList();
        } finally {
            em.close();
        }
    }

    public Reservation createReservation(String firstName, String lastName, String email, String phone,
                                         LocalDate checkIn, LocalDate checkOut, int adults, int children,
                                         String roomType, int roomQuantity, BigDecimal discountPercent,
                                         boolean redeemLoyalty, AdminRole role) {
        validateInputs(firstName, lastName, email, phone, checkIn, checkOut, adults, children, roomQuantity);
        pricingService.validateDiscountCap(role, discountPercent);

        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();

            Guest guest = findOrCreateGuest(em, firstName, lastName, email, phone);
            List<Room> rooms = getAvailableRoomsByType(em, roomType, roomQuantity);

            if (rooms.size() < roomQuantity) {
                em.persist(new WaitlistEntry(firstName + " " + lastName, phone, roomType.toUpperCase(), checkIn, checkOut));
                em.getTransaction().commit();
                throw new IllegalStateException("Not enough rooms available. Guest added to waitlist.");
            }

            Reservation reservation = new Reservation(guest, rooms, checkIn, checkOut, adults, children);
            reservation.setStatus("CONFIRMED");
            reservation.setDiscountPercent(discountPercent);

            long nights = pricingService.getStayNights(checkIn, checkOut);
            BigDecimal subtotal = pricingService.calculateSubtotal(rooms, nights);
            BigDecimal discountAmount = pricingService.calculateDiscountAmount(subtotal, discountPercent);
            BigDecimal afterDiscount = subtotal.subtract(discountAmount);

            int pointsRedeemed = 0;
            if (redeemLoyalty) {
                pointsRedeemed = pricingService.calculateRedeemablePoints(afterDiscount, guest);
                if (pointsRedeemed > 0) {
                    guest.redeemLoyaltyPoints(pointsRedeemed);
                    em.merge(guest);
                }
            }

            reservation.setLoyaltyPointsRedeemed(pointsRedeemed);
            reservation.setTotalAmount(
                    pricingService.calculateTotalWithLoyalty(rooms, checkIn, checkOut, discountPercent, guest, redeemLoyalty)
            );

            em.persist(reservation);

            for (Room room : rooms) {
                room.setAvailable(false);
                em.merge(room);
            }

            em.getTransaction().commit();
            return reservation;
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public Reservation updateReservation(Long reservationId, String firstName, String lastName, String email, String phone,
                                         LocalDate checkIn, LocalDate checkOut, int adults, int children,
                                         String roomType, int roomQuantity, BigDecimal discountPercent,
                                         boolean redeemLoyalty, AdminRole role) {
        validateInputs(firstName, lastName, email, phone, checkIn, checkOut, adults, children, roomQuantity);
        pricingService.validateDiscountCap(role, discountPercent);

        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();

            Reservation reservation = em.find(Reservation.class, reservationId);
            if (reservation == null) {
                throw new IllegalArgumentException("Reservation not found.");
            }

            if ("CHECKED_OUT".equalsIgnoreCase(reservation.getStatus()) || "CANCELLED".equalsIgnoreCase(reservation.getStatus())) {
                throw new IllegalStateException("Completed or cancelled reservations cannot be edited.");
            }

            for (Room room : reservation.getRooms()) {
                room.setAvailable(true);
                em.merge(room);
            }

            Guest guest = reservation.getGuest();
            guest.setFirstName(firstName);
            guest.setLastName(lastName);
            guest.setEmail(email);
            guest.setPhone(phone);
            em.merge(guest);

            List<Room> newRooms = getAvailableRoomsByType(em, roomType, roomQuantity);
            if (newRooms.size() < roomQuantity) {
                for (Room room : reservation.getRooms()) {
                    room.setAvailable(false);
                    em.merge(room);
                }
                em.persist(new WaitlistEntry(firstName + " " + lastName, phone, roomType.toUpperCase(), checkIn, checkOut));
                em.getTransaction().commit();
                throw new IllegalStateException("Updated room selection unavailable. Guest added to waitlist.");
            }

            reservation.setCheckInDate(checkIn);
            reservation.setCheckOutDate(checkOut);
            reservation.setAdultCount(adults);
            reservation.setChildCount(children);
            reservation.setRooms(newRooms);
            reservation.setStatus("CONFIRMED");
            reservation.setDiscountPercent(discountPercent);

            long nights = pricingService.getStayNights(checkIn, checkOut);
            BigDecimal subtotal = pricingService.calculateSubtotal(newRooms, nights);
            BigDecimal discountAmount = pricingService.calculateDiscountAmount(subtotal, discountPercent);
            BigDecimal afterDiscount = subtotal.subtract(discountAmount);

            int oldRedeemed = reservation.getLoyaltyPointsRedeemed();
            if (oldRedeemed > 0) {
                guest.addLoyaltyPoints(oldRedeemed);
            }

            int pointsRedeemed = 0;
            if (redeemLoyalty) {
                pointsRedeemed = pricingService.calculateRedeemablePoints(afterDiscount, guest);
                if (pointsRedeemed > 0) {
                    guest.redeemLoyaltyPoints(pointsRedeemed);
                }
            }

            reservation.setLoyaltyPointsRedeemed(pointsRedeemed);
            reservation.setTotalAmount(
                    pricingService.calculateTotalWithLoyalty(newRooms, checkIn, checkOut, discountPercent, guest, redeemLoyalty)
            );

            em.merge(guest);
            em.merge(reservation);

            for (Room room : newRooms) {
                room.setAvailable(false);
                em.merge(room);
            }

            em.getTransaction().commit();
            return reservation;
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public void cancelReservation(Long reservationId) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();

            Reservation reservation = em.find(Reservation.class, reservationId);
            if (reservation == null) {
                throw new IllegalArgumentException("Reservation not found.");
            }

            reservation.setStatus("CANCELLED");

            if (reservation.getLoyaltyPointsRedeemed() > 0) {
                Guest guest = reservation.getGuest();
                guest.addLoyaltyPoints(reservation.getLoyaltyPointsRedeemed());
                em.merge(guest);
                reservation.setLoyaltyPointsRedeemed(0);
            }

            em.merge(reservation);

            String roomTypeForNotification = null;
            for (Room room : reservation.getRooms()) {
                room.setAvailable(true);
                roomTypeForNotification = room.getRoomType();
                em.merge(room);
            }

            notifyWaitlistIfMatch(em, roomTypeForNotification);
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public void checkoutReservation(Long reservationId, String paymentMethod, BigDecimal discountPercent, AdminRole role) {
        pricingService.validateDiscountCap(role, discountPercent);

        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            em.getTransaction().begin();

            Reservation reservation = em.find(Reservation.class, reservationId);
            if (reservation == null) {
                throw new IllegalArgumentException("Reservation not found.");
            }

            if ("CHECKED_OUT".equalsIgnoreCase(reservation.getStatus())) {
                throw new IllegalStateException("Reservation already checked out.");
            }

            reservation.setDiscountPercent(discountPercent);

            BigDecimal total = pricingService.calculateTotalWithLoyalty(
                    reservation.getRooms(),
                    reservation.getCheckInDate(),
                    reservation.getCheckOutDate(),
                    discountPercent,
                    reservation.getGuest(),
                    reservation.getLoyaltyPointsRedeemed() > 0
            );

            reservation.setTotalAmount(total);

            Payment payment = new Payment(reservation, total, paymentMethod == null || paymentMethod.isBlank() ? "CARD" : paymentMethod);
            em.persist(payment);

            Guest guest = reservation.getGuest();
            int pointsEarned = pricingService.calculatePointsEarned(total);
            guest.addLoyaltyPoints(pointsEarned);
            em.merge(guest);

            reservation.setStatus("CHECKED_OUT");
            em.merge(reservation);

            String roomTypeForNotification = null;
            for (Room room : reservation.getRooms()) {
                room.setAvailable(true);
                roomTypeForNotification = room.getRoomType();
                em.merge(room);
            }

            notifyWaitlistIfMatch(em, roomTypeForNotification);
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public BigDecimal calculateReservationDisplayTotal(Reservation reservation) {
        if (reservation == null) {
            return BigDecimal.ZERO;
        }
        return reservation.getTotalAmount() == null ? BigDecimal.ZERO : reservation.getTotalAmount();
    }

    private void notifyWaitlistIfMatch(EntityManager em, String roomType) {
        if (roomType == null || roomType.isBlank()) {
            return;
        }

        List<WaitlistEntry> matches = em.createQuery(
                        "select w from WaitlistEntry w where upper(w.desiredRoomType) = :roomType and w.status = 'WAITING' order by w.waitlistId asc",
                        WaitlistEntry.class
                )
                .setParameter("roomType", roomType.toUpperCase())
                .setMaxResults(1)
                .getResultList();

        if (matches.isEmpty()) {
            return;
        }

        WaitlistEntry entry = matches.get(0);
        entry.setStatus("NOTIFIED");
        entry.setNotified(true);
        em.merge(entry);

        notificationService.notifyObservers(
                "Room available for waitlisted guest: " + entry.getGuestName()
                        + " | Type: " + entry.getDesiredRoomType()
                        + " | Phone: " + entry.getPhone()
        );
    }

    private Guest findOrCreateGuest(EntityManager em, String firstName, String lastName, String email, String phone) {
        List<Guest> guests = em.createQuery(
                        "select g from Guest g where lower(g.email) = :email or g.phone = :phone",
                        Guest.class
                )
                .setParameter("email", email.trim().toLowerCase())
                .setParameter("phone", phone.trim())
                .getResultList();

        if (guests.isEmpty()) {
            Guest guest = new Guest(firstName, lastName, email, phone);
            em.persist(guest);
            return guest;
        }

        Guest guest = guests.get(0);
        guest.setFirstName(firstName);
        guest.setLastName(lastName);
        guest.setEmail(email);
        guest.setPhone(phone);
        return em.merge(guest);
    }

    private List<Room> getAvailableRoomsByType(EntityManager em, String roomType, int roomQuantity) {
        return em.createQuery(
                        "select r from Room r where upper(r.roomType) = :roomType and r.isAvailable = true order by r.roomNumber",
                        Room.class
                )
                .setParameter("roomType", roomType.trim().toUpperCase())
                .setMaxResults(roomQuantity)
                .getResultList();
    }

    private void seedRoomsIfNeeded() {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            Long count = em.createQuery("select count(r) from Room r", Long.class).getSingleResult();
            if (count != null && count > 0) {
                return;
            }

            em.getTransaction().begin();

            List<Room> roomSeeds = new ArrayList<>();
            roomSeeds.add(new SingleRoom("S101", new BigDecimal("89.00")));
            roomSeeds.add(new SingleRoom("S102", new BigDecimal("89.00")));
            roomSeeds.add(new SingleRoom("S103", new BigDecimal("89.00")));
            roomSeeds.add(new SingleRoom("S104", new BigDecimal("89.00")));

            roomSeeds.add(new DoubleRoom("D201", new BigDecimal("129.00")));
            roomSeeds.add(new DoubleRoom("D202", new BigDecimal("129.00")));
            roomSeeds.add(new DoubleRoom("D203", new BigDecimal("129.00")));
            roomSeeds.add(new DoubleRoom("D204", new BigDecimal("129.00")));

            roomSeeds.add(new PenthouseRoom("P301", new BigDecimal("349.00")));
            roomSeeds.add(new PenthouseRoom("P302", new BigDecimal("349.00")));

            for (Room room : roomSeeds) {
                em.persist(room);
            }

            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    private void validateInputs(String firstName, String lastName, String email, String phone,
                                LocalDate checkIn, LocalDate checkOut, int adults, int children, int roomQuantity) {
        if (isBlank(firstName) || isBlank(lastName)) {
            throw new IllegalArgumentException("First and last name are required.");
        }
        if (isBlank(email) || !email.contains("@")) {
            throw new IllegalArgumentException("Valid email is required.");
        }
        if (isBlank(phone) || phone.trim().length() < 9) {
            throw new IllegalArgumentException("Valid phone number is required.");
        }
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("Check-in and check-out dates are required.");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Check-out must be after check-in.");
        }
        if (adults < 1) {
            throw new IllegalArgumentException("At least one adult is required.");
        }
        if (children < 0) {
            throw new IllegalArgumentException("Children cannot be negative.");
        }
        if (roomQuantity < 1) {
            throw new IllegalArgumentException("Room quantity must be at least 1.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}