package com.company;

import org.apache.commons.io.FilenameUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



/**
 * Created by martensigwart on 23.01.17.
 */
public class FeatureMethodEvaluator implements IEvaluator {

    public void evaluate(List<FSResult> results) {

        Map<String, XYDataset> datasetMap = getXYDataSetMap(results);

        datasetMap.forEach((name, dataset) -> {
            JFreeChart chart = createChart(name, dataset);
            try {
                ChartUtilities.writeChartAsPNG(new FileOutputStream("output/"+name+"-LineGraph.png"), chart, 500, 500);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


    }

    private JFreeChart createChart(String title, XYDataset dataset) {
        // create the chart...
        final JFreeChart chart = ChartFactory.createXYLineChart(
                title,                    // chart title
                "# Features",             // x axis label
                "% rightly classified",   // y axis label
                dataset,                  // data
                PlotOrientation.VERTICAL,
                true,                     // include legend
                true,                     // tooltips
                false                     // urls
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        chart.setBackgroundPaint(Color.white);

//        final StandardLegend legend = (StandardLegend) chart.getLegend();
        //      legend.setDisplaySeriesShapes(true);

        // get a reference to the plot for further customisation...
        final XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.lightGray);
        //    plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);

        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesShapesVisible(1, false);
        plot.setRenderer(renderer);

        NumberAxis domain = (NumberAxis) plot.getDomainAxis();
        domain.setRange(0.00, 100.00);
        domain.setTickUnit(new NumberTickUnit(10.0));
        domain.setVerticalTickLabels(true);
        NumberAxis range = (NumberAxis) plot.getRangeAxis();
        range.setRange(0.0, 100.0);
        range.setTickUnit(new NumberTickUnit(10.0));
        // OPTIONAL CUSTOMISATION COMPLETED.

        return chart;
    }

    private Map<String, XYDataset> getXYDataSetMap(List<FSResult> results) {
        Map<String, XYDataset> datasetMap = new HashMap<>();

        //sort results by data set
        Map<String, List<FSResult>> resultsByDataset = results
            .stream()
            .collect(Collectors.groupingBy(result -> result.datasetName));


        resultsByDataset
            .forEach((n, resultList) -> {

            //sort results by method
            Map<String, List<FSResult>> resultsByMethod = resultList
                .stream()
                .collect(Collectors.groupingBy(s -> s.fsMethod));

            //create xy series for each method
            Map<String, XYSeries> seriesMap = new HashMap<>();
            resultsByMethod
            .forEach((fsmethod, fsResults) -> {

                //sort results by number of features
                Map<Integer, List<FSResult>> noFeatureResults = fsResults
                    .stream()
                    .collect(Collectors.groupingBy(o -> o.numOfFeatures));

                //Average results for each number of features
                noFeatureResults
                    .forEach((i, results1) -> {

                        Double avrg = results1.stream()
                            .collect(Collectors.averagingDouble(result -> result.evaluation.pctCorrect()));


                        //check if series already exists, if not create it
                        XYSeries series;
                        if (seriesMap.containsKey(fsmethod)) {
                            series = seriesMap.get(fsmethod);
                        } else {
                            series = new XYSeries(fsmethod);
                            seriesMap.put(fsmethod, series);
                        }

                        //add or update rightly classified percentage
                        series.addOrUpdate(i, avrg);

                    });

            });

            //add series to data set
            final XYSeriesCollection dataset = new XYSeriesCollection();
            seriesMap.forEach((name, xyseries) -> {
                dataset.addSeries(xyseries);
            });

            String basename = FilenameUtils.getBaseName(n);

            datasetMap.put(basename, dataset);

        });

        return datasetMap;

    }
}
