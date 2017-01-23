package com.company;

import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by martensigwart on 23.01.17.
 */
public class FeatureMethodEvaluator implements IEvaluator {

    public void evaluate(List<FSResult> results) {

        XYDataset dataset = getXYDataSet(results);

    }

    private XYDataset getXYDataSet(List<FSResult> results) {
        Map<String, XYSeries> seriesMap = new HashMap<String, XYSeries>();

        //sort results by method
        Map<String, List<FSResult>> resultsByMethod = results
            .stream()
            .collect(Collectors.groupingBy(s -> s.fsMethod));

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
                            .collect(Collectors.averagingDouble(result -> result.evaluation.correct()));


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

        return dataset;

    }
}
