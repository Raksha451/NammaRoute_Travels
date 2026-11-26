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
    private final CoSeatRule coSeatRule = new CoSeatRule();
    private final List<Bus> buses;
    private final List<Booking> bookings;
    private final Waitlist waitlist;
    private final Random random = new Random();

    public BookingService(DataStore store) {
        this.store = store;
        this.buses = store.loadBuses();
        this.bookings = store.loadBookings();
        this.waitlist = store.loadWaitlist();
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

    // ---------- Change 1: surge pricing ----------

    /**
     * The price of each seat in a booking, in order. Seats are priced one by one, so if this
     * booking pushes the bus past 70%, the later seats in the same booking get the surge price.
     */
    public List<Double> previewFares(Bus bus, int seatCount) {
        List<Double> fares = new ArrayList<>();
        int alreadyBooked = bus.getBookedCount();
        for (int i = 0; i < seatCount; i++) {
            fares.add(pricing.priceFor(bus, alreadyBooked + i));
        }
        return fares;
    }

    /** The price the next seat on this bus would cost right now. */
    public double currentPrice(Bus bus) {
        return pricing.priceFor(bus, bus.getBookedCount());
    }

    public boolean isSurgeActive(Bus bus) {
        return pricing.isSurgeActive(bus.getBookedCount(), bus.getTotalSeats());
    }

    // ---------- Change 2: female co-seat rule ----------

    /** The passenger currently sitting in this seat (active ticket), or null if the seat is empty. */
    public Passenger getPassengerAt(Bus bus, String seatNo) {
        if (seatNo == null) return null;
        for (Booking b : bookings) {
            if (!b.getRouteId().equals(bus.getRouteId())) continue;
            Ticket ticket = b.findActiveTicket(seatNo);
            if (ticket != null) return ticket.getPassenger();
        }
        return null;
    }

    /** Returns the "reserved for a female co-passenger" message if the booking breaks the rule, else null. */
    public String checkCoSeatRule(Bus bus, List<String> seatNos, List<Passenger> passengers) {
        for (int i = 0; i < seatNos.size(); i++) {
            String seatNo = seatNos.get(i);
            String partner = bus.getPairedSeat(seatNo);
            if (partner == null) continue;                  // single berth: no pair, no restriction
            if (seatNos.contains(partner)) continue;        // both seats of the pair in ONE booking: allowed
            Passenger neighbour = getPassengerAt(bus, partner);
            if (!coSeatRule.isAllowed(passengers.get(i), neighbour)) {
                return coSeatRule.blockedMessage(seatNo);
            }
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
        List<Double> fares = previewFares(bus, seatNos.size());   // same prices the user was shown
        for (int i = 0; i < seatNos.size(); i++) {
            Seat seat = bus.getSeat(seatNos.get(i));
            booking.addTicket(new Ticket(booking.getPnr(), seat.getNumber(), passengers.get(i), fares.get(i)));
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

    /** Cancels the given seats, frees them, then offers the freed seats to the waitlist. Saves everything. */
    public CancellationResult cancelSeats(Booking booking, List<String> seatNos) {
        Bus bus = findBus(booking.getRouteId());
        double totalRefund = 0;
        for (String seatNo : seatNos) {
            Ticket ticket = booking.findActiveTicket(seatNo);
            double refund = refunds.calculateRefund(ticket.getFare(), bus.getDeparture(), LocalDateTime.now());
            ticket.cancel(refund);
            bus.getSeat(seatNo).setBooked(false);   // seat is free again immediately
            totalRefund += refund;
        }
        CancellationResult result = new CancellationResult(totalRefund);
        promoteFromWaitlist(bus, seatNos, result);
        saveAll();
        return result;
    }

    // ---------- Change 3: waitlist and auto-promotion ----------

    /** Returns a message if the person cannot join the waitlist, or null if they can. */
    public String checkCanJoinWaitlist(Bus bus) {
        if (bus.getDeparture().isBefore(LocalDateTime.now())) return "This bus has already departed.";
        if (bus.getAvailableCount() > 0) {
            return "Seats are still available on this bus, so there is no need to wait. Please use 'Book tickets'.";
        }
        if (waitlist.isFull(bus.getRouteId())) {
            return "The waitlist for this bus is full (" + Waitlist.MAX_PER_BUS + " of " + Waitlist.MAX_PER_BUS + ").";
        }
        return null;
    }

    /** Adds the passenger to the back of the queue and returns their position (1 = front). */
    public int joinWaitlist(Bus bus, Passenger passenger) {
        waitlist.add(new WaitlistEntry(bus.getRouteId(), passenger, LocalDateTime.now()));
        saveAll();
        return waitlist.forBus(bus.getRouteId()).size();
    }

    public List<WaitlistEntry> getWaitlist(Bus bus) {
        return waitlist.forBus(bus.getRouteId());
    }

    /**
     * Gives each freed seat to the first person in the queue who is allowed to take it.
     * Someone who would break the female co-seat rule is skipped but keeps their place at the front.
     */
    private void promoteFromWaitlist(Bus bus, List<String> freedSeats, CancellationResult result) {
        for (String seatNo : freedSeats) {
            Passenger neighbour = getPassengerAt(bus, bus.getPairedSeat(seatNo));
            for (WaitlistEntry entry : waitlist.forBus(bus.getRouteId())) {
                Passenger person = entry.getPassenger();
                if (!coSeatRule.isAllowed(person, neighbour)) {
                    result.addNote("Skipped " + person.getName() + " (waitlist): seat " + seatNo
                            + " is next to a solo female passenger. Stays at the front of the queue.");
                    continue;
                }
                // Eligible: confirm them in this seat with a new PNR, then remove them from the queue
                Seat seat = bus.getSeat(seatNo);
                double fare = pricing.priceFor(bus, bus.getBookedCount());
                Booking promoted = new Booking(generatePnr(), bus.getRouteId(), LocalDateTime.now());
                promoted.addTicket(new Ticket(promoted.getPnr(), seatNo, person, fare));
                seat.setBooked(true);
                bookings.add(promoted);
                waitlist.remove(entry);
                result.addPromotion(promoted);
                break;
            }
        }
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
        store.saveWaitlist(waitlist);
    }
}
