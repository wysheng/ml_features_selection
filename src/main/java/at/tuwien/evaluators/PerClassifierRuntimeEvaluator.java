package at.tuwien.evaluators;

import at.tuwien.FSResult;
import org.apache.commons.io.FilenameUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.TextAnchor;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Lukas on 24.01.2017.
 */
public class PerClassifierRuntimeEvaluator implements IEvaluator {

    @Override
    public void evaluate(List<FSResult> results) {
        results.parallelStream()
                .collect(Collectors.groupingBy(r -> r.datasetName))
                .forEach((k,v) -> createChartForDataset(v));
    }

    private void createChartForDataset(List<FSResult> resultList) {


        Map<Integer, List<FSResult>> resultsPerFeatureNum = resultList.stream().collect(Collectors.groupingBy(r -> r.numOfFeatures));
        resultsPerFeatureNum.forEach((k,v) -> saveChartToImage(resultList.get(0).datasetName, k, createBarChart(v)));



    }

    public JFreeChart createBarChart(List<FSResult> resultList){
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        resultList.forEach(
                res -> dataset.addValue(res.durationTotal(), res.classifier, res.fsMethod)
        );

        JFreeChart barChart = ChartFactory.createBarChart(
                "Accuracy by Classifier and Featureselection method - #Features " + String.valueOf(resultList.get(0).numOfFeatures),
                "#Features", "%Runtime",
                dataset, PlotOrientation.VERTICAL,
                true, true, false);

        CategoryItemRenderer renderer = ((CategoryPlot)barChart.getPlot()).getRenderer();

        renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());
        renderer.setBaseItemLabelsVisible(true);
        ItemLabelPosition position = new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12,
                TextAnchor.TOP_CENTER);
        renderer.setBasePositiveItemLabelPosition(position);

        return barChart;
    }



    private void saveChartToImage(String datasetName, int numFeat, JFreeChart chart){
        int width = 800; /* Width of the image */
        int height = 600; /* Height of the image */
        String basename = FilenameUtils.getBaseName(datasetName);

        File BarChart = new File( "output/"+ basename + "/" + numFeat +"-BarChartPerClassfierRuntime.png" );
        try {
            ChartUtilities.saveChartAsPNG( BarChart , chart , width , height );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
