package com.weather.converter.util.iwxxm;

import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlOptions;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;

/**
 * Created by r.rastakfard on 12/23/2015.
 */
public class CommonUtils {
    public static final String XML_SCHEMA_LOCATION = "http://schemas.wmo.int/iwxxm/1.0RC1 ./iwxxm.xsd http://schemas.wmo.int/metce/1.0RC1 ./metce.xsd";

    public static final String NS_GML = "http://www.opengis.net/gml/3.2";
    public static final String NS_MET_BASIC = "http://data.wmo.int/def/met-basic/1.0RC1";
    public static final String NS_OPM = "http://data.wmo.int/def/opm/1.0RC1";
    public static final String NS_METCE = "http://data.wmo.int/def/metce/1.0RC1";
    public static final String NS_SAF = "http://icao.int/saf/1.0RC1";
    public static final String NS_IWXXM = "http://icao.int/iwxxm/1.0RC1";
    public static final String NS_XS = "http://www.w3.org/2001/XMLSchema";
    public static final String NS_SAMS = "http://www.opengis.net/samplingSpatial/2.0";
    public static final String NS_SAM = "http://www.opengis.net/sampling/2.0";

    public static void writeXmlFile(XmlObject docObject, String targetFilePath) {

        File xmlFile = new File(targetFilePath);

        HashMap NamespaceMap = new HashMap();
        NamespaceMap.put("http://www.opengis.net/om/2.0", "om");
        NamespaceMap.put(NS_GML, "gml");
        NamespaceMap.put(NS_MET_BASIC, "");
        NamespaceMap.put(NS_OPM, "");
        NamespaceMap.put(NS_METCE, "metce");
        NamespaceMap.put(NS_SAF, "saf");
        NamespaceMap.put(NS_IWXXM, "");
        NamespaceMap.put(NS_XS, "");
        NamespaceMap.put(NS_SAMS, "sams");
        NamespaceMap.put(NS_SAM, "sam");


        XmlOptions xmlOptions = new XmlOptions();
        xmlOptions.setSavePrettyPrint();
        xmlOptions.setSaveSuggestedPrefixes(NamespaceMap);
        xmlOptions.setSaveAggressiveNamespaces();
        xmlOptions.setUseDefaultNamespace();


        try {
            docObject.save(xmlFile, xmlOptions);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String convertToTwoDigit(int number) {
        if (number < 10) {
            return "0" + number;
        }
        return String.valueOf(number);
    }

    public static Double convertToCelsius(Double airTemperatureVal, String tempUnit) {
        //TODO
        if (isNotCelsius(tempUnit)) {
            return null;
        }
        return airTemperatureVal;
    }

    public static int convertToMeters(Double doubleValue, String unit) {
        if ("http://opengis.net/def/uom/UCUM/0/km".equalsIgnoreCase(unit)) {
            return doubleValue.intValue() * 1000;
        } else if ("http://opengis.net/def/uom/UCUM/0/m".equalsIgnoreCase(unit)) {
            return doubleValue.intValue();
        }
        throw new RuntimeException("The unit value " + unit + " not supported");
    }

    public static boolean isNotCelsius(String tempUnit) {
        return !("Cel".equalsIgnoreCase(tempUnit) ||
                "http://opengis.net/def/uom/UCUM/0/C".equalsIgnoreCase(tempUnit) ||
                "http://opengis.net/def/uom/UCUM/0/Cel".equalsIgnoreCase(tempUnit));
    }

    public static String convertToFourDigit(Double visibility) {
        String result = "";
        if (visibility < 10) {
            result = "000" + visibility.intValue();
        } else if (visibility < 100) {
            result = "00" + visibility.intValue();
        } else if (visibility < 1000) {
            result = "0" + visibility.intValue();
        } else {
            result = String.valueOf(visibility.intValue());
        }
        return result;
    }

    public static Double convertMeterToMile(Double meter) {
        return meter * 0.000621371;
    }

    public static Double convertToInchesOfMercury(double altimeterSetting, String unit) {
        if ("hPa".equalsIgnoreCase(unit) || "http://opengis.net/def/uom/UCUM/0/hPa".equalsIgnoreCase(unit)) {
            double convertedVal = 0.0295299830714 * altimeterSetting;
            return round(convertedVal, 2);
        }
        //TODO
        return null;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static Double convertToString(String windSpeed, String unit) {
        /*if ("MPS".equals(unit)) {
            Double result = Double.valueOf(windSpeed) * 1.94384;
            return result;
        } else if ("KT".equals(unit)) {
            return Double.valueOf(windSpeed);
        }
        return null;*/
        return Double.valueOf(windSpeed);
    }
}
