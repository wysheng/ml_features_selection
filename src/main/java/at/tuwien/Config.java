package at.tuwien;

import at.tuwien.evaluators.FeatureMethodEvaluator;
import at.tuwien.evaluators.IEvaluator;
import at.tuwien.evaluators.PerClassifierAccuracyEvaluator;
import at.tuwien.evaluators.PerClassifierRuntimeEvaluator;
import weka.attributeSelection.*;
import weka.classifiers.Classifier;
import weka.classifiers.functions.SMO;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;

import java.util.ArrayList;
import java.util.List;


public class Config {
    private static Config ourInstance = new Config();
    public static Config getInstance() {
        return ourInstance;
    }


    public static final int NUM_FOLDS = 10;

    public List<DatasetEntry> datasets;
    public boolean numFeaturesInPercent = true;
    public List<Integer> numOfFeatures = new ArrayList<>();
    public List<Classifier> classifiers = new ArrayList<>();
    public List<ASEvaluation> attributeEvaluators = new ArrayList<>();
    public List<ASSearch> attributeRankers = new ArrayList<>();
    public List<IEvaluator> resultEvaluators = new ArrayList<>();


    private Config() {
        setupDefaultFeatureNums();
        setupClassifiers();
        setupAttributeSelectors();
        setupResultEvaluators();
    }

    private void setupResultEvaluators() {
        resultEvaluators.add(new FeatureMethodEvaluator());
        resultEvaluators.add(new PerClassifierAccuracyEvaluator());
        resultEvaluators.add(new PerClassifierRuntimeEvaluator());
    }

    private void setupDefaultFeatureNums() {
        numOfFeatures.add(10);
        numOfFeatures.add(20);
        numOfFeatures.add(30);
        numOfFeatures.add(40);
        numOfFeatures.add(50);
        numOfFeatures.add(60);
        numOfFeatures.add(70);
        numOfFeatures.add(80);
    }


    private void setupClassifiers() {
        classifiers.add(new RandomForest());
        classifiers.add(new IBk());
        classifiers.add(new SMO());

    }

    private void setupAttributeSelectors() {

        attributeEvaluators.add(new CorrelationAttributeEval());
        attributeEvaluators.add(new InfoGainAttributeEval());
        attributeEvaluators.add(new PrincipalComponents());
        attributeEvaluators.add(new ReliefFAttributeEval());
        attributeEvaluators.add(new SymmetricalUncertAttributeEval());

        attributeRankers.add(new Ranker());
        attributeRankers.add(new BestFirst());
    }


}
