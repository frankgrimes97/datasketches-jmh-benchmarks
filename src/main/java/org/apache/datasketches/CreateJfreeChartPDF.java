package org.apache.datasketches;

import de.rototor.pdfbox.graphics2d.PdfBoxGraphics2D;
import java.awt.Rectangle;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.jfree.chart.JFreeChart;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.data.xy.DefaultXYDataset;

public class CreateJfreeChartPDF {
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

    public double[][] getJFreeChartData() {
      final double[][] data = new double[2][xData.size()];
      data[0] = xData.stream().mapToDouble(Integer::doubleValue).toArray();
      data[1] = yData.stream().mapToDouble(Double::valueOf).toArray();

      return data;
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

  public static final JFreeChart getChart(final String title, final List<Series> seriesList) {
    final DefaultXYDataset xyDataset = new DefaultXYDataset();
    for (Series series : seriesList) {
      xyDataset.addSeries(series.getName(), series.getJFreeChartData());
    }

    final JFreeChart chart = ChartFactory.createXYLineChart(
      title,
      "",
      "ns/op",
      xyDataset);

    chart.getLegend().setPosition(RectangleEdge.RIGHT);

    final LogAxis xAxis = new LogAxis("uniques");
    xAxis.setBase(2);
    chart.getXYPlot().setDomainAxis(xAxis);

    return chart;
  }

    /**
   * Write multiple charts to an OutputStream
   *
   * @param charts List&lt;? extends Chart&gt;
   * @param os OutputStream
   * @throws IOException
   */
  public static void savePdfboxGraphics(List<? extends JFreeChart> charts, OutputStream os)
      throws IOException {

    PDDocument document = new PDDocument();
    PDRectangle mediaBox = null;
    PDPage page = null;
    PDPageContentStream contentStream = null;
    PdfBoxGraphics2D pdfBoxGraphics2D = null;
    PDFormXObject xform = null;
    final int width = 1280;
    final int height = 1024;
    for (JFreeChart chart : charts) {
      mediaBox = new PDRectangle(width, height);
      page = new PDPage(mediaBox);
      // add page
      document.addPage(page);
      pdfBoxGraphics2D = new PdfBoxGraphics2D(document, width, height);
      chart.draw(pdfBoxGraphics2D, new Rectangle(0, 0, width, height));
      pdfBoxGraphics2D.dispose();
      xform = pdfBoxGraphics2D.getXFormObject();

      contentStream = new PDPageContentStream(document, page);
      contentStream.drawForm(xform);
      contentStream.close();
    }

    document.save(os);
    document.close();
  }

  public static void main(String[] args) throws ClassNotFoundException, IOException {
    final List<JFreeChart> charts = new ArrayList<>();

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

    final String pdfFileName = String.format("HllUpdateSpeedProfileJFreeChart.pdf");
    try (FileOutputStream fos = new FileOutputStream(pdfFileName);
         BufferedOutputStream bos = new BufferedOutputStream(fos)) {
      savePdfboxGraphics(charts, bos);
    }
    

    final String pdfFileName2 = String.format("HllUpdateSpeedProfileOffheapFreeChart.pdf");
    try (FileOutputStream fos = new FileOutputStream(pdfFileName2);
         BufferedOutputStream bos = new BufferedOutputStream(fos)) {
      savePdfboxGraphics(
        Arrays.asList(
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
            )
          )
        ),
        bos);
    }
  }
}
