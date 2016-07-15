/*

Sample program for the generation of metar-A3-1.xml

The following libraries are required:

AvXML-1.0RC1
============
AvXML-1.0RC1-XML-Java_binding.jar

XMLBeans
========
jsr173_1.0_api.jar
resolver.jar
saxon9.jar
saxon9-dom.jar
xbean.jar
xbean_xpath.jar
xmlbeans-qname.jar
xmlpublic.jar

*/


package com.weather.converter.util.sample;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import javax.xml.namespace.QName;

import com.weather.converter.util.tac.METARUtils;
import net.opengis.gml.x32.DirectPositionType;
import net.opengis.gml.x32.FeaturePropertyType;
import net.opengis.gml.x32.PointType;
import net.opengis.gml.x32.ReferenceType;
import net.opengis.gml.x32.TimeInstantPropertyType;
import net.opengis.gml.x32.TimeInstantType;
import net.opengis.gml.x32.TimePeriodType;
import net.opengis.gml.x32.TimePositionType;
import net.opengis.om.x20.OMObservationPropertyType;
import net.opengis.om.x20.OMObservationType;
import net.opengis.om.x20.OMProcessPropertyType;
import net.opengis.om.x20.TimeObjectPropertyType;
import net.opengis.samplingSpatial.x20.SFSpatialSamplingFeatureType;
import net.opengis.samplingSpatial.x20.ShapeType;

import net.sf.jweather.metar.Metar;
import net.sf.jweather.metar.MetarParseException;
import net.sf.jweather.metar.MetarParser;
import org.apache.xmlbeans.*;

import xint.icao.iwxxm.x10RC1.AerodromeHorizontalVisibilityPropertyType;
import xint.icao.iwxxm.x10RC1.AerodromeHorizontalVisibilityType;
import xint.icao.iwxxm.x10RC1.AerodromeObservedCloudsPropertyType;
import xint.icao.iwxxm.x10RC1.AerodromeObservedCloudsType;
import xint.icao.iwxxm.x10RC1.AerodromeSurfaceWindPropertyType;
import xint.icao.iwxxm.x10RC1.AerodromeSurfaceWindType;
import xint.icao.iwxxm.x10RC1.CloudLayerPropertyType;
import xint.icao.iwxxm.x10RC1.CloudLayerType;
import xint.icao.iwxxm.x10RC1.METARDocument;
import xint.icao.iwxxm.x10RC1.METARType;
import xint.icao.iwxxm.x10RC1.MeteorologicalAerodromeObservationRecordType;
import xint.icao.iwxxm.x10RC1.MeteorologicalAerodromeReportStatusType;
import xint.icao.iwxxm.x10RC1.MeteorologicalAerodromeTrendForecastRecordType;
import xint.wmo.data.def.metBasic.x10RC1.AerodromeForecastWeatherType;
import xint.wmo.data.def.metBasic.x10RC1.AerodromePresentWeatherType;
import xint.wmo.data.def.metBasic.x10RC1.AirTemperatureType;
import xint.wmo.data.def.metBasic.x10RC1.AltimeterSettingType;
import xint.wmo.data.def.metBasic.x10RC1.CloudAmountReportedAtAerodromeType;
import xint.wmo.data.def.metBasic.x10RC1.CloudBaseHeightType;
import xint.wmo.data.def.metBasic.x10RC1.DewPointTemperatureType;
import xint.wmo.data.def.metBasic.x10RC1.PrevailingVisibilityType;
import xint.wmo.data.def.metBasic.x10RC1.WindDirectionType;
import xint.wmo.data.def.metBasic.x10RC1.WindSpeedType;


/**
 * @author Keith To
 *
 */
public class METARExample {
	
//	public static String XML_FILE_PATH       = "target/metar-A3-1.xml";
	public static String XML_FILE_PATH       = "target/sigmet-A6-2-TC.xml";
	public static final String XML_SCHEMA_LOCATION = "http://schemas.wmo.int/iwxxm/1.0RC1 ./iwxxm.xsd http://schemas.wmo.int/metce/1.0RC1 ./metce.xsd";
	
	public static final String NS_GML       = "http://www.opengis.net/gml/3.2";
	public static final String NS_MET_BASIC = "http://data.wmo.int/def/met-basic/1.0RC1";
	public static final String NS_OPM       = "http://data.wmo.int/def/opm/1.0RC1";
	public static final String NS_METCE     = "http://data.wmo.int/def/metce/1.0RC1";
	public static final String NS_SAF       = "http://icao.int/saf/1.0RC1";
	public static final String NS_IWXXM     = "http://icao.int/iwxxm/1.0RC1";
	public static final String NS_XS     	= "http://www.w3.org/2001/XMLSchema";
	public static final String NS_SAMS     	= "http://www.opengis.net/samplingSpatial/2.0";
	public static final String NS_SAM     	= "http://www.opengis.net/sampling/2.0";
	
	private static XmlCursor cursor = null;
	
	private static String report_datetime = "";
	private static String feature_of_interest_id = "";
	
	
	/**
	 * The main function
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws MetarParseException {
		
		Date data_time = new Date();
		report_datetime = convert_datetime_format( data_time, "yyyy-MM-dd'T'HH:mm:ssZ" );
		create_xml_instance();

        String tacMetarData = "2004/01/06 02:50\n";
//        tacMetarData = tacMetarData + "KLAX 060250Z 34010KT 10SM CLR 14/M07 A3012 RMK AO2 SLP199 T01441072 55003\n";
        tacMetarData += "YUDO 221630Z 24004MPS 0600 R12/1000U DZ FG SCT010 OVC020 17/16 Q1018 BECMG TL1700\n" +
                "0800 FG BECMG AT1800 9999 NSW";

        Metar metar = METARUtils.parseTACMetar(tacMetarData);

        System.out.println();

		
	}
	
	
	
	private static String date_to_yyyymmddhhiiss( String input_date ) {
		
		String yyyy = input_date.substring( 0,  4);
		String mm 	= input_date.substring( 5,  7);
		String dd 	= input_date.substring( 8,  10);
		String hh 	= input_date.substring(11, 13);
		String ii 	= input_date.substring(14, 16);
		String ss 	= input_date.substring(17, 19);
		
		String yyyymmddhhiiss = yyyy + mm + dd + hh + ii + ss;
		
		return yyyymmddhhiiss;
	}
	
	
	
	private static String convert_datetime_format( Date tar_date, String date_format ){

	    SimpleDateFormat sdf = new SimpleDateFormat( date_format );
	    sdf.setTimeZone( TimeZone.getTimeZone( "UTC" ) );

	    String formated_date = sdf.format( tar_date );
	    
	    ////////////////////////////////////////////////////////// 
	    // Before: 2012-07-23T02:33:09+0000
	    // After:  2012-07-23T02:33:09+00:00
	    char last_char = date_format.charAt( date_format.length() - 1 );
	    
	    if( last_char == 'Z' )
	    {
	      formated_date = formated_date.substring( 0, formated_date.length() - 2 ) + ':' + formated_date.substring( formated_date.length() - 2, formated_date.length() );
	    }
	    ////////////////////////////////////////////////////////// 

	    return formated_date;
	}
	
	
	
	
	private static void create_xml_instance()
	{

	    //================================================================
	    // METAR
	    METARDocument metar_doc = METARDocument.Factory.newInstance();

        try {
            METARDocument parse = METARDocument.Factory.parse(new FileInputStream(XML_FILE_PATH));
            System.out.println();
        } catch (XmlException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        METARType metar_type = metar_doc.addNewMETAR();
	    
	    metar_type.setId("Metar_001");


	    //================================================================
    	// MeteorologicalAerodromeReportStatus
    	metar_type.setStatus( MeteorologicalAerodromeReportStatusType.NORMAL );
    	metar_type.setAutomatedStation(false);
    	
	    
	    //================================================================
    	// observation
	    OMObservationPropertyType Observation = metar_type.addNewObservation();
	    
	    OMObservationType MeteorologicalAerodromeObservation = Observation.addNewOMObservation();
		
	    MeteorologicalAerodromeObservation.setId( "obs-yudo-20120822163000" );
	    
	    ReferenceType meteorologicalAerodromeObservationType = MeteorologicalAerodromeObservation.addNewType();
	    
	    meteorologicalAerodromeObservationType.setHref("http://data.wmo.int/def/49/MeteorologicalAerodromeObservation");
	    meteorologicalAerodromeObservationType.setTitle("Aerodrome Observation");

        //================================================================
        // phenomenonTime
        TimeObjectPropertyType phenomenonTime = MeteorologicalAerodromeObservation.addNewPhenomenonTime();
        TimeInstantType phenomenonTimeInstant = TimeInstantType.Factory.newInstance();
        String timeInstant_id = "time-instant-" + date_to_yyyymmddhhiiss( "2012-08-22T16:30:00+00:00" );

        phenomenonTimeInstant.setFrame(phenomenonTimeInstant.getFrame());
        TimePositionType phenomenonTimePosition = phenomenonTimeInstant.addNewTimePosition();

        phenomenonTimePosition.setFrame(phenomenonTimePosition.getFrame());
        phenomenonTimePosition.setStringValue( "2012-08-22T16:30:00+00:00" );

        cursor = phenomenonTime.newCursor();
        cursor.toNextToken();
        cursor.beginElement("TimeInstant", NS_GML);
        cursor.insertAttributeWithValue(new QName(NS_GML, "id"), timeInstant_id);

        XmlCursor placedCursor2 = phenomenonTimeInstant.newCursor();
        placedCursor2.copyXmlContents(cursor);   	    	
    	        
    	        
              
        //================================================================
        // resultTime
        TimeInstantPropertyType resultTime = MeteorologicalAerodromeObservation.addNewResultTime();
        TimeInstantType resultTimeInstant = resultTime.addNewTimeInstant();
        timeInstant_id = "time-instant-" + date_to_yyyymmddhhiiss( report_datetime );
        resultTimeInstant.setId( timeInstant_id );

        resultTimeInstant.setFrame(resultTimeInstant.getFrame());
        TimePositionType resultTimePosition = resultTimeInstant.addNewTimePosition();

        resultTimePosition.setFrame(resultTimePosition.getFrame());
        resultTimePosition.setStringValue( report_datetime );
  
  
        //================================================================
        //  procedure
        OMProcessPropertyType procedure = MeteorologicalAerodromeObservation.addNewProcedure();
        procedure.setHref("http://data.wmo.int/def/process/AutomatedMETARObservation");
        procedure.setTitle("Automated METAR");
    	    
        
        //================================================================
        //  observedProperty
        ReferenceType observedProperty = MeteorologicalAerodromeObservation.addNewObservedProperty();
        observedProperty.setHref("http://icao.int/property/AerodromeWeather-obs");
        observedProperty.setTitle("METAR observation properties");
        
        
        //================================================================
        //  result
        XmlObject result2 = MeteorologicalAerodromeObservation.addNewResult();

        cursor = result2.newCursor();
        cursor.toNextToken();
        cursor.beginElement("MeteorologicalAerodromeObservationRecord", NS_IWXXM);
        cursor.insertAttributeWithValue(new QName(NS_GML, "id"), "ObservationRecord_001");
        cursor.insertAttributeWithValue(new QName("", "ceilingAndVisibilityOK"), "false");

        MeteorologicalAerodromeObservationRecordType MeteorologicalAerodromeObservationRecord = MeteorologicalAerodromeObservationRecordType.Factory.newInstance();

          //================================================================
          //   airTemperature
          AirTemperatureType airTemperature = MeteorologicalAerodromeObservationRecord.addNewAirTemperature();
          airTemperature.setDoubleValue( 17 );
          airTemperature.setUom("http://opengis.net/def/uom/UCUM/0/C");


          //================================================================
          // dewpointTemperature
          DewPointTemperatureType dewpointTemperature = MeteorologicalAerodromeObservationRecord.addNewDewpointTemperature();
          dewpointTemperature.setDoubleValue( 16 );
          dewpointTemperature.setUom("http://opengis.net/def/uom/UCUM/0/C");

          
          //================================================================
          // qnh
          AltimeterSettingType qnh = MeteorologicalAerodromeObservationRecord.addNewQnh();
          qnh.setDoubleValue( 1018 );
          qnh.setUom("http://opengis.net/def/uom/UCUM/0/hPa");



          //================================================================
          // surfaceWind
          AerodromeSurfaceWindPropertyType surfaceWind = MeteorologicalAerodromeObservationRecord.addNewSurfaceWind();
          AerodromeSurfaceWindType AerodromeSurfaceWind = surfaceWind.addNewAerodromeSurfaceWind();

          AerodromeSurfaceWind.setVariableDirection(false);
          
          WindSpeedType windSpeed = AerodromeSurfaceWind.addNewWindSpeed();
          windSpeed.setDoubleValue( 4 );
          windSpeed.setUom( "http://opengis.net/def/uom/UCUM/0/m/s" );

          WindDirectionType meanWindDirection = AerodromeSurfaceWind.addNewMeanWindDirection();
          meanWindDirection.setDoubleValue( 240 );
          meanWindDirection.setUom( "http://opengis.net/def/uom/UCUM/0/deg" );



          //================================================================
          // visibility
          AerodromeHorizontalVisibilityPropertyType visibility = MeteorologicalAerodromeObservationRecord.addNewVisibility();
          
          AerodromeHorizontalVisibilityType AerodromeHorizontalVisibility = visibility.addNewAerodromeHorizontalVisibility();
          PrevailingVisibilityType prevailingVisibility = AerodromeHorizontalVisibility.addNewPrevailingVisibility();

          prevailingVisibility.setDoubleValue( 600 );
          prevailingVisibility.setUom( "http://opengis.net/def/uom/UCUM/0/m" );


          
          //================================================================
          // presentWeather
          AerodromePresentWeatherType presentWeather = MeteorologicalAerodromeObservationRecord.addNewPresentWeather();
          XmlCursor presentWeatherCursor = presentWeather.type.getAnnotation().getApplicationInformation()[0].newCursor();
          presentWeatherCursor.toChild(NS_XS, "vocabulary");
          String presentWeatherHref  = presentWeatherCursor.getTextValue();

          presentWeather.setHref(presentWeatherHref + "/" + "DZ");
          presentWeather.setTitle("Drizzle");


          //================================================================
          // presentWeather
          AerodromePresentWeatherType presentWeather2 = MeteorologicalAerodromeObservationRecord.addNewPresentWeather();
          presentWeatherCursor = presentWeather2.type.getAnnotation().getApplicationInformation()[0].newCursor();
          presentWeatherCursor.toChild(NS_XS, "vocabulary");
          presentWeatherHref  = presentWeatherCursor.getTextValue();

          presentWeather2.setHref(presentWeatherHref + "/" + "FG");
          presentWeather2.setTitle("Fog");



          //================================================================
          // cloud
          AerodromeObservedCloudsPropertyType cloud = MeteorologicalAerodromeObservationRecord.addNewCloud();
          AerodromeObservedCloudsType AerodromeObservedClouds = cloud.addNewAerodromeObservedClouds();

            //================================================================
            // layer
            CloudLayerPropertyType layer = AerodromeObservedClouds.addNewLayer();
            CloudLayerType CloudLayer = layer.addNewCloudLayer();

            // amount
            CloudAmountReportedAtAerodromeType cloudAmount = CloudLayer.addNewAmount();
            XmlCursor cloudAmountCursor = cloudAmount.type.getAnnotation().getApplicationInformation()[0].newCursor();
            cloudAmountCursor.toChild(NS_XS, "vocabulary");
            String cloudAmountHref  = cloudAmountCursor.getTextValue();
            cloudAmount.setHref(cloudAmountHref + "/" + "2");
            cloudAmount.setTitle("Scattered");					    	                

            // base
            CloudBaseHeightType base = CloudLayer.addNewBase();
            base.setDoubleValue(1000);
            base.setUom("http://opengis.net/def/uom/UCUM/0/ft");

            
            //================================================================
            // layer
            layer = AerodromeObservedClouds.addNewLayer();
            CloudLayer = layer.addNewCloudLayer();

            // amount
            cloudAmount = CloudLayer.addNewAmount();
            cloudAmountCursor = cloudAmount.type.getAnnotation().getApplicationInformation()[0].newCursor();
            cloudAmountCursor.toChild(NS_XS, "vocabulary");
            cloudAmountHref  = cloudAmountCursor.getTextValue();
            cloudAmount.setHref(cloudAmountHref + "/" + "4");
            cloudAmount.setTitle("Overcast");					    	                

            // base
            base = CloudLayer.addNewBase();
            base.setDoubleValue(2000);
            base.setUom("http://opengis.net/def/uom/UCUM/0/ft");


        XmlCursor polygonCursor = MeteorologicalAerodromeObservationRecord.newCursor();
        polygonCursor.copyXmlContents(cursor);

        
        //================================================================
        // featureOfInterest3
        FeaturePropertyType featureOfInterest3 = MeteorologicalAerodromeObservation.addNewFeatureOfInterest();
        featureOfInterest3 = fill_feature_of_interest(featureOfInterest3);
	    
    	
    	//================================================================
    	// MeteorologicalAerodromeTrendForecast
	    OMObservationPropertyType trend_forecast = metar_type.addNewTrendForecast();
	    
	    OMObservationType MeteorologicalAerodromeTrendForecast = trend_forecast.addNewOMObservation();
	    
	    MeteorologicalAerodromeTrendForecast.setId("trend-fcst-1-yudo");
	    
	    ReferenceType type = MeteorologicalAerodromeTrendForecast.addNewType();
	    
	    type.setHref("http://data.wmo.int/def/49/observationType/MeteorologicalAerodromeTrendForecast");
	    type.setTitle("Aerodrome Trend Forecast");


        //================================================================
        // phenomenonTime
        TimeObjectPropertyType fcst_phenomenonTime = MeteorologicalAerodromeTrendForecast.addNewPhenomenonTime();
        TimePeriodType phenomenonTimePeriodInstant = TimePeriodType.Factory.newInstance();

        TimePositionType BeginPosition = phenomenonTimePeriodInstant.addNewBeginPosition();
        BeginPosition.setStringValue("2012-08-22T16:30:00+00:00");

        TimePositionType EndPosition = phenomenonTimePeriodInstant.addNewEndPosition();
        EndPosition.setStringValue("2012-08-22T17:00:00+00:00");

        XmlCursor cursor2 = fcst_phenomenonTime.newCursor();
        cursor2.toNextToken();
        cursor2.beginElement("TimePeriod", NS_GML);
        cursor2.insertAttributeWithValue(new QName(NS_GML, "id"), "time-period-20120822163000-20120822170000");

        XmlCursor placedCursor3 = phenomenonTimePeriodInstant.newCursor();
        placedCursor3.copyXmlContents(cursor2);


        //================================================================
        // resultTime
        TimeInstantPropertyType fcst_resultTime = MeteorologicalAerodromeTrendForecast.addNewResultTime();

        TimeInstantType fcst_resultTimeInstant = fcst_resultTime.addNewTimeInstant();
        timeInstant_id = "fcst-time-instant1-" + date_to_yyyymmddhhiiss( report_datetime );
        fcst_resultTimeInstant.setId( timeInstant_id );

        fcst_resultTimeInstant.setFrame(fcst_resultTimeInstant.getFrame());
        TimePositionType fcst_resultTimePosition = fcst_resultTimeInstant.addNewTimePosition();

        fcst_resultTimePosition.setFrame(fcst_resultTimePosition.getFrame());
        fcst_resultTimePosition.setStringValue( report_datetime );


        //================================================================
        // procedure
        OMProcessPropertyType fcst_procedure = MeteorologicalAerodromeTrendForecast.addNewProcedure();
        fcst_procedure.setHref("http://data.wmo.int/def/process/HumanGeneratedTrendForecast");
        fcst_procedure.setTitle("Human-generated trend forecast");


        //================================================================
        // observedProperty
        ReferenceType fcst_observedProperty = MeteorologicalAerodromeTrendForecast.addNewObservedProperty();
        fcst_observedProperty.setHref("http://icao.int/property/AerodromeWeather-trend-fcst");
        fcst_observedProperty.setTitle("METAR trend forecast properties");


        //================================================================
        // result
        XmlObject trend_forecast_result2 = MeteorologicalAerodromeTrendForecast.addNewResult();

        cursor = trend_forecast_result2.newCursor();
        cursor.toNextToken();
        cursor.beginElement("MeteorologicalAerodromeTrendForecastRecord", NS_IWXXM);
        cursor.insertAttributeWithValue(new QName(NS_GML, "id"), "trend-fcst-record1-YUDO");

        MeteorologicalAerodromeTrendForecastRecordType MeteorologicalAerodromeTrendForecastRecord = MeteorologicalAerodromeTrendForecastRecordType.Factory.newInstance();

        cursor.insertAttributeWithValue(new QName("", "ceilingAndVisibilityOK"), "false");
        cursor.insertAttributeWithValue(new QName("", "changeIndicator"), "BECOMING");

          //================================================================
          // visibility
          PrevailingVisibilityType trend_forecast_visibility = MeteorologicalAerodromeTrendForecastRecord.addNewPrevailingHorizontalVisibility();
          trend_forecast_visibility.setDoubleValue(800);
          trend_forecast_visibility.setUom("http://opengis.net/def/uom/UCUM/0/m");

          //================================================================
          // weather
          AerodromeForecastWeatherType trend_forecast_presentWeather = MeteorologicalAerodromeTrendForecastRecord.addNewForecastWeather();
          XmlCursor forecastWeatherCursor = trend_forecast_presentWeather.type.getAnnotation().getApplicationInformation()[0].newCursor();
          forecastWeatherCursor.toChild(NS_XS, "vocabulary");
          String forecastWeatherHref  = forecastWeatherCursor.getTextValue();

          trend_forecast_presentWeather.setHref(forecastWeatherHref + "/" + "FG");
          trend_forecast_presentWeather.setTitle("Fog");


          
        polygonCursor = MeteorologicalAerodromeTrendForecastRecord.newCursor();
        polygonCursor.copyXmlContents(cursor);

        //================================================================
        // featureOfInterest3
        featureOfInterest3 = MeteorologicalAerodromeTrendForecast.addNewFeatureOfInterest();
        featureOfInterest3 = fill_feature_of_interest(featureOfInterest3);
	    

    	//================================================================
    	// MeteorologicalAerodromeTrendForecast
	    trend_forecast = metar_type.addNewTrendForecast();
	   	    
	    MeteorologicalAerodromeTrendForecast = trend_forecast.addNewOMObservation();
	    
	    MeteorologicalAerodromeTrendForecast.setId("trend-fcst-2-yudo");
	    
	    type = MeteorologicalAerodromeTrendForecast.addNewType();
	    
	    type.setHref("http://data.wmo.int/def/49/observationType/MeteorologicalAerodromeTrendForecast");
	    type.setTitle("Aerodrome Trend Forecast");
	    
	    
        //================================================================
        // phenomenonTime
        fcst_phenomenonTime = MeteorologicalAerodromeTrendForecast.addNewPhenomenonTime();
        phenomenonTimePeriodInstant = TimePeriodType.Factory.newInstance();

        BeginPosition = phenomenonTimePeriodInstant.addNewBeginPosition();
        BeginPosition.setStringValue("2012-08-22T18:00:00+00:00");

        EndPosition = phenomenonTimePeriodInstant.addNewEndPosition();
        EndPosition.setStringValue("2012-08-22T19:00:00+00:00");

        cursor2 = fcst_phenomenonTime.newCursor();
        cursor2.toNextToken();
        cursor2.beginElement("TimePeriod", NS_GML);
        cursor2.insertAttributeWithValue(new QName(NS_GML, "id"), "time-period-20120822180000-20120822190000");

        placedCursor3 = phenomenonTimePeriodInstant.newCursor();
        placedCursor3.copyXmlContents(cursor2);

        
        //================================================================
        // resultTime
        fcst_resultTime = MeteorologicalAerodromeTrendForecast.addNewResultTime();

        fcst_resultTimeInstant = fcst_resultTime.addNewTimeInstant();
        timeInstant_id = "fcst-time-instant2-" + date_to_yyyymmddhhiiss( report_datetime );
        fcst_resultTimeInstant.setId( timeInstant_id );

        fcst_resultTimeInstant.setFrame(fcst_resultTimeInstant.getFrame());
        fcst_resultTimePosition = fcst_resultTimeInstant.addNewTimePosition();

        fcst_resultTimePosition.setFrame(fcst_resultTimePosition.getFrame());
        fcst_resultTimePosition.setStringValue( report_datetime );


        //================================================================
        // procedure
        fcst_procedure = MeteorologicalAerodromeTrendForecast.addNewProcedure();
        fcst_procedure.setHref("http://data.wmo.int/def/process/HumanGeneratedTrendForecast");
        fcst_procedure.setTitle("Human-generated trend forecast");


        //================================================================
        // observedProperty
        fcst_observedProperty = MeteorologicalAerodromeTrendForecast.addNewObservedProperty();
        fcst_observedProperty.setHref("http://icao.int/property/AerodromeWeather-trend-fcst");
        fcst_observedProperty.setTitle("METAR trend forecast properties");


        //================================================================
        // result
        trend_forecast_result2 = MeteorologicalAerodromeTrendForecast.addNewResult();

        cursor = trend_forecast_result2.newCursor();
        cursor.toNextToken();
        cursor.beginElement("MeteorologicalAerodromeTrendForecastRecord", NS_IWXXM);
        cursor.insertAttributeWithValue(new QName(NS_GML, "id"), "trend-fcst-record2-YUDO");

        MeteorologicalAerodromeTrendForecastRecord = MeteorologicalAerodromeTrendForecastRecordType.Factory.newInstance();

        cursor.insertAttributeWithValue(new QName("", "ceilingAndVisibilityOK"), "false");
        cursor.insertAttributeWithValue(new QName("", "changeIndicator"), "BECOMING");


          //================================================================
          // visibility
          trend_forecast_visibility = MeteorologicalAerodromeTrendForecastRecord.addNewPrevailingHorizontalVisibility();
          trend_forecast_visibility.setDoubleValue(10);
          trend_forecast_visibility.setUom("http://opengis.net/def/uom/UCUM/0/km");


          //================================================================
          // weather
          trend_forecast_presentWeather = MeteorologicalAerodromeTrendForecastRecord.addNewForecastWeather();
          trend_forecast_presentWeather.setNilReason("http://data.wmo.int/def/nil-reason/nothingOfOperationalSignificance");


        polygonCursor = MeteorologicalAerodromeTrendForecastRecord.newCursor();
        polygonCursor.copyXmlContents(cursor);

        //================================================================
        // featureOfInterest3
        featureOfInterest3 = MeteorologicalAerodromeTrendForecast.addNewFeatureOfInterest();
        featureOfInterest3 = fill_feature_of_interest(featureOfInterest3);
	    
	    
      add_schema_location(metar_doc, XML_SCHEMA_LOCATION);
      validate_xml_instance( metar_doc );      
      write_xml_file(metar_doc, XML_FILE_PATH);
		
	}
	
	
	public static void write_xml_file( XmlObject doc_object, String target_file_path ){
		
		File xml_file = new File( target_file_path );		

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
		
		
		XmlOptions xml_options = new XmlOptions();
		xml_options.setSavePrettyPrint();
		xml_options.setSaveSuggestedPrefixes(NamespaceMap);
		xml_options.setSaveAggressiveNamespaces();
		xml_options.setUseDefaultNamespace();
		
		
		try {
			doc_object.save(xml_file, xml_options);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static boolean validate_xml_instance( XmlObject xml_doc_object ){
		boolean is_xml_valid = false;

        // A collection instance to hold validation error messages.
        ArrayList validation_messages = new ArrayList();

        // Validate the XML, collecting messages.
        is_xml_valid = xml_doc_object.validate(
                new XmlOptions().setErrorListener(validation_messages));

        // If the XML isn't valid, print the messages.
        if (!is_xml_valid)
        {
            System.out.println("\nInvalid XML: ");
            for (int i = 0; i < validation_messages.size(); i++)
            {
                XmlError error = (XmlError) validation_messages.get(i);
                System.out.println(error.getMessage());
                System.out.println(error.getObjectLocation());
            }
        }
        return is_xml_valid;
	}
	
	
	/**
	 * add schema location to the xml doc object
	 * 
	 * @param xml_doc_object     the xml doc object
	 * @param schema_location    the schema location
	 */
	public static void add_schema_location( XmlObject xml_doc_object, String schema_location ){
		
		XmlCursor cursor = xml_doc_object.newCursor();
		
		if(cursor.toFirstChild()){
			cursor.setAttributeText(new QName("http://www.w3.org/2001/XMLSchema-instance", "schemaLocation"), schema_location);
		}
		
	}
	
	
	private static FeaturePropertyType fill_feature_of_interest( FeaturePropertyType featureOfInterest3 )
	{
		if( feature_of_interest_id.compareTo("") == 0 )
		{
	    	SFSpatialSamplingFeatureType SFSpatialSamplingFeature = SFSpatialSamplingFeatureType.Factory.newInstance();
	    	
	    	feature_of_interest_id = "SFSpatialSamplingFeature_001";
	    	
	        SFSpatialSamplingFeature.setId(feature_of_interest_id);
	        ReferenceType SFSpatialSamplingFeatureType = SFSpatialSamplingFeature.addNewType();
	        SFSpatialSamplingFeatureType.setHref("http://www.opengis.net/def/samplingFeatureType/OGC-OM/2.0/SF_SamplingPoint");
	        SFSpatialSamplingFeatureType.setTitle("SF_SamplingPoint");
	
	        FeaturePropertyType sampledFeature = SFSpatialSamplingFeature.addNewSampledFeature();
	        sampledFeature.setHref("http://icao.int/id/aerodrome/YUDO");
	        sampledFeature.setTitle("Fake Airport");
	
	        ShapeType shape = SFSpatialSamplingFeature.addNewShape();
	
	        PointType point = PointType.Factory.newInstance();
	        point.setId("Point_001");
	        DirectPositionType pos = point.addNewPos();
	
	        pos.setStringValue("0.00 0.00");
	
	        List<String> latlon = Arrays.asList("Lat", "Lon");
	        pos.setAxisLabels(latlon);
	        pos.setSrsName("http://www.opengis.net/def/crs/EPSG/0/4326");
	        pos.setSrsDimension(new BigInteger("2"));
	
	
	
	        XmlCursor cursor2 = shape.newCursor();
	        cursor2.toNextToken();
	        cursor2.beginElement("Point", NS_GML);
	        cursor2.insertAttributeWithValue(new QName(NS_GML, "id"), "Point_001");
	
	
	        XmlCursor placedCursor2 = point.newCursor();
	        placedCursor2.getObject().xmlText();
	        placedCursor2.copyXmlContents(cursor2);
	        cursor2.dispose();
	        placedCursor2.dispose();
	
	        XmlCursor cursor = featureOfInterest3.newCursor();
	        cursor.toNextToken();
	        cursor.beginElement("SF_SpatialSamplingFeature", NS_SAMS);
	        cursor.insertAttributeWithValue(new QName(NS_GML, "id"), feature_of_interest_id);
	
	        XmlCursor polygonCursor = SFSpatialSamplingFeature.newCursor();
	        polygonCursor.copyXmlContents(cursor);
	
	        cursor.dispose();
	        polygonCursor.dispose();
		}
		else
		{
			featureOfInterest3.setHref( "#" + feature_of_interest_id );
		}
		
		return featureOfInterest3;
	}
	
	
}
