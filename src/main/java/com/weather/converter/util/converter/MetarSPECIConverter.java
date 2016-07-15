package com.weather.converter.util.converter;

import com.weather.converter.util.common.ApplicationUtils;
import com.weather.converter.util.iwxxm.CommonUtils;
import com.weather.converter.util.iwxxm.METARUtils;
import com.weather.converter.util.iwxxm.WindConverter;
import net.opengis.gml.x32.AngleType;
import net.opengis.gml.x32.impl.TimeInstantTypeImpl;
import net.opengis.gml.x32.impl.TimePeriodTypeImpl;
import net.opengis.om.x20.OMObservationPropertyType;
import org.apache.commons.lang3.StringUtils;
import org.apache.xmlbeans.XmlCalendar;
import org.apache.xmlbeans.XmlException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import xint.icao.iwxxm.x10RC1.*;
import xint.icao.iwxxm.x10RC1.impl.MeteorologicalAerodromeObservationRecordTypeImpl;
import xint.icao.iwxxm.x10RC1.impl.MeteorologicalAerodromeTrendForecastRecordTypeImpl;
import xint.icao.saf.x10RC1.RunwayPropertyType;
import xint.wmo.data.def.metBasic.x10RC1.*;
import xint.wmo.data.def.metce.x10RC1.ForecastChangeIndicatorType;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.Date;

import static com.weather.converter.util.common.MeteorologicalNamespaces.IWXXM_NAMESPACE;

/**
 * Created by r.rastakfard on 12/24/2015.
 */
public class MetarSPECIConverter extends TACConverter {

    String tacType = "METAR";
    private METARDocument metarDocument;
    private SPECIDocument speciDocument;
    private boolean documentParsed = false;
    private boolean NOSIG = true;
    private int forecastLineBreaker = 4;


    public MetarSPECIConverter(String TacType) {
        tacType = TacType;
    }


    @Override
    public void parseXmlDocument(String path) throws IOException, XmlException {
        if (isMetar()) {
            metarDocument = ApplicationUtils.parseAsMetarDocument(path);
        } else {
            speciDocument = ApplicationUtils.parseAsSPECIDocument(path);
        }
        documentParsed = true;
    }

    public void initTAC() throws IOException, XmlException {

        if (!documentParsed) return;

        DateTime phenomenonTime = setDateAndType();

        setMetarStationId();

        setPhenomenonTime(phenomenonTime);

        setReportModifier();

        setWindInformation();

        setHorizontalVisibility();

        setRunwayVisualRange();

        setPresentWeathers();

        setSkyCondition();

        setTemperature();

        setAltimeter();

        setRecentWeather();

        setRecentWindShear();

        tacResult.append(NEW_LINE);

        setTrendForecasts(phenomenonTime);

        setRemarks();

        setSupplementaryRemarks();

        setMeanSeaLevelPressure();

        setRunwayReport();

        /*
            End of Metar/SPECI
         */
        tacResult.append("=");

//        tacResult = breakIntoMultipleLine(tacResult);

        documentParsed = true;
    }

    /**
     * METAR reports may include runway reports which contain information
     * about snow/water coverage and friction coefficient. A runway report
     * consists of eight digits in five groups (RRRRERCReReRBRBR) with the
     * following interpretation:
     */
    private void setRunwayReport() {

    }

    private StringBuilder breakIntoMultipleLine(StringBuilder tacResult) {
        String result = tacResult.toString();
        StringBuilder stringBuilder = new StringBuilder();
        String[] splitLine = result.split(NEW_LINE);
        for (String line : splitLine) {
            int lineCount = line.length() / LINE_WORD_COUNT;
            int lastProcessedLine = 0;
            for (int i = 0; i < lineCount; i++) {
                stringBuilder.append(line.substring(i * LINE_WORD_COUNT, LINE_WORD_COUNT) + NEW_LINE);
                lastProcessedLine++;
            }
            if (lineCount == 0) {
                stringBuilder.append(line + NEW_LINE);
            } else {
                stringBuilder.append(line.substring(lastProcessedLine * LINE_WORD_COUNT));
            }
        }

        return stringBuilder;
    }

    /**
     * Trend forecasts are included when a change, required to be indicated in accordance with the governing criteria for significant changes, is expected for one or several of the observed elements - wind, horizontal visibility, present weather, clouds, or vertical visibility. The governing criteria for issuing trend forecasts is specified in publication WMO-No. 49-Technical Regulations.
     * <p/>
     * <p/>
     * TTTTT - Change Indicator.
     * One of the following change indicators will be used for TTTTT:
     * <p/>
     * BECMG or
     * TEMPO.
     * TTGGgg - Time Group
     * The time group GGgg is followed by one of the letter indicators FM (from), or TL (until) or AT (at) for TT to indicate the beginning (FM) or the end (TL) of a forecast change, or the time (AT) at which a specific forecast conditions(s) is(are) expected (in hours and minutes for GGgg) the change is expected to occur.
     * <p/>
     * BECMG - Becoming
     * The change indicator BECMG is used to describe expected changes to meteorological conditions which reach or pass specified threshold criteria at either a regular or irregular rate. These changes are indicated as follows:
     * <p/>
     * (a) When the change is forecast to begin and end wholly within the trend forecast period: by the change indicator BECMG followed by the letter indicators FM and TL respectively with their associated time groups, to indicate the beginning and end of the change. For example, a trend forecast period from 1000 to 1200 UTC is in the form BECMG FM1030 TL1130;
     * (b) When the change is forecast to occur from the beginning of the trend forecast period and be completed before the end of that period: by the change indicator BECMG followed only by the letter indicator TL and its associated time group (the letter indicator FM and its associated time group being omitted), to indicate the end of the change. For example: BECMG TL1100.
     * (c) When the change is forecast to begin during the trend forecast period and be completed at the end of that period: by the change indicator BECMG followed only the letter indicator FM and its associated time group (the letter indicator TL and its associated time group being omitted), to indicate the beginning of the change. For example: BECMG FM1100.
     * (d) When the change is forecast to occur at a specific time during the trend forecast period: by the change indicator BECMG followed by the letter indicator AT and its associated time group, to indicate the time of the change. For example: BECMG AT1100.
     * (e) When changes are forecast to take place at midnight UTC, the time shall be indicated:
     * (i) By 0000 when associated with FM and AT;
     * (ii) By 2400 when associated with TL.
     * (f) When the change is forecast to commence at the beginning of the trend forecast period and be completed by the end of that period, or when the change is forecast to occur within the trend forecast period but the time of the change is uncertain (possibly shortly after the beginning of the trend forecast period, or midway or near the end of that period), the change shall be indicated by only the change indicator BECMG (letter indicator(s) FM and TL or AT and associated time group(s) being omitted).
     * TEMPO - Temporary
     * This change indicator is used to describe expected temporary fluctuations to meteorological conditions which reach or pass specified threshold criteria and last for a period of less than one hour in each instance and in the aggregate cover less than half of the forecast period during which the fluctuations are expected to occur. The time indicators FM and TL, with the associated time group, is used with TEMPO in the same manner as used with BECMG in a, b, c and f above.
     * <p/>
     * Following the change groups TTTTT (TTGGgg), only the group(s) referring to the element(s) which is(are) forecast to change significantly shall be included. However, in the case of significant changes of the clouds, all cloud groups including any significant layer(s) or masses not expected to change, shall be included.
     * Inclusion of significant forecast weather w'w' using appropriate abbreviations from Table A-10 is restricted to indicate the onset, cessation, or change, in intensity of the following weather phenomena.
     * <p/>
     * - Freezing precipitation;
     * -	Moderate or heavy rain, snow, ice pellets, hail, small hail, snow pellets, rain and snow mixed;
     * -	Drifting dust, sand, or snow.
     * -	Blowing dust, sand, or snow (including duststorm and sandstorm);
     * - Thunderstorm (with rain, ice pellets, hail or soft hail, or snow, or combination therof);
     * - Squall;
     * -	Funnel Cloud (tornado or waterspout);
     * -	Other weather phenomena given in Table A-10 which are expected to cause a significant change in visibility.
     * NSW	-	No Significant Weather
     * To indicate the end of significant weather phenomena w'w', the abbreviation NSW will replace w'w'.
     * <p/>
     * SKC	-	Sky Clear
     * To indicate a change to clear sky, the abbreviation SKC will replace NsNsNshshs hs or VVhshshs.
     * <p/>
     * NOSIG	-	No Significant change
     * When none of the elements are expected to change significantly as to require a change to be indicated, this shall be indicated by the code word NOSIG. NOSIG (no significant change) is used to indicate meteorological conditions which do not reach or pass specified threshold criteria.
     *
     * @param phenomenonTime
     */
    private void setTrendForecasts(DateTime phenomenonTime) {

        OMObservationPropertyType[] trendForecastArray;

        if (isMetar()) {
            trendForecastArray = metarDocument.getMeteorologicalAerodromeObservationReport().getTrendForecastArray();
        } else {
            trendForecastArray = speciDocument.getMeteorologicalAerodromeObservationReport().getTrendForecastArray();
        }


        int i = 1;

        for (OMObservationPropertyType trendForecast : trendForecastArray) {
            MeteorologicalAerodromeTrendForecastRecordTypeImpl
                    trendForecastRecord = (MeteorologicalAerodromeTrendForecastRecordTypeImpl)
                    trendForecast.getOMObservation().getResult().
                            selectChildren(new QName(IWXXM_NAMESPACE, "MeteorologicalAerodromeTrendForecastRecord"))[0];

            ForecastChangeIndicatorType.Enum changeIndicator = trendForecastRecord.getChangeIndicator();
            if (changeIndicator != null) {
                if ("BECOMING".equalsIgnoreCase(changeIndicator.toString())) {
                    tacResult.append("BECMG" + SPACE);
                } else {
                    tacResult.append("TEMPO" + SPACE);
                }
                //TODO add TTGGgg - Time Group
                Date beginTrendForecastTime = ((XmlCalendar) ((TimePeriodTypeImpl) trendForecast.getOMObservation().getPhenomenonTime().getAbstractTimeObject()).getBeginPosition().getObjectValue()).getTime();
                Date endTrendForecastTime = ((XmlCalendar) ((TimePeriodTypeImpl) trendForecast.getOMObservation().getPhenomenonTime().getAbstractTimeObject()).getEndPosition().getObjectValue()).getTime();

                if (beginTrendForecastTime != null && endTrendForecastTime != null) {

                    DateTime beginTrendForecastDateTime = new DateTime(beginTrendForecastTime, DateTimeZone.UTC);
                    DateTime endTrendForecastDateTime = new DateTime(endTrendForecastTime, DateTimeZone.UTC);
                    DateTime dateTime = phenomenonTime;
                    DateTime endPhenomenonTime = dateTime.plusHours(2);

                    if (beginTrendForecastDateTime.getHourOfDay() == 0 &&
                            beginTrendForecastDateTime.getMinuteOfHour() == 0) {
                        tacResult.append("TL2400" + SPACE);
                    } else if (beginTrendForecastTime.compareTo(dateTime.toDate()) > 0 &&
                            endTrendForecastTime.compareTo(endPhenomenonTime.toDate()) == 0) {
                        String hour = CommonUtils.convertToTwoDigit(beginTrendForecastDateTime.getHourOfDay());
                        String minute = CommonUtils.convertToTwoDigit(beginTrendForecastDateTime.getMinuteOfHour());
                        tacResult.append("FM" + hour + minute + SPACE);
                    } else if (beginTrendForecastTime.compareTo(dateTime.toDate()) == 0 &&
                            endTrendForecastTime.compareTo(endPhenomenonTime.toDate()) < 0) {
                        String hour = CommonUtils.convertToTwoDigit(endTrendForecastDateTime.getHourOfDay());
                        String minute = CommonUtils.convertToTwoDigit(endTrendForecastDateTime.getMinuteOfHour());
                        tacResult.append("TL" + hour + minute + SPACE);
                    } else if ((beginTrendForecastTime.compareTo(dateTime.toDate()) > 0 &&
                            endTrendForecastTime.compareTo(endPhenomenonTime.toDate()) > 0) ||
                            (beginTrendForecastTime.compareTo(endTrendForecastTime) == 0)) {
                        String hour = CommonUtils.convertToTwoDigit(beginTrendForecastDateTime.getHourOfDay());
                        String minute = CommonUtils.convertToTwoDigit(beginTrendForecastDateTime.getMinuteOfHour());
                        tacResult.append("AT" + hour + minute + SPACE);
                    } else if (beginTrendForecastTime.compareTo(dateTime.toDate()) > 0 &&
                            endTrendForecastTime.compareTo(endPhenomenonTime.toDate()) < 0) {
                        String hour = CommonUtils.convertToTwoDigit(beginTrendForecastDateTime.getHourOfDay());
                        String minute = CommonUtils.convertToTwoDigit(beginTrendForecastDateTime.getMinuteOfHour());
                        String hour1 = CommonUtils.convertToTwoDigit(endTrendForecastDateTime.getHourOfDay());
                        String minute1 = CommonUtils.convertToTwoDigit(endTrendForecastDateTime.getMinuteOfHour());
                        tacResult.append("FM" + hour + minute + SPACE);
                        tacResult.append("TL" + hour1 + minute1 + SPACE);
                    }
                }

                PrevailingVisibilityType prevailingHorizontalVisibility = trendForecastRecord.getPrevailingHorizontalVisibility();
                if (prevailingHorizontalVisibility != null) {
                    String prevailingHorizontalVisibilityStr = "";
                    String unit = prevailingHorizontalVisibility.getUom();
                    int phvInMeters = CommonUtils.convertToMeters(prevailingHorizontalVisibility.getDoubleValue(), unit);
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


                AerodromeForecastWeatherType forecastWeather = trendForecastRecord.getForecastWeather();
                if (forecastWeather.getNilReason() != null) {
                    tacResult.append("NWS" + SPACE);
                } else {
                    String href = forecastWeather.getHref().substring(forecastWeather.getHref().lastIndexOf("/") + 1);
                    tacResult.append(href + SPACE);
                }
                NOSIG = false;
            }


            AerodromeCloudForecastPropertyType cloud = trendForecastRecord.getCloud();
            if (cloud != null) {
                AerodromeCloudForecastType aerodromeCloudForecast = cloud.getAerodromeCloudForecast();
                VerticalVisibilityType verticalVisibility = aerodromeCloudForecast.getVerticalVisibility();
                if (verticalVisibility != null) {
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
                } else {
                    //TODO
                    tacResult.append("VV///" + SPACE);
                }
                NOSIG = false;
            }


            boolean ceilingAndVisibilityOK = trendForecastRecord.getCeilingAndVisibilityOK();
            if (ceilingAndVisibilityOK) {
                tacResult.append("CAVOK" + SPACE);
            }

            AerodromeSurfaceWindTrendForecastPropertyType surfaceWind = trendForecastRecord.getSurfaceWind();

            if (surfaceWind != null) {
                AerodromeSurfaceWindTrendForecastType aerodromeSurfaceWindTrendForecast = surfaceWind.getAerodromeSurfaceWindTrendForecast();
                String meanWindDirection = aerodromeSurfaceWindTrendForecast.getMeanWindDirection().getStringValue().split("\\.")[0];
                WindSpeedType windSpeedType = aerodromeSurfaceWindTrendForecast.getWindSpeed();
                String windSpeed = "";
                String windSection = "";
                if (windSpeedType != null) {
                    windSpeed = windSpeedType.getStringValue();

                    String windUnit = METARUtils.getWindUnit(aerodromeSurfaceWindTrendForecast.getWindSpeed().getUom());
                    WindConverter windConverter = new WindConverter(windSpeed, windUnit).invoke();
                    String windSpeedStr = windConverter.toString();
                    if (windSpeedStr.length() == 1) {
                        windSpeedStr = "0" + windSpeedStr;
                    }
                    GustSpeedType windGust = aerodromeSurfaceWindTrendForecast.getWindGustSpeed();
                    windSection = windSpeedStr;
                    if (windGust != null) {
                        windConverter = new WindConverter(windGust.getStringValue(), windUnit);
                        windSection += "G" + windConverter.toString();
                    }

                    tacResult.append(meanWindDirection + windSection + windUnit + SPACE);
                }
                NOSIG = false;
            }
            if (forecastLineBreaker == i) {
                i = 0;
                tacResult.append(NEW_LINE);
            }
            i++;

        }

        if (NOSIG) {
            tacResult.append("NOSIG" + SPACE);
        }

    }


    /**
     * The MSL pressure, reported to the nearest tenth of a millibar, will
     * always be the last field of the METAR report, prefixed with “SLP”. The
     * MSL pressure is reported in an abbreviated coded form.
     * If the coded MSL pressure value starts with a 9, 8 or 7, add the
     * number “9” to the beginning (i.e. 880 becomes 988.0).
     * If the coded MSL pressure value starts with a 0, 1, 2 or 3, add the
     * number “10” to the beginning (i.e. 134 becomes 1013.4).
     */
    private void setMeanSeaLevelPressure() {
        //TODO exists in us-iwxxm but not in iwxxm 1.0.RC1
    }

    /**
     * Other supplementary remarks of operational significance may be
     * included using standard meteorological abbreviations.
     */
    private void setSupplementaryRemarks() {

    }

    /**
     * Where observed, the cloud type and opacity for each reported cloud
     * layer will be included in remarks.
     */
    private void setRemarks() {

    }

    /**
     * Recent wind shear information below 1,600 feet AGL will be provided
     * when reported by an aircraft (usually on takeoff or landing).
     */
    private void setRecentWindShear() {
        AerodromeWindShearPropertyType windShear = getMeteorologicalAerodromeObservationRecord().getWindShear();
        if (windShear != null) {
            boolean setAllRunwaysAffected = windShear.getAerodromeWindShear().isSetAllRunwaysAffected();
            if (setAllRunwaysAffected) {
                tacResult.append("WS ALL RWY" + SPACE);
            } else {
                RunwayPropertyType[] affectedRunwayArray = windShear.getAerodromeWindShear().getAffectedRunwayArray();
                tacResult.append("WS" + SPACE);
                for (RunwayPropertyType runway : affectedRunwayArray) {
                    //TODO Parallel runways are distinguished by appending L, C or R to runway designator.
                    String designator = runway.getRunway().getDesignator();
                    tacResult.append("RWY" + designator + SPACE);
                }
            }
        }
    }

    /**
     * Recent weather since the last observation is reported, to include:
     * freezing precipitation; moderate or heavy rain, snow, blowing snow,
     * snow pellets, hail, or ice pellets; thunderstorm, sandstorm, or duststorm;
     * volcanic ash; funnel cloud, tornado, and water-sprout.
     */
    private void setRecentWeather() {
        AerodromeRecentWeatherType recentWeather = getMeteorologicalAerodromeObservationRecord().getRecentWeather();
        if (recentWeather != null) {
            tacResult.append("RE");
            String phenomena = recentWeather.getHref().substring(recentWeather.getHref().lastIndexOf("/") + 1);
            tacResult.append(phenomena + SPACE);
        }
        //TODO REFZRA Recent Weather - Freezing rain has been observed during the hour since the last report, but not at the time of the report.
    }

    /**
     * Altimeter setting (in U.S. reports) is always prefixed with an A
     * indicating inches of mercury; reported using four digits: tens,
     * units, tenths, and hundredths.
     * See Observing and Coding Pressure for additional information.
     */
    private void setAltimeter() {
        Double altimeterSetting = getMeteorologicalAerodromeObservationRecord().getQnh().getDoubleValue();
        String unit = getMeteorologicalAerodromeObservationRecord().getQnh().getUom();
        //TODO For A
        //TODO In addition, for those areas of the world that use Millibars, this will show more like Q1011 rather than A3000.
        // altimeterSetting = convertToInchesOfMercury(altimeterSetting, unit);
        // String altimeterSettingStr = convertAltimeterToStr(altimeterSetting);
        // tacResult.append("A" + altimeterSettingStr + SPACE);
        String altimeterSettingStr = String.valueOf(altimeterSetting.intValue());
        tacResult.append("Q" + altimeterSettingStr + SPACE);


    }

    private String convertAltimeterToStr(double altimeterSetting) {
        Double fractionalPart = altimeterSetting % 1;
        Double integralPart = altimeterSetting - fractionalPart;
        fractionalPart = CommonUtils.round(fractionalPart * 100, 0);
        return String.valueOf(integralPart.intValue()) + String.valueOf(fractionalPart.intValue());
    }


    /**
     * Temperature and dew point are reported to the nearest whole degree
     * Celsius. The letter “M” will precede negative values.
     */
    private void setTemperature() {
        AirTemperatureType airTemperature = getMeteorologicalAerodromeObservationRecord().getAirTemperature();
        Double airTemperatureVal = airTemperature.getDoubleValue();
        String airTemperatureStr = getTemperature(airTemperatureVal, airTemperature.getUom());
        tacResult.append(airTemperatureStr + "/");
        DewPointTemperatureType dewpointTemperature = getMeteorologicalAerodromeObservationRecord().getDewpointTemperature();
        Double dewpointTemperatureVal = dewpointTemperature.getDoubleValue();
        String dewpointTemperatureStr = getTemperature(dewpointTemperatureVal, dewpointTemperature.getUom());
        tacResult.append(dewpointTemperatureStr + SPACE);
    }

    private String getTemperature(Double airTemperatureVal, String tempUnit) {
        airTemperatureVal = CommonUtils.convertToCelsius(airTemperatureVal, tempUnit);
        String result = "";
        if (airTemperatureVal.intValue() < 0) {
            result += "M";
        }
        if ((airTemperatureVal < 10 && airTemperatureVal >= 0) ||
                airTemperatureVal < 0 && airTemperatureVal > -10) {
            result += "0";
        }
        Integer temperature = (Integer) airTemperatureVal.intValue();
        if (temperature < 0) temperature = temperature * -1;
        result += (temperature).toString();
        return result;
    }


    /**
     * Sky Condition - The cloud layer at 800 feet is broken, covering from
     * 5/8 to 7/8 of the observed sky. The next cloud layer at 4,000 feet,
     * combined with the lower cloud layer, is overcast covering 8/8 of the
     * sky, as observed from the ground.
     * Clouds are reported based on the summation amount of each cloud
     * layer as observed from the surface up.
     */
    private void setSkyCondition() {

        AerodromeObservedCloudsPropertyType cloud = getMeteorologicalAerodromeObservationRecord().getCloud();
        if (cloud != null) {
            CloudLayerPropertyType[] layerArray = cloud.getAerodromeObservedClouds().getLayerArray();
            METARUtils.parseCloudLayers(layerArray, tacResult);
        }
    }


    /**
     * Present weather is comprised of weather phenomenon (precipitation,
     * obscuration or others), which may be preceded by one or two
     * qualifiers (intensify or proximity to the station and descriptor).
     * The dominant weather phenomenon will be reported first.
     */
    private void setPresentWeathers() {

        String Obscuration = "";
        String Precipitation = "";
        String Thunderstorm = "";
        String lightPrecipitation = "";
        AerodromePresentWeatherType[] presentWeatherArray = getMeteorologicalAerodromeObservationRecord().getPresentWeatherArray();
        for (AerodromePresentWeatherType presentWeatherType : presentWeatherArray) {
            String title = presentWeatherType.getTitle();
            String phenomena = presentWeatherType.getHref().substring(presentWeatherType.getHref().lastIndexOf("/") + 1);
            if (title.toLowerCase().startsWith("precipitation")) {
                Precipitation += phenomena;
            } else if (title.equalsIgnoreCase("Mist")) {
                Obscuration += phenomena;
            } else if (title.equalsIgnoreCase("Thunderstorm")) {
                Thunderstorm += phenomena;
            } else if (title.equalsIgnoreCase("light precipitation")) {
                lightPrecipitation += phenomena;
            } else {
                tacResult.append(phenomena + SPACE);
            }

        }

        Precipitation = Precipitation.equals("") ? "" : Precipitation + SPACE;
        Obscuration = Obscuration.equals("") ? "" : Obscuration + SPACE;
        Thunderstorm = Thunderstorm.equals("") ? "" : Thunderstorm + SPACE;
        lightPrecipitation = lightPrecipitation.equals("") ? "" : lightPrecipitation + SPACE;

        String result = Precipitation + Obscuration + Thunderstorm + lightPrecipitation;
        result = result.equals("") ? "" : result + SPACE;
        tacResult.append(result);

    }

    /**
     * A 10-minute RVR evaluation value in hundreds of feet is
     * reported if prevailing visibility is < or = 1 mile or RVR < or
     * = 6000 feet; always appended with FT to indicate feet; value
     * prefixed with M or P to indicate value is lower or higher than
     * the reportable RVR value. See Observing and Coding Runway
     * Visual Range for additional information.
     */
    private void setRunwayVisualRange() {
        AerodromeRunwayVisualRangePropertyType[] rvrArray = getMeteorologicalAerodromeObservationRecord().getRvrArray();
        for (AerodromeRunwayVisualRangePropertyType rvr : rvrArray) {
            AerodromeRunwayVisualRangeType aerodromeRunwayVisualRange = rvr.getAerodromeRunwayVisualRange();
            String designator = aerodromeRunwayVisualRange.getRunway().getRunway().getDesignator();
            tacResult.append("R");
            tacResult.append(designator);
            tacResult.append("/");
            boolean setOneMinuteMeanMinimumRVR = aerodromeRunwayVisualRange.isSetOneMinuteMeanMinimumRVR();
            //TODO Prefixes P (plus) and M (minus) indicate the actual
            // value being greater or smaller than the maximum or minimum measurable value.
            if (!setOneMinuteMeanMinimumRVR) {
                Double meanRVR = aerodromeRunwayVisualRange.getMeanRVR().getDoubleValue();
                String meanRVRStr = CommonUtils.convertToFourDigit(meanRVR);
                tacResult.append(meanRVRStr);
            }
            if (setOneMinuteMeanMinimumRVR) {
                double doubleValue = aerodromeRunwayVisualRange.getOneMinuteMeanMaximumRVR().getDoubleValue();
                String visibilityString = CommonUtils.convertToFourDigit(doubleValue);
                tacResult.append(visibilityString);
            }
            boolean setOneMinuteMeanMaximumRVR = aerodromeRunwayVisualRange.isSetOneMinuteMeanMaximumRVR();
            if (setOneMinuteMeanMaximumRVR) {
                tacResult.append("v");
                double doubleValue = aerodromeRunwayVisualRange.getOneMinuteMeanMaximumRVR().getDoubleValue();
                String visibilityString = CommonUtils.convertToFourDigit(doubleValue);
                tacResult.append(visibilityString);
            }
            String tendency = aerodromeRunwayVisualRange.getPastTendency().toString();
            if (StringUtils.isEmpty(tendency)) {
                tacResult.append(tendency.toUpperCase().charAt(0));
            }
            tacResult.append(SPACE);
        }
    }

    /**
     * Prevailing visibility in statute miles and fractions with space
     * between whole miles and fractions; always appended with SM
     * to indicate statute miles; values <1/4SM reported as M1/4SM.
     * See Observing and Coding Visibility for additional
     * information.
     * <p/>
     * <p/>
     * VVVV: The minimum horizontal visibility in meters. If the visibility is better than 10 km, 9999 is used. 9999 means a minimum visibility of 50 m or less.
     * VxVxVxVx: The maximum horizontal visibility in meters.
     * Dv: In case of marked directional variation in visibility, the approximate direction of minimum and maximum visibility is given as one of eight compass points (N, SW, …)
     * Usually, only the minimum visibility is reported. If the minimum is less than 1500 m and the maximum is over 5000 m, the maximum visibility and its direction are indicated by a second visibility group following the minimum visibility.
     * In the United States, visibility is given in statutes miles and fractions. In that case, format VVVVVSM is used.
     */
    private void setHorizontalVisibility() {
        MeteorologicalAerodromeObservationRecordTypeImpl meteorologicalAerodromeObservationRecord = getMeteorologicalAerodromeObservationRecordType();
        AerodromeHorizontalVisibilityPropertyType visibility = meteorologicalAerodromeObservationRecord.getVisibility();
        if (visibility != null) {
            AerodromeHorizontalVisibilityType aerodromeHorizontalVisibility = visibility.getAerodromeHorizontalVisibility();
            boolean setMinimumVisibility = visibility.getAerodromeHorizontalVisibility().isSetMinimumVisibility();
            PrevailingVisibilityType prevailingVisibility = aerodromeHorizontalVisibility.getPrevailingVisibility();
            String prevailingVisibilityStr = "";
            String minimumVisibilityStr = "";
            if (setMinimumVisibility) {
                VisibilityAeroType minimumVisibility = aerodromeHorizontalVisibility.getMinimumVisibility();
                minimumVisibilityStr = CommonUtils.convertToFourDigit(minimumVisibility.getDoubleValue());
                tacResult.append(minimumVisibilityStr);
            }
            String direction = "";
            boolean setMinimumVisibilityDirection = aerodromeHorizontalVisibility.isSetMinimumVisibilityDirection();
            if (setMinimumVisibilityDirection) {
                AngleType minimumVisibilityDirection = aerodromeHorizontalVisibility.getMinimumVisibilityDirection();
                direction = minimumVisibilityDirection.toString();
                tacResult.append(direction);
            }
            if (setMinimumVisibility) {
                tacResult.append(SPACE);
            }

            if (prevailingVisibility != null) {
                Double prevailingVisibilityDouble = prevailingVisibility.getDoubleValue();
                prevailingVisibilityStr = CommonUtils.convertToFourDigit(prevailingVisibility.getDoubleValue());
                tacResult.append(prevailingVisibilityStr);
                if (setMinimumVisibilityDirection) {
                    tacResult.append(direction);
                }
                tacResult.append(SPACE);
            }

            //TODO ask mr.Davoodi
           /* Double prevailingVisibilityDouble = Double.valueOf(prevailingVisibility.getStringValue());
            Double prevailingVisibilityInMile = convertMeterToMile(prevailingVisibilityDouble);
            String visibilitySection = "";
            if (prevailingVisibilityInMile >= 15) {
                visibilitySection = "15SM ";
            }
            if (prevailingVisibilityInMile < 1 / 4) {

            }
            tacResult.append(prevailingVisibilityInMile.intValue() + "SM" + SPACE);*/

        }
    }


    /**
     * AUTO: Indicates a fully automated report with no human
     * intervention. It is removed when an observer logs on to the
     * system. COR: Indicates a corrected observation. No modifier
     * indicates human observer or automated system with human
     * logged on for oversight functions.
     * The letter CCA is used to indicate the ﬁrst correction, CCB for second, etc.
     * AUTO indicates the observation was taken by an AWOS or LWIS.
     */
    private void setReportModifier() {
        //TODO CCA & CCB & COR
        if (isMetar()) {
            if (metarDocument.getMeteorologicalAerodromeObservationReport().getAutomatedStation()) {
                tacResult.append("AUTO ");
            }
        } else {
            if (speciDocument.getMeteorologicalAerodromeObservationReport().getAutomatedStation()) {
                tacResult.append("AUTO ");
            }
        }
    }

    /**
     * All dates and times in UTC using a 24-hour clock; two-digit
     * date and four-digit time; always appended with Z to indicate
     * UTC.
     *
     * @param phenomenonTime
     */
    private void setPhenomenonTime(DateTime phenomenonTime) {

        tacResult.append(CommonUtils.convertToTwoDigit(phenomenonTime.getDayOfMonth()));
        tacResult.append(CommonUtils.convertToTwoDigit(phenomenonTime.getHourOfDay()));
        tacResult.append(CommonUtils.convertToTwoDigit(phenomenonTime.getMinuteOfHour()));
        tacResult.append("Z" + SPACE);
    }

    /**
     * METAR: hourly (scheduled) report; SPECI: special
     * (unscheduled) report.
     *
     * @return
     */
    private DateTime setDateAndType() {
        Date phenomenonTime;

        if (isMetar()) {
            phenomenonTime = ((XmlCalendar) ((TimeInstantTypeImpl) metarDocument.getMeteorologicalAerodromeObservationReport()
                    .getObservation().getOMObservation().getPhenomenonTime().getAbstractTimeObject()).
                    getTimePosition().getObjectValue()).getTime();
        } else {
            phenomenonTime = ((XmlCalendar) ((TimeInstantTypeImpl) speciDocument.getMeteorologicalAerodromeObservationReport()
                    .getObservation().getOMObservation().getPhenomenonTime().getAbstractTimeObject()).
                    getTimePosition().getObjectValue()).getTime();
        }
        DateTime dateTime = new DateTime(phenomenonTime, DateTimeZone.UTC);
        tacResult.append(phenomenonTime + NEW_LINE);
        tacResult.append(tacType + SPACE);
        return dateTime;
    }

    /**
     * Four character ICAO location identifier.
     */
    private void setMetarStationId() {
        String[] observationTitle;
        if (isMetar()) {
            observationTitle =
                    metarDocument.getMeteorologicalAerodromeObservationReport().getObservation().getOMObservation().getId().split("-");
        } else {
            observationTitle =
                    speciDocument.getMeteorologicalAerodromeObservationReport().getObservation().getOMObservation().getId().split("-");
        }
        tacResult.append(observationTitle[1].toUpperCase() + SPACE);
    }

    /**
     * Set wind direction and speed
     * Direction in tens of degrees from true north (first three digits);
     * next two digits: speed in whole knots; if needed, include
     * character as: Gusts (character) followed by maximum observed
     * speed; always appended with KT to indicate knots; 00000KT
     * for calm; if direction varies by 60o or more and speed greater
     * than 6 knots, a Variable wind direction group is reported,
     * otherwise omitted. If wind direction is variable and speed 6
     * knots or less, replace wind direction with VRB followed by
     * wind speed in knots. Observing and Coding Wind for
     * additional information.
     * <p>
     * An aggregation of surface wind conditions typically reported together at an aerodrome,
     * including wind direction information, wind speed, and wind gusts.
     * Wind direction is reported according to ICAO Annex 3 / WMO No. 49-2 Section 4.1.5.2b:
     * variations from the mean wind direction during the past 10 minutes is reported as follows, if the total variation is 60 or more:
     * <p>
     * 1) when the total variation is 60 or more and less than 180 and the wind speed is 1.5 m/s (3 kt) or more, such directional
     * variations are reported as the two extreme directions between which the surface wind has varied;
     * </p>
     * <p>
     * 2) when the total variation is 60 or more and less than 180 and the wind speed is less than 1.5 m/s (3 kt), the wind direction
     * is reported as variable with no mean wind direction;
     * </p>
     * <p>
     * 3) when the total variation is 180 or more, the wind direction is reported as variable with no mean wind direction"
     * </p>
     */
    private void setWindInformation() {
        AerodromeSurfaceWindPropertyType surfaceWind = getAerodromeSurfaceWind();
        String windSection = "";
        String meanWindDirection = "";
        Double meanWindDirectionDouble = surfaceWind.getAerodromeSurfaceWind().getMeanWindDirection().getDoubleValue();
        meanWindDirection = METARUtils.getMeanWindDirectionString(meanWindDirectionDouble);

        WindSpeedType windSpeedType = surfaceWind.getAerodromeSurfaceWind().getWindSpeed();

        String windSpeed = "";
        if (windSpeedType != null) {
            windSpeed = windSpeedType.getStringValue();
            String windUnit = METARUtils.getWindUnit(surfaceWind.getAerodromeSurfaceWind().getWindSpeed().getUom());
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
            boolean variableDirection = surfaceWind.getAerodromeSurfaceWind().getVariableDirection();
            windSection = windSpeedStr;
            if (windSpeedDouble.compareTo(WindConverter.MIN_WIND_SPEED_THRESHOLD) <= 0) {
                if (variableDirection) {
                    meanWindDirection = "VRB";
                }
                //TODO ask mr. davoodi
            } else {
                if (surfaceWind.getAerodromeSurfaceWind().isSetWindGust()) {
                    GustSpeedType windGust = surfaceWind.getAerodromeSurfaceWind().getWindGust();
                    windConverter = new WindConverter(windGust.getStringValue(), windUnit).invoke();
                    windSection += "G" + windConverter.toString();
                }
            }
            tacResult.append(meanWindDirection + windSection + windUnit + SPACE);

            boolean setExtremeClockwiseWindDirection = surfaceWind.getAerodromeSurfaceWind().isSetExtremeClockwiseWindDirection();
            if (setExtremeClockwiseWindDirection) {
                double extremeCounterClockwiseWindDirection = surfaceWind.getAerodromeSurfaceWind().getExtremeCounterClockwiseWindDirection().getDoubleValue();
                Double extremeClockwiseWindDirection = surfaceWind.getAerodromeSurfaceWind().getExtremeClockwiseWindDirection().getDoubleValue();
                String extremeClockwiseWindDirectionStr = METARUtils.getMeanWindDirectionString(extremeClockwiseWindDirection);
                String extremeCounterClockwiseWindDirectionStr = METARUtils.getMeanWindDirectionString(extremeCounterClockwiseWindDirection);
                tacResult.append(extremeClockwiseWindDirectionStr + "V" + extremeCounterClockwiseWindDirectionStr + SPACE);
            }
        } else {
            tacResult.append("00000KT" + SPACE);
        }
    }


    private AerodromeSurfaceWindPropertyType getAerodromeSurfaceWind() {
        MeteorologicalAerodromeObservationRecordTypeImpl meteorologicalAerodromeObservationRecord = getMeteorologicalAerodromeObservationRecordType();
        return meteorologicalAerodromeObservationRecord.getSurfaceWind();
    }

    private MeteorologicalAerodromeObservationRecordTypeImpl getMeteorologicalAerodromeObservationRecordType() {
        return getMeteorologicalAerodromeObservationRecord();
    }

    private MeteorologicalAerodromeObservationRecordTypeImpl getMeteorologicalAerodromeObservationRecord() {
        if (isMetar()) {
            return (MeteorologicalAerodromeObservationRecordTypeImpl) metarDocument.getMeteorologicalAerodromeObservationReport().getObservation().
                    getOMObservation().getResult().selectChildren(new QName(IWXXM_NAMESPACE, "MeteorologicalAerodromeObservationRecord"))[0];
        } else {
            return (MeteorologicalAerodromeObservationRecordTypeImpl) speciDocument.getMeteorologicalAerodromeObservationReport().getObservation().
                    getOMObservation().getResult().selectChildren(new QName(IWXXM_NAMESPACE, "MeteorologicalAerodromeObservationRecord"))[0];
        }
    }


    public String buildTAC() {
        return tacResult.toString();
    }

    public boolean isMetar() {
        return "METAR".equalsIgnoreCase(tacType);
    }
}
