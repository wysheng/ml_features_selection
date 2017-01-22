package com.company;

import org.apache.commons.cli.*;
import weka.classifiers.Classifier;
import weka.classifiers.evaluation.Evaluation;
import weka.core.Debug;
import weka.core.Instances;
import weka.core.converters.ConverterUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.company.Config.NUM_FOLDS;
import static java.lang.System.exit;

public class Main {

    public static void main(String[] args) throws Exception {
        CommandLineParser parser = new DefaultParser();
        // automatically generate the help statement
        HelpFormatter formatter = new HelpFormatter();

        Option numFeaturesOpt = new Option(
                "n",
                "numFeatures",
                true,
                "number of features, separated by ','");

        Options options = new Options();
        options.addOption(numFeaturesOpt);
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

        if(commandLine.hasOption('n')){
            List<String> numFeaturesListStrings = Arrays.asList(commandLine.getOptionValue('n').split(","));
             List<Integer> numFeaturesList = numFeaturesListStrings
                     .stream()
                     .map(s -> Integer.parseInt(s))
                     .collect(Collectors.toList());
            Config.getInstance().numOfFeatures = numFeaturesList;
            if(!commandLine.hasOption('p')) {
                Config.getInstance().numFeaturesInPercent = false;
            }
        }


        Config.getInstance().datasetFilename = commandLine.getArgList();




        ConverterUtils.DataSource source = new ConverterUtils.DataSource(Config.getInstance().datasetFilename.get(0));
        Instances data = source.getDataSet();
        data.setClassIndex(60);

        Classifier cls1 = Config.getInstance().classifiers.get(0);
        cls1.buildClassifier(data);

        Evaluation eval = new Evaluation(data);
        Debug.Random rand = new Debug.Random(1);  // using seed = 1
        eval.crossValidateModel(cls1, data, NUM_FOLDS, rand);

        System.out.println(eval.toSummaryString());


    }
}
