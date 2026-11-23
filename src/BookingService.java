import java.util.List;

/**
 * All the business rules. No System.out / Scanner in here.
 * (Stage 2: only bus management. Booking and cancelling are added in later stages.)
 */
public class BookingService {
    private final DataStore store;
    private final List<Bus> buses;

    public BookingService(DataStore store) {
        this.store = store;
        this.buses = store.loadBuses();
    }

    // ---------- Feature 1: buses ----------

    public void addBus(Bus bus) {
        if (findBus(bus.getRouteId()) != null) {
            throw new IllegalArgumentException("Route ID " + bus.getRouteId() + " already exists.");
        }
        buses.add(bus);
        store.saveBuses(buses);
    }

    public List<Bus> getBuses() { return buses; }

    public Bus findBus(String routeId) {
        for (Bus b : buses) {
            if (b.getRouteId().equalsIgnoreCase(routeId)) return b;
        }
        return null;
    }
}