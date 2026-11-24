import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** One booking transaction. A single PNR can hold several tickets (one per seat). */
public class Booking {
    private final String pnr;
    private final String routeId;
    private final LocalDateTime bookingTime;
    private final List<Ticket> tickets = new ArrayList<>();

    public Booking(String pnr, String routeId, LocalDateTime bookingTime) {
        this.pnr = pnr;
        this.routeId = routeId;
        this.bookingTime = bookingTime;
    }

    public void addTicket(Ticket ticket) {
        tickets.add(ticket);
    }

    /** Tickets that have not been cancelled. */
    public List<Ticket> getActiveTickets() {
        List<Ticket> active = new ArrayList<>();
        for (Ticket t : tickets) {
            if (!t.isCancelled()) active.add(t);
        }
        return active;
    }

    public Ticket findActiveTicket(String seatNo) {
        for (Ticket t : tickets) {
            if (!t.isCancelled() && t.getSeatNo().equals(seatNo)) return t;
        }
        return null;
    }

    /** Total amount originally paid for all seats in this booking. */
    public double getTotalPaid() {
        double total = 0;
        for (Ticket t : tickets) total += t.getFare();
        return total;
    }

    public String getPnr() { return pnr; }
    public String getRouteId() { return routeId; }
    public LocalDateTime getBookingTime() { return bookingTime; }
    public List<Ticket> getTickets() { return tickets; }
}
