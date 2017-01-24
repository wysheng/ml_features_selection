package at.tuwien;

import at.tuwien.evaluators.IEvaluator;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static at.tuwien.Config.NUM_FOLDS;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.exit;

public class Main {

    public static final List<FSResult> fsResults = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        CommandLineParser parser = new DefaultParser();
        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();


        File outputFile = new File("output");
        outputFile.mkdir();

        Option numFeaturesOpt = new Option(
                "n",
                "numFeatures",
                true,
                "number of features, separated by ','");

        Option classIndicesOpt = new Option(
                "c",
                "classIndex",
                true,
                "specifies class indicies, separated by ','. Per default this will assume the last attribute as the class");

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

        //Defaults to last attribute of dataset if not set
        if(commandLine.hasOption('c')){
            List<String> classIndicesStrings = Arrays.asList(commandLine.getOptionValue('c').split(","));
            for(int i = 0; i < classIndicesStrings.size(); ++i){
                config.datasets.get(i).classIndex = Integer.parseInt(classIndicesStrings.get(0));
            }
        }



        for(DatasetEntry dataset : config.datasets){
            System.out.println("Evaluating dataset: " + dataset.datasetFile);
            evaluateDataset(dataset);
        }


        List<FSResult> results = new ArrayList<>();
        for(FSResult result : fsResults){
            System.out.println("Dataset: " + result.datasetName +
                    "\nNumber of Features: " + result.numOfFeatures +
                    "\nFSMethod: " + result.fsMethod +
                    "\nClassifier: " + result.classifier);
            System.out.println(result.evaluation.pctCorrect());
            System.out.println(result.evaluation.pctIncorrect());
            results.add(result);
//            System.out.println(result.evaluation.toSummaryString());
//            System.out.println(result.evaluation.toClassDetailsString());
        }

        //print diagrams
        for (IEvaluator evaluator : config.resultEvaluators) {
            evaluator.evaluate(results);
        }

    }

    private static void evaluateDataset(DatasetEntry dataset) throws Exception {
        ConverterUtils.DataSource source = new ConverterUtils.DataSource(dataset.datasetFile);
        Instances data = source.getDataSet();
        if(dataset.classIndex == 0){
            dataset.classIndex = data.numAttributes() -1;
        }
        data.setClassIndex(dataset.classIndex);

        System.out.println("#Evaluating Baseline");
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

            System.out.println("#Evaluating for " + absNumberFeat + " Features now");
            for(ASEvaluation attributeEvaluator : Config.getInstance().attributeEvaluators){
                System.out.println("##Using " + attributeEvaluator.getClass().getSimpleName());
                Ranker ranker = new Ranker();
                ranker.setNumToSelect(absNumberFeat);

                AttributeSelection filter = new AttributeSelection();
                filter.setEvaluator(attributeEvaluator);
                filter.setSearch(ranker); // entry 0 is the default Ranker
                filter.setInputFormat(data);

                long filteringStartTime = System.currentTimeMillis();
                Instances reducedSet = Filter.useFilter(data, filter);
                long filterDuration = currentTimeMillis() - filteringStartTime;
                System.out.println("--Filtering took " + filterDuration + " millis");

                List<FSResult> singleFsMethodResults = runAllClassifiers(reducedSet);
                int finalAbsNumberFeat = absNumberFeat;
                singleFsMethodResults = singleFsMethodResults.parallelStream()
                        .map(i -> {
                            i.fsMethod = attributeEvaluator.getClass().getSimpleName();
                            i.numOfFeatures = finalAbsNumberFeat;
                            i.datasetName = dataset.datasetFile;
                            i.durationFiltering = filterDuration;
                            return i; })
                        .collect(Collectors.toList());
                fsResults.addAll(singleFsMethodResults);
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
        System.out.println("###Running " + classifier.getClass().getSimpleName());
        long classificationStartTime = currentTimeMillis();

        classifier.buildClassifier(data);

        Evaluation eval = new Evaluation(data);
        Debug.Random rand = new Debug.Random(1);  // using seed = 1
        eval.crossValidateModel(classifier, data, NUM_FOLDS, rand);
        long classificationDuration = currentTimeMillis() - classificationStartTime;

        FSResult result = new FSResult();
        result.classifier = classifier.getClass().getSimpleName();
        result.evaluation = eval;
        result.durationCrossValidation = classificationDuration;
        System.out.println("---Classification took " + classificationDuration + " millis");
        return result;
    }

}
