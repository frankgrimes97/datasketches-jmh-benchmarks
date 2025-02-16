package org.apache.datasketches;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.knowm.xchart.PdfboxGraphicsEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.Styler;
import org.openjdk.jmh.runner.RunnerException;

public class CreateXChartPDF {
  public static final class Series {
    private final String name;

    private final List<Integer> xData = new ArrayList<>();
    private final List<Double> yData = new ArrayList<>();

    public Series( final String offheap, final String tgtHllType, final String filename) throws IOException {
      this("", offheap, tgtHllType, filename);
    }

    public Series(final String javaVersion, final String offheap, final String tgtHllType, final String filename) throws IOException {
      this.name = javaVersion + tgtHllType + (Boolean.parseBoolean(offheap) ? " (offheap)" : "");

      for (String line : Files.readAllLines(new File(filename).toPath())) {
        if (line.contains("InU")) {
          continue; // skip header
        }
        final String[] split = line.split("\\t");
        if (split.length == 3) {
          appendValue(Integer.parseInt(split[0]), Double.parseDouble(split[2]));
        }
      }
    }

    public String getName() {
      return name;
    }

    public List<Integer> getXData() {
      return xData;
    }

    public List<Double> getYData() {
      return yData;
    }

    public void appendValue(final int x, final double y) {
      xData.add(x);
      yData.add(y);
    }

    public void appendValues(final Series series) {
      xData.addAll(series.xData);
      yData.addAll(series.yData);
    }
  }

  public static final XYChart getChart(final String title, final List<Series> seriesList) {
    final XYChart chart = new XYChartBuilder()
      .width(1280)
      .height(1024)
      .theme(Styler.ChartTheme.GGPlot2)
      .title(title)
      .xAxisTitle("uniques")
      .yAxisTitle("ns/op")
      .build();

    chart.getStyler().setXAxisMaxLabelCount(30);
    chart.getStyler().setXAxisLogarithmic(true);

    for (Series series : seriesList) {
      chart.addSeries(series.getName(), series.getXData(), series.getYData());
    }

    return chart;
  }

  public static void main(String[] args) throws ClassNotFoundException, IOException, RunnerException {
    final List<XYChart> charts = new ArrayList<>();

    final String baseInputPath = "sample-chart-inputs/";

    final String pathJava8 = baseInputPath + "java8/";
    charts.add(getChart("HllUpdateSpeedProfile (Java 8)",
       Arrays.asList(
         new Series("false", "HLL_4", pathJava8 + "BaseUpdateSpeedProfileHLL4.txt"),
         new Series("false", "HLL_6", pathJava8 + "BaseUpdateSpeedProfileHLL6.txt"),
         new Series("false", "HLL_8", pathJava8 + "BaseUpdateSpeedProfileHLL8.txt"),
         new Series("true", "HLL_4", pathJava8 + "BaseUpdateSpeedProfileHLL4-offheap.txt"),
         new Series("true", "HLL_6", pathJava8 + "BaseUpdateSpeedProfileHLL6-offheap.txt"),
         new Series("true", "HLL_8", pathJava8 + "BaseUpdateSpeedProfileHLL8-offheap.txt")
       )
    ));

    final String pathJava11 = baseInputPath + "java11/";
    charts.add(getChart("HllUpdateSpeedProfile (Java 11)",
       Arrays.asList(
         new Series("false", "HLL_4", pathJava11 + "BaseUpdateSpeedProfileHLL4.txt"),
         new Series("false", "HLL_6", pathJava11 + "BaseUpdateSpeedProfileHLL6.txt"),
         new Series("false", "HLL_8", pathJava11 + "BaseUpdateSpeedProfileHLL8.txt"),
         new Series("true", "HLL_4", pathJava11 + "BaseUpdateSpeedProfileHLL4-offheap.txt"),
         new Series("true", "HLL_6", pathJava11 + "BaseUpdateSpeedProfileHLL6-offheap.txt"),
         new Series("true", "HLL_8", pathJava11 + "BaseUpdateSpeedProfileHLL8-offheap.txt")
       )
    ));

    final String pathJava17 = baseInputPath + "java17/";
    charts.add(getChart("HllUpdateSpeedProfile (Java 17)",
       Arrays.asList(
         new Series("false", "HLL_4", pathJava17 + "BaseUpdateSpeedProfileHLL4.txt"),
         new Series("false", "HLL_6", pathJava17 + "BaseUpdateSpeedProfileHLL6.txt"),
         new Series("false", "HLL_8", pathJava17 + "BaseUpdateSpeedProfileHLL8.txt"),
         new Series("true", "HLL_4", pathJava17 + "BaseUpdateSpeedProfileHLL4-offheap.txt"),
         new Series("true", "HLL_6", pathJava17 + "BaseUpdateSpeedProfileHLL6-offheap.txt"),
         new Series("true", "HLL_8", pathJava17 + "BaseUpdateSpeedProfileHLL8-offheap.txt")
       )
    ));

    final String pdfFileName = String.format("HllUpdateSpeedProfileXChart.pdf");
    try (FileOutputStream fos = new FileOutputStream(pdfFileName);
         BufferedOutputStream bos = new BufferedOutputStream(fos)) {
      PdfboxGraphicsEncoder.savePdfboxGraphics(charts, bos);
    }
    

    final String pdfFileName2 = String.format("HllUpdateSpeedProfileOffheapXChart.pdf");
    try (FileOutputStream fos = new FileOutputStream(pdfFileName2);
         BufferedOutputStream bos = new BufferedOutputStream(fos)) {
      PdfboxGraphicsEncoder.savePdfboxGraphics(
        getChart("HllUpdateSpeedProfile",
        Arrays.asList(
//          new Series("java8 ", "true", "HLL_4", pathJava8 + "BaseUpdateSpeedProfileHLL4-offheap.txt"),
          new Series("java8 ", "true", "HLL_6", pathJava8 + "BaseUpdateSpeedProfileHLL6-offheap.txt"),
//          new Series("java8 ", "true", "HLL_8", pathJava8 + "BaseUpdateSpeedProfileHLL8-offheap.txt")
//         new Series("java11 ", "true", "HLL_4", pathJava11 + "BaseUpdateSpeedProfileHLL4-offheap.txt"),
         new Series("java11 ", "true", "HLL_6", pathJava11 + "BaseUpdateSpeedProfileHLL6-offheap.txt"),
//         new Series("java11 ", "true", "HLL_8", pathJava11 + "BaseUpdateSpeedProfileHLL8-offheap.txt"),
//         new Series("java17 ", "true", "HLL_4", pathJava17 + "BaseUpdateSpeedProfileHLL4-offheap.txt"),
         new Series("java17 ", "true", "HLL_6", pathJava17 + "BaseUpdateSpeedProfileHLL6-offheap.txt")
//         new Series("java17 ", "true", "HLL_8", pathJava17 + "BaseUpdateSpeedProfileHLL8-offheap.txt")
        )),
        bos);
    }
  }
}
