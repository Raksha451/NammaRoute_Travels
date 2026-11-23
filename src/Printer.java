import java.util.List;

/** All the text that gets printed to the screen. */
public class Printer {
    private static final String CURRENCY = "Rs ";

    public static String money(double amount) {
        return CURRENCY + String.format("%.2f", amount);
    }

    public static void printBusList(List<Bus> buses) {
        if (buses.isEmpty()) {
            System.out.println("No bus services added yet.");
            return;
        }
        System.out.println();
        System.out.println("=== ALL BUS SERVICES ===");
        System.out.printf("%-8s | %-24s | %-16s | %-11s | %-10s | %s%n",
                "Route", "From -> To", "Departure", "Type", "Price", "Free/Total");
        System.out.println("-".repeat(95));
        for (Bus b : buses) {
            System.out.printf("%-8s | %-24s | %-16s | %-11s | %-10s | %d/%d%n",
                    b.getRouteId(), b.getOrigin() + " -> " + b.getDestination(),
                    b.getDeparture().format(Bus.DATE_FORMAT), b.getType().getLabel(),
                    money(b.getBasePrice()), b.getAvailableCount(), b.getTotalSeats());
        }
    }
}