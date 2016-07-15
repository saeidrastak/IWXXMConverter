package com.weather.converter.util.common;

import org.apache.xmlbeans.XmlException;
import xint.icao.iwxxm.x10RC1.METARDocument;
import xint.icao.iwxxm.x10RC1.SIGMETDocument;
import xint.icao.iwxxm.x10RC1.SPECIDocument;
import xint.icao.iwxxm.x10RC1.TAFDocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by Rastakfard (Saeid.Rastak@Gmail.com) on 1/5/2016.
 */
public class ApplicationUtils {

    public static Collection<File> readWXXMFiles(String dir, boolean RECURSIVE) {
        String[] extensions = new String[]{"xml"};
        File directory = new File(dir);
        if (!directory.exists()) {
            System.out.println("Source directory does not exists!");
            return new ArrayList<File>();
        }
        return org.apache.commons.io.FileUtils.listFiles(directory, extensions, RECURSIVE);
    }

    public static METARDocument parseAsMetarDocument(String XML_FILE_PATH) throws XmlException, IOException {
        return METARDocument.Factory.parse(new FileInputStream(XML_FILE_PATH));
    }

    public static TAFDocument parseAsTAFDocument(String XML_FILE_PATH) throws XmlException, IOException {
        return TAFDocument.Factory.parse(new FileInputStream(XML_FILE_PATH));
    }

    public static SPECIDocument parseAsSPECIDocument(String XML_FILE_PATH) throws XmlException, IOException {
        return SPECIDocument.Factory.parse(new FileInputStream(XML_FILE_PATH));
    }

    public static SIGMETDocument parseAsSIGMETDocument(String XML_FILE_PATH) throws XmlException, IOException {
        return SIGMETDocument.Factory.parse(new FileInputStream(XML_FILE_PATH));
    }

    public static boolean isWindowsEnvironment() {
        OSCheck.OSType ostype = OSCheck.getOperatingSystemType();
        return ostype.equals(OSCheck.OSType.Windows);
    }

    public static boolean isLINUXEnvironment() {
        OSCheck.OSType ostype = OSCheck.getOperatingSystemType();
        return ostype.equals(OSCheck.OSType.Linux);
    }


    public static String getPathSeparator() {
        return ApplicationUtils.isWindowsEnvironment() ? "\\" : "/";
    }
}
