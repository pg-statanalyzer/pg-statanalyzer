package ru.postgrespro.perf.pgmicrobench.statanalyzer.startpoint;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class LinearRegression {
    private double[] coefficients;
    private double intercept;

    public LinearRegression(String coefsJsonFilePath) {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(coefsJsonFilePath)) {
            if (in == null) {
                throw new IllegalArgumentException("Resource not found: " + coefsJsonFilePath);
            }

            String json = new BufferedReader(new InputStreamReader(in))
                    .lines()
                    .collect(Collectors.joining("\n"));

//            String json = new String(Files.readAllBytes(Path.of(coefsJsonFilePath)));
            Gson gson = new Gson();
            RegressionParams params = gson.fromJson(json, RegressionParams.class);
            this.coefficients = params.coefficients;
            this.intercept = params.intercept;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load regression parameters from file: " + coefsJsonFilePath, e);
        }
    }

    public double predict(double[] input) {
        double result = intercept;
        for (int i = 0; i < input.length; i++) {
            result += input[i] * coefficients[i];
        }
        return result;

    }

    private static class RegressionParams {
        double[] coefficients;
        double intercept;
    }
}
