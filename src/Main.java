import java.io.UncheckedIOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/** The console menu. It only talks to the user; the rules are in BookingService. */
public class Main {
    private static final InputHelper input = new InputHelper();
    private static BookingService service;

    public static void main(String[] args) {
        try {
            service = new BookingService(new DataStore("data"));
        } catch (RuntimeException e) {
            System.out.println("Could not load saved data: " + e.getMessage());
            return;
        }

        System.out.println("=====================================");
        System.out.println("   NammaRoute Travels - Booking System");
        System.out.println("=====================================");

        try {
            boolean running = true;
            while (running) {
                printMenu();
                int choice = input.readInt("Enter your choice: ", 0, 8);
                try {
                    switch (choice) {
                        case 1 -> addBus();
                        case 2 -> Printer.printBusList(service.getBuses());
                        case 3 -> showSeatMap();
                        case 4 -> bookTickets();
                        case 5 -> cancelTickets();
                        case 6 -> showManifest();
                        case 7 -> joinWaitlist();
                        case 8 -> showWaitlist();
                        case 0 -> running = false;
                    }
                } catch (IllegalArgumentException | IllegalStateException | UncheckedIOException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
            System.out.println("Thank you. All data is saved. Goodbye!");
        } catch (NoSuchElementException e) {
            System.out.println("\nInput closed. Goodbye!");
        }
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("---------- MAIN MENU ----------");
        System.out.println("1. Add a bus service");
        System.out.println("2. View all bus services");
        System.out.println("3. View seat map");
        System.out.println("4. Book tickets");
        System.out.println("5. Cancel tickets");
        System.out.println("6. Conductor passenger manifest");
        System.out.println("7. Join waitlist (full buses)");
        System.out.println("8. View waitlist");
        System.out.println("0. Exit");
    }

    /** Asks for a Route ID and returns the bus, or null (after printing an error) if not found. */
    private static Bus askForBus() {
        String routeId = input.readRouteId("Enter Route ID (example NR-101): ");
        Bus bus = service.findBus(routeId);
        if (bus == null) System.out.println("Error: no bus found with Route ID " + routeId + ".");
        return bus;
    }

    // ---------- Feature 1 ----------
    private static void addBus() {
        System.out.println("\n--- Add a Bus Service ---");
        String routeId = input.readRouteId("Route ID (example NR-101): ");
        if (service.findBus(routeId) != null) {
            System.out.println("Error: Route ID " + routeId + " already exists.");
            return;
        }
        String origin = input.readText("Origin (example Bengaluru): ");
        String destination = input.readText("Destination (example Goa): ");

        LocalDateTime departure = input.readDateTime("Departure (yyyy-MM-dd HH:mm): ");
        if (departure.isBefore(LocalDateTime.now())) {
            System.out.println("Error: departure time must be in the future.");
            return;
        }
        double price = input.readPrice("Base ticket price in Rs: ");

        System.out.println("Bus type:  1. 2x2 Seater (32 seats)   2. 2x1 Sleeper (12 lower + 12 upper berths)");
        int typeChoice = input.readInt("Choose type (1 or 2): ", 1, 2);
        BusType type = (typeChoice == 1) ? BusType.SEATER_2X2 : BusType.SLEEPER_2X1;

        service.addBus(new Bus(routeId, origin, destination, departure, price, type));
        System.out.println("Bus " + routeId + " added successfully.");
    }

    // ---------- Feature 2 ----------
    private static void showSeatMap() {
        Bus bus = askForBus();
        if (bus == null) return;
        Printer.printSeatMap(bus);
        Printer.printPriceInfo(bus, service.currentPrice(bus), service.isSurgeActive(bus));
    }

    // ---------- Feature 3 ----------
    private static void bookTickets() {
        System.out.println("\n--- Book Tickets ---");
        Bus bus = askForBus();
        if (bus == null) return;
        if (bus.getAvailableCount() == 0) {
            System.out.println("This bus is fully booked. Please use option 7 to join the waitlist.");
            return;
        }

        Printer.printSeatMap(bus);
        Printer.printPriceInfo(bus, service.currentPrice(bus), service.isSurgeActive(bus));
        List<String> seatNos = input.readSeatNumbers("Enter seat numbers (example 1A, 1B): ");

        String error = service.checkSeatsForBooking(bus, seatNos);
        if (error != null) {
            System.out.println("Booking stopped. " + error);
            return;
        }

        List<Passenger> passengers = new ArrayList<>();
        for (String seatNo : seatNos) {
            System.out.println("\nPassenger details for seat " + seatNo + ":");
            String name = input.readName("  Name: ");
            int age = input.readInt("  Age (1-120): ", 1, 120);
            String gender = input.readGender("  Gender (M / F / Other): ");
            passengers.add(new Passenger(name, age, gender));
        }

        // Female co-seat safety rule (needs the passengers' gender, so it runs after the details)
        String ruleError = service.checkCoSeatRule(bus, seatNos, passengers);
        if (ruleError != null) {
            System.out.println();
            System.out.println(ruleError);
            System.out.println("No seats were booked.");
            return;
        }

        // Show the price (including any surge) BEFORE the booking is confirmed
        List<Double> fares = service.previewFares(bus, seatNos.size());
        Printer.printFareSummary(bus, seatNos, fares);
        if (!input.readYesNo("Confirm booking? (Y/N): ")) {
            System.out.println("Booking cancelled. No seats were booked.");
            return;
        }

        Booking booking = service.book(bus, seatNos, passengers);
        Printer.printReceipt(bus, booking);
    }

    // ---------- Feature 4 ----------
    private static void cancelTickets() {
        System.out.println("\n--- Cancel Tickets ---");
        String pnr = input.readText("Enter PNR (example PNR-84920): ").toUpperCase();
        if (pnr.matches("\\d+")) pnr = "PNR-" + pnr;   // allow typing just the digits

        Booking booking = service.findBooking(pnr);
        if (booking == null) {
            System.out.println("Error: no booking found with PNR " + pnr + ".");
            return;
        }
        if (booking.getActiveTickets().isEmpty()) {
            System.out.println("All seats in " + pnr + " are already cancelled.");
            return;
        }

        Printer.printActiveTickets(booking);
        List<String> seatsToCancel = new ArrayList<>();
        if (input.readYesNo("\nCancel ALL active seats in this booking? (Y/N): ")) {
            for (Ticket t : booking.getActiveTickets()) seatsToCancel.add(t.getSeatNo());
        } else {
            seatsToCancel = input.readSeatNumbers("Enter the seat numbers to cancel (example 1B): ");
        }

        String error = service.checkSeatsForCancel(booking, seatsToCancel);
        if (error != null) {
            System.out.println("Cancellation stopped. " + error);
            return;
        }

        CancellationResult result = service.cancelSeats(booking, seatsToCancel);
        System.out.println();
        System.out.println("CANCELLATION CONFIRMED");
        System.out.println("PNR              : " + pnr);
        System.out.println("Seats cancelled  : " + String.join(", ", seatsToCancel));
        System.out.println("Refund amount    : " + Printer.money(result.getRefund()));
        showWaitlistOutcome(booking, seatsToCancel, result);
    }

    /** Tells the user which freed seats went to the waitlist and which are simply available again. */
    private static void showWaitlistOutcome(Booking cancelled, List<String> seatsCancelled, CancellationResult result) {
        for (String note : result.getNotes()) {
            System.out.println("Waitlist note    : " + note);
        }
        List<String> promotedSeats = new ArrayList<>();
        Bus bus = service.findBus(cancelled.getRouteId());
        for (Booking promoted : result.getPromotedBookings()) {
            Ticket t = promoted.getTickets().get(0);
            promotedSeats.add(t.getSeatNo());
            System.out.println();
            System.out.println("WAITLIST PROMOTION: " + t.getPassenger().getName() + " is now CONFIRMED in seat "
                    + t.getSeatNo() + " (new PNR " + promoted.getPnr() + ").");
            Printer.printReceipt(bus, promoted);
        }
        for (String seatNo : seatsCancelled) {
            if (!promotedSeats.contains(seatNo)) {
                System.out.println("Seat " + seatNo + " is now available again.");
            }
        }
    }

    // ---------- Feature 5 ----------
    private static void showManifest() {
        Bus bus = askForBus();
        if (bus != null) Printer.printManifest(bus, service.getManifest(bus));
    }

    // ---------- Change 3 ----------
    private static void joinWaitlist() {
        System.out.println("\n--- Join Waitlist ---");
        Bus bus = askForBus();
        if (bus == null) return;

        String problem = service.checkCanJoinWaitlist(bus);
        if (problem != null) {
            System.out.println("Cannot join waitlist. " + problem);
            return;
        }

        String name = input.readName("  Name: ");
        int age = input.readInt("  Age (1-120): ", 1, 120);
        String gender = input.readGender("  Gender (M / F / Other): ");

        int position = service.joinWaitlist(bus, new Passenger(name, age, gender));
        System.out.println("Added to the waitlist for " + bus.getRouteId() + ". Position: " + position
                + " of " + Waitlist.MAX_PER_BUS + ".");
        System.out.println("If a seat is cancelled, you will be confirmed automatically with a new PNR.");
    }

    private static void showWaitlist() {
        Bus bus = askForBus();
        if (bus != null) Printer.printWaitlist(bus, service.getWaitlist(bus));
    }
}
