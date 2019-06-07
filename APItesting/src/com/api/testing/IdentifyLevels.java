package com.api.testing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class IdentifyLevels {

	public static void main(String[] args) {

		try {
			// System.out.println("Java version "+
			// System.getProperty("java.version"));
//			String fnName = "TIME_SERIES_MONTHLY";
			// String symbol = "tcs.ns";
//			String symbol = "tatasteel.ns";
			// String symbol = "sbin.ns";
			// String symbol = "infy.ns";
			// outputsize = full/compact
			String symbol = "^NSEI";
			getHTFLevels(symbol, "monthly");

		} catch (Exception e) {

			e.printStackTrace();

		}

	}

	static void getHTFLevels(String symbol, String timeFrame) {
		String fnName = "TIME_SERIES_MONTHLY";
		if ("monthly".equalsIgnoreCase(timeFrame)) {
			// fnName = "TIME_SERIES_MONTHLY";
			fnName = "TIME_SERIES_MONTHLY";
		} else if ("weekly".equalsIgnoreCase(timeFrame)) {
			fnName = "TIME_SERIES_WEEKLY";
		} else if ("daily".equalsIgnoreCase(timeFrame)) {
			fnName = "TIME_SERIES_DAILY";
		}
		String urlString = formURL(fnName, symbol);
		Map map = retriveAPIdata(urlString);
		listSupportResistance(map, timeFrame, "high");
	}

	static void listSupportResistance(Map map, String timeSeries, String timeFrame) {
		/*
		 * 1. Identify Candle type 
		 * 2. check leg-in or leg-out candle 
		 * 	a. If gap found check gap type and decide leg type 
		 * 3. Check if level(basing) found 
		 * 4. Check level Type - RBR, DBR, RBD or DBD 
		 * 5. Set level high/low 
		 * 6. set all time high/low
		 */

		String timeSeriesKey = "";

		
		if ("daily".equalsIgnoreCase(timeSeries)) {
			timeSeriesKey = "Time Series (Daily)";
		} else if ("monthly".equalsIgnoreCase(timeSeries)) {
			timeSeriesKey = "Monthly Time Series";
		}
		System.out.println(timeFrame + " Support Resistance levels \n");
		Map map1 = (Map) map.get(timeSeriesKey);
		String strDate = "";
		int noOfdays = 2500;
		int i = 0;
		
		CandleLevelDetails candleLevel = new CandleLevelDetails();
		while (i != noOfdays) {	
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -i);
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			strDate = formatter.format(cal.getTime());

			// System.out.println("\n" + strDate);
			if (map1.containsKey(strDate)) {
				Map map2 = (Map) map1.get(strDate);
				candleLevel = setCandleDetail(map2,candleLevel);				
				candleLevel = getCandleType(candleLevel);
				if (!"high".equalsIgnoreCase(timeFrame))
					candleLevel = getGapType(candleLevel);
				getLegType(candleLevel);
				markSupportResistance(candleLevel,timeFrame);
				setLevelRange(candleLevel,timeFrame);
				if(candleLevel.isMarkLevel()){
					System.out.println(" Mark level "+  strDate+" CandleType "+ candleLevel.getCurrCandleType());
				}
			}
			candleLevel.setLastCandleType(candleLevel.getCurrCandleType());	
			candleLevel.setLastCandleHigh(candleLevel.getCandleHigh());
			candleLevel.setLastCandlelow(candleLevel.getCandlelow());			
		}

	}

	static String formURL(String fnName, String symbol) {
		String urlBase = "https://www.alphavantage.co/query?function=";
		String outputsize = "&outputsize=full";
		String apiKey = "&apikey=F4ASHUF1BONNF5AQ";
		// symbol = "NSE:ASHOKLEY";
		String urlString = urlBase + fnName + "&symbol=" + symbol + outputsize + apiKey;
		return urlString;
	}

	static Map retriveAPIdata(String urlString) {
		Map map = new HashMap<>();
		try {
			URL url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			String output;
			StringBuffer buffer = new StringBuffer();
			System.out.println("URL ...." + urlString + " \n");
			while ((output = br.readLine()) != null) {
				buffer = buffer.append(output);
			}
			conn.disconnect();

			Gson gson = new Gson();
			map = gson.fromJson(buffer.toString(), Map.class);
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return map;

	}

	static CandleLevelDetails setLevelRange(CandleLevelDetails candleLevel,String timeFrame) {
		
		if(candleLevel.isSupportLevel()){//Only Candles inside Support
			if("high".equalsIgnoreCase(timeFrame)){
				if(candleLevel.getCandleOpen() > candleLevel.getSupportHigh())
					candleLevel.setSupportHigh(candleLevel.getCandleOpen());
				if(candleLevel.getCandleClose() > candleLevel.getSupportHigh())
					candleLevel.setSupportHigh(candleLevel.getCandleClose());	
			}else{
				if(candleLevel.getCandleHigh() > candleLevel.getSupportHigh())
					candleLevel.setSupportHigh(candleLevel.getCandleHigh());
			}				
			if(candleLevel.getSupportLow() == 0.0 ||  candleLevel.getCandlelow() < candleLevel.getSupportLow())
				candleLevel.setSupportLow(candleLevel.getCandlelow());
				
//			System.out.println(" Support "+  strDate+" CandleType "+ currCandle);	
		}else if(candleLevel.isResistLevel()){//Only Candles inside Resistance
			if("high".equalsIgnoreCase(timeFrame)){
				if(candleLevel.getResistLow() == 0.0 || candleLevel.getCandleOpen() < candleLevel.getResistLow())
					candleLevel.setResistLow(candleLevel.getCandleOpen());
				if(candleLevel.getResistLow() == 0.0 || candleLevel.getCandleClose() < candleLevel.getResistLow())
					candleLevel.setResistLow(candleLevel.getCandleClose());	
			}else{
				if(candleLevel.getResistLow() == 0.0 || candleLevel.getCandlelow() < candleLevel.getResistLow())
					candleLevel.setResistLow(candleLevel.getCandlelow());
			}
			if(candleLevel.getCandleHigh() > candleLevel.getResistHigh())
				candleLevel.setResistHigh(candleLevel.getCandleHigh()) ;				
//			System.out.println(" Resistance "+  strDate+" CandleType "+ currCandle);					
		}else{//Candle is LG or LR or SL outside levels
			//TODO fix the logic to avoid only leg-in and leg out candle for RBR, DBR, RBD & DBD -- Avoiding all consecutive LG/LR now
			
			if(!candleLevel.markLevel){
			if(candleLevel.getCandleHigh() > candleLevel.getAllTimeHigh() && candleLevel.getLastCandleType() != "LR"){
				//TODO avoid first candle if LR
				candleLevel.setAllTimeHigh(candleLevel.getCandleHigh()) ;					
			}
				
			if(candleLevel.getAllTimeLow() == 0.0 || candleLevel.getCandlelow() < candleLevel.getAllTimeLow() && candleLevel.getLastCandleType() != "LG" ){	//TODO avoid first candle if LG								
				candleLevel.setAllTimeLow(candleLevel.getCandlelow());					
			}
			}else{//Use leg-out candle extreme as level extreme for DBR & RBD
				if(candleLevel.getCandleHigh() > candleLevel.getResistHigh() && candleLevel.getLastCandleType() != "LR")
					candleLevel.setResistHigh(candleLevel.getCandleHigh());
				if(candleLevel.getSupportLow() == 0.0 || ( candleLevel.getCandlelow()  < candleLevel.getSupportLow() && candleLevel.getLastCandleType() != "LG" ))
					candleLevel.setSupportLow(candleLevel.getCandlelow()) ;					
			}
		}
		
		return candleLevel;
	}
	static CandleLevelDetails markSupportResistance(CandleLevelDetails candleLevel,String timeFrame) {
		
		if ("LG".equalsIgnoreCase(candleLevel.getCurrCandleType())){
			candleLevel.setNoOfLR(0); 
			if(candleLevel.isSupportLevel() || candleLevel.isResistLevel()){//Restart the LG candle count after a level
				candleLevel.setNoOfLG(1);
				candleLevel.setNoOfSL(0);
//				candleLevel.setMarkLevel(true);		
				candleLevel.setSupportLevel(false);
				candleLevel.setResistLevel(false);				
			}			
			else{				
//				candleLevel.setMarkLevel(false);	
				candleLevel.setNoOfLG(candleLevel.getNoOfLG()+1);				
			}
							
		}			
		else if ("LR".equalsIgnoreCase(candleLevel.getCurrCandleType())){
			candleLevel.setNoOfLG(0);
			if(candleLevel.isSupportLevel() || candleLevel.isResistLevel()){//Restart the LR candle count after a level
				candleLevel.setNoOfLR(1); 
				candleLevel.setNoOfSL(0);
//				candleLevel.setMarkLevel(true);		
				candleLevel.setSupportLevel(false);
				candleLevel.setResistLevel(false);	
			}				
			else{
//				candleLevel.setMarkLevel(false);	
				candleLevel.setNoOfLR(candleLevel.getNoOfLR()+1);		
			}
							
		}	else if ("SL".equalsIgnoreCase(candleLevel.getCurrCandleType())) {
			candleLevel.setNoOfSL(candleLevel.getNoOfSL()+1);	
			/*if((supportGap || resistGap) && (supportLevel || resistLevel)){
				markLevel = true;
				
			}else{
				markLevel = false;
			}*/
//			candleLevel.setMarkLevel(false);
			
			//gap is considered as 2 large green candles
			if ((candleLevel.getNoOfLG() >= 2 || "S".equalsIgnoreCase(candleLevel.getGapType())
					|| ("high".equalsIgnoreCase(timeFrame) && candleLevel.getNoOfLG() >= 1))  && candleLevel.getNoOfSL() < 6)
				candleLevel.setSupportLevel(true);
			//gap is considered as 2 large red candles
			else if ((candleLevel.getNoOfLR() >= 2 || "R".equalsIgnoreCase(candleLevel.getGapType()) 
					|| ("high".equalsIgnoreCase(timeFrame) && candleLevel.getNoOfLR() >= 1)) && candleLevel.getNoOfSL() < 6)
				candleLevel.setResistLevel(true);
			else {
				candleLevel.setNoOfLG(0);
				candleLevel.setNoOfLR(0); 
				candleLevel.setNoOfSL(0);
				
				candleLevel.setSupportLevel(false);
				candleLevel.setResistLevel(false);	
			}
		}
		
		if(candleLevel.isLegInCandle && (candleLevel.isSupportLevel() || candleLevel.isResistLevel())){
			candleLevel.setMarkLevel(true);				
			//TODO set mark level to true only for leg-in candle, currently setting for both leg-in and leg-out candle(Only for Gaps)
			/*supportLevel = false;
			resistLevel = false;*/
		}else{
			candleLevel.setMarkLevel(false);
		}
		
		return candleLevel;
		
	}
	static CandleLevelDetails getLegType(CandleLevelDetails candleLevel) {
		//gap or any of large candle is considered for leg-out and leg-in candle
		if (!"".equalsIgnoreCase(candleLevel.getGapType()) || "LG".equalsIgnoreCase(candleLevel.getCurrCandleType())
				|| "LR".equalsIgnoreCase(candleLevel.getCurrCandleType())) {
			//TODO continuous LG and LR will change the logic
			if (candleLevel.isLegOutCandle && (candleLevel.isSupportLevel() || candleLevel.isResistLevel())) {
				candleLevel.setLegInCandle(true);				
			} else {
				candleLevel.setLegOutCandle(true);
			}
		}else{
			candleLevel.setLegInCandle(false);
			candleLevel.setLegOutCandle(false);			
		}
		return candleLevel;
	}
 
	static CandleLevelDetails getGapType(CandleLevelDetails candleLevel) {
		String gapType = "";
		if (candleLevel.getCandleClose() > candleLevel.getLastCandlelow()  && 
				candleLevel.getCandleClose()  > candleLevel.getLastCandleHigh())
			gapType = "R";
		if (candleLevel.getCandleClose() < candleLevel.getLastCandlelow() && 
				candleLevel.getCandleClose() < candleLevel.getLastCandleHigh())
			gapType = "S";
		// System.out.println(" Gap on "+ strDate+" candleClose - "+ candleLevel.getCandleClose()
		// +" lastCandleHigh - "+ candleLevel.getLastCandleHigh() + " lastCandlelow - "+
		// candleLevel.getLastCandlelow());
		
		candleLevel.setGapType(gapType);
		return candleLevel;
	}

	static CandleLevelDetails setCandleDetail(Map map,CandleLevelDetails candleLevel) {
		candleLevel.setCandleHigh(Double.parseDouble(map.get("2. high").toString()));
		candleLevel.setCandlelow(Double.parseDouble(map.get("3. low").toString()));
		candleLevel.setCandleOpen(Double.parseDouble(map.get("1. open").toString()));
		candleLevel.setCandleClose(Double.parseDouble(map.get("4. close").toString()));	
		
		return candleLevel;
	}
	static CandleLevelDetails getCandleType(CandleLevelDetails candleLevel) {		
		Double daysRange = candleLevel.getCandleHigh()-candleLevel.getCandlelow();
		Double bodyRange = candleLevel.getCandleClose()-candleLevel.getCandleOpen();		
		String candleType = "";
		double candleSize = bodyRange / daysRange;
		if (candleSize > 0.5) {
			candleType = "LG";
		} else if (candleSize < -0.5) {
			candleType = "LR";
		} else {
			candleType = "SL";
		}
		candleLevel.setCurrCandleType(candleType);

		return candleLevel;
	}
}
