/** One seat (or berth) on a bus. */
public class Seat {
    private final String number;   // e.g. "1A" or "L5"
    private boolean booked;

    public Seat(String number) {
        this.number = number;
    }

    public String getNumber() { return number; }
    public boolean isBooked() { return booked; }
    public void setBooked(boolean booked) { this.booked = booked; }
}