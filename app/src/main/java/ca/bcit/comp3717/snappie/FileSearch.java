package ca.bcit.comp3717.snappie;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;

public class FileSearch {
    private static final String TAG = "FileSearch";

    public static ArrayList<String> getFilePaths(String directory) {
        ArrayList<String> pathList = new ArrayList<>();
        File dir = new File(directory);
        File[] listFiles = dir.listFiles();
        for (File file : listFiles) {
            if (file.isFile()) {
                pathList.add(file.getAbsolutePath());
            }
        }
        return pathList;
    }
}
