import com.google.common.primitives.Doubles;
import kahan.KahanSummation;
import plot.Plot;

import java.util.Collection;
import java.util.List;
import java.util.Random;

public class Main {
    private static final int POWER = 52;
    private static final int WINDOW_WIDTH = 1280;
    private static final int WINDOW_HEIGHT = 800;
    private static final int NOISE_COUNT = 1_000_000;
    private static final int NOISE_ACCUMULATION = 1_000_000;
    private static final double PRECISION = 0.0000000000000001;
    private static final double SQRT_MAX = 100.0;
    private static final double SQRT_STEP = 0.001;
    private static final double SQRT_MIN = 1.1;
    private static final double EXP_MIN = -10.05;
    private static final double EXP_MAX = 3.0;
    private static final double EXP_STEP = 0.001;
    @SuppressWarnings("NumericOverflow")
    private static final int DOTS_COUNT = (int) ((SQRT_MAX - SQRT_MIN) / SQRT_STEP) + 2;
    @SuppressWarnings("NumericOverflow")
    private static final int EXP_DOTS_COUNT = (int) ((EXP_MAX - EXP_MIN) / EXP_STEP) + 2;
    private static final String RELATIVE_ERROR = "Relative Error";
    private static final String SQUARE_ROOT = "Square root";

    /******************************************************************************************************************/

    @SuppressWarnings({"OverlyComplexArithmeticExpression", "MagicNumber"})
    private static double getTaylorSqrt(final double x) {
        return x / 2
                - x * x / 8
                + x * x * x / 16
                - 5 * x * x * x * x / 128
                + 7 * x * x * x * x * x / 256;
    }

    private static double getTaylorSqr(final double x) {
        return 2 * x + x * x;
    }

    private static double getDeepSqrt(final double x) {
        double result = x;

        for (int i = 0; i < POWER; i++) {
            result = Math.sqrt(result);
        }

        return result;
    }

    private static double getDeepSqr(final double x) {
        double result = x;

        for (int i = 0; i < POWER; i++) {
            result *= result;
        }

        return result;
    }

    @SuppressWarnings({"MagicNumber", "IfMayBeConditional"})
    private static double getDeepSqrtOptimized(final double x) {
        double result = x;

        for (int i = 0; i < POWER; i++) {
            if (result >= 1.0005) {
                result = Math.sqrt(result);
            } else if (result < 0.9995) {
                result = getTaylorSqrt(result);
            } else {
                result = getTaylorSqrt(result - 1);
            }
        }

        return result;
    }

    @SuppressWarnings("MagicNumber")
    private static double getDeepSqrOptimized(final double x) {
        double result = x;

        for (int i = 0; i < POWER; i++) {
            if (result < 0.0005) {
                result = getTaylorSqr(result);
            } else if (result >= 1.0005) {
                result *= result;
            } else {
                result = getTaylorSqr(result) + 1;
            }
        }

        return result;
    }

    private static void performSquareRootPart() {
        final Plot plot1 = new Plot(
                SQUARE_ROOT, "X", RELATIVE_ERROR,
                WINDOW_WIDTH, WINDOW_HEIGHT);

        final Plot plot2 = new Plot(
                SQUARE_ROOT, "X", RELATIVE_ERROR,
                WINDOW_WIDTH, WINDOW_HEIGHT);

        final double[] x = new double[DOTS_COUNT];
        final double[] y = new double[DOTS_COUNT];
        final double[] optimized = new double[DOTS_COUNT];
        int i = 0;

        for (double current = SQRT_MIN; current < SQRT_MAX; current += SQRT_STEP) {
            x[i] = current;
            final double squareRooted = getDeepSqrt(current);
            final double squareRootedOptimized = getDeepSqrtOptimized(current);
            final double restored = getDeepSqr(squareRooted);
            final double restoredOptimized = getDeepSqrOptimized(squareRootedOptimized);
            final double error = Math.abs(current - restored) / current;
            final double errorOptimized = Math.abs(current - restoredOptimized) / current;
            y[i] = error;
            optimized[i] = errorOptimized;
            i++;
        }

        plot1.addGraph("Relative error for naive sqrt extraction", x, y);
        plot2.addGraph("Relative error for Taylor optimize sqrt extraction", x, optimized);
        plot1.autoScale();
        plot2.autoScale();
        plot1.setVisible(true);
        plot2.setVisible(true);
    }

    /******************************************************************************************************************/

    private static double getNaiveTaylorExp(final double x) {
        int k = 0;
        double res = 0, current = 0, last;
        double numer = 1;
        double denumer = 1;

        do {
            last = current;
            current = numer / denumer;
            res += current;
            numer *= x;
            k++;
            denumer *= k;
        } while (Math.abs(last - current) > PRECISION);

        return res;
    }

    private static double getOptimizedTaylorExp(final double x) {
        int k = 0;
        double res = 0, current = 0, last;
        double numer = 1;
        double denumer = 1;

        do {
            last = current;
            current = numer / denumer;
            res += current;
            numer *= Math.abs(x);
            k++;
            denumer *= k;
        } while (Math.abs(last - current) > PRECISION);

        return x > 0 ? res : 1 / res;
    }

    @SuppressWarnings("NonReproducibleMathCall")
    private static void performExpPart() {
        final Plot plot1 = new Plot(
                "Exp", "x", RELATIVE_ERROR,
                WINDOW_WIDTH, WINDOW_HEIGHT);

        final Plot plot2 = new Plot(
                "Exp", "x", RELATIVE_ERROR,
                WINDOW_WIDTH, WINDOW_HEIGHT);

        final double[] x = new double[EXP_DOTS_COUNT];
        final double[] original = new double[EXP_DOTS_COUNT];
        final double[] naiveTaylor = new double[EXP_DOTS_COUNT];
        final double[] optimizedTaylor = new double[EXP_DOTS_COUNT];
        final double[] naiveError = new double[EXP_DOTS_COUNT];
        final double[] optimizedError = new double[EXP_DOTS_COUNT];
        int i = 0;

        for (double current = EXP_MIN; current < EXP_MAX; current += EXP_STEP) {
            x[i] = current;
            original[i] = Math.exp(current);
            naiveTaylor[i] = getNaiveTaylorExp(current);
            optimizedTaylor[i] = getOptimizedTaylorExp(current);
            naiveError[i] = Math.abs(original[i] - naiveTaylor[i]) / original[i];
            optimizedError[i] = Math.abs(original[i] - optimizedTaylor[i]) / original[i];
            i++;
        }

        plot1.addGraph("Relative error for naive Taylor exp(x)", x, naiveError);
        plot2.addGraph("Relative error for optimized Taylor exp(x)", x, optimizedError);
        plot1.autoScale();
        plot2.autoScale();
        plot1.setVisible(true);
        plot2.setVisible(true);
    }

    /******************************************************************************************************************/

    private static double getDispersion(final double[] data, final double mean) {
        final List<Double> listData = Doubles.asList(data);
        final double[] sum = {0.0};

        listData.stream()
                .mapToDouble(value -> value)
                .forEach(value -> sum[0] += (value - mean) * (value - mean));

        return sum[0] / (listData.size() - 1);
    }

    private static double getMeanOptimized(final Collection<Double> listData) {
        final KahanSummation kahanSummation = new KahanSummation();
        listData.stream().forEach(kahanSummation::add);

        return kahanSummation.value() / listData.size();
    }

    private static double getMean(final Collection<Double> listData) {
        return listData
                .stream()
                .mapToDouble(value -> value)
                .average()
                .orElse(0);
    }

    private static void performDispersionPart() {
        //noinspection UnsecureRandomNumberGeneration
        final Random random = new Random();
        final double[] noise = Doubles.asList(new double[NOISE_COUNT])
                .stream()
                .mapToDouble(value -> random.nextGaussian())
                .toArray();

        final double[] accumulatedNoise = Doubles.asList(noise)
                .stream()
                .mapToDouble(value -> value + NOISE_ACCUMULATION)
                .toArray();

        final double noiseMean = getMean(Doubles.asList(noise));
        final double noiseMeanKahan = getMeanOptimized(Doubles.asList(noise));
        final double accumulatedNoiseMean = getMean(Doubles.asList(accumulatedNoise));
        final double accumulatedNoiseMeanKahan = getMeanOptimized(Doubles.asList(accumulatedNoise));

        final double noiseDispersion = getDispersion(noise, noiseMean);
        final double noiseDispersionKahan = getDispersion(noise, noiseMeanKahan);
        final double accumulatedNoiseDispersion = getDispersion(accumulatedNoise, accumulatedNoiseMean);
        final double accumulatedNoiseDispersionKahan = getDispersion(accumulatedNoise, accumulatedNoiseMeanKahan);

        System.out.println("Noise mean:                             " + noiseMean);
        System.out.println("Noise mean Kahan:                       " + noiseMeanKahan);
        System.out.println("Accumulated noise mean:                 " + accumulatedNoiseMean);
        System.out.println("Accumulated noise mean Kahan:           " + accumulatedNoiseMeanKahan);
        System.out.println("Noise dispersion:                       " + noiseDispersion);
        System.out.println("Noise dispersion Kahan:                 " + noiseDispersionKahan);
        System.out.println("Accumulated noise dispersion:           " + accumulatedNoiseDispersion);
        System.out.println("Accumulated noise dispersion optimized: " + accumulatedNoiseDispersionKahan);
    }

    /******************************************************************************************************************/

    public static void main(final String[] args) {
        performSquareRootPart();
        performExpPart();
        performDispersionPart();
    }

}