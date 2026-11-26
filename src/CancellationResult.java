import java.util.ArrayList;
import java.util.List;

/** What happened when seats were cancelled: the refund, and any waitlist passengers who got the freed seats. */
public class CancellationResult {
    private final double refund;
    private final List<Booking> promotedBookings = new ArrayList<>();   // new confirmed bookings
    private final List<String> notes = new ArrayList<>();               // e.g. "skipped Ravi ..."

    public CancellationResult(double refund) {
        this.refund = refund;
    }

    public void addPromotion(Booking booking) { promotedBookings.add(booking); }
    public void addNote(String note) { notes.add(note); }

    public double getRefund() { return refund; }
    public List<Booking> getPromotedBookings() { return promotedBookings; }
    public List<String> getNotes() { return notes; }
}
