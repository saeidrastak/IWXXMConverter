package com.weather.converter.util.converter;

import com.weather.converter.util.common.ApplicationUtils;
import com.weather.converter.util.iwxxm.CommonUtils;
import com.weather.converter.util.iwxxm.METARUtils;
import com.weather.converter.util.iwxxm.WindConverter;
import net.opengis.gml.x32.impl.TimePeriodTypeImpl;
import net.opengis.om.x20.OMObservationPropertyType;
import net.opengis.om.x20.OMObservationType;
import net.opengis.om.x20.TimeObjectPropertyType;
import org.apache.xmlbeans.XmlCalendar;
import org.apache.xmlbeans.XmlException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import xint.icao.iwxxm.x10RC1.*;
import xint.icao.iwxxm.x10RC1.impl.MeteorologicalAerodromeForecastRecordTypeImpl;
import xint.wmo.data.def.metBasic.x10RC1.*;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.Date;

import static com.weather.converter.util.common.MeteorologicalNamespaces.IWXXM_NAMESPACE;

/**
 * Created by Rastakfard (Saeid.Rastak@Gmail.com) on 1/4/2016.
 */
public class TAFConverter extends TACConverter {

    private boolean tafParsed = false;
    private TAFDocument tafDocument;

    private MeteorologicalAerodromeForecastRecordTypeImpl getMeteorologicalAerodromeObservationRecord() {
        return (MeteorologicalAerodromeForecastRecordTypeImpl) tafDocument.getTAF().getBaseForecast().getOMObservation().
                getResult().selectChildren(new QName(IWXXM_NAMESPACE, "MeteorologicalAerodromeForecastRecord"))[0];
    }

    @Override
    public void parseXmlDocument(String path) throws IOException, XmlException {
        try {
            tafDocument = ApplicationUtils.parseAsTAFDocument(path);
            tafParsed = true;
        } catch (XmlException ex) {
            //TODO handle exception gracefully
        }
    }

    @Override
    public void initTAC() {
        if (!tafParsed) return;

        parseReportType();
        parseStationIdentifier();
        parseOriginDateTime();
        parseValidPeriodDateTime();

        parseForecastMeteorologicalConditions();


        parseForecastChanges();


    }


    private void parseForecastMeteorologicalConditions() {

        OMObservationType omObservation = tafDocument.getTAF().getBaseForecast().getOMObservation();
        MeteorologicalAerodromeForecastRecordTypeImpl forecastRecordType = getForecastRecordType(omObservation);
        parseForecastRecord(forecastRecordType, null, false);

    }

    private void parseForecastRecord(MeteorologicalAerodromeForecastRecordTypeImpl forecastRecordType, TimeObjectPropertyType phenomenonTime, boolean isChangeForecast) {
        if (isChangeForecast) {
            String changeIndicator = forecastRecordType.getChangeIndicator().toString();
            if ("BECOMING".equalsIgnoreCase(changeIndicator)) {
                tacResult.append("BCMG" + SPACE);
            } else if ("TEMPORARY_FLUCTUATIONS".equalsIgnoreCase(changeIndicator)) {
                tacResult.append("TEMPO" + SPACE);
            } else if ("FROM".equalsIgnoreCase(changeIndicator)) {
                tacResult.append("FM" + SPACE);
            } else if ("TO".equalsIgnoreCase(changeIndicator)) {
                tacResult.append("TO" + SPACE);
            }
            Date beginTime = ((XmlCalendar) ((TimePeriodTypeImpl) phenomenonTime.getAbstractTimeObject()).getBeginPosition().getObjectValue()).getTime();
            Date endTime = ((XmlCalendar) ((TimePeriodTypeImpl) phenomenonTime.getAbstractTimeObject()).getEndPosition().getObjectValue()).getTime();
            setValidTime(beginTime, endTime);
        }
        parseWindInformation(forecastRecordType);
        parseHorizontalVisibility(forecastRecordType);
        parseWeatherInformation(forecastRecordType);
        parseSkyCondition(forecastRecordType);
        parseWindSheer(forecastRecordType);
    }

    private void parseForecastChanges() {
        OMObservationPropertyType[] changeForecastArray = tafDocument.getTAF().getChangeForecastArray();
        for (OMObservationPropertyType changeForecast : changeForecastArray) {
            OMObservationType omObservation = changeForecast.getOMObservation();
            TimeObjectPropertyType phenomenonTime = omObservation.getPhenomenonTime();
            MeteorologicalAerodromeForecastRecordTypeImpl forecastRecordType = (MeteorologicalAerodromeForecastRecordTypeImpl) omObservation.getResult().
                    selectChildren(IWXXM_NAMESPACE, "MeteorologicalAerodromeForecastRecord")[0];
            parseForecastRecord(forecastRecordType, phenomenonTime, true);
            tacResult.append(NEW_LINE);
        }
    }


    private void parseWindSheer(MeteorologicalAerodromeForecastRecordTypeImpl forecastRecordType) {
        //TODO not exists in 1.0.RC1 but exist in US format
    }

    private void parseSkyCondition(MeteorologicalAerodromeForecastRecordTypeImpl forecastRecordType) {
        AerodromeCloudForecastPropertyType cloud = forecastRecordType.getCloud();
        if (cloud != null) {
            VerticalVisibilityType verticalVisibility = cloud.getAerodromeCloudForecast().getVerticalVisibility();
            if (verticalVisibility == null) {
//                tacResult.append("VV///" + SPACE);
            } else {
                String unit = verticalVisibility.getUom();
                String resultVerticalVisibilityStr = "";
                Double verticalVisibilityDoubleValue = verticalVisibility.getDoubleValue();
                if (verticalVisibilityDoubleValue == 0 || verticalVisibilityDoubleValue == null) {
                    resultVerticalVisibilityStr = "///";
                } else {
                    Double resultVerticalVisibility = verticalVisibilityDoubleValue / 100;
                    if (resultVerticalVisibility < 10) {
                        resultVerticalVisibilityStr = "00" + resultVerticalVisibility;
                    } else if (resultVerticalVisibility < 100) {
                        resultVerticalVisibilityStr = "0" + resultVerticalVisibility;
                    } else {
                        resultVerticalVisibilityStr = String.valueOf(resultVerticalVisibility);
                    }
                }
                tacResult.append("VV" + resultVerticalVisibilityStr + SPACE);
            }
            CloudLayerPropertyType[] layerArray = cloud.getAerodromeCloudForecast().getLayerArray();
            METARUtils.parseCloudLayers(layerArray, tacResult);
        }
    }

    private void parseWeatherInformation(MeteorologicalAerodromeForecastRecordTypeImpl forecastRecordType) {
        AerodromeForecastWeatherType[] weatherArray = forecastRecordType.getWeatherArray();
        for (AerodromeForecastWeatherType forecastWeatherType : weatherArray) {
            String weather = forecastWeatherType.getHref().substring(forecastWeatherType.getHref().lastIndexOf("/") + 1);
            tacResult.append(weather + SPACE);
        }
    }

    private void parseHorizontalVisibility(MeteorologicalAerodromeForecastRecordTypeImpl forecastRecordType) {
        boolean setPrevailingHorizontalVisibility = forecastRecordType.isSetPrevailingHorizontalVisibility();
        if (setPrevailingHorizontalVisibility) {
            PrevailingVisibilityType prevailingVisibility = forecastRecordType.getPrevailingHorizontalVisibility();
            String prevailingHorizontalVisibilityStr = "";
            String unit = prevailingVisibility.getUom();
            int phvInMeters = CommonUtils.convertToMeters(prevailingVisibility.getDoubleValue(), unit);
            if (phvInMeters < 1000 && phvInMeters > 100) {
                prevailingHorizontalVisibilityStr = "0" + phvInMeters;
            } else if (phvInMeters < 100) {
                prevailingHorizontalVisibilityStr = "00" + phvInMeters;
            } else if (phvInMeters >= 10000) {
                prevailingHorizontalVisibilityStr = "9999";
            } else if (phvInMeters < 10000) {
                prevailingHorizontalVisibilityStr = String.valueOf(phvInMeters);
            }
            tacResult.append(prevailingHorizontalVisibilityStr + SPACE);
        }
    }

    private void parseWindInformation(MeteorologicalAerodromeForecastRecordTypeImpl forecastRecordType) {

        boolean setSurfaceWind = forecastRecordType.isSetSurfaceWind();
        if (setSurfaceWind) {
            AerodromeSurfaceWindForecastPropertyType surfaceWind = forecastRecordType.getSurfaceWind();
            String windSection = "";
            String meanWindDirection = "";
            AerodromeSurfaceWindForecastType aerodromeSurfaceWindForecast = surfaceWind.getAerodromeSurfaceWindForecast();
            Double meanWindDirectionDouble = aerodromeSurfaceWindForecast.getMeanWindDirection().getDoubleValue();
            meanWindDirection = METARUtils.getMeanWindDirectionString(meanWindDirectionDouble);

            WindSpeedType windSpeedType = aerodromeSurfaceWindForecast.getWindSpeed();

            String windSpeed = "";
            if (windSpeedType != null) {
                windSpeed = windSpeedType.getStringValue();
                String windUnit = METARUtils.getWindUnit(aerodromeSurfaceWindForecast.getWindSpeed().getUom());
                WindConverter windConverter = new WindConverter(windSpeed, windUnit).invoke();
                String windSpeedStr = windConverter.toString();
                Double windSpeedDouble = windConverter.toDouble();
                if (windSpeedDouble == 0) {
                    tacResult.append("00000KT" + SPACE);
                    return;
                }
                if (windSpeedStr.length() == 1) {
                    windSpeedStr = "0" + windSpeedStr;
                }
                boolean variableDirection = aerodromeSurfaceWindForecast.getVariableWindDirection();
                windSection = windSpeedStr;
                if (windSpeedDouble.compareTo(WindConverter.MIN_WIND_SPEED_THRESHOLD) <= 0) {
                    if (variableDirection) {
                        meanWindDirection = "VRB";
                    }
                    //TODO ask mr. davoodi
                } else {
                    if (aerodromeSurfaceWindForecast.getWindGustSpeed() != null) {
                        GustSpeedType windGust = aerodromeSurfaceWindForecast.getWindGustSpeed();
                        windConverter = new WindConverter(windGust.getStringValue(), windUnit).invoke();
                        windSection += "G" + windConverter.toString();
                    }
                }
                tacResult.append(meanWindDirection + windSection + windUnit + SPACE);

                //TODO
            /*boolean setExtremeClockwiseWindDirection = aerodromeSurfaceWindForecast.isSetExtremeClockwiseWindDirection();
            if (setExtremeClockwiseWindDirection) {
                double extremeCounterClockwiseWindDirection = surfaceWind.getAerodromeSurfaceWind().getExtremeCounterClockwiseWindDirection().getDoubleValue();
                Double extremeClockwiseWindDirection = surfaceWind.getAerodromeSurfaceWind().getExtremeClockwiseWindDirection().getDoubleValue();
                String extremeClockwiseWindDirectionStr = getMeanWindDirectionString(extremeClockwiseWindDirection);
                String extremeCounterClockwiseWindDirectionStr = getMeanWindDirectionString(extremeCounterClockwiseWindDirection);
                tacResult.append(extremeClockwiseWindDirectionStr + "V" + extremeCounterClockwiseWindDirectionStr + SPACE);
            }*/
            } else {
                tacResult.append("00000KT" + SPACE);
            }
        }
    }

    private MeteorologicalAerodromeForecastRecordTypeImpl getForecastRecordType(OMObservationType omObservation) {
        MeteorologicalAerodromeForecastRecordTypeImpl forecastRecordType =
                (MeteorologicalAerodromeForecastRecordTypeImpl) omObservation.getResult().selectChildren(new QName(IWXXM_NAMESPACE, "MeteorologicalAerodromeForecastRecord"))[0];
        return forecastRecordType;
    }

    private void parseValidPeriodDateTime() {
        Date beginTime = ((XmlCalendar) tafDocument.getTAF().getValidTime().getTimePeriod().getBeginPosition().getObjectValue()).getTime();
        Date endTime = ((XmlCalendar) tafDocument.getTAF().getValidTime().getTimePeriod().getEndPosition().getObjectValue()).getTime();
        setValidTime(beginTime, endTime);
    }

    private void setValidTime(Date beginTime, Date endTime) {
        DateTime beginDateTime = new DateTime(beginTime, DateTimeZone.UTC);
        DateTime endDateTime = new DateTime(endTime, DateTimeZone.UTC);
        tacResult.append(CommonUtils.convertToTwoDigit(beginDateTime.getDayOfMonth()));
        tacResult.append(CommonUtils.convertToTwoDigit(beginDateTime.getHourOfDay()));
        tacResult.append("/" + CommonUtils.convertToTwoDigit(endDateTime.getDayOfMonth()));
        tacResult.append(CommonUtils.convertToTwoDigit(endDateTime.getHourOfDay()));
        tacResult.append(SPACE);
    }

    private void parseOriginDateTime() {

        Date time = ((XmlCalendar) tafDocument.getTAF().getIssueTime().getTimeInstant().getTimePosition().getObjectValue()).getTime();
        DateTime dateTime = new DateTime(time, DateTimeZone.UTC);
        tacResult.append(CommonUtils.convertToTwoDigit(dateTime.getDayOfMonth()));
        tacResult.append(CommonUtils.convertToTwoDigit(dateTime.getHourOfDay()));
        tacResult.append(CommonUtils.convertToTwoDigit(dateTime.getMinuteOfHour()));
        tacResult.append("Z" + SPACE);

    }

    private void parseStationIdentifier() {
        String[] observationTitle;
        observationTitle = tafDocument.getTAF().getId().split("-");
        tacResult.append(observationTitle[1] + SPACE);
    }

    private void parseReportType() {
//        String title = tafDocument.getTAF().getBaseForecast().getOMObservation().getProcedure().getTitle();
        tacResult.append("TAF" + SPACE);
    }

    @Override
    public String buildTAC() {
        return tacResult.toString();
    }


}
