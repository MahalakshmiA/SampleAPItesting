package com.api.testing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


public class ReadStocks {
	
	private static String filePath = "E:\\Soosai\\Stocks\\SampleAPItesting-master\\SampleAPItesting-master\\APItesting\\config\\file\\";
//	private static String filePath = "D:\\Soosai\\APItesting\\config\\file\\";

	public static void main(String[] args) {
		getAllStocksMap();
		
	}

	/**
	 * 
	 */
	public static Map<String, ArrayList<String>> getAllStocksMap() {
		

		Map<String, ArrayList<String>> allStocksMap = new HashMap<String, ArrayList<String>>();
		ArrayList<String> allStocksList = new ArrayList<String> ();
		// System.out.println(getFile("D:/file/test.txt"));
		ArrayList<String> megacapList = readStocksFile(filePath+"MegaCapStocks.txt");
		System.out.println("megacapList Size" + megacapList.size());
		allStocksMap.put("MEGACAP", megacapList);
		
		ArrayList<String> largeCapList = readStocksFile(filePath+"LargeCapStocks.txt");
		System.out.println("largeCapList Size" + largeCapList.size());
		allStocksMap.put("LARGECAP", largeCapList);
		
		ArrayList<String> midCapList = readStocksFile(filePath+"MidCapStocks.txt");
		System.out.println("midCapList Size" + midCapList.size());
		allStocksMap.put("MIDCAP", midCapList);
		
		ArrayList<String> smallCapList = readStocksFile(filePath+"SmallCapStocks.txt");
		System.out.println("smallCapList Size" + smallCapList.size());
		allStocksMap.put("SMALLCAP", smallCapList);
		
		ArrayList<String> niftyList = readStocksFile(filePath+"niftyStocks.txt");
		System.out.println("niftyList Size" + niftyList.size());
		allStocksMap.put("NIFTY", niftyList);

		System.out.println(allStocksMap.size());
		
		allStocksList.addAll(megacapList);
		allStocksList.addAll(largeCapList);
		allStocksList.addAll(midCapList);
		allStocksList.addAll(smallCapList);
		
		System.out.println("All Stocks List Size" + allStocksList.size());	
		
		for (String s: allStocksMap.get("NIFTY")){
			System.out.println(s);
		}
		return allStocksMap;
	}
	
	public static ArrayList<String> getIndexStocksList(String indexName) {
		return readStocksFile(filePath+indexName+".txt");
		
	}

	/**
	 * @param fileName
	 * @param megaCapStockList
	 */
	private static ArrayList<String> readStocksFile(String fileName) {
		File file = new File(fileName);
		ArrayList<String> stockList = new ArrayList<String>();

		try {
			Scanner scanner = new Scanner(file);

			while (scanner.hasNext()) {
				String line = scanner.nextLine();
				if (line.endsWith(".NS") || "^NSEI".equalsIgnoreCase(line)) {
					stockList.add(line);

				}else if (line.endsWith(".BO")) {
					

				}else{
					stockList.add(line+".NS");
				}
			}

			scanner.close();
			Collections.sort(stockList);
			System.out.println("No of Stock in  " + fileName + "  :" +stockList.size());

		} catch (IOException e) {
			e.printStackTrace();
		}
		return stockList;
	}



}
