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
import java.util.Map.Entry;
import java.util.TreeMap;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * @author 525523
 *
 */
public class StockNotification {

	// public static String fileName = "TwoHourLevels";
	// public static String fileName = "Hourlylevels";
	public static String fileName = "niftyStocksLevels";
	public static String path = "D:\\Soosai\\APItesting\\config\\file\\";
	// public static String niftyStocksLevelsPath =
	// "E:\\Soosai\\Stocks\\SampleAPItesting-master\\SampleAPItesting-master\\APItesting\\config\\file\\niftyStocksLevels.txt";
	public static String niftyStocksLevelsPath = path + fileName + ".txt";
	public static String rejectedStocksListPath = path + "rejectedStocksList.txt";
	public static String niftyStocksMonthlyLevelPath = path + "niftyStocksMonthlyLevels.txt";
	public static String niftyStocksWeeklyLevelPath = path + "niftyStocksMonthlyLevels.txt";
	public static String niftyMonthlyTrendListPath = path + "niftyStocksMonthlyTrend.txt";
	public static String niftyWeeklylyTrendListPath = path + "niftyStocksWeeklyTrend.txt";
	public static String notifyFile = path + "Notify" + fileName + ".txt";
	public static int notifyPercent = 5;
	public static int inputLevelPercent = 5;
	private static DecimalFormat df2 = new DecimalFormat("#.##");
	private static int i = 0;
	private static long starttime;
	private static long endtime;

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		getStocksNotification();
		// getNiftyStocksMonthlyLevels();

	}

	private static void getStocksNotification() throws IOException {
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
				String notificationLevel = levels.getStockName() + "|" + levels.getDate() + "|" + levels.getLevelType()
						+ "|" + levels.getOldLevel() + "|" + levels.getOldLevelEnd() + "|" + levels.getOldLevelPercent()
						+ "|" + levels.getNewLevel() + "|" + df2.format(levels.getNewLevelPercent()) + "|"
						+ Double.valueOf(levels.getScore());
				System.out.println(notificationLevel);
				notificationLevels.append(notificationLevel + "\n");

			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {

			writeToFile(notifyFile, notificationLevels);
			// writeToFile("E:\\Soosai\\Stocks\\SampleAPItesting-master\\SampleAPItesting-master\\APItesting\\config\\file\\notifyNiftyDailyLevels.txt",notificationLevels);
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
		TreeMap<String, MnthlyLvlStockDetail> curveLvlMap;
		if(fileName.equalsIgnoreCase("niftyStocksLevels")) {
			curveLvlMap = getNiftyStocksMonthlyLevels(niftyStocksMonthlyLevelPath);			
		}else {
			curveLvlMap = getNiftyStocksMonthlyLevels(niftyStocksMonthlyLevelPath);			
		}
		
		TreeMap<String, String> monthlyTrendMap = getNiftyTrends(niftyMonthlyTrendListPath);
		TreeMap<String, String> weeklyTrendMap = getNiftyTrends(niftyWeeklylyTrendListPath);

		// shortListedStocks.removeAll(rejectedListStocks);

		starttime = System.currentTimeMillis();		
		long timetaken;
		if (!shortListedStocks.isEmpty()) {
			System.out.println("\nShortListed levels Size" + shortListedStocks.size());
			int listSize = shortListedStocks.size();
			for (StockLevels levels : shortListedStocks) {
				boolean toProceed = true;
				String stockName = levels.getStockName();
				String levelType = levels.getLevelType();
				Double oldLevel = levels.getOldLevel();
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
				if (toProceed) {
					quote = 0.0;
					if (inputLevelPercent == notifyPercent) {
						levels.setNewLevel(levels.getOldLevel());
						levels.setNewLevelPercent(levels.getOldLevelPercent());

					} else {

						quote = getQuote(levels.getStockName(), "&apikey=F4ASHUF1BONNF5AQ");
						double newLevelPercent;
						if (levels.getLevelType().equalsIgnoreCase("Support")) {
							newLevelPercent = ((quote - levels.getOldLevel()) * 100 / quote);
						} else {
							newLevelPercent = ((levels.getOldLevel() - quote) * 100 / quote);
						}
						// System.out.println(newLevelPercent);
						if (newLevelPercent < notifyPercent) {
							levels.setNewLevel(quote);
							levels.setNewLevelPercent(newLevelPercent);

						} else {
							continue;
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
					MnthlyLvlStockDetail curveLvlDetail = curveLvlMap.get(stockName);
					int curveScore = getCurveScore(curveLvlDetail, oldLevel, levelType);
					int trendScore = getTrendScore(monthlyTrendMap, weeklyTrendMap, stockName, levelType);
					levels.setScore(curveScore + trendScore);
					System.out.println(curveLvlDetail.getStockName() + "|" + curveScore + trendScore);
					newShortListedStocks.add(levels);
				}

			}

		}
		return newShortListedStocks;

	}

	private static int getTrendScore(TreeMap<String, String> monthlyTrendMap, TreeMap<String, String> weeklyTrendMap,
			String stockName, String levelType) {
		String monthlyTrend = monthlyTrendMap.get(stockName);
		String weeklyTrend = weeklyTrendMap.get(stockName);
		int score = 0;
		long timetaken;
		try {
			if (fileName.equalsIgnoreCase("niftyStocksLevels")) {
				if (levelType.equalsIgnoreCase("Resistance") && monthlyTrend.equalsIgnoreCase("Down")) {
					score = score + 1;
				} else if (levelType.equalsIgnoreCase("Support") && monthlyTrend.equalsIgnoreCase("Up")) {
					score = score + 1;
				}
			} else {
				String hourlyTrend = IdentifyTrend.identifyTrend(stockName, "60mins");
				if (levelType.equalsIgnoreCase("Resistance")) {

					if (hourlyTrend.equalsIgnoreCase("Down")) {
						score = score + 1;
					}

				} else if (levelType.equalsIgnoreCase("Support")) {

					if (hourlyTrend.equalsIgnoreCase("Up")) {
						score = score + 1;
					}

				}
				i++;
				endtime = System.currentTimeMillis();
				timetaken = endtime - starttime;
				if (timetaken < 60000 && (i % 5 == 0) ) {
					System.out.println("Time taken & Wait time ..." + timetaken + " & " + (61000 - timetaken));
					Thread.sleep(61000 - timetaken);
					starttime = System.currentTimeMillis();
				}

			}

			String dailyTrend = IdentifyTrend.identifyTrend(stockName, "daily");
			i++;
			endtime = System.currentTimeMillis();
			timetaken = endtime - starttime;
			if (timetaken < 60000 && (i % 5 == 0) ) {
				System.out.println("Time taken & Wait time ..." + timetaken + " & " + (61000 - timetaken));
				Thread.sleep(61000 - timetaken);
				starttime = System.currentTimeMillis();
			}

			if (levelType.equalsIgnoreCase("Resistance")) {
				if (weeklyTrend.equalsIgnoreCase("Down")) {
					score = score + 1;
				}
				if (dailyTrend.equalsIgnoreCase("Down")) {
					score = score + 1;
				}

			} else if (levelType.equalsIgnoreCase("Support")) {
				if (weeklyTrend.equalsIgnoreCase("Up")) {
					score = score + 1;
				}

				if (dailyTrend.equalsIgnoreCase("Up")) {
					score = score + 1;
				}

			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return score;
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

	private static TreeMap<String, MnthlyLvlStockDetail> getNiftyStocksMonthlyLevels(String levelPath) {

		BufferedReader br = null;
		FileReader fr = null;
		TreeMap<String, MnthlyLvlStockDetail> mnthlyLvlStockDetailsMap = new TreeMap<String, MnthlyLvlStockDetail>();

		try {

			fr = new FileReader(levelPath);
			br = new BufferedReader(fr);

			// read line by line
			String line;

			while ((line = br.readLine()) != null) {
				// System.out.println(line);
				String[] splitLines = line.split("\\|");
				if (mnthlyLvlStockDetailsMap.containsKey(splitLines[0])) {

					MnthlyLvlStockDetail monthlyLvlDetail = mnthlyLvlStockDetailsMap.get(splitLines[0]);

					setMnthlyLvlStockDetail(splitLines, monthlyLvlDetail);

				} else {
					MnthlyLvlStockDetail monthlyLvlDetail = new MnthlyLvlStockDetail();
					monthlyLvlDetail.setStockName(splitLines[0]);
					setMnthlyLvlStockDetail(splitLines, monthlyLvlDetail);

					mnthlyLvlStockDetailsMap.put(splitLines[0], monthlyLvlDetail);
				}

			}

			System.out.println("mnthlyLvlStockDetailsMap Size" + mnthlyLvlStockDetailsMap.size());
			/*
			 * for (Entry<String, MnthlyLvlStockDetail> entry1 :
			 * mnthlyLvlStockDetailsMap.entrySet()) { System.out.println(entry1.getKey() +
			 * "| Support Date - " + entry1.getValue().getSupportDate() +
			 * "| curve Low start - " + entry1.getValue().getCurveLowStart() +
			 * "| curve Low end - " + entry1.getValue().getCurveLowEnd() +
			 * "| Resistance date - " + entry1.getValue().getResistanceDate() +
			 * "| curve High start - " + entry1.getValue().getCurveHighStart() +
			 * "| curve High end - " + entry1.getValue().getCurveHighEnd() + "| Score " +
			 * entry1.getValue().getScore());
			 * 
			 * }
			 */

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
		return mnthlyLvlStockDetailsMap;

	}

	private static void setMnthlyLvlStockDetail(String[] splitLines, MnthlyLvlStockDetail monthlyLvlDetail) {

		if (splitLines[1].equalsIgnoreCase("No Support") || splitLines[1].equalsIgnoreCase("No Resistance")) {

			if (splitLines[1].equalsIgnoreCase("No Support")) {
				monthlyLvlDetail.setCurveLowStart(Double.valueOf("0"));
				monthlyLvlDetail.setCurveLowEnd(Double.valueOf("0"));

			} else {
				monthlyLvlDetail.setCurveHighStart(Double.valueOf("0"));
				monthlyLvlDetail.setCurveHighEnd(Double.valueOf("0"));

			}

			monthlyLvlDetail.setScore(1);

		} else {
			String date = splitLines[1];
			String levelType = splitLines[2];
			if (levelType.equalsIgnoreCase("Support")) {
				monthlyLvlDetail.setSupportDate(date);
				monthlyLvlDetail.setCurveLowStart(Double.valueOf(splitLines[3]));
				monthlyLvlDetail.setCurveLowEnd(Double.valueOf(splitLines[4]));

			} else {
				monthlyLvlDetail.setResistanceDate(date);
				monthlyLvlDetail.setCurveHighStart(Double.valueOf(splitLines[3]));
				monthlyLvlDetail.setCurveHighEnd(Double.valueOf(splitLines[4]));

			}
			if (0 != monthlyLvlDetail.getScore() && monthlyLvlDetail.getScore() == 1) {
				monthlyLvlDetail.setScore(1);
			} else {
				monthlyLvlDetail.setScore(0);
			}

		}
	}

	private static int getCurveScore(MnthlyLvlStockDetail monthlyLvlDetail, Double level, String levelType) {

		int score = 0;
		Double curveLowStart = monthlyLvlDetail.getCurveLowStart();
		Double curveLowEnd = monthlyLvlDetail.getCurveLowEnd();
		Double curveHighStart = monthlyLvlDetail.getCurveHighStart();
		Double curveHighEnd = monthlyLvlDetail.getCurveHighEnd();
		Double curveSection = (curveHighStart - curveLowStart) / 3;
		Double curve1 = curveLowStart + curveSection;
		Double curve2 = curve1 + curveSection;

		if (monthlyLvlDetail != null && 0 != monthlyLvlDetail.getScore()) {
			if (monthlyLvlDetail.getScore() == 1) {
				score = monthlyLvlDetail.getScore();
			} else {

				if (levelType.equalsIgnoreCase("Support") && (curveLowEnd <= level && level <= curveLowStart)) {
					score = 2;

				} else if (levelType.equalsIgnoreCase("Resistance")
						&& (curveHighStart <= level && level <= curveHighEnd)) {
					score = 2;

				} else if (levelType.equalsIgnoreCase("Support") && (curveLowStart <= level && level <= curve1)) {
					score = 2;
				} else if (levelType.equalsIgnoreCase("Resistance") && (curve2 <= level && level <= curveHighStart)) {
					score = 2;

				} else if (curve1 <= level && level <= curve2) {
					score = 1;

				} else {
					score = 0;
				}

			}
		}
		return score;

	}

	private static TreeMap<String, String> getNiftyTrends(String fileName) {

		BufferedReader br = null;
		FileReader fr = null;
		TreeMap<String, String> niftyTrendList = new TreeMap<String, String>();

		try {

			fr = new FileReader(fileName);
			br = new BufferedReader(fr);

			// read line by line
			String line;

			while ((line = br.readLine()) != null) {
				// System.out.println(line);
				String[] splitLines = line.split("\\|");

				niftyTrendList.put(splitLines[0], splitLines[1]);
			}

			System.out.println("mnthlyLvlStockDetailsMap Size" + niftyTrendList.size());

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
		return niftyTrendList;

	}

}
