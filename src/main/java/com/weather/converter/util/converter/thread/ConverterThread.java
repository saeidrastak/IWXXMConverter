package com.weather.converter.util.converter.thread;

import com.weather.converter.util.common.ApplicationUtils;
import com.weather.converter.util.converter.TACConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by Rastakfard (Saeid.Rastak@Gmail.com) on 1/5/2016.
 */
public class ConverterThread implements Callable<List<String>> {

    private final TACConverter tacConverter;
    List<String> xmlFilePaths;
    String destinationFileDir;
    Integer threadNumber;
    boolean replace = false;

    public ConverterThread(List<String> xmlFilePaths,
                           String destinationFileDir,
                           Integer threadNo,
                           TACConverter tacConverter, Boolean replace) {
        this.xmlFilePaths = xmlFilePaths;
        this.threadNumber = threadNo;
        this.destinationFileDir = destinationFileDir;
        this.tacConverter = tacConverter;
        this.replace = replace;
    }

    @Override
    public List<String> call() throws Exception {
        List<String> metarResults = new ArrayList<String>();
        for (String path : xmlFilePaths) {
            tacConverter.initTAC(path);
            String pathSeparator = ApplicationUtils.getPathSeparator();
            String fileName = path.substring(path.lastIndexOf(pathSeparator) + 1).replace(".xml", ".tac");
            String metarResult = tacConverter.buildTACFile(destinationFileDir + pathSeparator + fileName, replace);
            metarResults.add(metarResult);
        }
//        System.out.println("Thread " + this.threadNumber + " Finished!");
        return metarResults;
    }

}
