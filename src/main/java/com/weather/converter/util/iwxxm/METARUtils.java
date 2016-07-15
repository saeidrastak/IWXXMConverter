package com.weather.converter.util.iwxxm;

import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.XmlException;
import xint.icao.iwxxm.x10RC1.CloudLayerPropertyType;
import xint.icao.iwxxm.x10RC1.METARDocument;
import xint.wmo.data.def.metBasic.x10RC1.SigConvectiveCloudTypeType;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by r.rastakfard on 12/23/2015.
 */
public class METARUtils extends CommonUtils implements WeatherLoader {
    private static final String SPACE = " ";

    public static String getMeanWindDirectionString(Double meanWindDirection) {
        String meanWindDirectionStr;
        if (meanWindDirection < 10) {
            meanWindDirectionStr = "00" + meanWindDirection.intValue();
        } else if (meanWindDirection < 100) {
            meanWindDirectionStr = "0" + meanWindDirection.intValue();
        } else {
            meanWindDirectionStr = String.valueOf(meanWindDirection.intValue());
        }
        return meanWindDirectionStr;
    }

    public static String getWindUnit(String uom) {
        if (uom.endsWith("m/s")) {
            return "MPS";
        }
        return "KT";
    }

    public static void parseCloudLayers(CloudLayerPropertyType[] layerArray, StringBuilder tacResult) {
        for (CloudLayerPropertyType layer : layerArray) {
            String title = layer.getCloudLayer().getAmount().getTitle();
            if ("Broken".equalsIgnoreCase(title)) {
                tacResult.append("BKN");
            } else if ("Overcast".equalsIgnoreCase(title)) {
                tacResult.append("OVC");
            } else if ("Few".equalsIgnoreCase(title)) {
                tacResult.append("FEW");
            } else if ("Scattered".equalsIgnoreCase(title)) {
                tacResult.append("SCT");
            }
            Double base = Double.valueOf(layer.getCloudLayer().getBase().getStringValue());
            String unit = layer.getCloudLayer().getBase().getUom();
            //TODO feet or meter
            base = convertToFeet(base, unit);
//            double v = base / 100;
            String baseStr = String.valueOf(base).split("\\.")[0];
            if (baseStr.length() == 2) {
                baseStr = "0" + baseStr;
            } else if (baseStr.length() == 1) {
                baseStr = "00" + baseStr;
            }
            tacResult.append(baseStr);

            SigConvectiveCloudTypeType cloudType = layer.getCloudLayer().getCloudType();
            if (cloudType != null) {
                String cloudTypeTitle = cloudType.getTitle();
                if ("Cumulonimbus".equalsIgnoreCase(cloudTypeTitle)) {
                    tacResult.append("CB");
                } else if ("Cumulus congestus".equalsIgnoreCase(cloudTypeTitle)) {
                    tacResult.append("TCU");
                } else {
                    if (StringUtils.isEmpty(cloudType.getHref())) {
                        String cloudName = cloudType.getHref().substring(cloudType.getHref().lastIndexOf("/") + 1);
                        tacResult.append(cloudName);
                    }
                }

            }
            tacResult.append(SPACE);
        }
    }

    private static Double convertToFeet(Double base, String unit) {
        if ("http://opengis.net/def/uom/UCUM/0/m".equalsIgnoreCase(unit)
                || "m".equalsIgnoreCase(unit)) {
//            return base * 3.28084;
            return base / 30;
        } else if ("ft".equalsIgnoreCase(unit) || unit.endsWith("ft")) {
            return base;
        }
        throw new RuntimeException("invalid unit " + unit + "!");
    }

    @Override
    public METARDocument loadMETARDocument(String xmlFilePath) throws IOException, XmlException {
        return METARDocument.Factory.parse(new FileInputStream(xmlFilePath));
    }

    @Override
    public void saveMETARDocument(METARDocument metarDocument, String xmlFilePath) {
        writeXmlFile(metarDocument, xmlFilePath);
    }

    @Override
    public METARDocument initNewMETARDocument() {
        return METARDocument.Factory.newInstance();
    }
}
