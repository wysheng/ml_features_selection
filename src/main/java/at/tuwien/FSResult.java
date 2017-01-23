package at.tuwien;

import weka.classifiers.evaluation.Evaluation;

/**
 * Created by appler on 22.01.17.
 */
public class FSResult {
    public String datasetName;
    public int numOfFeatures;
    public String classifier;
    public String fsMethod;
    public Evaluation evaluation;

    public long durationFiltering;
    public long durationCrossValidation;
    public long durationTotal(){
        return durationFiltering + durationCrossValidation;
    }
}
