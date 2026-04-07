package com.seneca.hotelreservation_system.service;

import com.seneca.hotelreservation_system.model.AdminRole;
import com.seneca.hotelreservation_system.model.Guest;
import com.seneca.hotelreservation_system.model.Room;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class PricingService {

    private static final BigDecimal TAX_RATE = new BigDecimal("0.13");
    private static final BigDecimal ADMIN_CAP = new BigDecimal("15.00");
    private static final BigDecimal MANAGER_CAP = new BigDecimal("30.00");
    private static final BigDecimal LOYALTY_POINT_VALUE = new BigDecimal("0.10");
    private static final BigDecimal LOYALTY_REDEEM_CAP_PERCENT = new BigDecimal("20.00");

    public void validateDiscountCap(AdminRole role, BigDecimal discountPercent) {
        BigDecimal requested = discountPercent == null ? BigDecimal.ZERO : discountPercent;
        BigDecimal cap = role == AdminRole.MANAGER ? MANAGER_CAP : ADMIN_CAP;

        if (requested.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Discount cannot be negative.");
        }

        if (requested.compareTo(cap) > 0) {
            throw new IllegalArgumentException(
                    role + " discount cap exceeded. Max allowed is " + cap.stripTrailingZeros().toPlainString() + "%."
            );
        }
    }

    public long getStayNights(LocalDate checkIn, LocalDate checkOut) {
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (nights <= 0) {
            throw new IllegalArgumentException("Check-out must be after check-in.");
        }
        return nights;
    }

    public BigDecimal calculateSubtotal(List<Room> rooms, long nights) {
        BigDecimal subtotal = BigDecimal.ZERO;
        for (Room room : rooms) {
            subtotal = subtotal.add(room.getBasePrice().multiply(BigDecimal.valueOf(nights)));
        }
        return subtotal.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateDiscountAmount(BigDecimal subtotal, BigDecimal discountPercent) {
        BigDecimal safeDiscount = discountPercent == null ? BigDecimal.ZERO : discountPercent;
        return subtotal.multiply(safeDiscount)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTax(BigDecimal amountAfterDiscount) {
        return amountAfterDiscount.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
    }

    public int calculateRedeemablePoints(BigDecimal subtotalAfterDiscount, Guest guest) {
        if (guest == null || guest.getLoyaltyPoints() <= 0) {
            return 0;
        }

        BigDecimal capAmount = subtotalAfterDiscount
                .multiply(LOYALTY_REDEEM_CAP_PERCENT)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        int maxPointsByCap = capAmount.divide(LOYALTY_POINT_VALUE, 0, RoundingMode.DOWN).intValue();
        return Math.min(guest.getLoyaltyPoints(), maxPointsByCap);
    }

    public BigDecimal calculateLoyaltyDiscountFromPoints(int pointsToRedeem) {
        if (pointsToRedeem <= 0) {
            return BigDecimal.ZERO;
        }
        return LOYALTY_POINT_VALUE.multiply(BigDecimal.valueOf(pointsToRedeem)).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTotal(List<Room> rooms, LocalDate checkIn, LocalDate checkOut, BigDecimal discountPercent) {
        long nights = getStayNights(checkIn, checkOut);
        BigDecimal subtotal = calculateSubtotal(rooms, nights);
        BigDecimal discount = calculateDiscountAmount(subtotal, discountPercent);
        BigDecimal afterDiscount = subtotal.subtract(discount);
        BigDecimal tax = calculateTax(afterDiscount);
        return afterDiscount.add(tax).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateTotalWithLoyalty(List<Room> rooms, LocalDate checkIn, LocalDate checkOut,
                                                BigDecimal discountPercent, Guest guest, boolean redeemLoyalty) {
        long nights = getStayNights(checkIn, checkOut);
        BigDecimal subtotal = calculateSubtotal(rooms, nights);
        BigDecimal discount = calculateDiscountAmount(subtotal, discountPercent);
        BigDecimal afterDiscount = subtotal.subtract(discount);

        if (redeemLoyalty && guest != null) {
            int points = calculateRedeemablePoints(afterDiscount, guest);
            BigDecimal loyaltyDiscount = calculateLoyaltyDiscountFromPoints(points);
            afterDiscount = afterDiscount.subtract(loyaltyDiscount);
        }

        BigDecimal tax = calculateTax(afterDiscount);
        return afterDiscount.add(tax).setScale(2, RoundingMode.HALF_UP);
    }

    public int calculatePointsEarned(BigDecimal totalPaid) {
        if (totalPaid == null || totalPaid.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }
        return totalPaid.divide(new BigDecimal("10"), 0, RoundingMode.DOWN).intValue();
    }
}