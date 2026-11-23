import java.io.UncheckedIOException;
import java.time.LocalDateTime;
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
                int choice = input.readInt("Enter your choice: ", 0, 2);
                try {
                    switch (choice) {
                        case 1 -> addBus();
                        case 2 -> Printer.printBusList(service.getBuses());
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
        System.out.println("0. Exit");
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
}