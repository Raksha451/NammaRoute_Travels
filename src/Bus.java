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

    /**
     * The seat right next to this one in its 2-seat pair, or null if the seat has no pair.
     * 2x2 Seater: A-B and C-D in every row (1A-1B, 1C-1D ...).
     * 2x1 Sleeper: each row of 3 berths on a deck is a double + a single,
     *              so L1-L2 are a pair, L3 is a single, L4-L5 are a pair, L6 is a single ...
     */
    public String getPairedSeat(String number) {
        if (!seats.containsKey(number)) return null;
        if (type == BusType.SEATER_2X2) {
            String row = number.substring(0, number.length() - 1);
            char letter = number.charAt(number.length() - 1);
            return switch (letter) {
                case 'A' -> row + "B";
                case 'B' -> row + "A";
                case 'C' -> row + "D";
                case 'D' -> row + "C";
                default -> null;
            };
        }
        String deck = number.substring(0, 1);                    // "L" or "U"
        int n = Integer.parseInt(number.substring(1));
        int position = (n - 1) % 3;                              // 0, 1 = the pair, 2 = the single
        if (position == 0) return deck + (n + 1);
        if (position == 1) return deck + (n - 1);
        return null;
    }
}
