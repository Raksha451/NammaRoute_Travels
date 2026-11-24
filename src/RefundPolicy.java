import java.time.Duration;
import java.time.LocalDateTime;

/**
 * All refund rules live here.
 * V1.0 ASSUMPTION (confirm with the client!):
 *   24 hours or more before departure  -> 90% refund
 *   6 to 24 hours before departure     -> 50% refund
 *   less than 6 hours before departure -> no refund
 */
public class RefundPolicy {
    public double calculateRefund(double farePaid, LocalDateTime departure, LocalDateTime now) {
        long hoursLeft = Duration.between(now, departure).toHours();
        if (hoursLeft >= 24) return farePaid * 0.90;
        if (hoursLeft >= 6) return farePaid * 0.50;
        return 0;
    }
}