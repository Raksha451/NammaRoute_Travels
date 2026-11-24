import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * All the business rules. No System.out / Scanner in here.
 * Methods that find a problem return an error message (or throw IllegalArgumentException).
 */
public class BookingService {
    private final DataStore store;
    private final PricingPolicy pricing = new PricingPolicy();
    private final List<Bus> buses;
    private final List<Booking> bookings;
    private final Random random = new Random();

    public BookingService(DataStore store) {
        this.store = store;
        this.buses = store.loadBuses();
        this.bookings = store.loadBookings();
    }

    // ---------- Feature 1: buses ----------

    public void addBus(Bus bus) {
        if (findBus(bus.getRouteId()) != null) {
            throw new IllegalArgumentException("Route ID " + bus.getRouteId() + " already exists.");
        }
        buses.add(bus);
        saveAll();
    }

    public List<Bus> getBuses() { return buses; }

    public Bus findBus(String routeId) {
        for (Bus b : buses) {
            if (b.getRouteId().equalsIgnoreCase(routeId)) return b;
        }
        return null;
    }

    // ---------- Feature 3: booking ----------

    /** Returns an error message if the seats cannot be booked, or null if everything is fine. */
    public String checkSeatsForBooking(Bus bus, List<String> seatNos) {
        if (seatNos.isEmpty()) return "Please enter at least one seat number.";
        if (bus.getDeparture().isBefore(LocalDateTime.now())) return "This bus has already departed.";
        Set<String> seen = new HashSet<>();
        for (String seatNo : seatNos) {
            if (!seen.add(seatNo)) return "Seat " + seatNo + " was entered more than once.";
            Seat seat = bus.getSeat(seatNo);
            if (seat == null) return "Seat " + seatNo + " does not exist on this bus.";
            if (seat.isBooked()) return "Seat " + seatNo + " is already booked.";
        }
        return null;
    }

    /** Books the seats (already validated) and saves everything. */
    public Booking book(Bus bus, List<String> seatNos, List<Passenger> passengers) {
        Booking booking = new Booking(generatePnr(), bus.getRouteId(), LocalDateTime.now());
        for (int i = 0; i < seatNos.size(); i++) {
            Seat seat = bus.getSeat(seatNos.get(i));
            double fare = pricing.priceFor(bus, seat);
            booking.addTicket(new Ticket(booking.getPnr(), seat.getNumber(), passengers.get(i), fare));
            seat.setBooked(true);
        }
        bookings.add(booking);
        saveAll();
        return booking;
    }

    private String generatePnr() {
        String pnr;
        do {
            pnr = "PNR-" + (10000 + random.nextInt(90000));   // PNR-10000 to PNR-99999
        } while (findBooking(pnr) != null);
        return pnr;
    }

    public Booking findBooking(String pnr) {
        for (Booking b : bookings) {
            if (b.getPnr().equalsIgnoreCase(pnr)) return b;
        }
        return null;
    }

    // ---------- Saving ----------

    private void saveAll() {
        store.saveBuses(buses);
        store.saveBookings(bookings);
    }
}