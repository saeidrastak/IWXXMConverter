package com.weather.converter.util.tac;

import net.sf.jweather.metar.Metar;
import net.sf.jweather.metar.MetarParseException;
import net.sf.jweather.metar.MetarParser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by r.rastakfard on 12/23/2015.
 */
public class METARUtils {

    public static Metar parseTACMetar(String metarData) throws MetarParseException {
        return MetarParser.parse(metarData);
    }
    public static String convertDatetimeFormat(Date tar_date, String date_format){

        SimpleDateFormat sdf = new SimpleDateFormat( date_format );
        sdf.setTimeZone( TimeZone.getTimeZone("UTC") );

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
}
