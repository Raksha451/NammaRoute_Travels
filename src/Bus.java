import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** A bus service on a route, together with all of its seats. */
public class Bus {
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final String routeId;
    private final String origin;
    private final String destination;
    private final LocalDateTime departure;
    private final double basePrice;
    private final BusType type;
    // LinkedHashMap keeps the seats in layout order (1A, 1B, 1C ...)
    private final Map<String, Seat> seats = new LinkedHashMap<>();

    public Bus(String routeId, String origin, String destination,
               LocalDateTime departure, double basePrice, BusType type) {
        this.routeId = routeId;
        this.origin = origin;
        this.destination = destination;
        this.departure = departure;
        this.basePrice = basePrice;
        this.type = type;
        createSeats();
    }

    private void createSeats() {
        if (type == BusType.SEATER_2X2) {
            // 8 rows x 4 seats = 32 seats
            for (int row = 1; row <= 8; row++) {
                for (char letter = 'A'; letter <= 'D'; letter++) {
                    addSeat(row + "" + letter);
                }
            }
        } else {
            // 12 lower + 12 upper berths = 24 berths
            for (int i = 1; i <= 12; i++) addSeat("L" + i);
            for (int i = 1; i <= 12; i++) addSeat("U" + i);
        }
    }

    private void addSeat(String number) {
        seats.put(number, new Seat(number));
    }

    /** Returns the seat, or null if no such seat exists on this bus. */
    public Seat getSeat(String number) {
        return seats.get(number);
    }

    public Collection<Seat> getSeats() { return seats.values(); }

    /** Position of a seat in the layout; used to sort the manifest by seat number. */
    public int seatPosition(String number) {
        return new ArrayList<>(seats.keySet()).indexOf(number);
    }

    public int getTotalSeats() { return seats.size(); }

    public int getBookedCount() {
        int count = 0;
        for (Seat s : seats.values()) if (s.isBooked()) count++;
        return count;
    }

    public int getAvailableCount() { return getTotalSeats() - getBookedCount(); }

    public List<String> getBookedSeatNumbers() {
        List<String> list = new ArrayList<>();
        for (Seat s : seats.values()) if (s.isBooked()) list.add(s.getNumber());
        return list;
    }

    public String getRouteId() { return routeId; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public LocalDateTime getDeparture() { return departure; }
    public double getBasePrice() { return basePrice; }
    public BusType getType() { return type; }
}