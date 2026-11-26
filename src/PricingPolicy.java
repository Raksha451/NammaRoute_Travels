/**
 * All ticket-price rules live here.
 *
 * V2.0 surge rule: when a bus is 70% or more full, every seat sold from then on costs 20% extra.
 * Tickets that were already sold keep the price they paid (the price is stored on each Ticket).
 */
public class PricingPolicy {
    private static final int SURGE_AT_PERCENT = 70;      // bus is this full (or more) -> surge
    private static final int SURGE_EXTRA_PERCENT = 20;   // surge adds this much

    /** True when the bus is 70% or more full. Whole-number maths avoids decimal rounding problems. */
    public boolean isSurgeActive(int bookedSeats, int totalSeats) {
        return bookedSeats * 100 >= totalSeats * SURGE_AT_PERCENT;
    }

    /** Price of the NEXT seat sold, given how many seats are already booked on the bus. */
    public double priceFor(Bus bus, int seatsAlreadyBooked) {
        double price = bus.getBasePrice();
        if (isSurgeActive(seatsAlreadyBooked, bus.getTotalSeats())) {
            price = price * (100 + SURGE_EXTRA_PERCENT) / 100;
        }
        return Math.round(price * 100) / 100.0;
    }
}
