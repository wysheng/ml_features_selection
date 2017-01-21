package com.company;

import weka.core.Instances;
import weka.core.converters.ConverterUtils;

public class Main {

    public static void main(String[] args) throws Exception {
	// write your code here


        ConverterUtils.DataSource source = new ConverterUtils.DataSource("./data/GTZAN_O.rh.arff");
        Instances data = source.getDataSet();






    }
}
