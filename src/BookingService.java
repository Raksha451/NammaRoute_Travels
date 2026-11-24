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

    // ---------- Saving ----------

    private void saveAll() {
        store.saveBuses(buses);
        store.saveBookings(bookings);
    }
}