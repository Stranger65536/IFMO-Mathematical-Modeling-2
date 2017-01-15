import plot.Plot;

import static java.lang.StrictMath.*;

public class Main {
    public static final int WINDOW_WIDTH = 1280;
    public static final int WINDOW_HEIGHT = 800;
    public static final double LOG_MAX = 2.0;
    public static final double LOG_MAX_2 = 100.0;
    public static final double LOG_STEP = 0.001;
    public static final double LOG_MIN = 0.000001;
    public static final double LOG_MIN_2 = 0.000001;
    @SuppressWarnings("NumericOverflow")
    public static final int DOTS_COUNT = (int) ((LOG_MAX - LOG_MIN) / LOG_STEP) + 2;
    @SuppressWarnings("NumericOverflow")
    public static final int DOTS_COUNT_2 = (int) ((LOG_MAX_2 - LOG_MIN_2) / LOG_STEP) + 2;
    public static final String RELATIVE_ERROR = "Relative Error";

    @SuppressWarnings("OverlyComplexArithmeticExpression")
    private static double getTaylorLog(final double x) {
        final double y = x - 1;
        return y - y * y / 2
                + y * y * y / 3
                - y * y * y * y / 4
                + y * y * y * y * y / 5
                - y * y * y * y * y * y / 6;
    }

    @SuppressWarnings({"OverlyComplexArithmeticExpression", "MagicNumber"})
    private static double getChebyshevLog(final double x) {
        final double y = x - 1;
        return -0.3958333333333333
                - cos(6 * acos(y)) / 192
                + cos(5 * acos(y)) / 80
                - cos(4 * acos(y)) / 16
                + 7 * cos(3 * acos(y)) / 48
                - 29 * cos(2 * acos(y)) / 64
                + 11 * cos(acos(y)) / 8;
    }

    @SuppressWarnings({"OverlyComplexArithmeticExpression", "MagicNumber"})
    private static double getLagrangeLog(final double x) {
        final double y = x - 1;
        return -13.8164
                + 948.578 * y
                - 2763.64 * y * y
                + 1837.95 * y * y * y;
    }

    @SuppressWarnings({"MagicNumber", "SuspiciousNameCombination"})
    private static double getTaylorOptimized(final double x) {
        double y = x;
        int count = 0;

        while (y > 1.001 || y < 0.999) {
            y = sqrt(y);
            count++;
        }

        return pow(2, count) * getTaylorLog(y);
    }

    @SuppressWarnings({"NonReproducibleMathCall", "OverlyLongMethod"})
    public static void main(final String[] args) {
        final Plot plot1 = new Plot(
                "Log", "X", RELATIVE_ERROR,
                WINDOW_WIDTH, WINDOW_HEIGHT);
        final Plot plot2 = new Plot(
                "Log", "X", RELATIVE_ERROR,
                WINDOW_WIDTH, WINDOW_HEIGHT);
        final Plot plot3 = new Plot(
                "Log", "X", RELATIVE_ERROR,
                WINDOW_WIDTH, WINDOW_HEIGHT);
        final Plot plot4 = new Plot(
                "Log", "X", RELATIVE_ERROR,
                WINDOW_WIDTH, WINDOW_HEIGHT);
        final Plot plot5 = new Plot(
                "Log", "X", RELATIVE_ERROR,
                WINDOW_WIDTH, WINDOW_HEIGHT);

        final double[] x = new double[DOTS_COUNT];
        final double[] original = new double[DOTS_COUNT];
        final double[] taylor = new double[DOTS_COUNT];
        final double[] taylorError = new double[DOTS_COUNT];
        final double[] chebyshev = new double[DOTS_COUNT];
        final double[] chebyshevError = new double[DOTS_COUNT];
        final double[] lagrange = new double[DOTS_COUNT];
        final double[] lagrangeError = new double[DOTS_COUNT];

        final double[] taylorChebyshevError = new double[DOTS_COUNT];
        int i = 0;

        for (double current = LOG_MIN; current < LOG_MAX; current += LOG_STEP) {
            x[i] = current;
            original[i] = log(current);
            taylor[i] = getTaylorLog(current);
            taylorError[i] = abs((taylor[i] - original[i]) / original[i]);
            lagrange[i] = getLagrangeLog(current);
            lagrangeError[i] = abs((lagrange[i] - original[i]) / original[i]);
            chebyshev[i] = getChebyshevLog(current);
            chebyshevError[i] = abs((chebyshev[i] - original[i]) / original[i]);
            taylorChebyshevError[i] = abs((taylor[i] - chebyshev[i]) / taylor[i]);
            i++;
        }

        int i1 = 0;
        final double[] x1 = new double[DOTS_COUNT_2];
        final double[] original1 = new double[DOTS_COUNT_2];
        final double[] optimizedTaylor = new double[DOTS_COUNT_2];
        final double[] optimizedTaylorError = new double[DOTS_COUNT_2];

        for (double current = LOG_MIN_2; current < LOG_MAX_2; current += LOG_STEP) {
            x1[i1] = current;
            original1[i1] = log(current);
            optimizedTaylor[i1] = getTaylorOptimized(current);
            optimizedTaylorError[i1] = abs((optimizedTaylor[i1] - original1[i1]) / original1[i1]);
            i1++;
        }

        plot1.addGraph("Relative error for Taylor", x, taylorError);
        plot2.addGraph("Relative error for Chebyshev", x, chebyshevError);
        plot3.addGraph("Relative error for Lagrange", x, lagrangeError);
        plot4.addGraph("Relative error for Taylor & Chebyshev", x, taylorChebyshevError);
        plot5.addGraph("Relative error for Optimized Taylor", x1, optimizedTaylorError);
        plot1.autoScale();
        plot2.autoScale();
        plot3.autoScale();
        plot4.autoScale();
        plot5.autoScale();
        plot1.setVisible(true);
        plot2.setVisible(true);
        plot3.setVisible(true);
        plot4.setVisible(true);
        plot5.setVisible(true);
    }
}