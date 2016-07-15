package com.weather.converter.util.converter;

import org.apache.commons.io.FileUtils;
import org.apache.xmlbeans.XmlException;

import java.io.File;
import java.io.IOException;

/**
 * Created by Rastakfard (Saeid.Rastak@Gmail.com) on 1/4/2016.
 */
public abstract class TACConverter {


    String SPACE = " ";
    String NEW_LINE = System.getProperty("line.separator");
    int LINE_WORD_COUNT = 87;
    StringBuilder tacResult = new StringBuilder();

    public void initTAC(String path) throws IOException, XmlException {
        parseXmlDocument(path);
        resetTACResult();
        initTAC();
    }

    private void resetTACResult() {
        tacResult = new StringBuilder();
    }


    public abstract void parseXmlDocument(String path) throws IOException, XmlException;

    public abstract void initTAC() throws IOException, XmlException;

    public abstract String buildTAC();

    public String buildTACFile(String pathToSave, boolean replace) throws IOException {
        File resultTACFile = new File(pathToSave);
        String result = buildTAC();
        if (resultTACFile.exists() && !replace) {
            return result;
        }
        FileUtils.writeStringToFile(resultTACFile, result);
        return result;
    }
}
