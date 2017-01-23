package at.tuwien;

/**
 * Created by lukas on 1/22/17.
 */
public class DatasetEntry {

    public DatasetEntry(String datasetFile) {
        this.datasetFile = datasetFile;
    }

    String datasetFile;
    //defaulting to 0, then picking the last attribute as class index
    int classIndex = 0;
}
