import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** Reads and validates what the user types. Keeps asking until the input is valid. */
public class InputHelper {
    private final Scanner scanner = new Scanner(System.in);

    /** Plain non-empty text. Commas and | are rejected because our CSV files use them. */
    public String readText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String text = scanner.nextLine().trim();
            if (text.isEmpty()) {
                System.out.println("  Error: this cannot be empty.");
            } else if (text.contains(",") || text.contains("|")) {
                System.out.println("  Error: please do not use ',' or '|'.");
            } else {
                return text;
            }
        }
    }

    public String readRouteId(String prompt) {
        while (true) {
            String id = readText(prompt).toUpperCase();
            if (id.matches("[A-Z0-9-]+")) return id;
            System.out.println("  Error: use only letters, digits and '-' (example: NR-101).");
        }
    }

    public int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String text = scanner.nextLine().trim();
            try {
                int value = Integer.parseInt(text);
                if (value >= min && value <= max) return value;
                System.out.println("  Error: enter a number between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("  Error: '" + text + "' is not a valid number.");
            }
        }
    }

    public double readPrice(String prompt) {
        while (true) {
            System.out.print(prompt);
            String text = scanner.nextLine().trim();
            try {
                double value = Double.parseDouble(text);
                if (value > 0 && value <= 100000) return value;
                System.out.println("  Error: price must be more than 0 (and at most 100000).");
            } catch (NumberFormatException e) {
                System.out.println("  Error: '" + text + "' is not a valid price.");
            }
        }
    }

    public LocalDateTime readDateTime(String prompt) {
        while (true) {
            System.out.print(prompt);
            String text = scanner.nextLine().trim();
            try {
                return LocalDateTime.parse(text, Bus.DATE_FORMAT);
            } catch (DateTimeParseException e) {
                System.out.println("  Error: use the format yyyy-MM-dd HH:mm (example: 2026-11-27 21:30).");
            }
        }
    }

    public String readName(String prompt) {
        while (true) {
            String name = readText(prompt);
            if (name.matches("[A-Za-z][A-Za-z .'-]*")) return name;
            System.out.println("  Error: name can contain only letters and spaces.");
        }
    }

    /** Returns "M", "F" or "Other". */
    public String readGender(String prompt) {
        while (true) {
            System.out.print(prompt);
            String text = scanner.nextLine().trim().toUpperCase();
            if (text.equals("M")) return "M";
            if (text.equals("F")) return "F";
            if (text.equals("OTHER")) return "Other";
            System.out.println("  Error: enter M, F or Other.");
        }
    }

    /** Reads seat numbers like "1a, 1B 2C" and returns them as ["1A","1B","2C"]. */
    public List<String> readSeatNumbers(String prompt) {
        System.out.print(prompt);
        String line = scanner.nextLine().trim().toUpperCase();
        List<String> seats = new ArrayList<>();
        for (String part : line.split("[,\\s]+")) {
            if (!part.isEmpty()) seats.add(part);
        }
        return seats;
    }

    public boolean readYesNo(String prompt) {
        while (true) {
            System.out.print(prompt);
            String text = scanner.nextLine().trim().toUpperCase();
            if (text.equals("Y") || text.equals("YES")) return true;
            if (text.equals("N") || text.equals("NO")) return false;
            System.out.println("  Error: please type Y or N.");
        }
    }
}