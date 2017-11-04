package com.bittrex;

import com.bittrex.Currency;
import com.bittrex.JSONParser;
import com.bittrex.JsonData;
import com.bittrex.StringData;
import org.json.JSONException;

import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

public class Test extends Thread {
	static boolean isActive = true;
	static List<MarketBean> activeMarkets = new ArrayList<>();
	public Test() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args)   throws JSONException{
		TradeManager tradeManager = new TradeManager();
		tradeManager.getCoinBalance("BTC");

		if(activeMarkets.isEmpty())
			activeMarkets = tradeManager.getActiveMarkets();

		int threadNumber = activeMarkets.size()/10+1;

		for(int i=0;i<threadNumber;i++){
			Test test = new Test();
			test.setName(String.valueOf(i));
			test.start();
		}

	}

	public static void testStringData() {
		StringData bittrexData = new StringData();
		System.out.println(bittrexData.getBalances());
		System.out.println(bittrexData.getBalance("XMR"));
		System.out.println(bittrexData.getBalance("LTC"));
		System.out.println(bittrexData.getOpenOrders());
		System.out.println(bittrexData.getOpenOrders("ETH", "BAT"));
		System.out.println(bittrexData.getOpenOrders(Currency.ETH, Currency.BAT));
		System.out.println(bittrexData.getOrder("fbfbd380-fb3e-49b8-9cc4-626fcdf9959b"));
		System.out.println(bittrexData.getOrderHistory());
		System.out.println(bittrexData.getMarketSummary());
		System.out.println(bittrexData.getMarketSummary(Currency.BTC, Currency.XMR));
		System.out.println(bittrexData.getTicker(Currency.BTC, Currency.XMR));
		System.out.println(bittrexData.getTicker("BTC-XMR"));
		System.out.println(bittrexData.getMarkets());
		System.out.println(bittrexData.getCurrencies());
		System.out.println(bittrexData.cancelOrder("fbfbd380-fb3e-49b8-9cc4-626fcdf9959b"));
		System.out.println(bittrexData.createBuyOrder(Currency.ETH, Currency.BAT, 10, 0.00006));
		System.out.println(bittrexData.createSellOrder(Currency.ETH, Currency.BAT, 10, 0.1));
	}

	public  void run()  {
		// Get Wallet-address and balance of BITCOIN:
		System.out.println("Running Thread Name: "+ this.currentThread().getName());
		double balance = 0L;
		List<MarketBean> activeMarketsSubList = new ArrayList<>();
		activeMarketsSubList = activeMarkets.subList(Integer.valueOf(this.currentThread().getName())*10, Integer.valueOf(this.currentThread().getName())*10+10);
		TradeManager tradeManager = new TradeManager();
		while (isActive) {
			try {
				while (tradeManager.isTradeOngoing) {
					tradeManager.hodl();
				}

				for (MarketBean market : activeMarketsSubList) {
					tradeManager.detectPump(market);

					if (tradeManager.isTradeOngoing)
						break;
				}
	/*
				JsonObject bitcoinBalance = bittrexData.getBalance("BTC").getJsonObject("result");
				String wallet = JSONParser.getStringValue(bitcoinBalance, "CryptoAddress");
				double balanceValue = JSONParser.getDoubleValue(bitcoinBalance, "Balance");
				double balancePending = JSONParser.getDoubleValue(bitcoinBalance, "Pending");
				System.out.println("Wallet:\t\t" + wallet);
				System.out.println("Balance:\t" + balanceValue);
				System.out.println("Pending:\t" + balancePending);*/
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
	}
}
