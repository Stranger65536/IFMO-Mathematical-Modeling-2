import plot.Plot;

import static java.lang.StrictMath.*;

public class Main {
    public static final int WINDOW_WIDTH = 1280;
    public static final int WINDOW_HEIGHT = 800;
    public static final double SQRT_MAX = 20.0;
    public static final double SQRT_STEP = 0.001;
    public static final double SQRT_MIN = 0.0;
    @SuppressWarnings("NumericOverflow")
    public static final int DOTS_COUNT = (int) ((SQRT_MAX - SQRT_MIN) / SQRT_STEP) + 2;
    public static final double EPS = 5.0E-16;
    public static final String RELATIVE_ERROR = "Relative Error";

    private static double getFunction(final double x, final double constant) {
        return x * x - constant;
    }

    private static double getDerivative(final double x) {
        return 2 * x;
    }

    @SuppressWarnings({"NonReproducibleMathCall", "OverlyLongMethod"})
    public static void main(final String[] args) {
        final Plot plot1 = new Plot(
                "Log", "X", RELATIVE_ERROR,
                WINDOW_WIDTH, WINDOW_HEIGHT);

        final double[] x = new double[DOTS_COUNT];
        final double[] original = new double[DOTS_COUNT];
        final double[] newton = new double[DOTS_COUNT];
        final double[] error = new double[DOTS_COUNT];

        int i = 0;
        for (double current = SQRT_MIN; current < SQRT_MAX; current += SQRT_STEP) {
            x[i] = current;
            original[i] = sqrt(current);
            newton[i] = newtonSqrt(current);
            error[i] = Math.abs(original[i] - newton[i]) / original[i];
            i++;
        }

        plot1.addGraph(RELATIVE_ERROR, x, error);
        plot1.autoScale();
        plot1.setVisible(true);
    }

    private static double newtonSqrt(final double x) {
        double nextX = x;
        double currentX = 0;

        while (Math.abs(currentX - nextX) > EPS) {
            currentX = nextX;
            nextX = currentX - getFunction(currentX, x) / getDerivative(currentX);
        }

        return currentX;
    }
}