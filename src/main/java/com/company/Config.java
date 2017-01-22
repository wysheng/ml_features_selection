package com.company;

import weka.attributeSelection.*;
import weka.classifiers.Classifier;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by elisabethappler on 22.01.17.
 */
public class Config {
    private static Config ourInstance = new Config();
    public static Config getInstance() {
        return ourInstance;
    }


    public static final int NUM_FOLDS = 10;

    public List<String> datasetFilenames;
    public List<DatasetEntry> datasets;
    public boolean numFeaturesInPercent = true;
    public List<Integer> numOfFeatures = new ArrayList<>();
    public int numFeaturesTotal = 0;
    public List<Classifier> classifiers = new ArrayList<>();
    public List<ASEvaluation> attributeEvaluators = new ArrayList<>();
    public List<ASSearch> attributeRankers = new ArrayList<>();


    private Config() {
        setupDefaultFeatureNums();
        setupClassifiers();
        setupAttributeSelectors();
    }

    private void setupDefaultFeatureNums() {
        numOfFeatures.add(10);
        numOfFeatures.add(20);
        numOfFeatures.add(30);
        numOfFeatures.add(40);
        numOfFeatures.add(50);
    }


    private void setupClassifiers() {
        classifiers.add(new RandomForest());
        classifiers.add(new IBk());

    }

    private void setupAttributeSelectors() {

        attributeEvaluators.add(new CorrelationAttributeEval());
        attributeEvaluators.add(new InfoGainAttributeEval());
        attributeEvaluators.add(new PrincipalComponents());

        attributeRankers.add(new Ranker());
        attributeRankers.add(new BestFirst());
    }


}
