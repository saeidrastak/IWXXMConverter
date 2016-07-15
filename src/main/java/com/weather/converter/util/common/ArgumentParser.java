package com.weather.converter.util.common;

import jargs.gnu.CmdLineParser;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;

/**
 * Created by Rastakfard (Saeid.Rastak@Gmail.com) on 1/5/2016.
 */
public class ArgumentParser {

    private String destinationFolderArg;
    private String sourceFolderArg;
    private String tacTypeArg;
    private Boolean removeSourceFilesArg;

    private void printSplashScreen() throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream("splash.txt")));
        String line = null;
        while((line = in.readLine()) != null) {
            System.out.println(line);
        }
    }

    public HashMap<String, Object> parseArgs(String[] args) throws IOException {
        printSplashScreen();
        HashMap<String,Object> result = new HashMap<String, Object>();
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option destinationFolder = parser.addStringOption('d', "destFolder");
        CmdLineParser.Option sourceFolder = parser.addStringOption('s', "sourceFolder");
        CmdLineParser.Option tacType = parser.addStringOption('t', "tacType");
        CmdLineParser.Option removeSourceFiles = parser.addBooleanOption('r', "removeSourceFiles");
        CmdLineParser.Option silentMode = parser.addBooleanOption("silent");
        CmdLineParser.Option replaceMode = parser.addBooleanOption("replace");


        try {
            parser.parse(args);
        }
        catch ( CmdLineParser.OptionException e ) {
            System.err.println(e.getMessage());
            exit(2,"");
        }

        destinationFolderArg = (String) parser.getOptionValue(destinationFolder, System.getProperty("user.home") + "/destination-folder");
        if (destinationFolderArg ==null){
            exit(3,"Please provide parameter -d,--destFolder destination folder path");
        }
        result.put("destinationFolderArg",destinationFolderArg);
        sourceFolderArg = (String) parser.getOptionValue(sourceFolder);
        if (sourceFolderArg ==null){
            exit(4, "Please provide parameter -s,--sourceFolder source folder path");
        }
        result.put("sourceFolderArg",sourceFolderArg);
        tacTypeArg = (String) parser.getOptionValue(tacType);
        if (noValidTacType(tacTypeArg)){
            exit(5, "Please provide parameter -t,--tacType [METAR | TAF | SIGMET | SPECI]");
        }
        result.put("tacTypeArg",tacTypeArg);
        removeSourceFilesArg = (Boolean) parser.getOptionValue(removeSourceFiles, Boolean.FALSE);
        if (removeSourceFilesArg ==null){
            exit(6, "Please provide parameter -r,--removeSourceFiles");
        }
        result.put("removeSourceFilesArg",removeSourceFilesArg);

        Boolean silentOptionValue = (Boolean) parser.getOptionValue(silentMode, Boolean.FALSE);
        if (silentOptionValue==null){
            silentOptionValue = false;
        }
        result.put("silent", silentOptionValue);

        Boolean replaceOptionValue = (Boolean) parser.getOptionValue(replaceMode, Boolean.FALSE);
        if (replaceOptionValue==null){
            replaceOptionValue = false;
        }
        result.put("replace", replaceOptionValue);
        return result;
    }

    private static boolean noValidTacType(String tacTypeArg) {
        if (StringUtils.isEmpty(tacTypeArg)){
            return true;
        }
        if (!"METAR".equals(tacTypeArg.toUpperCase()) && !"TAF".equals(tacTypeArg.toUpperCase())
                && !"SPECI".equals(tacTypeArg.toUpperCase()) &&!"SIGMET".equals(tacTypeArg.toUpperCase())){
            return true;
        }
        return false;
    }

    private static void exit(int exitCode, String additionalHelp) {
        printUsage(additionalHelp);
        System.exit(exitCode);
    }

    private static void printUsage(String additionalHelp) {
        System.err.println(
                "Usage: java -jar iwxxm-converter.jar [-d,--destFolder destinationFolderPath] [{-s,--sourceFolder} sourceFolderPath]\n" +
                        "                  [[-t,--tacType] [METAR | TAF | SIGMET | SPECI]] [-r,--removeSourceFiles] --silent --replace");
        System.err.println(additionalHelp);
    }
}
