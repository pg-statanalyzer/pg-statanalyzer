package ru.postgrespro.perf.pgmicrobench.statanalyzer.histogram;

import org.HdrHistogram.Histogram;
import org.HdrHistogram.HistogramIterationValue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Utility class for working with histograms.
 */
public class HistogramUtil {
    /**
     * Saves the given histogram to a CSV file, with bucketed values based on linear binning.
     *
     * @param histogram the histogram to save
     * @param csv the file to save the histogram data to
     */
    public static synchronized void saveHistogram2csv(Histogram histogram, File csv) {
        try (PrintWriter pw = new PrintWriter(csv)) {
            long bins = (long) Math.sqrt(histogram.getTotalCount() * 0.99) + 1;

            long maxValue = histogram.getValueAtPercentile(99.);
            long minValue = histogram.getMinValue();

            long width = (long) ((maxValue - minValue) / (double) bins) + 1;


            pw.println("bucket,value,width");
            synchronized (histogram) {
                for (HistogramIterationValue x : histogram.linearBucketValues(width)) {
                    if (x.getValueIteratedTo() > maxValue) {
                        break;
                    }
                    pw.println(x.getValueIteratedTo() - width / 2. + ", " + x.getCountAddedInThisIterationStep() + ", " + width);
                }
            }
        } catch (FileNotFoundException ignored) {
            System.out.println("File not found: " + csv);
        }
    }
}
