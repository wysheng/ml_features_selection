package at.tuwien.evaluators;

import at.tuwien.FSResult;
import org.apache.commons.io.FilenameUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Created by lukas on 1/23/17.
 */
public class PerClassifierAccuracyEvaluator implements IEvaluator{
    @Override
    public void evaluate(List<FSResult> results) {
        results.parallelStream()
                .collect(Collectors.groupingBy(r -> r.datasetName))
                .forEach((k,v) -> createChartForDataset(v));
    }

    private void createChartForDataset(List<FSResult> resultList) {

        List<JFreeChart> resultCharts = new ArrayList<>();

        Map<Integer, List<FSResult>> resultsPerFeatureNum = resultList.stream().collect(Collectors.groupingBy(r -> r.numOfFeatures));
        resultsPerFeatureNum.forEach((k,v) -> resultCharts.add(createBarChart(v)));


        for (JFreeChart resultChart : resultCharts) {
            int width = 800; /* Width of the image */
            int height = 600; /* Height of the image */
            String basename = FilenameUtils.getBaseName(resultList.get(0).datasetName);

            //TODO filename should contain numbers of features used in this graph, but no variable available, so using random numbers for filenames
            File BarChart = new File( "output/"+ basename + "-" + new Random().nextInt() +"-BarChartPerClassfierAccuracy.png" );
            try {
                ChartUtilities.saveChartAsPNG( BarChart , resultChart , width , height );

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public JFreeChart createBarChart(List<FSResult> resultList){
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        resultList.forEach(
                res -> dataset.addValue(res.evaluation.pctCorrect(), res.classifier, res.fsMethod)
        );

        JFreeChart barChart = ChartFactory.createBarChart(
                "Accuracy by Classifier and Featureselection method - #Features " + String.valueOf(resultList.get(0).numOfFeatures),
                "#Features", "%Accuracy",
                dataset, PlotOrientation.VERTICAL,
                true, true, false);
        return barChart;
    }
}
