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

    // ---------- Feature 2: seat map ----------

    public static void printSeatMap(Bus bus) {
        System.out.println();
        System.out.println("--- Bus " + bus.getRouteId() + ": " + bus.getOrigin() + " to "
                + bus.getDestination() + " (" + bus.getDeparture().format(Bus.DATE_FORMAT) + ") ---");
        System.out.println("Available: [Seat No] | Booked: [XX]");
        System.out.println();

        if (bus.getType() == BusType.SEATER_2X2) {
            for (int row = 1; row <= 8; row++) {
                // two seats, a gap for the aisle, then two seats
                System.out.println("Row " + row + ":  "
                        + seatLabel(bus, row + "A", 2) + " " + seatLabel(bus, row + "B", 2)
                        + "     "
                        + seatLabel(bus, row + "C", 2) + " " + seatLabel(bus, row + "D", 2));
            }
        } else {
            printDeck(bus, "Lower berths", "L");
            printDeck(bus, "Upper berths", "U");
        }
        System.out.println();
        System.out.println("Available: " + bus.getAvailableCount() + " | Booked: " + bus.getBookedCount());
    }

    private static void printDeck(Bus bus, String title, String prefix) {
        System.out.println(title + ":");
        for (int start = 1; start <= 12; start += 6) {
            StringBuilder line = new StringBuilder("  ");
            for (int i = start; i < start + 6; i++) {
                line.append(seatLabel(bus, prefix + i, 3)).append(' ');
            }
            System.out.println(line);
        }
    }

    /** "[1A]" if free, "[XX]" if booked. The width keeps the columns lined up. */
    private static String seatLabel(Bus bus, String seatNo, int width) {
        String text = bus.getSeat(seatNo).isBooked() ? "XX" : seatNo;
        return "[" + String.format("%-" + width + "s", text) + "]";
    }
}