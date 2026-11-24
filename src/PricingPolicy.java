/**
 * All ticket-price rules live here.
 * V1.0 assumption: every seat costs the base price (upper and lower berths are the same).
 * If the client says otherwise (or V2.0 adds new rules), only this class needs to change.
 */
public class PricingPolicy {
    public double priceFor(Bus bus, Seat seat) {
        return bus.getBasePrice();
    }
}