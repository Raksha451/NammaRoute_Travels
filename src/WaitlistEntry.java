import java.time.LocalDateTime;

/** One person waiting for a seat on a full bus. */
public class WaitlistEntry {
    private final String routeId;
    private final Passenger passenger;
    private final LocalDateTime joinedAt;

    public WaitlistEntry(String routeId, Passenger passenger, LocalDateTime joinedAt) {
        this.routeId = routeId;
        this.passenger = passenger;
        this.joinedAt = joinedAt;
    }

    public String getRouteId() { return routeId; }
    public Passenger getPassenger() { return passenger; }
    public LocalDateTime getJoinedAt() { return joinedAt; }
}
