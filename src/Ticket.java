/** One seat inside a booking: who sits there, what they paid, and whether it was cancelled. */
public class Ticket {
    private final String pnr;
    private final String seatNo;
    private final Passenger passenger;
    private final double fare;
    private boolean cancelled;
    private double refundAmount;

    public Ticket(String pnr, String seatNo, Passenger passenger, double fare) {
        this.pnr = pnr;
        this.seatNo = seatNo;
        this.passenger = passenger;
        this.fare = fare;
    }

    public void cancel(double refundAmount) {
        this.cancelled = true;
        this.refundAmount = refundAmount;
    }

    public String getPnr() { return pnr; }
    public String getSeatNo() { return seatNo; }
    public Passenger getPassenger() { return passenger; }
    public double getFare() { return fare; }
    public boolean isCancelled() { return cancelled; }
    public double getRefundAmount() { return refundAmount; }
}