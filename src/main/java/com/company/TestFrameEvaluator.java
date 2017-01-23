package com.company;

import org.jfree.ui.ApplicationFrame;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by martensigwart on 23.01.17.
 */
public class TestFrameEvaluator extends ApplicationFrame {
    public TestFrameEvaluator(String title) {

        super(title);

        List<FSResult> results = createSampleResults();
    }

    private List<FSResult> createSampleResults() {
        List<FSResult> results = new ArrayList<>();
        results.add(new FSResult());
        return null;
    }
}
