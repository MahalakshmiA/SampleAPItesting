package com.api.testing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
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
import java.util.TreeMap;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sample.Client2;
import com.sample.Details;
import com.sample.HourlyHandler;

public class StocksHourlyLevels {

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
	private static String symbolGlobal = "";
	
	
	
	public static void main(String[] args) throws Exception {
		String fileName = "niftyStocks";
		getLevels(fileName);
	}


	public static void getLevels(String fileName) throws IOException {
		long starttimefinal = System.currentTimeMillis();
		long endtimefinal;
		System.out.println("StocksHourlyLevels Start time " +  new Date(starttimefinal));	
		int i = 0;
		long starttime = System.currentTimeMillis();
		long endtime;
		long timetaken;
		String fnName = "TIME_SERIES_MONTHLY";
//		String symbol = "ADANIPORTS.NS";
		String interval = "60min";
		boolean nseStock = true;
		boolean is120 = true;
		StringBuffer twoHourlevelsList = new StringBuffer();
		StringBuffer hourlylevelsList = new StringBuffer();
		TreeMap<String, Details> sortedCandlesList;
		try {
			for (String symbol : ReadStocks.getIndexStocksList(fileName)) {
			resetGlobalVariables();		
			symbolGlobal = symbol;
			sortedCandlesList = HourlyHandler.getCandlesList(symbol,interval);
			TreeMap<String, Details> consolidatedHighLowList = HourlyHandler.getHourlyCandlesList(sortedCandlesList, nseStock, is120);
			StringBuffer twoHourLevels =  listSupportResistance(consolidatedHighLowList);
			System.out.println("twoHourLevels \n"+twoHourLevels );
			twoHourlevelsList.append(twoHourLevels);
			
			resetGlobalVariables();	
			TreeMap<String, Details> consolidatedHighLowList1 = HourlyHandler.getHourlyCandlesList(sortedCandlesList, nseStock, false);
			StringBuffer hourlyLevels =  listSupportResistance(consolidatedHighLowList1);
			System.out.println("hourlyLevels \n"+hourlyLevels );
			hourlylevelsList.append(hourlyLevels);
			i++;
			endtime = System.currentTimeMillis();
			timetaken = endtime - starttime;
			if (timetaken < 60000 && (i % 30 == 0)) {
				System.out.println("Time taken & Wait time ..." + timetaken + " & " + (61000-timetaken) );
				Thread.sleep(61000-timetaken);
				starttime = System.currentTimeMillis();
			}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
//			writeToFile("D:\\Soosai\\APItesting\\config\\file\\stocksHourlyLevels.txt",twoHourlevelsList);
			writeToFile("E:\\Soosai\\Stocks\\SampleAPItesting-master\\SampleAPItesting-master\\APItesting\\config\\file\\TwoHourlevels.txt",twoHourlevelsList);
			writeToFile("E:\\Soosai\\Stocks\\SampleAPItesting-master\\SampleAPItesting-master\\APItesting\\config\\file\\Hourlylevels.txt",hourlylevelsList);
			}
		
		endtimefinal = System.currentTimeMillis();
		System.out.println("StocksHourlyLevels End time " + new Date(endtimefinal) );
		System.out.println("time taken " + (endtimefinal - starttimefinal));
	}


	static StringBuffer listSupportResistance(TreeMap<String, Details> consolidatedHighLowList) throws Exception{
		/*
		 * 1. Identify Candle type 2. check leg-in or leg-out candle a. If gap found
		 * check gap type and decide leg type 3. Check if level(basing) found 4. Check
		 * level Type - RBR, DBR, RBD or DBD 5. Set level high/low 6. set all time
		 * high/low
		 */
		/*String timeSeriesKey = "";
		if ("daily".equalsIgnoreCase(timeSeries)) {
			timeSeriesKey = "Time Series (Daily)";
		} else if ("monthly".equalsIgnoreCase(timeSeries)) {
			timeSeriesKey = "Monthly Time Series";
		}
//		System.out.println(timeFrame + " Support Resistance levels \n");
		Map map1 = new HashMap<String, HashMap>(); 
		if(map.containsKey("Note")){
			System.out.println(symbolGlobal+" : " +map.get("Note"));
		}else if(map.containsKey("Error Message")) {
			System.out.println(symbolGlobal+ " : " +map.get("Error Message"));			
			throw new Exception();
		}else {
			map1= 	(Map) map.get(timeSeriesKey);			
		}
		*/
		String timeFrame = "daily";
		String strDate = "";
		Calendar cal1 = Calendar.getInstance();
		cal1.add(Calendar.DATE, 1);
		SimpleDateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd");
		String startDate = formatter1.format(cal1.getTime());
//		String startDate = "2019-06-27";
		int noOfdays = 1000;
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
		
		boolean supportGap;
		boolean resistGap;
		String printLevel = "";
		StringBuffer levels = new StringBuffer();
		
//		System.out.println("consolidatedHighLowList size"+ consolidatedHighLowList.size());

		for (Entry<String, Details> entry1 : consolidatedHighLowList.entrySet()) {	        
			supportGap = false;
			resistGap = false;							
				strDate = entry1.getValue().getStartDate();
				candleHigh = Double.parseDouble(entry1.getValue().getHigh());
				candlelow = Double.parseDouble(entry1.getValue().getLow());

				candleOpen = Double.parseDouble(entry1.getValue().getOpen());
				candleClose = Double.parseDouble(entry1.getValue().getClose());				
				// Avoid candle with all 0 data, consider it as data issue and skip that candle
				if (candleHigh == 0.0 || candlelow == 0.0 || candleOpen == 0.0 || candleClose == 0.0) {
					i++;
					continue;
				}
				currCandle = entry1.getValue().getCandleType();
				if(quote == 0.0 && candleClose != 0.0) {
					quote = candleClose;
//					System.out.println(symbolGlobal+" | "  + quote);
				}
				if (Math.abs(((candleClose - quote) * 100 / quote)) > 10) {
					break;
				}				
//				System.out.println(strDate);

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
				if (markLevel) {
					
					if (supportLevel) {
						/*
						 * System.out.println("Supp " + strDate + " Type " + currCandle + " suppLow " +
						 * supportLow + " suppHigh " + supportHigh + " Low " + allTimeLow);
						 */
						if (supportHigh != 0.0 && supportLow != 0.0
								&& (supportHigh < allTimeLow || allTimeLow == 0.0)) {
							// Get levels which are within certain percentage from current price
//							if (((quote - supportHigh) * 100 / quote) < 20) {
								printLevel = symbolGlobal+"|"+strDate+"|Support|"+supportHigh+"|"+supportLow+"|"+String.format("%.2f",((quote - supportHigh) * 100 / quote));
								/*System.out.println(
										" Support level " + strDate + " from  " + supportHigh + " to " + supportLow);*/
//								System.out.println(printLevel);
								levels.append(printLevel+"\n");
//								isSuppAvailble = true;
//							}
						}
					} else if (resistLevel) {
						// System.out.println("Resist "+ strDate+" CandleType "+ currCandle +" resHigh "
						// + resistHigh+" resLow " + resistLow +" High " + allTimeHigh);
						if (resistHigh != 0.0 && resistLow != 0.0 && resistLow > allTimeHigh) {
							// Get levels which are within certain percentage from current price
//							if (((resistLow - quote) * 100 / quote) < 20) {
								printLevel = symbolGlobal+"|"+strDate+"|Resistance|"+resistLow+"|"+resistHigh+"|"+String.format("%.2f",((resistLow - quote) * 100 / quote));
								/*System.out.println(
										" Resistance level " + strDate + " from  " + resistLow + " to " + resistHigh);*/
//								System.out.println(printLevel);
								levels.append(printLevel+"\n");
//								isResistAvailble = true;
//							}
						}
					}
					supportLevel = false;
					resistLevel = false;
					isLegOutCandle = false;

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
//			}

			lastCandleHigh = candleHigh;
			lastCandlelow = candlelow;
			lastCandle = currCandle;
			i++;
		}
		return levels;

		/*if (!isSuppAvailble)
			System.out.println("No Support level ");
		if (!isResistAvailble)
			System.out.println("No Resistance level ");*/

	}
	
	static Map retriveAPIdata(String urlString) {
		Map map = new HashMap<>();
		try {
			URL url = new URL(urlString);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			if (conn.getResponseCode() != 200) {
				System.out.println("Failed : HTTP error code : "+ conn.getResponseCode());
//				throw new Exception("Failed : HTTP error code : " + conn.getResponseCode());
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
//		isLegOutCandle = false;
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

			if ((noOfLG >= 2 || supportGap || ("high".equalsIgnoreCase(timeFrame) &&  noOfLG >= 1)))
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

			 if ((noOfLR >= 2 || resistGap || ("high".equalsIgnoreCase(timeFrame) && noOfLR >= 1)))
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
			if ((noOfLG >= 2 || supportGap || ("high".equalsIgnoreCase(timeFrame) && noOfLG >= 1)) && noOfSL < 6 && isLegOutCandle)
				supportLevel = true;
			// gap is considered as 2 large red candles
			else if ((noOfLR >= 2 || resistGap || ("high".equalsIgnoreCase(timeFrame) && noOfLR >= 1)) && noOfSL < 6 && isLegOutCandle)
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
	public static void writeToFile(String pFilename, StringBuffer pData) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(pFilename));
        out.write(pData.toString());
        out.flush();
        out.close();
    }
	
	static void  resetGlobalVariables() {
		lastCandle = "";
		currCandle = "";
		noOfLG = 0;
		noOfLR = 0;
		noOfSL = 0;
		quote = 0.0;
		supportLevel = false;
		isLegOutCandle = false;
		resistLevel = false;
		markLevel = false;
		
	}
	
}
