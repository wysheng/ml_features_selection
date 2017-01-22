package com.company;

import weka.attributeSelection.AttributeSelection;
import weka.classifiers.Classifier;

import java.util.List;

/**
 * Created by elisabethappler on 22.01.17.
 */
public class Config {
    private static Config ourInstance = new Config();

    public static Config getInstance() {
        return ourInstance;
    }

    private Config() {
    }

    public List<String> datasetFilename;
    public List<Integer> numOfFeatures;
    public List<Classifier> classifiers;
    public List<AttributeSelection> attributeSelectors;

}
