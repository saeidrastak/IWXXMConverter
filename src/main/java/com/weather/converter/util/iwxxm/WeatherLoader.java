package com.weather.converter.util.iwxxm;

import org.apache.xmlbeans.XmlException;
import xint.icao.iwxxm.x10RC1.METARDocument;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by r.rastakfard on 12/23/2015.
 */
public interface WeatherLoader {

    METARDocument loadMETARDocument(String xmlFilePath) throws IOException, XmlException;
    void saveMETARDocument(METARDocument metarDocument, String xmlFilePath);
    METARDocument initNewMETARDocument();
}
