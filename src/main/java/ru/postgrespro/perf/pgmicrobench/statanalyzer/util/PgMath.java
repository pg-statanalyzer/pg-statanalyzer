package ru.postgrespro.perf.pgmicrobench.statanalyzer.util;

import org.apache.commons.math3.special.Gamma;
import org.apache.commons.math3.util.FastMath;

public class PgMath {

    public static double squareGammaDoubleGammaRatio(double x) {
        return FastMath.exp(2 * Gamma.logGamma(x + 1) - Gamma.logGamma(2 * x + 1));
    }

    /**
     * Inverse of the function value = x * Gamma(x)^2 / Gamma(2 * x) / 2.
     * aka value = Gamma(x + 1)^2 / Gamma(2 * x + 1).
     * D(f) = (0; 1)
     * E(f) = (0; +inf)
     *
     * @param value value in range [0, 1].
     * @return x.
     */
    public static double invSquareGammaDoubleGammaRatio(double value) {
        if (value > 1 || value < 0) {
            return Double.NaN;
        }

        double left = 1e-10;
        double right = 50;

        for (int i = 0; i < 100; i++) {
            double m = (right + left) / 2;

            if (squareGammaDoubleGammaRatio(m) < value) {
                right = m;
            } else {
                left = m;
            }
        }

        return (left + right) / 2;
    }

    /**
     * Inverse of the function value = -x * Gamma(-x)^2 / Gamma(-2 * x) / 2.
     * aka value = Gamma(-x + 1)^2 / Gamma(-2 * x + 1).
     * Domain(f) = [0; 1]
     * E(f) = [0; 0.5]
     *
     * @param value value in range [0, 1].
     * @return x.
     */
    public static double minusInvSquareGammaDoubleGammaRatio(double value) {
        if (value > 1 || value < 0) {
            return Double.NaN;
        } else if (value == 1.0) {
            return 0;
        } else if (value == 0.0) {
            return 0.5;
        }

        double left = 0;
        double right = 0.5;

        for (int i = 0; i < 100; i++) {
            double m = (right + left) / 2;

            if (squareGammaDoubleGammaRatio(-m) < value) {
                right = m;
            } else {
                left = m;
            }
        }

        return (left + right) / 2;
    }
}
