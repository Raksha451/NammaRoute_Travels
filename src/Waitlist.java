import java.util.ArrayList;
import java.util.List;

/**
 * The waiting queues for all buses. First in, first out (FIFO):
 * entries are kept in the order people joined, so the front of a bus's queue is its first entry.
 */
public class Waitlist {
    public static final int MAX_PER_BUS = 5;

    private final List<WaitlistEntry> entries = new ArrayList<>();

    public void add(WaitlistEntry entry) {
        entries.add(entry);
    }

    public void remove(WaitlistEntry entry) {
        entries.remove(entry);
    }

    /** The queue for one bus, front of the queue first. Returns a copy, so it is safe to loop and remove. */
    public List<WaitlistEntry> forBus(String routeId) {
        List<WaitlistEntry> queue = new ArrayList<>();
        for (WaitlistEntry e : entries) {
            if (e.getRouteId().equals(routeId)) queue.add(e);
        }
        return queue;
    }

    public boolean isFull(String routeId) {
        return forBus(routeId).size() >= MAX_PER_BUS;
    }

    /** Every entry for every bus, in joining order (used when saving to the file). */
    public List<WaitlistEntry> getAll() {
        return entries;
    }
}
