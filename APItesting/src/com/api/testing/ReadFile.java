package com.api.testing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ReadFile {

	public static void main(String[] args) {

		Map<String, ArrayList<String>> allStocksMap = new HashMap<String, ArrayList<String>>();
		// System.out.println(getFile("D:/file/test.txt"));
		ArrayList<String> megacapList = readStocksFile("D:/file/MegaCapStocks.txt");
		allStocksMap.put("MEGACAP", megacapList);
		
		ArrayList<String> largeCapList = readStocksFile("D:/file/LargeCapStocks.txt");
		allStocksMap.put("LARGECAP", largeCapList);
		
		ArrayList<String> midCapList = readStocksFile("D:/file/MidCapStocks.txt");
		allStocksMap.put("MIDCAP", midCapList);
		
		ArrayList<String> smallCapList = readStocksFile("D:/file/SmallCapStocks.txt");
		allStocksMap.put("SMALLCAP", smallCapList);
		
		ArrayList<String> niftyList = readStocksFile("D:/file/niftyStocks.txt");
		allStocksMap.put("NIFTY", niftyList);

		System.out.println(allStocksMap.size());
	}

	public static ArrayList<String> readMegaCapStocks(String fileName) {

		ArrayList<String> megacapStockList = readStocksFile(fileName);
		System.out.println("megacapStockList Size" + megacapStockList.size());
		return megacapStockList;

	}
	
	public static ArrayList<String> readLargeCapStocks(String fileName) {

		ArrayList<String> largecapStockList = readStocksFile(fileName);
		System.out.println("largecapStockList Size" + largecapStockList.size());
		return largecapStockList;

	}
	
	public static ArrayList<String> readMidCapStocks(String fileName) {

		ArrayList<String> midcapStockList = readStocksFile(fileName);
		System.out.println("midcapStockList Size" + midcapStockList.size());
		return midcapStockList;

	}
	
	public static ArrayList<String> readSmallCapStocks(String fileName) {

		ArrayList<String> smallcapStockList = readStocksFile(fileName);
		System.out.println("smallcapStockList Size" + smallcapStockList.size());
		return smallcapStockList;

	}
	
	public static ArrayList<String> readNiftyStocks(String fileName) {

		ArrayList<String> niftyStockList = readStocksFile(fileName);
		System.out.println("niftyStockList Size" + niftyStockList.size());
		return niftyStockList;

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
				if (line.endsWith(".NS")) {
					stockList.add(line);

				}else if (line.endsWith(".BO")) {
					stockList.add(line);

				}else{
					stockList.add(line+".NS");
				}
			}

			scanner.close();
			System.out.println(stockList.size());
			/*for (String s : stockList) {
				System.out.println("S ->" + s);
			}*/

		} catch (IOException e) {
			e.printStackTrace();
		}
		return stockList;
	}

}