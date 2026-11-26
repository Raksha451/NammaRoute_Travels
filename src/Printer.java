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

    /** One deck of a sleeper: 4 rows, each with a pair of berths, a gap, then a single berth. */
    private static void printDeck(Bus bus, String title, String prefix) {
        System.out.println(title + ":");
        for (int row = 0; row < 4; row++) {
            int first = row * 3 + 1;
            System.out.println("  Row " + (row + 1) + ":  "
                    + seatLabel(bus, prefix + first, 3) + " " + seatLabel(bus, prefix + (first + 1), 3)
                    + "     "
                    + seatLabel(bus, prefix + (first + 2), 3));
        }
    }

    /** "[1A]" if free, "[XX]" if booked. The width keeps the columns lined up. */
    private static String seatLabel(Bus bus, String seatNo, int width) {
        String text = bus.getSeat(seatNo).isBooked() ? "XX" : seatNo;
        return "[" + String.format("%-" + width + "s", text) + "]";
    }

    // ---------- Feature 3: receipt ----------

    public static void printReceipt(Bus bus, Booking booking) {
        System.out.println();
        System.out.println("==================== BOOKING RECEIPT ====================");
        System.out.println("PNR           : " + booking.getPnr());
        System.out.println("Bus           : " + bus.getRouteId() + " (" + bus.getType().getLabel() + ")");
        System.out.println("Route         : " + bus.getOrigin() + " -> " + bus.getDestination());
        System.out.println("Departure     : " + bus.getDeparture().format(Bus.DATE_FORMAT));
        System.out.println("Booked on     : " + booking.getBookingTime().format(Bus.DATE_FORMAT));
        System.out.println("---------------------------------------------------------");
        System.out.printf("%-6s | %-20s | %-3s | %-6s | %s%n", "Seat", "Passenger", "Age", "Gender", "Fare");
        for (Ticket t : booking.getTickets()) {
            Passenger p = t.getPassenger();
            System.out.printf("%-6s | %-20s | %-3d | %-6s | %s%n",
                    t.getSeatNo(), p.getName(), p.getAge(), p.getGender(), money(t.getFare()));
        }
        System.out.println("---------------------------------------------------------");
        System.out.println("TOTAL PAID    : " + money(booking.getTotalPaid()));
        System.out.println("=========================================================");
    }

    // ---------- Feature 4: cancel screen ----------

    public static void printActiveTickets(Booking booking) {
        System.out.println();
        System.out.println("Booking " + booking.getPnr() + " on route " + booking.getRouteId() + " - active seats:");
        for (Ticket t : booking.getActiveTickets()) {
            System.out.printf("  %-5s %-20s fare %s%n", t.getSeatNo(), t.getPassenger().getName(), money(t.getFare()));
        }
    }

    // ---------- Feature 5: manifest ----------

    public static void printManifest(Bus bus, List<Ticket> manifest) {
        System.out.println();
        System.out.println("=== PASSENGER MANIFEST: " + bus.getRouteId() + " (" + bus.getOrigin()
                + " -> " + bus.getDestination() + ") ===");
        System.out.printf("%-5s | %-9s | %-20s | %-3s | %s%n", "Seat", "PNR", "Passenger Name", "Age", "Gender");
        System.out.println("-".repeat(60));
        for (Ticket t : manifest) {
            Passenger p = t.getPassenger();
            System.out.printf("%-5s | %-9s | %-20s | %-3d | %s%n",
                    t.getSeatNo(), t.getPnr(), p.getName(), p.getAge(), p.getGender());
        }
        System.out.println("-".repeat(60));
        System.out.println("Total Booked: " + bus.getBookedCount() + " | Available: " + bus.getAvailableCount());
    }

    // ---------- Change 1: surge price display ----------

    public static void printPriceInfo(Bus bus, double currentPrice, boolean surge) {
        if (surge) {
            int percentFull = bus.getBookedCount() * 100 / bus.getTotalSeats();
            System.out.println("SURGE PRICING ACTIVE (bus is " + percentFull + "% full): "
                    + money(currentPrice) + " per seat (+20%)");
        } else {
            System.out.println("Ticket price per seat: " + money(currentPrice));
        }
    }

    /** Shows the price of every seat BEFORE the booking is confirmed. */
    public static void printFareSummary(Bus bus, List<String> seatNos, List<Double> fares) {
        System.out.println();
        System.out.println("--- FARE SUMMARY ---");
        double total = 0;
        for (int i = 0; i < seatNos.size(); i++) {
            boolean surge = fares.get(i) > bus.getBasePrice();
            System.out.printf("Seat %-4s : %s%s%n", seatNos.get(i), money(fares.get(i)),
                    surge ? "   (surge price +20%)" : "");
            total += fares.get(i);
        }
        System.out.println("TOTAL     : " + money(total));
    }

    // ---------- Change 3: waitlist ----------

    public static void printWaitlist(Bus bus, List<WaitlistEntry> queue) {
        System.out.println();
        System.out.println("=== WAITLIST: " + bus.getRouteId() + " (" + bus.getOrigin() + " -> "
                + bus.getDestination() + ") ===");
        if (queue.isEmpty()) {
            System.out.println("Nobody is on the waitlist.");
            return;
        }
        System.out.printf("%-3s | %-20s | %-3s | %-6s | %s%n", "No", "Passenger Name", "Age", "Gender", "Joined");
        System.out.println("-".repeat(60));
        int position = 1;
        for (WaitlistEntry e : queue) {
            Passenger p = e.getPassenger();
            System.out.printf("%-3d | %-20s | %-3d | %-6s | %s%n", position++, p.getName(), p.getAge(),
                    p.getGender(), e.getJoinedAt().format(Bus.DATE_FORMAT));
        }
        System.out.println("-".repeat(60));
        System.out.println("Waiting: " + queue.size() + " of " + Waitlist.MAX_PER_BUS + " places used");
    }
}
