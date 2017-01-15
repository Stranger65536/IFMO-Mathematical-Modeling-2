package plot;

import com.google.common.base.Preconditions;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;

public class Plot extends JFrame {
    private static final int LABEL_PERIOD = 25;
    private static final double RANGE_PADDING = 0.00;
    private static final NumberFormat FORMAT = new DecimalFormat("0.###########E0");
    private final XYSeriesCollection dataset;
    private final JFreeChart chart;

    public Plot(
            final String title,
            final String labelX,
            final String labelY,
            final int width,
            final int height) {
        super(title);
        customizeWindow(width, height);

        dataset = new XYSeriesCollection();
        chart = createChart(title, labelX, labelY);
        final ChartPanel chartPanel = new ChartPanel(chart);
        this.getContentPane().add(chartPanel, BorderLayout.CENTER);
    }

    private static void customizePlot(final XYPlot plot) {
        plot.setBackgroundPaint(Color.lightGray);
        //noinspection MagicNumber
        plot.setBackgroundPaint(new Color(235, 235, 235));
        plot.setRangeGridlinePaint(Color.DARK_GRAY);
        plot.setDomainGridlinePaint(Color.DARK_GRAY);
    }

    private static void customizeRangeAxis(final XYPlot plot) {
        final NumberAxis range = (NumberAxis) plot.getRangeAxis();
        range.setRange(0.0, 1.0);
        //noinspection MagicNumber
        range.setTickUnit(new NumberTickUnit(0.025));
        plot.setDomainGridlinesVisible(true);
    }

    private static void customizeDomainAxis(final XYPlot plot) {
        final NumberAxis domain = (NumberAxis) plot.getDomainAxis();
        domain.setRange(0.00, 1.00);
        //noinspection MagicNumber
        domain.setTickUnit(new NumberTickUnit(0.025));
        domain.setVerticalTickLabels(true);
    }

    private static void customizeRenderer(final XYPlot plot, final int seriesIndex) {
        ((XYLineAndShapeRenderer) plot.getRenderer()).setSeriesLinesVisible(seriesIndex, true);
        ((XYLineAndShapeRenderer) plot.getRenderer()).setSeriesShapesVisible(seriesIndex, false);
    }

    private void customizeWindow(final int width, final int height) {
        this.setSize(width, height);
        this.getContentPane().setLayout(new BorderLayout());
        final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    private JFreeChart createChart(
            final String title,
            final String labelX,
            final String labelY) {
        final JFreeChart chartXY = ChartFactory.createXYLineChart(
                title,                      // chart title
                labelX,                     // x axis label
                labelY,                     // y axis label
                this.dataset,               // data
                PlotOrientation.VERTICAL,
                true,                       // include legend
                true,                       // tooltips
                false                       // urls
        );
        chartXY.setBackgroundPaint(Color.white);

        final XYPlot plot = chartXY.getXYPlot();

        customizePlot(plot);
        customizeDomainAxis(plot);
        customizeRangeAxis(plot);

        return chartXY;
    }

    public void addGraph(
            final String name,
            final double[] x,
            final double[] y) {
        Preconditions.checkNotNull(x, y);
        Preconditions.checkArgument(x.length == y.length);

        final XYSeries series = new XYSeries(name);

        for (int i = 0; i < x.length; i++) {
            series.add(x[i], y[i]);
        }

        this.dataset.addSeries(series);
        customizeRenderer((XYPlot) this.chart.getPlot(), this.dataset.getSeries().size() - 1);
    }

    @SuppressWarnings("AccessToNonThreadSafeStaticField")
    public void autoScale() {
        boolean atLeastOneSeriesDefined = false;
        double maxX = 0.0, maxY = 0.0, minX = 0.0, minY = 0.0;

        for (Object o : this.dataset.getSeries()) {
            final XYSeries series = (XYSeries) o;

            if (series == null) {
                continue;
            }

            atLeastOneSeriesDefined = true;
            maxX = series.getMaxX() > maxX ? series.getMaxX() : maxX;
            maxY = series.getMaxY() > maxY ? series.getMaxY() : maxY;
            minX = series.getMinX() < minX ? series.getMinX() : minX;
            minY = series.getMinY() < minY ? series.getMinY() : minY;
        }

        final double rangeX = maxX - minX;
        final double rangeY = maxY - minY;

        if (atLeastOneSeriesDefined) {
            final NumberAxis domain = (NumberAxis) ((XYPlot) chart.getPlot()).getDomainAxis();
            domain.setRange(minX - rangeX * RANGE_PADDING, maxX + rangeX * RANGE_PADDING);
            domain.setTickUnit(new NumberTickUnit(LABEL_PERIOD * (maxX - minX) / this.getSize().getWidth()));
            final NumberAxis range = (NumberAxis) ((XYPlot) chart.getPlot()).getRangeAxis();
            range.setRange(minY - rangeY * RANGE_PADDING, maxY + rangeY * RANGE_PADDING);
            range.setTickUnit(new NumberTickUnit(LABEL_PERIOD * (maxY - minY) / this.getSize().getHeight(), FORMAT));
        }
    }
}