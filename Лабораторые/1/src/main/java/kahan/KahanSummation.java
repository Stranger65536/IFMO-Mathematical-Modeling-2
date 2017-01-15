package kahan;

@SuppressWarnings("InstanceVariableMayNotBeInitialized")
public class KahanSummation {
    private double c;
    private double value;

    public double value() {
        return value;
    }

    public void add(final double v) {
        final double y = v - c;
        final double t = value + y;
        c = t - value - y;
        value = t;
    }
    
    public void clear() {
        value = c = 0;
    }
}
    
