/**
 * 
 */
package com.api.testing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * @author 525523
 *
 */
public class StockNotification {

//	public static String fileName = "TwoHourLevels";
//	public static String fileName = "Hourlylevels";
	public static String fileName = "niftyStocksLevels";
//	public static String niftyStocksLevelsPath = "E:\\Soosai\\Stocks\\SampleAPItesting-master\\SampleAPItesting-master\\APItesting\\config\\file\\niftyStocksLevels.txt";
	public static String niftyStocksLevelsPath = "D:\\Soosai\\APItesting\\config\\file\\"+fileName+".txt";
	public static String rejectedStocksListPath = "D:\\Soosai\\APItesting\\config\\file\\rejectedStocksList.txt";
	public static int notifyPercent = 3;
	public static int inputLevelPercent = 3;
	private static DecimalFormat df2 = new DecimalFormat("#.##");

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		long starttimefinal = System.currentTimeMillis();
		System.out.println("Start time " + new Date(starttimefinal));
		StringBuffer notificationLevels = new StringBuffer();
		// TODO Auto-generated method stub
		try {
			ArrayList<StockLevels> newShortListedStocks = getNotifications();
			
			Collections.sort(newShortListedStocks, new Comparator<StockLevels>() {
				  public int compare(StockLevels u1, StockLevels u2) {
				    return u1.getNewLevelPercent().compareTo(u2.getNewLevelPercent());
				  }
				});
			
			System.out.println("\nNewShortListedStocks Size " + newShortListedStocks.size() + "\n");
			
			for (StockLevels levels : newShortListedStocks) {
				String notificationLevel = levels.getStockName() + "|" + levels.getDate()  + "|" +levels.getLevelType() +
						"|" + levels.getOldLevel()+ "|" + levels.getOldLevelEnd()
				+ "|" + levels.getOldLevelPercent() + "|" + levels.getNewLevel() + "|"
				+ df2.format(levels.getNewLevelPercent());
				System.out.println(notificationLevel);
				notificationLevels.append(notificationLevel+"\n");

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			String notifyFile = "D:\\Soosai\\APItesting\\config\\file\\Notify"+fileName+".txt";
			writeToFile(notifyFile,notificationLevels);
//			writeToFile("E:\\Soosai\\Stocks\\SampleAPItesting-master\\SampleAPItesting-master\\APItesting\\config\\file\\notifyNiftyDailyLevels.txt",notificationLevels);
		}
		long endtimefinal = System.currentTimeMillis();
		System.out.println("\nEnd time " + new Date(endtimefinal));
		System.out.println("\nTotal Time Taken " + (endtimefinal - starttimefinal));

	}

	public static ArrayList<StockLevels> getNotifications() throws Exception {

		double quote;
		ArrayList<StockLevels> shortListedStocks = getshortListedStocks();
		ArrayList<StockLevels> newShortListedStocks = new ArrayList<StockLevels>();
		ArrayList<StockLevels> rejectedListStocks = getRejectListStocks();
//		shortListedStocks.removeAll(rejectedListStocks);
		int i = 0;
		long starttime = System.currentTimeMillis();
		long endtime;
		long timetaken;
		if (!shortListedStocks.isEmpty()) {
			System.out.println("\nShortListed levels Size" + shortListedStocks.size());
			int listSize = shortListedStocks.size();
			for (StockLevels levels : shortListedStocks) {
				boolean toProceed = true;
				String stockName = levels.getStockName();
				String levelType = levels.getLevelType();
				Double oldLevel =  levels.getOldLevel();
				Double oldLevelEnd = levels.getOldLevelEnd();
				for (StockLevels rejectedLevels : rejectedListStocks) {
					if (stockName.equalsIgnoreCase(rejectedLevels.getStockName())
							&& levelType.equalsIgnoreCase(rejectedLevels.getLevelType())
							&& oldLevel.equals(rejectedLevels.getOldLevel())
							&& oldLevelEnd.equals(rejectedLevels.getOldLevelEnd())) {
						
						toProceed = false;
						break;
					}

				}
				if(toProceed) {
				quote = 0.0;
				if(inputLevelPercent == notifyPercent) {
					levels.setNewLevel(levels.getOldLevel());
					levels.setNewLevelPercent(levels.getOldLevelPercent());
					newShortListedStocks.add(levels);
					
				}
				else {
					
				
				
				quote = getQuote(levels.getStockName(), "&apikey=F4ASHUF1BONNF5AQ");
				double newLevelPercent;
				if(levels.getLevelType().equalsIgnoreCase("Support")) {
					newLevelPercent = ((quote - levels.getOldLevel()) * 100 / quote);
				}else {
					newLevelPercent = ((levels.getOldLevel() - quote) * 100 / quote);
				}
				
				// System.out.println(newLevelPercent);
				if (newLevelPercent < notifyPercent) {
					levels.setNewLevel(quote);
					levels.setNewLevelPercent(newLevelPercent);
					newShortListedStocks.add(levels);
				}
				i++;
				endtime = System.currentTimeMillis();
				timetaken = endtime - starttime;
				if (timetaken < 60000 && (i % 5 == 0) && i < listSize) {
					System.out.println("Time taken & Wait time ..." + timetaken + " & " + (61000 - timetaken));
					Thread.sleep(61000 - timetaken);
					starttime = System.currentTimeMillis();
				}
				}
			}
			}

		}
		return newShortListedStocks;

	}

	static double getQuote(String symbol, String apiKey) throws Exception {
		Double quote = 0.0;
		String fnName = "GLOBAL_QUOTE";
		String urlString = formURL(fnName, symbol, apiKey);
		Map map = retriveAPIdata(urlString);
		if (map.containsKey("Note")) {
			System.out.println("Note : " + map.get("Note"));
		} else {
			Map map2 = (Map) map.get("Global Quote");
			if (null != map2) {
				quote = Double.parseDouble(map2.get("05. price").toString());
			}
		}

		map.clear();
		return quote;

	}

	static String formURL(String fnName, String symbol, String apiKey) {
		String urlBase = "https://www.alphavantage.co/query?function=";
		String outputsize = "&outputsize=full";
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
				System.out.println("Failed : HTTP error code : " + conn.getResponseCode());

			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			String output;
			StringBuffer buffer = new StringBuffer();
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

	public static ArrayList<StockLevels> getshortListedStocks() {
		BufferedReader br = null;
		FileReader fr = null;
		ArrayList<StockLevels> shortListedStocks = new ArrayList<>();

		try {

			fr = new FileReader(niftyStocksLevelsPath);
			br = new BufferedReader(fr);

			// read line by line
			String line;

			while ((line = br.readLine()) != null) {
				// System.out.println(line);
				String[] splitLines = line.split("\\|");
				StockLevels levels = new StockLevels();
				levels.setStockName(splitLines[0]);
				levels.setDate(splitLines[1]);
				levels.setLevelType(splitLines[2]);
				levels.setOldLevel(Double.valueOf(splitLines[3]));
				levels.setOldLevelEnd((Double.valueOf(splitLines[4])));
				levels.setOldLevelPercent(Double.valueOf(splitLines[5]));
				if (levels.getOldLevelPercent() < inputLevelPercent) {
					shortListedStocks.add(levels);
				}
			}

		} catch (IOException e) {
			System.err.format("IOException: %s%n", e);
		} finally {
			try {
				if (br != null)
					br.close();

				if (fr != null)
					fr.close();
			} catch (IOException ex) {
				System.err.format("IOException: %s%n", ex);
			}
		}
		return shortListedStocks;
	}
	
	public static void writeToFile(String pFilename, StringBuffer pData) throws IOException {
        BufferedWriter out = new BufferedWriter(new FileWriter(pFilename));
        out.write(pData.toString());
        out.flush();
        out.close();
    }
	
	public static ArrayList<StockLevels> getRejectListStocks() {
		BufferedReader br = null;
		FileReader fr = null;
		ArrayList<StockLevels> rejectListStocks = new ArrayList<>();

		try {

			fr = new FileReader(rejectedStocksListPath);
			br = new BufferedReader(fr);

			// read line by line
			String line;

			while ((line = br.readLine()) != null) {
				// System.out.println(line);
				String[] splitLines = line.split("\\|");
				StockLevels levels = new StockLevels();
				levels.setStockName(splitLines[0]);
				levels.setDate(splitLines[1]);
				levels.setLevelType(splitLines[2]);
				levels.setOldLevel(Double.valueOf(splitLines[3]));
				levels.setOldLevelEnd((Double.valueOf(splitLines[4])));
				levels.setOldLevelPercent(Double.valueOf(splitLines[5]));
				rejectListStocks.add(levels);
			}

		} catch (IOException e) {
			System.err.format("IOException: %s%n", e);
		} finally {
			try {
				if (br != null)
					br.close();

				if (fr != null)
					fr.close();
			} catch (IOException ex) {
				System.err.format("IOException: %s%n", ex);
			}
		}
		return rejectListStocks;
	}


}
