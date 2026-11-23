/** The two bus layouts NammaRoute supports. */
public enum BusType {
    SEATER_2X2("2x2 Seater"),
    SLEEPER_2X1("2x1 Sleeper");

    private final String label;

    BusType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}