import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final RefundPolicy refunds = new RefundPolicy();
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

    // ---------- Feature 4: cancelling ----------

    /** Returns an error message if these seats cannot be cancelled, or null if fine. */
    public String checkSeatsForCancel(Booking booking, List<String> seatNos) {
        if (seatNos.isEmpty()) return "Please enter at least one seat number.";
        Bus bus = findBus(booking.getRouteId());
        if (bus != null && bus.getDeparture().isBefore(LocalDateTime.now())) {
            return "This bus has already departed, so it cannot be cancelled.";
        }
        Set<String> seen = new HashSet<>();
        for (String seatNo : seatNos) {
            if (!seen.add(seatNo)) return "Seat " + seatNo + " was entered more than once.";
            if (booking.findActiveTicket(seatNo) == null) {
                return "Seat " + seatNo + " is not an active seat in " + booking.getPnr() + ".";
            }
        }
        return null;
    }

    /** Cancels the given seats, frees them, saves, and returns the total refund. */
    public double cancelSeats(Booking booking, List<String> seatNos) {
        Bus bus = findBus(booking.getRouteId());
        double totalRefund = 0;
        for (String seatNo : seatNos) {
            Ticket ticket = booking.findActiveTicket(seatNo);
            double refund = refunds.calculateRefund(ticket.getFare(), bus.getDeparture(), LocalDateTime.now());
            ticket.cancel(refund);
            bus.getSeat(seatNo).setBooked(false);   // seat is free again immediately
            totalRefund += refund;
        }
        saveAll();
        return totalRefund;
    }

    // ---------- Feature 5: manifest ----------

    /** All active tickets on a bus, sorted by seat order. */
    public List<Ticket> getManifest(Bus bus) {
        List<Ticket> list = new ArrayList<>();
        for (Booking b : bookings) {
            if (b.getRouteId().equals(bus.getRouteId())) list.addAll(b.getActiveTickets());
        }
        list.sort((a, b) -> Integer.compare(bus.seatPosition(a.getSeatNo()), bus.seatPosition(b.getSeatNo())));
        return list;
    }

    // ---------- Saving ----------

    private void saveAll() {
        store.saveBuses(buses);
        store.saveBookings(bookings);
    }
}