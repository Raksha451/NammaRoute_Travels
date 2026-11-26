/**
 * Female co-passenger safety rule (V2.0).
 * If a female passenger sits alone in a 2-seat pair, the empty seat next to her
 * can only be booked by another female passenger.
 * This class only holds the rule itself; BookingService decides when to ask it.
 */
public class CoSeatRule {

    /**
     * @param newPassenger the person who wants the seat
     * @param neighbour    the passenger already sitting in the paired seat (null if nobody)
     */
    public boolean isAllowed(Passenger newPassenger, Passenger neighbour) {
        if (neighbour == null) return true;                       // nobody next to the seat
        boolean neighbourIsFemale = neighbour.getGender().equals("F");
        boolean newIsFemale = newPassenger.getGender().equals("F");
        return !neighbourIsFemale || newIsFemale;                 // next to a female -> must be female
    }

    public String blockedMessage(String seatNo) {
        return "Sorry, seat " + seatNo + " is reserved for a female co-passenger. Please choose another seat.";
    }
}
