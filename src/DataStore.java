import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves and loads everything from CSV files. This class knows NOTHING about menus or booking rules.
 *
 * buses.csv : routeId,origin,destination,departure,basePrice,busType,bookedSeats(separated by |)
 */
public class DataStore {
    private final Path busFile;

    public DataStore(String folder) {
        try {
            Files.createDirectories(Paths.get(folder));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not create data folder", e);
        }
        busFile = Paths.get(folder, "buses.csv");
    }

    // ---------- Buses ----------

    public List<Bus> loadBuses() {
        List<Bus> buses = new ArrayList<>();
        for (String line : readLines(busFile)) {
            String[] p = line.split(",", -1);
            Bus bus = new Bus(p[0], p[1], p[2], LocalDateTime.parse(p[3], Bus.DATE_FORMAT),
                    Double.parseDouble(p[4]), BusType.valueOf(p[5]));
            for (String seatNo : p[6].split("\\|")) {
                Seat seat = bus.getSeat(seatNo);
                if (seat != null) seat.setBooked(true);
            }
            buses.add(bus);
        }
        return buses;
    }

    public void saveBuses(List<Bus> buses) {
        List<String> lines = new ArrayList<>();
        for (Bus b : buses) {
            lines.add(String.join(",", b.getRouteId(), b.getOrigin(), b.getDestination(),
                    b.getDeparture().format(Bus.DATE_FORMAT), String.valueOf(b.getBasePrice()),
                    b.getType().name(), String.join("|", b.getBookedSeatNumbers())));
        }
        writeLines(busFile, lines);
    }

    // ---------- Helpers ----------

    private List<String> readLines(Path file) {
        List<String> result = new ArrayList<>();
        if (!Files.exists(file)) return result;
        try {
            for (String line : Files.readAllLines(file)) {
                if (!line.isBlank()) result.add(line);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read " + file, e);
        }
        return result;
    }

    private void writeLines(Path file, List<String> lines) {
        try {
            Files.write(file, lines);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save " + file, e);
        }
    }
}
