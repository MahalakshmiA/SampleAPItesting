package com.api.testing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class NiftyStocksDailyLevels2 {

	private static String lastCandle = "";
	private static String currCandle = "";
	private static int noOfLG = 0;
	private static int noOfLR = 0;
	private static int noOfSL = 0;
	private static boolean supportLevel = false;
	private static boolean resistLevel = false;
	private static boolean markLevel = false;
	private static boolean isLegOutCandle = false;
	private static Double quote = 0.0;

	public static void main(String[] args) {

		try {
			long starttimefinal = System.currentTimeMillis();
			long endtimefinal;
			System.out.println("Start time "+ new Date (starttimefinal));
			long starttime = System.currentTimeMillis();
		       long endtime;
		       long timetaken;
			String fnName = "TIME_SERIES_MONTHLY";
			int i = 0;
			for (String symbol : ReadStocks.getIndexStocksList("niftyStocks")) {
				try {
					/*
					 * quote = 0.0; getQuote(symbol); System.out.println(symbol+" | " + quote); i++;
					 * endtime = System.currentTimeMillis(); timetaken = endtime-starttime;
					 * if((i%5==0)){ System.out.println("Time taken & Wait time ... "+ timetaken
					 * +" & "+ (61000-timetaken)); if(timetaken<61000)
					 * Thread.sleep(61000-timetaken); starttime = System.currentTimeMillis(); }
					 */
//					getDailyLevels(symbol);
					System.out.println(symbol);
			          getHTFLevels( symbol,  "monthly");
					endtime = System.currentTimeMillis();
			          timetaken = endtime-starttime;
			          i++;
			          if(i%5==0){
			        	  System.out.println("Time taken & Wait time ... "+ timetaken +" & "+ (61000-timetaken));
			        	  if(timetaken<61000)
			        	  Thread.sleep(61000-timetaken);
			        	  starttime = System.currentTimeMillis();
			          }
			          
			         
					/*
					 * if(i%2 == 0) { Thread.sleep(61000); }
					 */
					/*Thread.sleep(1000);
					
					Thread.sleep(3000);*/
					
				} catch (Exception e) {					
					e.printStackTrace();
				}
			}
			 endtimefinal = System.currentTimeMillis();
	          System.out.println("End time "+ new Date (endtimefinal));
	          System.out.println("time taken "+(endtimefinal-starttimefinal));
			

		} catch (Exception e) {

			e.printStackTrace();

		}

	}
	
	static void  getQuote(String symbol){
//		Double quote = 0.0;
		Double change = 0.0;
		Double changePercent = 0.0;
		String fnName = "GLOBAL_QUOTE";
		String urlString = formURL(fnName, symbol);
		Map map = retriveAPIdata(urlString);	
		Map map2 = (Map)map.get("Global Quote");
		quote = Double.parseDouble(map2.get("05. price").toString());
		map.clear();
		//TODO solve issue with negative value and percentage
//		change = Double.parseDouble(map2.get("09. change").toString()); 		
//		changePercent = Double.parseDouble(map2.get("10. change percent").toString()); 
//		System.out.println("quote |"+" Change |" + " changePercent " + quote+" | "  + change + " | " + changePercent);
		
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

	static void getWeeklyLevels(String symbol) {

	}

	static void getDailyLevels(String symbol) {
		String fnName = "TIME_SERIES_DAILY";
		String urlString = formURL(fnName, symbol);
		Map map = retriveAPIdata(urlString);
		listSupportResistance(map, "daily", "low");
	}

	static String formURL(String fnName, String symbol) {
		String urlBase = "https://www.alphavantage.co/query?function=";
		String outputsize = "&outputsize=full";
		String apiKey = "&apikey=F4ASHUF1BONNF5AQ";
		// symbol = "NSE:ASHOKLEY";
		String urlString = urlBase + fnName + "&symbol=" + symbol + outputsize + apiKey;
		return urlString;
	}

	static void listSupportResistance(Map map, String timeSeries, String timeFrame) {
		/*
		 * 1. Identify Candle type 2. check leg-in or leg-out candle a. If gap found
		 * check gap type and decide leg type 3. Check if level(basing) found 4. Check
		 * level Type - RBR, DBR, RBD or DBD 5. Set level high/low 6. set all time
		 * high/low
		 */
		String timeSeriesKey = "";
		if ("daily".equalsIgnoreCase(timeSeries)) {
			timeSeriesKey = "Time Series (Daily)";
		} else if ("monthly".equalsIgnoreCase(timeSeries)) {
			timeSeriesKey = "Monthly Time Series";
		}
//		System.out.println(timeFrame + " Support Resistance levels \n");
		Map map1 = new HashMap<String, HashMap>(); 
		map1= 	(Map) map.get(timeSeriesKey);
		String strDate = "";
		String startDate = "2019-06-14";
		int noOfdays = 100;
		int i = 0;
		Double supportHigh = 0.0;
		Double supportLow = 0.0;
		Double resistHigh = 0.0;
		Double resistLow = 0.0;

		Double allTimeHigh = 0.0;
		Double allTimeLow = 0.0;
		Double candleHigh = 0.0;
		Double candlelow = 0.0;
		Double candleOpen = 0.0;
		Double candleClose = 0.0;

		Double lastCandleHigh = 0.0;
		Double lastCandlelow = 0.0;

		boolean isSuppAvailble = false;
		boolean isResistAvailble = false;
		boolean supportGap;
		boolean resistGap;
		boolean testModule = false;
		while (i != noOfdays) {
			supportGap = false;
			resistGap = false;
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, -i);
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
			strDate = formatter.format(cal.getTime());

			try {
				if (cal.getTime().after(new SimpleDateFormat("yyyy-MM-dd").parse(startDate))) {
//					System.out.println(" Date to Skip " + cal.getTime());
					i++;
					continue;
				}

			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// System.out.println("\n" + strDate);
			if (map1.containsKey(strDate)) {
				Map map2 = (Map) map1.get(strDate);
				if(null != map2) {				
				
				candleHigh = Double.parseDouble(map2.get("2. high").toString());
				candlelow = Double.parseDouble(map2.get("3. low").toString());

				candleOpen = Double.parseDouble(map2.get("1. open").toString());
				candleClose = Double.parseDouble(map2.get("4. close").toString());

				// Avoid candle with all 0 data, consider it as data issue and skip that candle
				if (candleHigh == 0.0 || candlelow == 0.0 || candleOpen == 0.0 || candleClose == 0.0) {
					i++;
					continue;
				}
				currCandle = getCandleType(map2);
				if(quote == 0.0 && candleClose != 0.0) {
					quote = candleClose;
				}
				}
				// TODO Move gap logic to new method
				if (!"high".equalsIgnoreCase(timeFrame) && lastCandlelow != 0.0 && lastCandleHigh != 0.0) {
					if (candleClose > lastCandlelow && candleClose > lastCandleHigh) {
						resistGap = true;
						// System.out.println("Resist Gap "+ strDate+" Close "+ candleClose +" lastHigh
						// "+ lastCandleHigh + " lastLow "+ lastCandlelow);
					}

					if (candleClose < lastCandlelow && candleClose < lastCandleHigh) {
						supportGap = true;
						// System.out.println("Supp Gap "+ strDate+" Close "+ candleClose +" lastHigh "+
						// lastCandleHigh + " lastLow "+ lastCandlelow);
					}
				}
				/*
				 * testModule = true; if(testModule) { lastCandleHigh = candleHigh;
				 * lastCandlelow = candlelow; lastCandle = currCandle; i++; continue; }
				 */

				markSupportResistance(currCandle, timeFrame, supportGap, resistGap);
				// TODO Identify level type based on Legout,basing and leg in
				// Consider leg-out and leg-in candles to set level low high based on level type

				if (!markLevel && supportLevel) {// Only Candles inside Support , only boring candle
					if ("high".equalsIgnoreCase(timeFrame)) {
						if (candleOpen > supportHigh)
							supportHigh = candleOpen;
						if (candleClose > supportHigh)
							supportHigh = candleClose;
					} else {
						if (candleHigh > supportHigh)
							supportHigh = candleHigh;
					}
					if (supportLow == 0.0 || candlelow < supportLow)
						supportLow = candlelow;

					// If previous candle is legout candle , Use leg-out candle extreme as level
					// extreme for RBR, DBR, RBD & DBD
					if (supportLow == 0.0 || (lastCandlelow < supportLow))
						supportLow = lastCandlelow;

					// System.out.println(" Support "+ strDate+" CandleType "+ currCandle);
				} else if (!markLevel && resistLevel) {// Only Candles inside Resistance , only boring candle
					if ("high".equalsIgnoreCase(timeFrame)) {
						if (resistLow == 0.0 || candleOpen < resistLow)
							resistLow = candleOpen;
						if (resistLow == 0.0 || candleClose < resistLow)
							resistLow = candleClose;
					} else {
						if (resistLow == 0.0 || candlelow < resistLow)
							resistLow = candlelow;
					}
					if (candleHigh > resistHigh)
						resistHigh = candleHigh;

					// If previous candle is legout candle,Use leg-out candle extreme as level
					// extreme for RBR, DBR, RBD & DBD
					if (lastCandleHigh > resistHigh)
						resistHigh = lastCandleHigh;

					// System.out.println(" Resistance "+ strDate+" CandleType "+ currCandle);
				} else {// Candle is LG or LR or SL outside levels
						// TODO fix the logic to avoid only leg-in and leg out candle for RBR, DBR, RBD
						// & DBD -- Avoiding all consecutive LG/LR now

					// setLowHigh();
					if (!markLevel) {
						/*
						 * if(allTimeLow == 0.0 || candlelow < allTimeLow && currCandle != "LG" ){
						 * //TODO avoid first candle if LG allTimeLow = candlelow; }
						 */
						if (isLegOutCandle) {
							if (allTimeLow == 0.0 || lastCandlelow < allTimeLow) {
								allTimeLow = lastCandlelow;
							}
							if (lastCandleHigh > allTimeHigh) {
								// TODO avoid first candle if LR
								allTimeHigh = lastCandleHigh;
							}

						} else {
							if (allTimeLow == 0.0 || candlelow < allTimeLow) {
								allTimeLow = candlelow;
							}
							if (candleHigh > allTimeHigh) {
								// TODO avoid first candle if LR
								allTimeHigh = candleHigh;
							}

						}
						/*
						 * if(allTimeLow == 0.0 || candlelow < allTimeLow && !isLegOutCandle ){ //TODO
						 * avoid first candle if LG allTimeLow = candlelow; }
						 * 
						 * if(candleHigh > allTimeHigh && currCandle != "LR"){ //TODO avoid first candle
						 * if LR allTimeHigh = candleHigh; }
						 */

					} else {// Use leg-in candle extreme as level extreme for RBD & DBR
						if (resistLevel && candleHigh > resistHigh && currCandle == "LG")
							resistHigh = candleHigh;
						if (supportLevel && (supportLow == 0.0 || (candlelow < supportLow && currCandle == "LR")))
							supportLow = candlelow;
					}

				}
				/*
				 * if(supportGap || resistGap){ supportLevel = false; resistLevel = false; }
				 */
				// System.out.println(strDate +" Type " +currCandle + " ResistGap " +resistGap+
				// " SuppGap " +supportGap+ " High "+ allTimeHigh+" Low "+ allTimeLow);
				// if(markLevel || ((resistLevel || supportLevel ) && (supportGap ||
				// resistGap))){
				if (markLevel) {
					if (supportLevel) {
						/*
						 * System.out.println("Supp " + strDate + " Type " + currCandle + " suppLow " +
						 * supportLow + " suppHigh " + supportHigh + " Low " + allTimeLow);
						 */
						if (supportHigh != 0.0 && supportLow != 0.0
								&& (supportHigh < allTimeLow || allTimeLow == 0.0)) {
							// Get levels which are within certain percentage from current price
							if (((quote - supportHigh) * 100 / quote) < 20) {
								System.out.println(
										" Support level " + strDate + " from  " + supportHigh + " to " + supportLow);
								isSuppAvailble = true;
							}
						}
					} else if (resistLevel) {
						// System.out.println("Resist "+ strDate+" CandleType "+ currCandle +" resHigh "
						// + resistHigh+" resLow " + resistLow +" High " + allTimeHigh);
						if (resistHigh != 0.0 && resistLow != 0.0 && resistLow > allTimeHigh) {
							// Get levels which are within certain percentage from current price
							if (((resistLow - quote) * 100 / quote) < 20) {
								System.out.println(
										" Resistance level " + strDate + " from  " + resistLow + " to " + resistHigh);
								isResistAvailble = true;
							}
						}
					}
					supportLevel = false;
					resistLevel = false;

					/*
					 * if(isSuppAvailble && isResistAvailble) break;
					 */

					if (supportLow != 0.0 && (supportLow < allTimeLow || allTimeLow == 0.0))
						allTimeLow = supportLow;
					if (resistHigh > allTimeHigh)
						allTimeHigh = resistHigh;

					supportHigh = 0.0;
					supportLow = 0.0;
					resistHigh = 0.0;
					resistLow = 0.0;

					// TODO check whether leg out candle low/high is considered for level
					/*
					 * if(candleHigh > allTimeHigh && currCandle != "LR")//set leg-out candle high
					 * as all time high allTimeHigh = candleHigh; if(candlelow < allTimeLow &&
					 * currCandle != "LG" )//set leg-out candle low as all time low allTimeLow =
					 * candlelow;
					 */

					if (candleHigh > allTimeHigh)// set leg-in candle high as all time high
						allTimeHigh = candleHigh;
					if (candlelow < allTimeLow)// set leg-in candle low as all time low
						allTimeLow = candlelow;

				}
			}

			lastCandleHigh = candleHigh;
			lastCandlelow = candlelow;
			lastCandle = currCandle;
			i++;
		}

		if (!isSuppAvailble)
			System.out.println("No Support level ");
		if (!isResistAvailble)
			System.out.println("No Resistance level ");

	}

	/*
	 * static void setLowHigh(Double allTimeHigh, Double allTimeLow,Double
	 * candleHigh,Double candlelow) { if(!markLevel){ if(candleHigh > allTimeHigh &&
	 * currCandle != "LR"){ //TODO avoid first candle if LR allTimeHigh =
	 * candleHigh; }
	 * 
	 * if(allTimeLow == 0.0 || candlelow < allTimeLow && currCandle != "LG" ){
	 * //TODO avoid first candle if LG allTimeLow = candlelow; } }else{//Use leg-out
	 * candle extreme as level extreme for DBR & RBD if(candleHigh > resistHigh &&
	 * currCandle != "LR") resistHigh = candleHigh; if(supportLow == 0.0 || (
	 * candlelow < supportLow && currCandle != "LG" )) supportLow = candlelow; }
	 * 
	 * }
	 */

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
//			System.out.println("URL ...." + urlString + " \n");
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

	static void markSupportResistance(String currCandle, String timeFrame, boolean supportGap, boolean resistGap) {
		/*
		 * noOfLG = noOfLG+countLG; noOfLR = noOfLR+countLR;
		 */
		boolean legInCandle = false;
		isLegOutCandle = false;
		if (supportGap || resistGap) {
			isLegOutCandle = true;
			if (supportLevel || resistLevel) {
				legInCandle = true;
			}
			if (supportGap)
				noOfLG = noOfLG + 2;
			if (resistGap)
				noOfLR = noOfLR + 2;

		}

		if ("LG".equalsIgnoreCase(currCandle)) {
			noOfLR = 0;
			if (supportLevel || resistLevel) {// Considered as leg-in candle. Restart the LG candle count after a level
				noOfLG = 1;
				noOfSL = 0;
				legInCandle = true;
			} else {
				markLevel = false;
				noOfLG++;
			}

			// if ((noOfLG >= 2 || supportGap || ("high".equalsIgnoreCase(timeFrame) &&
			// noOfLG >= 1)))
			isLegOutCandle = true;

		} else if ("LR".equalsIgnoreCase(currCandle)) {
			noOfLG = 0;
			if (supportLevel || resistLevel) {// Considered as leg-in candle.Restart the LR candle count after a level
				noOfLR = 1;
				noOfSL = 0;
				legInCandle = true;

			} else {
				markLevel = false;
				noOfLR++;
			}

			// if ((noOfLR >= 2 || resistGap || ("high".equalsIgnoreCase(timeFrame) &&
			// noOfLR >= 1)))
			isLegOutCandle = true;

		} else if ("SL".equalsIgnoreCase(currCandle)) {
			noOfSL++;
			/*
			 * if((supportGap || resistGap) && (supportLevel || resistLevel)){ markLevel =
			 * true;
			 * 
			 * }else{ markLevel = false; }
			 */
			markLevel = false;

			// gap is considered as 2 large green candles
			if ((noOfLG >= 2 || supportGap || ("high".equalsIgnoreCase(timeFrame) && noOfLG >= 1)) && noOfSL < 6)
				supportLevel = true;
			// gap is considered as 2 large red candles
			else if ((noOfLR >= 2 || resistGap || ("high".equalsIgnoreCase(timeFrame) && noOfLR >= 1)) && noOfSL < 6)
				resistLevel = true;
			else {
				noOfLG = 0;
				noOfLR = 0;
				noOfSL = 0;

				supportLevel = false;
				resistLevel = false;
			}
		}

		// if((supportGap || resistGap) && legInCandle){
		if (legInCandle) {
			markLevel = true;
			if ("LG".equalsIgnoreCase(currCandle)) {
				noOfLG = 1;
			} else if ("LR".equalsIgnoreCase(currCandle)) {
				noOfLR = 1;
			} else if ("SL".equalsIgnoreCase(currCandle)) {
				noOfSL = 1;
			}

			if (supportGap)
				noOfLG = 2;
			if (resistGap)
				noOfLR = 2;

			// TODO add candle count for gap
			/*
			 * supportLevel = false; resistLevel = false;
			 */
			// TODO set mark level to true only for leg-in candle, currently setting for
			// both leg-in and leg-out candle(Only for Gaps)
			/*
			 * supportLevel = false; resistLevel = false;
			 */
		} else {
			markLevel = false;
		}

	}

	static String getCandleType(Map map) {
		Double daysRange = Double.parseDouble(map.get("2. high").toString())
				- Double.parseDouble(map.get("3. low").toString());
		Double bodyRange = Double.parseDouble(map.get("4. close").toString())
				- Double.parseDouble(map.get("1. open").toString());
		String candleType = "";
		double candleSize = bodyRange / daysRange;
		if (candleSize > 0.5) {
			candleType = "LG";
		} else if (candleSize < -0.5) {
			candleType = "LR";
		} else {
			candleType = "SL";
		}
		/*
		 * System.out.println("\n daysRange " + daysRange.floatValue());
		 * System.out.println("\n bodyRange " + bodyRange.floatValue());
		 */
		// System.out.println("\n bodyRange/daysRange " + bodyRange/daysRange);
		// System.out.println("\n candleType " + candleType);

		return candleType;
	}

}
