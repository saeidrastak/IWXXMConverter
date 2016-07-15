package com.weather.converter.main;

import com.weather.converter.util.common.ApplicationUtils;
import com.weather.converter.util.common.ArgumentParser;
import com.weather.converter.util.converter.MetarSPECIConverter;
import com.weather.converter.util.converter.SIGMETConverter;
import com.weather.converter.util.converter.TACConverter;
import com.weather.converter.util.converter.TAFConverter;
import com.weather.converter.util.converter.thread.ConverterThread;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by r.rastakfard on 12/30/2015.
 */
public class WXXMConversionApplication {


    private static int THREAD_MAX_SIZE = 20;
    private static int FILE_COUNT_PER_THREAD = 50;
    private static String METAR = "METAR";
    private static String SPECI = "SPECI";
    private static String TAF = "TAF";
    private static String SIGMET = "SIGMET";

    public static void main(String[] args) throws ExecutionException, InterruptedException, IOException {

        ArgumentParser argumentParser = new ArgumentParser();
        HashMap<String, Object> params = argumentParser.parseArgs(args);
        List<String> filePaths = readFiles((String) params.get("sourceFolderArg"));
//        tafExample(params);
        convertToTac(filePaths, (String) params.get("tacTypeArg"),
                (String) params.get("destinationFolderArg"),
                (Boolean) params.get("silent"), (Boolean) params.get("replace"));

    }


    private static List<String> readFiles(String sourceFileDir) {
        try {
            List<String> filePaths = new ArrayList<String>();
            boolean RECURSIVE = true;
            Collection<File> WXXMFiles = ApplicationUtils.readWXXMFiles(sourceFileDir, RECURSIVE);
            for (File XML_FILE : WXXMFiles) {
                filePaths.add(XML_FILE.getAbsolutePath());
            }

            return filePaths;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private static void convertToTac(List<String> sourceFilePaths, final String tacType, final String destinationFileDir, Boolean silent, Boolean replace) throws InterruptedException, java.util.concurrent.ExecutionException {
        ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_MAX_SIZE);
        CompletionService<List<String>> pool = new ExecutorCompletionService<List<String>>(threadPool);
        boolean invalidTacType = false;
        int fromIndex = 0;
        int filesCount = sourceFilePaths.size();
        int stepSize = FILE_COUNT_PER_THREAD;
        int i = 0;
        List<String> sourceFileSubList;
        TACConverter tacConverter = null;
        if (filesCount == 0) {
            System.exit(-1);
        }
        System.out.println("Start processing source directory files...");
        do {
            Callable thread = null;
            sourceFileSubList = sourceFilePaths.subList(fromIndex, filesCount < (fromIndex + stepSize) ? filesCount : (fromIndex + stepSize));
            if (METAR.equalsIgnoreCase(tacType)) {
                tacConverter = new MetarSPECIConverter(METAR);
            } else if (TAF.equalsIgnoreCase(tacType)) {
                tacConverter = new TAFConverter();
            } else if (SIGMET.equalsIgnoreCase(tacType)) {
                tacConverter = new SIGMETConverter();
            } else if (SPECI.equalsIgnoreCase(tacType)) {
                tacConverter = new MetarSPECIConverter(SPECI);
            } else {
                invalidTacType = true;
            }
            if (invalidTacType) {
                throw new UnsupportedOperationException(tacType + " : Tac Type invalid!");
            }
            thread = new ConverterThread(sourceFileSubList,
                    destinationFileDir, i, tacConverter, replace);
            pool.submit(thread);
            fromIndex += stepSize;
            ++i;
        } while (fromIndex < filesCount);

        threadPool.shutdown();

        int totalFiles = 0;
        for (int j = 0; j < i; j++) {
            List<String> result = pool.take().get();
            if (!silent) {
                for (String resultStr : result) {
                    System.out.println(resultStr);
                    System.out.println();
                }
            }
            totalFiles += result.size();
        }


        System.out.println("Converting " + tacType.toUpperCase() + " files finished.");
        System.out.println("Total Converted files is : " + filesCount);
        System.out.println("You can see converted files at " + destinationFileDir);
    }
}
