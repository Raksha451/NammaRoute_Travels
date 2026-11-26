import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Saves and loads everything from CSV files. This class knows NOTHING about menus or booking rules.
 *
 * buses.csv    : routeId,origin,destination,departure,basePrice,busType,bookedSeats(separated by |)
 * bookings.csv : pnr,routeId,bookingTime,seatNo,name,age,gender,fare,status,refund   (one line per ticket)
 * waitlist.csv : routeId,joinedAt,name,age,gender   (one line per waiting passenger, oldest first)
 */
public class DataStore {
    private final Path busFile;
    private final Path bookingFile;
    private final Path waitlistFile;

    public DataStore(String folder) {
        try {
            Files.createDirectories(Paths.get(folder));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create data folder", e);
        }
        busFile = Paths.get(folder, "buses.csv");
        bookingFile = Paths.get(folder, "bookings.csv");
        waitlistFile = Paths.get(folder, "waitlist.csv");
    }

    // ---------- Buses ----------

    public List<Bus> loadBuses() {
        List<Bus> buses = new ArrayList<>();
        for (String line : readLines(busFile)) {
            String[] p = line.split(",", -1);
            Bus bus = new Bus(p[0], p[1], p[2], LocalDateTime.parse(p[3], Bus.DATE_FORMAT),
                    Double.parseDouble(p[4]), BusType.valueOf(p[5]));
            for (String seatNo : p[6].split("\\|")) {
                Seat seat = bus.getSeat(seatNo);
                if (seat != null) seat.setBooked(true);
            }
            buses.add(bus);
        }
        return buses;
    }

    public void saveBuses(List<Bus> buses) {
        List<String> lines = new ArrayList<>();
        for (Bus b : buses) {
            lines.add(String.join(",", b.getRouteId(), b.getOrigin(), b.getDestination(),
                    b.getDeparture().format(Bus.DATE_FORMAT), String.valueOf(b.getBasePrice()),
                    b.getType().name(), String.join("|", b.getBookedSeatNumbers())));
        }
        writeLines(busFile, lines);
    }

    // ---------- Bookings ----------

    public List<Booking> loadBookings() {
        Map<String, Booking> byPnr = new LinkedHashMap<>();
        for (String line : readLines(bookingFile)) {
            String[] p = line.split(",", -1);
            Booking booking = byPnr.get(p[0]);
            if (booking == null) {
                booking = new Booking(p[0], p[1], LocalDateTime.parse(p[2], Bus.DATE_FORMAT));
                byPnr.put(p[0], booking);
            }
            Passenger passenger = new Passenger(p[4], Integer.parseInt(p[5]), p[6]);
            Ticket ticket = new Ticket(p[0], p[3], passenger, Double.parseDouble(p[7]));
            if (p[8].equals("CANCELLED")) ticket.cancel(Double.parseDouble(p[9]));
            booking.addTicket(ticket);
        }
        return new ArrayList<>(byPnr.values());
    }

    public void saveBookings(List<Booking> bookings) {
        List<String> lines = new ArrayList<>();
        for (Booking b : bookings) {
            for (Ticket t : b.getTickets()) {
                Passenger p = t.getPassenger();
                lines.add(String.join(",", b.getPnr(), b.getRouteId(),
                        b.getBookingTime().format(Bus.DATE_FORMAT), t.getSeatNo(), p.getName(),
                        String.valueOf(p.getAge()), p.getGender(), String.valueOf(t.getFare()),
                        t.isCancelled() ? "CANCELLED" : "BOOKED", String.valueOf(t.getRefundAmount())));
            }
        }
        writeLines(bookingFile, lines);
    }

    // ---------- Waitlist ----------

    /** If waitlist.csv does not exist yet (old V1.0 data), the waitlist simply starts empty. */
    public Waitlist loadWaitlist() {
        Waitlist waitlist = new Waitlist();
        for (String line : readLines(waitlistFile)) {
            String[] p = line.split(",", -1);
            Passenger passenger = new Passenger(p[2], Integer.parseInt(p[3]), p[4]);
            waitlist.add(new WaitlistEntry(p[0], passenger, LocalDateTime.parse(p[1], Bus.DATE_FORMAT)));
        }
        return waitlist;
    }

    public void saveWaitlist(Waitlist waitlist) {
        List<String> lines = new ArrayList<>();
        for (WaitlistEntry e : waitlist.getAll()) {
            Passenger p = e.getPassenger();
            lines.add(String.join(",", e.getRouteId(), e.getJoinedAt().format(Bus.DATE_FORMAT),
                    p.getName(), String.valueOf(p.getAge()), p.getGender()));
        }
        writeLines(waitlistFile, lines);
    }

    // ---------- Helpers ----------

    private List<String> readLines(Path file) {
        List<String> result = new ArrayList<>();
        if (!Files.exists(file)) return result;
        try {
            for (String line : Files.readAllLines(file)) {
                if (!line.isBlank()) result.add(line);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + file, e);
        }
        return result;
    }

    private void writeLines(Path file, List<String> lines) {
        try {
            Files.write(file, lines);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save " + file, e);
        }
    }
}
