import java.time.Duration;
import java.time.LocalDateTime;


public class RefundPolicy {
    public double calculateRefund(double farePaid, LocalDateTime departure, LocalDateTime now) {
        long hoursLeft = Duration.between(now, departure).toHours();
        if (hoursLeft >= 24) return farePaid * 0.90;
        if (hoursLeft >= 6) return farePaid * 0.50;
        return 0;
    }
}