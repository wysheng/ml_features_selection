package com.company;

import org.apache.commons.cli.*;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.Ranker;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Debug;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.company.Config.NUM_FOLDS;
import static java.lang.System.exit;

public class Main {

    public static final List<FSResult> fsResults = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        CommandLineParser parser = new DefaultParser();
        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();

        Option numFeaturesOpt = new Option(
                "n",
                "numFeatures",
                true,
                "number of features, separated by ','");

        Option classIndicesOpt = new Option(
                "c",
                "classIndex",
                true,
                "specifies class indicies, separated by ','");
        classIndicesOpt.isRequired();

        Options options = new Options();
        options.addOption(numFeaturesOpt);
        options.addOption(classIndicesOpt);
        options.addOption("p", "numPercent", false, "number of features is given in percentage of instance numbers");
        options.addOption("h", "help", false, "prints usage");

        CommandLine commandLine = parser.parse(options, args);

        if(commandLine.hasOption('h')){
            formatter.printHelp( "fsEval [options] [dataset [dataset2 [dataset3] ...]]", options );
            exit(0);
        }

        if(commandLine.getArgList().size() == 0){
            formatter.printHelp( "fsEval [options] [dataset [dataset2 [dataset3] ...]]", options );
            exit(1);
        }

        Config config = Config.getInstance();
        config.datasets = commandLine.getArgList().stream().map(DatasetEntry::new).collect(Collectors.toList());



        if(commandLine.hasOption('n')){
            List<String> numFeaturesListStrings = Arrays.asList(commandLine.getOptionValue('n').split(","));
             List<Integer> numFeaturesList = numFeaturesListStrings
                     .stream()
                     .map(Integer::parseInt)
                     .collect(Collectors.toList());
            Config.getInstance().numOfFeatures = numFeaturesList;
            if(!commandLine.hasOption('p')) {
                Config.getInstance().numFeaturesInPercent = false;
            }
        }

        List<String> classIndicesStrings = Arrays.asList(commandLine.getOptionValue('c').split(","));

        for(int i = 0; i < classIndicesStrings.size(); ++i){
            config.datasets.get(i).classIndex = Integer.parseInt(classIndicesStrings.get(0));
        }






        for(DatasetEntry dataset : config.datasets){
            evaluateDataset(dataset);
        }

        for(FSResult result : fsResults){
            System.out.println(result.evaluation.toSummaryString());
            System.out.println(result.evaluation.toClassDetailsString());
        }

    }

    private static void evaluateDataset(DatasetEntry dataset) throws Exception {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(dataset.datasetFile);
        Instances data = source.getDataSet();
        data.setClassIndex(dataset.classIndex);


        //RUN WITHOUT ATTRIBUTE SELECTION
        List<FSResult> initialResults = runAllClassifiers(data);
        initialResults = initialResults.parallelStream()
                .map(i -> {
                    i.fsMethod = "None";
                    i.numOfFeatures = 0;
                    i.datasetName = dataset.datasetFile;
                    return i; })
                .collect(Collectors.toList());
        fsResults.addAll(initialResults);

        for(int numFeat : Config.getInstance().numOfFeatures){
            int absNumberFeat = numFeat;
            if(Config.getInstance().numFeaturesInPercent){
                absNumberFeat = (int)(data.numAttributes()*(numFeat/100.0f));
            }

            for(ASEvaluation attributeEvaluator : Config.getInstance().attributeEvaluators){
                Ranker ranker = new Ranker();
                ranker.setNumToSelect(absNumberFeat);

                AttributeSelection filter = new AttributeSelection();
                filter.setEvaluator(attributeEvaluator);
                filter.setSearch(ranker); // entry 0 is the default Ranker
                filter.setInputFormat(data);
                Instances reducedSet = Filter.useFilter(data, filter);

                List<FSResult> singleFsMethodResults = runAllClassifiers(reducedSet);
                int finalAbsNumberFeat = absNumberFeat;
                initialResults = initialResults.parallelStream()
                        .map(i -> {
                            i.fsMethod = attributeEvaluator.getClass().getSimpleName();
                            i.numOfFeatures = finalAbsNumberFeat;
                            i.datasetName = dataset.datasetFile;
                            return i; })
                        .collect(Collectors.toList());
                fsResults.addAll(initialResults);
            }
        }


    }



    private static List<FSResult> runAllClassifiers(Instances data) throws Exception {
        List<FSResult> resultList = new ArrayList<>();
        for(Classifier classifier : Config.getInstance().classifiers){
            resultList.add(runClassifier(classifier, data));
        }
        return resultList;
    }



    public static FSResult runClassifier(Classifier classifier, Instances data) throws Exception {
        classifier.buildClassifier(data);

        Evaluation eval = new Evaluation(data);
        Debug.Random rand = new Debug.Random(1);  // using seed = 1
        eval.crossValidateModel(classifier, data, NUM_FOLDS, rand);

        FSResult result = new FSResult();
        result.classifier = classifier.getClass().getSimpleName();
        result.evaluation = eval;
        return result;
    }

}
