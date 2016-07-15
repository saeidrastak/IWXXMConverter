package com.weather.converter.util.converter;

import com.weather.converter.util.common.ApplicationUtils;
import com.weather.converter.util.common.MeteorologicalNamespaces;
import com.weather.converter.util.iwxxm.METARUtils;
import com.weather.converter.util.iwxxm.WindConverter;
import net.opengis.gml.x32.*;
import net.opengis.gml.x32.impl.*;
import net.opengis.om.x20.OMObservationPropertyType;
import net.opengis.om.x20.OMObservationType;
import net.opengis.om.x20.TimeObjectPropertyType;
import net.opengis.samplingSpatial.x20.impl.SFSpatialSamplingFeatureTypeImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.XmlCalendar;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import xint.icao.iwxxm.x10RC1.MeteorologicalPositionPropertyType;
import xint.icao.iwxxm.x10RC1.MeteorologicalPositionType;
import xint.icao.iwxxm.x10RC1.SIGMETDocument;
import xint.icao.iwxxm.x10RC1.SIGMETType;
import xint.icao.iwxxm.x10RC1.impl.*;
import xint.icao.saf.x10RC1.AirspaceSolidPropertyType;
import xint.wmo.data.def.metce.x10RC1.ExpectedIntensityChangeType;
import xint.wmo.data.def.metce.x10RC1.TropicalCyclonePropertyType;
import xint.wmo.data.def.metce.x10RC1.VolcanoPropertyType;
import xint.wmo.data.def.metce.x10RC1.impl.VolcanoTypeImpl;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.weather.converter.util.common.MeteorologicalNamespaces.*;

/**
 * Created by Rastakfard (Saeid.Rastak@Gmail.com) on 1/4/2016.
 */
public class SIGMETConverter extends TACConverter {


    String issuingAirTrafficServicesUnit;
    private boolean sigmetParsed = false;
    private SIGMETDocument sigmetDocument;
    private String tacType = "SIGMET";

    @Override
    public void parseXmlDocument(String path) throws IOException, XmlException {
        sigmetDocument = ApplicationUtils.parseAsSIGMETDocument(path);
        sigmetParsed = true;
    }

    @Override
    public void initTAC() {
        setTACType();
        setIssuingOffice();
        setDateTimeOfIssue();
        setBulletinNumber(getSigmet());
        setValidityPeriod();
        setLocation();

        setAnalysisArray();

        setForecast();


        tacResult.append("=");
    }

    private void setForecast() {

        OMObservationPropertyType forecastPositionAnalysis = null;
        boolean isTropicalCycloneSIGMET = false;
        if (sigmetDocument instanceof TropicalCycloneSIGMETDocumentImpl) {
            forecastPositionAnalysis = ((TropicalCycloneSIGMETDocumentImpl) sigmetDocument).getTropicalCycloneSIGMET().getForecastPositionAnalysis();
            isTropicalCycloneSIGMET = true;
        } else if (sigmetDocument instanceof VolcanicAshSIGMETDocumentImpl) {
            forecastPositionAnalysis = ((VolcanicAshSIGMETDocumentImpl) sigmetDocument).getVolcanicAshSIGMET().getForecastPositionAnalysis();
        }

        if (forecastPositionAnalysis != null) {
            tacResult.append("FCST ");
            OMObservationType omObservation = forecastPositionAnalysis.getOMObservation();
            setHeaderPartOfMessage(omObservation);
            setTime(omObservation.getPhenomenonTime());

            XmlObject[] meteorologicalPositionCollections = omObservation.getResult().selectChildren(new QName(IWXXM_NAMESPACE, "MeteorologicalPositionCollection"));
            if (meteorologicalPositionCollections != null && meteorologicalPositionCollections.length == 0) {
                calculateMeteorologicalPositionCollectionsXml(omObservation.getResult());
            } else if (meteorologicalPositionCollections != null && meteorologicalPositionCollections.length != 0) {
                if (sigmetDocument instanceof TropicalCycloneSIGMETDocumentImpl) {
                    tacResult.append("TC ");
                }
                MeteorologicalPositionCollectionTypeImpl meteorologicalPositionCollection = (MeteorologicalPositionCollectionTypeImpl) meteorologicalPositionCollections[0];
                MeteorologicalPositionPropertyType[] memberArray = meteorologicalPositionCollection.getMemberArray();
                if (memberArray != null) {
                    for (MeteorologicalPositionPropertyType member : memberArray) {
                        MeteorologicalPositionType meteorologicalPosition = member.getMeteorologicalPosition();
                        AirspaceSolidPropertyType geometry = meteorologicalPosition.getGeometry();
                        calculateGeometry(geometry);
                    }
                }
            }

        }
    }

    private void calculateMeteorologicalPositionCollectionsXml(XmlObject result) {
        XmlObject[] meteorologicalPositionCollections = result.selectChildren(new QName("MeteorologicalPositionCollection"));
        // when MeteorologicalPositionCollection element has not IWXXM namespace
        if (meteorologicalPositionCollections.length > 0) {
            XmlObject[] members = meteorologicalPositionCollections[0].selectChildren(new QName("member"));
            if (members.length > 0) {
                for (XmlObject member : members) {
                    XmlObject[] meteorologicalPositions = member.selectChildren(new QName(IWXXM_NAMESPACE, "MeteorologicalPosition"));
                    if (meteorologicalPositions.length > 0) {
                        XmlObject[] airspaceSolid = meteorologicalPositions[0].selectChildren(new QName(IWXXM_NAMESPACE, "geometry"))[0].
                                selectChildren(new QName(SAF_NAMESPACE, "AirspaceSolid"));
                        if (airspaceSolid.length > 0) {
                            XmlObject[] horizontalProjections = airspaceSolid[0].selectChildren(new QName(SAF_NAMESPACE, "horizontalProjection"));
                            tacResult.append("TC ");
                            if (horizontalProjections.length > 0) {
                                XmlObject[] point = horizontalProjections[0].selectChildren(new QName(GML_NAMESPACE, "Point"));
                                if (point.length > 0) {
                                    String pos = point[0].selectChildren(GML_NAMESPACE, "pos")[0].toString();
                                    int lastIndexOf = pos.lastIndexOf("</");
                                    String substring = pos.substring(lastIndexOf - 11, lastIndexOf);
                                    String[] pointXY = substring.split(SPACE);
                                    ArrayList<Double> doubleXY = new ArrayList<Double>();
                                    doubleXY.add(new Double(pointXY[0]));
                                    doubleXY.add(new Double(pointXY[1]));
                                    tacResult.append("CENTRE ");
                                    setAltitude(doubleXY);
                                    break;
                                }
                                XmlObject[] polygon = horizontalProjections[0].selectChildren(new QName(GML_NAMESPACE, "Polygon"));
                                if (polygon.length > 0) {

                                    break;
                                }

                                XmlObject[] circleByCenterPoint = horizontalProjections[0].selectChildren(new QName(GML_NAMESPACE, "CircleByCenterPoint"));
                                if (circleByCenterPoint.length > 0) {

                                }
                            }
                        }
                    }

                }
            }
        }
    }

    private void setTime(TimeObjectPropertyType phenomenonTime) {
        Object dateTime = ((TimeInstantTypeImpl) phenomenonTime.getAbstractTimeObject()).getTimePosition().getObjectValue();
        if (dateTime instanceof String) {
            String dateTimeStr = (String) dateTime;
            String[] ts = dateTimeStr.split("-")[2].split("T")[1].split(":");
            String hour = ts[0].length() == 1 ? "0" + ts[0] : ts[0];
            String minute = ts[1].length() == 1 ? "0" + ts[1] : ts[1];
            tacResult.append(hour + minute + "Z ");
        } else if (dateTime instanceof XmlCalendar) {
            XmlCalendar xmlCalendar = (XmlCalendar) dateTime;
            DateTime newDateTime = new DateTime(xmlCalendar.getTime(), DateTimeZone.UTC);
            String hourOfDay = String.valueOf(newDateTime.getHourOfDay());
            String minuteOfHour = String.valueOf(newDateTime.getMinuteOfHour());
            hourOfDay = hourOfDay.length() == 1 ? "0" + hourOfDay : hourOfDay;
            minuteOfHour = minuteOfHour.length() == 1 ? "0" + minuteOfHour : minuteOfHour;
            tacResult.append(hourOfDay + minuteOfHour + "Z ");
        }
    }

    private void setAnalysisArray() {
        OMObservationPropertyType[] analysisArray = getSigmet().getAnalysisArray();
        for (OMObservationPropertyType analysis : analysisArray) {
            setHeaderPartOfMessage(analysis.getOMObservation());
            setPhenomenon();
            setAnalysisResult(analysis);
            tacResult.append(NEW_LINE);
        }

    }

    private void setAnalysisResult(OMObservationPropertyType analysis) {
        EvolvingMeteorologicalConditionTypeImpl evolvingMeteorologicalCondition = (EvolvingMeteorologicalConditionTypeImpl)
                analysis.getOMObservation().getResult().selectChildren(new QName(IWXXM_NAMESPACE, "EvolvingMeteorologicalCondition"))[0];

        tacResult.append("OBS" + SPACE);
        tacResult.append("AT ");
        setTime(analysis.getOMObservation().getPhenomenonTime());
        tacResult.append(NEW_LINE);

        AirspaceSolidPropertyType geometry = evolvingMeteorologicalCondition.getGeometry();
        calculateGeometry(geometry);
        calculateSpeedAndDirection(evolvingMeteorologicalCondition);

        ExpectedIntensityChangeType.Enum intensityChange = evolvingMeteorologicalCondition.getIntensityChange();
        String intensityChangeStr = intensityChange.toString();
        if (intensityChangeStr.toUpperCase().contains("NO_CHANGE")) {
            tacResult.append("NC" + SPACE);
        } else if (intensityChangeStr.toUpperCase().contains("INTENSIFYING")) {
            tacResult.append("INTSF" + SPACE);
        } else if (intensityChangeStr.toUpperCase().contains("WEAKENING")) {
            tacResult.append("WKN" + SPACE);
        }


        String end = evolvingMeteorologicalCondition.getId().split("-")[0].toUpperCase();
        if (end.equalsIgnoreCase("VA")) {
            tacResult.append("VA CLD ");
        }

    }

    private void calculateSpeedAndDirection(EvolvingMeteorologicalConditionTypeImpl evolvingMeteorologicalCondition) {
        String direction = getDirection(evolvingMeteorologicalCondition.getDirectionOfMotion());
        tacResult.append("MOV " + direction + SPACE);
        WindConverter invoke = new WindConverter(evolvingMeteorologicalCondition.getSpeedOfMotion().getStringValue(), evolvingMeteorologicalCondition.getSpeedOfMotion().getUom()).invoke();
        tacResult.append(invoke.toString() + METARUtils.getWindUnit(evolvingMeteorologicalCondition.getDirectionOfMotion().getUom()) + SPACE);
    }

    private void calculateGeometry(AirspaceSolidPropertyType geometry) {
        AssociationRoleType horizontalProjection = geometry.getAirspaceSolid().getHorizontalProjection();

        XmlObject[] points = horizontalProjection.selectChildren(new QName(GML_NAMESPACE, "CircleByCenterPoint"));
        LengthType upperLimit = geometry.getAirspaceSolid().getUpperLimit();
        LengthType lowerLimit = geometry.getAirspaceSolid().getLowerLimit();
        if (points.length > 0) {

            CircleByCenterPointTypeImpl point = (CircleByCenterPointTypeImpl) points[0];
            List listValue = point.getPos().getListValue();
            setAltitude(listValue);

            tacResult.append("CB" + SPACE);
            setUpperAndLowerLimit(upperLimit, lowerLimit);
            Double radius = point.getRadius().getDoubleValue();
            String uom = point.getRadius().getUom();
            String unit = getCircleUnit(uom);
            tacResult.append("WI " + radius.intValue() + unit + SPACE).append("OF CENTRE ");
            return;
        }

        points = horizontalProjection.selectChildren(new QName(GML_NAMESPACE, "Polygon"));
        if (points.length > 0) {

            setUpperAndLowerLimit(upperLimit, lowerLimit);
            tacResult.append("N OF ");
            PolygonTypeImpl point = (PolygonTypeImpl) points[0];
            List listValue = ((LinearRingTypeImpl) point.getExterior().getAbstractRing()).getPosList().getListValue();
            for (int i = 0; i < listValue.size(); i += 2) {
                List list = listValue.subList(i, i + 2);
                setAltitude(list);
                if ((i + 2) != listValue.size()) {
                    tacResult.append("- ");
                }
            }
            return;
        }

        points = horizontalProjection.selectChildren(new QName(GML_NAMESPACE, "Point"));
        if (points.length > 0) {
            tacResult.append("CENTRE ");
            PointTypeImpl point = (PointTypeImpl) points[0];
            List listValue = point.getPos().getListValue();
            for (int i = 0; i < listValue.size(); i += 2) {
                List list = listValue.subList(i, i + 2);
                setAltitude(list);
            }
            return;
        }


    }

    private String getDirection(AngleType directionOfMotion) {
        double doubleValue = directionOfMotion.getDoubleValue();
        if (doubleValue == 0 || doubleValue == 360) {
            return "N";
        }
        if (doubleValue == 90) {
            return "E";
        }
        if (doubleValue == 180) {
            return "S";
        }
        if (doubleValue == 270) {
            return "W";
        }
        if (doubleValue > 0 && doubleValue < 90) {
            return "NE";
        }
        if (doubleValue > 90 && doubleValue < 180) {
            return "SE";
        }
        if (doubleValue > 180 && doubleValue < 270) {
            return "SW";
        }
        if (doubleValue > 270 && doubleValue < 360) {
            return "NW";
        }
        throw new RuntimeException("directionOfMotion must be between 0 - 360 degree :  " + doubleValue);
    }

    private void setUpperAndLowerLimit(LengthType upperLimit, LengthType lowerLimit) {
        if (upperLimit != null && lowerLimit == null) {
            Double upper = upperLimit.getDoubleValue() / 100;
            //TODO
//                tacResult.append("TOP " + "FL" + upper.intValue() + SPACE);
            tacResult.append("SFC/" + "FL" + upper.intValue() + SPACE);
        }
        if (lowerLimit != null) {
            Double lower = lowerLimit.getDoubleValue() / 100;
            tacResult.append("DOWN " + "FL" + lower.intValue() + SPACE);
        }
    }

    private String getCircleUnit(String uom) {
        if (uom.toUpperCase().endsWith("N.MI")) {
            return "NM";
        }
        throw new RuntimeException("UOM not support : " + uom);
    }

    private void setHeaderPartOfMessage(OMObservationType omObservationType) {

        XmlObject[] sf_spatialSamplingFeatureArray = (XmlObject[]) omObservationType.getFeatureOfInterest().
                selectChildren(new QName(MeteorologicalNamespaces.SAMS_NAMESPACE, "SF_SpatialSamplingFeature"));

        if (sf_spatialSamplingFeatureArray.length != 0) {
            SFSpatialSamplingFeatureTypeImpl sf_spatialSamplingFeature = (SFSpatialSamplingFeatureTypeImpl) sf_spatialSamplingFeatureArray[0];
            FeaturePropertyType sampledFeature = sf_spatialSamplingFeature.getSampledFeature();
            String[] split = sampledFeature.getHref().split("/");
            tacResult.append(getIssuingAirTrafficServicesUnit() + SPACE);
            String name = sf_spatialSamplingFeature.getId().substring(sf_spatialSamplingFeature.getId().lastIndexOf("-") + 1).toUpperCase();
            tacResult.append(name + SPACE);
            tacResult.append(split[split.length - 2].toUpperCase() + SPACE);
        }
    }

    private void setPhenomenon() {
        String phenomenon = getSigmet().getPhenomenon().getHref().substring(getSigmet().getPhenomenon().getHref().lastIndexOf("/") + 1);
        TropicalCyclonePropertyType tropicalCyclone = null;
        VolcanoPropertyType eruptingVolcano = null;
        tacResult.append(phenomenon + SPACE);
        if (sigmetDocument instanceof TropicalCycloneSIGMETDocumentImpl) {
            tropicalCyclone = ((TropicalCycloneSIGMETDocumentImpl) sigmetDocument).getTropicalCycloneSIGMET().getTropicalCyclone();
        } else if (sigmetDocument instanceof VolcanicAshSIGMETDocumentImpl) {
            eruptingVolcano = ((VolcanicAshSIGMETDocumentImpl) sigmetDocument).getVolcanicAshSIGMET().getEruptingVolcano();
        }

        if (tropicalCyclone != null) {
            String name2 = tropicalCyclone.getTropicalCyclone().getName2();
            tacResult.append(name2 + SPACE);
        }
        if (eruptingVolcano != null) {
            tacResult.append("ERUPTION" + SPACE);
            VolcanoTypeImpl volcano = (VolcanoTypeImpl) eruptingVolcano.selectChildren(new QName(MTCE_NAMESPACE, "Volcano"))[0];
            tacResult.append(volcano.getName2() + SPACE);
            PointTypeImpl point = (PointTypeImpl) volcano.getPosition().selectChildren(new QName(GML_NAMESPACE, "Point"))[0];
            List<Double> listValue = point.getPos().getListValue();
            if (listValue.size() > 0) {
                tacResult.append("PSN" + SPACE);
                setAltitude(listValue);

            }
            tacResult.append("VA CLD" + SPACE);
            System.out.println();
        }
    }

    private void setAltitude(List<Double> listValue) {
        Double NS = listValue.get(0);
        int neg = 1;
        if (NS.compareTo(new Double(0)) > 0) {
            tacResult.append("N");
            neg = 1;
        } else {
            tacResult.append("S0");
            neg = -1;
        }
        String NSStr = String.valueOf(NS.intValue() * neg);
        tacResult.append(NSStr.length() == 1 ? "0" + NSStr : NSStr);
        String NsStrPart2 = String.valueOf(new Double(new Double((NS - NS.intValue()) * 100).floatValue() * neg).intValue());
        tacResult.append((NsStrPart2.length() == 1 ? "0" + NsStrPart2 : NsStrPart2) + SPACE);
        Double EW = listValue.get(1);
        if (EW.compareTo(new Double(0)) > 0) {
            tacResult.append("E");
            neg = 1;
        } else {
            tacResult.append("W0");
            neg = -1;
        }
        String EWStr = String.valueOf(EW.intValue() * neg);
        tacResult.append(EWStr.length() == 1 ? "0" + EWStr : EWStr);
        String EWStrPart2 = String.valueOf(new Double(new Double((EW - EW.intValue()) * 100).floatValue() * neg).intValue());
        tacResult.append((EWStrPart2.length() == 1 ? "0" + EWStrPart2 : EWStrPart2) + SPACE);
    }

    private void setLocation() {
        String originatingMeteorologicalWatchOffice = getSigmet().getOriginatingMeteorologicalWatchOffice().getHref().substring(getSigmet().getOriginatingMeteorologicalWatchOffice().getHref().lastIndexOf("/") + 1);
        tacResult.append(originatingMeteorologicalWatchOffice + SPACE + "-" + NEW_LINE);
    }

    private void setDateTimeOfIssue() {

    }

    private void setIssuingOffice() {
        //issuingAirTrafficServicesUnit
        issuingAirTrafficServicesUnit = getSigmet().getIssuingAirTrafficServicesUnit().getHref().substring(getSigmet().getIssuingAirTrafficServicesUnit().getHref().lastIndexOf("/") + 1);
        tacResult.append(issuingAirTrafficServicesUnit + SPACE);
    }


    public String getIssuingAirTrafficServicesUnit() {
        return issuingAirTrafficServicesUnit;
    }

    private void setValidityPeriod() {

        TimePeriodType timePeriod = getSigmet().getValidPeriod().getTimePeriod();
        if (timePeriod != null) {
            Date beginDate = ((XmlCalendar) timePeriod.getBeginPosition().getObjectValue()).getTime();
            Date endDate = ((XmlCalendar) timePeriod.getEndPosition().getObjectValue()).getTime();
            DateTime beginDateTime = new DateTime(beginDate, DateTimeZone.UTC);
            DateTime endDateTime = new DateTime(endDate, DateTimeZone.UTC);

            tacResult.append("VALID" + SPACE);
            tacResult.append(getDate(beginDateTime)).append("/");
            tacResult.append(getDate(endDateTime)).append(SPACE);
        } else {
            String[] split = getSigmet().getValidPeriod().getHref().split("-");
            String fromDateTime = split[2];
            String toDateTime = split[3];
            fromDateTime = getDateTimeFormat(fromDateTime);
            toDateTime = getDateTimeFormat(toDateTime);
            tacResult.append("VALID" + SPACE);
            tacResult.append(fromDateTime).append("/");
            tacResult.append(toDateTime).append(SPACE);

        }
    }

    private String getDateTimeFormat(String dateTime) {
        String[] ts = dateTime.split("T");
        String time = ts[1].replace("Z", "");
        if (time.length() == 1) {
            time = "0" + time + "00";
        } else if (time.length() == 2) {
            time = time + "00";
        }
        return ts[0] + time;
    }

    private String getDate(String dateTime) {
        if (!StringUtils.isEmpty(dateTime)) {
            String[] time = dateTime.split("T")[1].split(":");
            String[] date = dateTime.split("T")[0].split("-");
            return date[2] + time[0] + time[1];
        }
        throw new RuntimeException("Date Time is Invalid!");
    }

    private String getDate(DateTime dateTime) {
        String result = "";
        if (dateTime != null) {
            int dayOfMonth = dateTime.getDayOfMonth();
            if (dayOfMonth < 10) {
                result += "0" + dayOfMonth;
            } else {
                result += String.valueOf(dayOfMonth);
            }

            int hourOfDay = dateTime.getHourOfDay();
            if (hourOfDay < 10) {
                result += "0" + hourOfDay;
            } else {
                result += String.valueOf(hourOfDay);
            }
            int minuteOfHour = dateTime.getMinuteOfHour();
            if (minuteOfHour < 10) {
                result += "0" + minuteOfHour;
            } else {
                result += String.valueOf(minuteOfHour);
            }
            return result;
        }
        throw new RuntimeException("Date Time is Invalid!");
    }

    private String setBulletinNumber(SIGMETType sigmet) {
        tacResult.append(tacType + SPACE);
        String sequenceNumber = sigmet.getSequenceNumber();
        if (sequenceNumber.length() == 1) {
            sequenceNumber = "0" + sequenceNumber;
        }
        tacResult.append(sequenceNumber + SPACE);
//        tacResult.append("A" + sequenceNumber + SPACE);
        return sequenceNumber;
    }

    private void setTACType() {
        SIGMETType sigmet = getSigmet();
        if (sigmet instanceof TropicalCycloneSIGMETTypeImpl) {
            tacResult.append("WVCN" + SPACE);
        } else if (sigmet instanceof VolcanicAshSIGMETTypeImpl) {
            tacResult.append("WCCN" + SPACE);
        } else {
            tacResult.append("WSCN" + SPACE);
        }
    }

    @Override
    public String buildTAC() {
        return tacResult.toString();
    }

    public SIGMETType getSigmet() {
        return sigmetDocument.getSIGMET();
    }
}
