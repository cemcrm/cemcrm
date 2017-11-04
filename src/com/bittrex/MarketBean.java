package com.bittrex;

import org.json.JSONException;
import org.json.JSONObject;

public class MarketBean {

    private static final String NAME = "MarketName";
    private static final String BASE_VOLUME = "BaseVolume";
    private static final String LAST_PRICE = "Last";
    private static final String ASK = "Ask";
    private static final String BID = "Bid";
    private static final String HIGH = "High";
    private static final String LOW = "Low";

    private String name;
    private double baseVolume;
    private double last;
    private double ask;
    private double bid;
    private double high;
    private double low;
    private Boolean hasPotential;
    private Integer upTrendCycleCount;

    public void fillMarketObject(JSONObject jsonObject) throws JSONException {
        this.name = jsonObject.getString(NAME);
        this.baseVolume = jsonObject.getLong(BASE_VOLUME);
        this.last = jsonObject.getDouble(LAST_PRICE);
        this.ask = jsonObject.getDouble(ASK);
        this.bid = jsonObject.getDouble(BID);
        this.high = jsonObject.getDouble(HIGH);
        this.low = jsonObject.getDouble(LOW);
    }

    public Boolean getHasPotential() {
        return hasPotential;
    }

    public void setHasPotential(Boolean hasPotential) {
        this.hasPotential = hasPotential;
    }

    public Integer getUpTrendCycleCount() {
        return upTrendCycleCount;
    }

    public void setUpTrendCycleCount(Integer upTrendCycleCount) {
        this.upTrendCycleCount = upTrendCycleCount;
    }

    public double getBid() {
        return bid;
    }

    public void setBid(double bid) {
        this.bid = bid;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public double getBaseVolume() {
        return baseVolume;
    }

    public void setBaseVolume(double baseVolume) {
        this.baseVolume = baseVolume;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLast() {
        return last;
    }

    public void setLast(double last) {
        this.last = last;
    }

    public double getAsk() {
        return ask;
    }

    public void setAsk(double ask) {
        this.ask = ask;
    }


}
