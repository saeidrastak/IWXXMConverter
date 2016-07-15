package com.weather.converter.util.converter;

import com.weather.converter.util.common.ApplicationUtils;
import org.apache.xmlbeans.XmlException;
import xint.icao.iwxxm.x10RC1.SPECIDocument;

import java.io.IOException;

/**
 * Created by Rastakfard (Saeid.Rastak@Gmail.com) on 1/4/2016.
 */
public class SPECIConverter extends TACConverter {
    private boolean speciParsed = false;
    private SPECIDocument speciDocument;

    @Override
    public void parseXmlDocument(String path) throws IOException, XmlException {
        speciDocument = ApplicationUtils.parseAsSPECIDocument(path);
        speciParsed = true;
    }

    @Override
    public void initTAC() {

        if (!speciParsed) return;


    }

    @Override
    public String buildTAC() {
        return tacResult.toString();
    }

}
