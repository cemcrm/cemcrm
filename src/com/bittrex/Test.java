package com.bittrex;

import com.bittrex.Currency;
import com.bittrex.JSONParser;
import com.bittrex.JsonData;
import com.bittrex.StringData;
import org.json.JSONException;

import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.List;

public class Test {
	static boolean isActive = true;
	static List<MarketBean> activeMarkets = new ArrayList<>();
	public Test() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args)  throws JSONException{
		testJsonData();
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

	public static void testJsonData()  throws JSONException {
		// Get Wallet-address and balance of BITCOIN:
		double balance = 0L;
		TradeManager tradeManager = new TradeManager();
		JsonData bittrexData = new JsonData();
		tradeManager.getCoinBalance("BTC");
		if(activeMarkets.isEmpty())
			activeMarkets = tradeManager.getActiveMarkets();

		while (isActive) {
			try {
				while (tradeManager.isTradeOngoing) {
					tradeManager.hodl();
				}

				for (MarketBean market : activeMarkets) {
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
