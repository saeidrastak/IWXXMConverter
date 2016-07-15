package com.weather.converter.util.iwxxm;

/**
 * Created by Rastakfard (Saeid.Rastak@Gmail.com) on 2/5/2016.
 */
public class WindConverter {

    public static final Double MIN_WIND_SPEED_THRESHOLD = 3d;

    private String windSpeed;
    private String windUnit;
    private Double doubleResult;
    private String stringResult;

    public WindConverter(String windSpeed, String windUnit) {
        this.windSpeed = windSpeed;
        this.windUnit = windUnit;
    }

    public Double toDouble() {
        return doubleResult;
    }

    public String toString() {
        return stringResult;
    }

    public WindConverter invoke() {
        doubleResult = CommonUtils.convertToString(windSpeed, windUnit);
        stringResult = String.valueOf(doubleResult.intValue());
        return this;
    }
}