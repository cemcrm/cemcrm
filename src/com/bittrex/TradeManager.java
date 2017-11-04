package com.bittrex;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TradeManager {
    static StringData bittrexData = new StringData();
    static String address;
    static double balance;
    static double lastPrice = 0;
    static Boolean isTradeOngoing = false;
    static String uuid;
    static MarketBean currentMarket;
    static Boolean isOrderFilled = false;
    static Boolean isOrderSold = false;
    static BigDecimal buyPrice;
    static int lossCount = 0;
    static double tradeQuantity = 0;

    static double lastId = 0;
    static double currentId;

    public void detectPump(MarketBean market) throws JSONException {
        String marketHistoryString;
        marketHistoryString = bittrexData.getMarketHistory(market.getName());
        JSONObject jsonObject = new JSONObject(marketHistoryString);
        if(jsonObject.getString("success").equals("true")) {
            JSONArray jsonArray = (JSONArray) jsonObject.get("result");
            analyseMarketTransactions(jsonArray,market);
        }
    }

    public List<MarketBean> getActiveMarkets() throws JSONException {
        String marketString;
        List<MarketBean> activeMarkets = new ArrayList<>();
        marketString = bittrexData.getMarkets();
        JSONObject jsonObject = new JSONObject(marketString);
        if(jsonObject.getString("success").equals("true")) {
            JSONArray jsonArray = (JSONArray) jsonObject.get("result");
            for(int i=0;i<jsonArray.length();i++){
                JSONObject jsonObject1 = jsonArray.getJSONObject(i);

                if((jsonObject1.getString("IsActive").equals("true") && jsonObject1.getString("BaseCurrency").equals("BTC"))) {
                    MarketBean marketBean = new MarketBean();
                    JSONObject jsonObject2 = new JSONObject(bittrexData.getMarketSummary(jsonObject1.getString("MarketName")));
                    marketBean.fillMarketObject(jsonObject2.getJSONArray("result").getJSONObject(0));
                    activeMarkets.add(marketBean);
                }
            }

        }
        return activeMarkets;
    }

    public void analyseMarketTransactions(JSONArray jsonArray, MarketBean market) throws JSONException {
        System.out.println("Analysing "+ market.getName());
        int numberOfBuys = 0;
        int increaseNumber = 0;
        double firstPrice = 0L;
        double currentPrice = 0L;
        double topPrice = 0L;
        double totalBought = 0L;
        if(!isMarketActive(jsonArray.getJSONObject(0), jsonArray.getJSONObject(10)))
            return;
        for(int i=15;i>0;i--){
            JSONObject jsonObject1 = jsonArray.getJSONObject(i);
            if(jsonObject1.getString("OrderType").equals("BUY")) {
                numberOfBuys++;
                currentPrice = jsonObject1.getDouble("Price");
                totalBought += jsonObject1.getDouble("Total");
                if(firstPrice == 0){
                    firstPrice = currentPrice;
                    topPrice = currentPrice;
                }
                if(currentPrice>topPrice) {
                    topPrice = currentPrice;
                    increaseNumber++;
                }
            }
        }
       // if(numberOfBuys>=9 && increaseNumber>3 && currentPrice>(firstPrice*101)/100 && totalBought>0.5)
        if(numberOfBuys>=13 && increaseNumber>3 && currentPrice>firstPrice && totalBought>1.0)
            startTrade(market);
    }

    public void startTrade(MarketBean market) throws JSONException {
        System.out.println("Starting trade for "+ market);

        String tickerString;
        String buyorderString;

        tickerString = bittrexData.getTicker(market.getName());
        JSONObject jsonObject = new JSONObject(tickerString);
        BigDecimal marketAsk = new BigDecimal(jsonObject.getJSONObject("result").getString("Ask"));
        tradeQuantity = (balance*6/100)/marketAsk.doubleValue();

        System.out.println("Create buy order "+ market + ", " +tradeQuantity+ "," +marketAsk);

        buyorderString = bittrexData.createBuyOrder(market.getName(),tradeQuantity,marketAsk);
        JSONObject jsonObject1 = new JSONObject(buyorderString);

        if (jsonObject1.getString("success").equals("true")){
            isTradeOngoing = true;
            uuid = jsonObject1.getJSONObject("result").getString("uuid");
            currentMarket = market;
            buyPrice = marketAsk;
        }
    }

    public void getCoinBalance(String marketName) throws JSONException {
        String balanceString;
        balanceString = bittrexData.getBalance(marketName);
        JSONObject jsonObject = new JSONObject(balanceString);
        balance = jsonObject.getJSONObject("result").getDouble("Available");
        address = jsonObject.getJSONObject("result").getString("CryptoAddress");
        System.out.println("Starting trade for "+ marketName);
    }


    public void hodl() throws JSONException {
        if(currentMarket == null) {
            isTradeOngoing = false;
            return;
        }
        while(!isOrderFilled) {
            retryBuyOrder();
        }
        stayOrLeave();
    }

    public void retryBuyOrder() throws JSONException {
        String cancelOrderString;
        cancelOrderString = bittrexData.cancelOrder(uuid);
        JSONObject jsonObject = new JSONObject(cancelOrderString);
        if(jsonObject.getString("success").equals("false")) {
            isOrderFilled = true;
        }
        if(jsonObject.getString("success").equals("true")) {
            startTrade(currentMarket);
        }
    }

    public void retrySellOrder() throws JSONException {
        String cancelOrderString;
        cancelOrderString = bittrexData.cancelOrder(uuid);
        JSONObject jsonObject = new JSONObject(cancelOrderString);
        if(jsonObject.getString("success").equals("false")) {
            isOrderSold = true;
        }
        if(jsonObject.getString("success").equals("true")) {
            stopTrade();
        }
    }

    public void stayOrLeave() throws JSONException {
        String marketHistoryString;
        double currentPrice;
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(!isTradeOngoing)
            return;
        marketHistoryString = bittrexData.getMarketHistory(currentMarket.getName());
        JSONObject jsonObject = new JSONObject(marketHistoryString);
        if(jsonObject.getString("success").equals("true")) {
            JSONArray jsonArray = (JSONArray) jsonObject.get("result");
            JSONObject jsonObject1 = jsonArray.getJSONObject(0);
            currentId =  jsonObject1.getDouble("Id");
            if(currentId != lastId)
                lastId = currentId;
            else
                stayOrLeave();
            currentPrice = jsonObject1.getDouble("Price");
            if(currentPrice<buyPrice.doubleValue() && (lastPrice == 0 || lastPrice>currentPrice)) {
                lossCount++;
            }
            else if (currentPrice>buyPrice.doubleValue()){
                buyPrice = BigDecimal.valueOf(currentPrice);
                lossCount=0;
            }
            lastPrice = currentPrice;
            if(lossCount >= 4 && currentPrice<buyPrice.doubleValue()*97/100){
                stopTrade();
                return;
            }
            if(currentPrice>buyPrice.doubleValue()*115/100){
                lastPrice = currentPrice;
                stopTrade();
                return;
            }

        }
        stayOrLeave();
    }

    public void stopTrade() throws JSONException {
        String sellOrderString;
        sellOrderString = bittrexData.createSellOrder(currentMarket.getName(), tradeQuantity, lastPrice);
        JSONObject jsonObject = new JSONObject(sellOrderString);
        if(jsonObject.getString("success").equals("true")) {
            isTradeOngoing = false;
            uuid = jsonObject.getJSONObject("result").getString("uuid");
        }
        if(!isOrderSold)
            retrySellOrder();
        return;
    }

    public boolean isMarketActive(JSONObject jsonObject, JSONObject jsonObject1) throws JSONException {
        String cancelOrderString;
        String time = jsonObject.getString("TimeStamp").replace("T", " ");
        String time1 = jsonObject1.getString("TimeStamp").replace("T", " ");

        Timestamp ts = Timestamp.valueOf(time);
        Timestamp ts1 = Timestamp.valueOf(time1);

        if(Math.abs(ts.getTime()-ts1.getTime())<50000)
            return true;
        return false;
    }

    public boolean checkMarketVolume(String marketName) throws JSONException {
        String marketSummaryString;
        int volume;
        double last;
        marketSummaryString = bittrexData.getMarketSummary(marketName);
        JSONObject jsonObject = new JSONObject(marketSummaryString);
        if(!jsonObject.getString("success").equals("true")) {
            return false;
        }
        JSONArray jsonArray = (JSONArray) jsonObject.get("result");
        JSONObject jsonObject1 = jsonArray.getJSONObject(0);
        volume =  jsonObject1.getInt("BaseVolume");
        last = jsonObject1.getDouble("Last");
        if (volume > 70 && last>0.00005)
            return true;

        return false;

    }

}
