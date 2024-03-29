package com.api.testing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.Gson;
import com.sample.Timezone;


public class IdentifyTrend {
	
	private static StringBuffer trendList = new StringBuffer();
	public static String fileName = "niftyStocksLevels";
//	public static String path = "D:\\Soosai\\APItesting\\config\\file\\";
	public static String path = "E:\\Soosai\\Stocks\\SampleAPItesting-master\\SampleAPItesting-master\\APItesting\\config\\file\\";
	public static void main(String[] args) throws IOException {
		
		long starttimefinal = System.currentTimeMillis();
		long endtimefinal;
		System.out.println("Start time " +  new Date(starttimefinal));	
		int i = 0;
		long starttime = System.currentTimeMillis();
		long endtime;
		long timetaken;
		String timeFrame = "monthly";
		
		try {
			for (String symbol : ReadStocks.getIndexStocksList("MegaCapStocks")) {
				identifyTrend(symbol,timeFrame);
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
			String outputFile  = path+ "niftyStocks"+timeFrame+"Trend.txt";
			NiftyStocksDailyLevels2.writeToFile(outputFile,trendList);
			}
	}


	public static String identifyTrend(String symbol,String timeframe) {
		String trend = "";
		try {
//			HashMap<String, String> map1 = new HashMap<>();
			String interval="&interval="+timeframe;
			String time_period = "&time_period=50";
			String series_type="&series_type=close";
			String urlString = formURL("SMA", symbol,interval,time_period,series_type, "&apikey=O959V1I2ZMN9KIBK");
//			System.out.println("URL " +urlString);
			Map map =NiftyStocksDailyLevels2.retriveAPIdata(urlString);
					
			if (map.containsKey("Note")) {
				System.out.println("Note : " + map.get("Note"));
			} else {
				Map map1 =  (Map) map.get("Technical Analysis: SMA");
				if (null != map1) {					
					if(isAscendingOrdered(map1)) {
						System.out.println(symbol + " UP Trend ");
						trendList.append(symbol+"|UP\n");
						trend = "UP";
						
					}else if (isDescendingOrdered(map1)) {
						System.out.println(symbol +" DOWN Trend ");		
						trendList.append(symbol+"|DOWN\n");
						trend = "DOWN";
					}else {
						System.out.println(symbol +" NO Trend ");
						trendList.append(symbol+"|NO\n");
						trend = "NO";
					}
					
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return trend;
	}

	
	static String formURL(String fnName, String symbol,String interval,String time_period,String series_type, String apiKey) {
		String urlBase = "https://www.alphavantage.co/query?function=";		
		String urlString = urlBase + fnName + "&symbol=" + symbol + interval+time_period+series_type + apiKey;
		return urlString;
	}
	
	static boolean isAscendingOrdered(Map map) {
		Set keys =  map.keySet();
		Iterator it = keys.iterator();
		int i=0;
	    if (it.hasNext()) {
	      Map map1 = (Map)map.get((String) it.next());
	      Double prev = Double.parseDouble(map1.get("SMA").toString());	     
//	      System.out.println("SMA " + prev);
	      while (it.hasNext()) {
	    	  map1 = (Map)map.get((String) it.next());
		      Double next = Double.parseDouble(map1.get("SMA").toString());		    	 
//	    	  System.out.println("next " +next);
	        if (prev <= next) {
	          return false;
	        }
	        prev = next;
	        i++;
	        if (i== 7)
		    	  break;
	      }	     
	    }
	    return true;
	  }
	
static boolean isDescendingOrdered(Map map) {
		
	Set keys =  map.keySet();
	Iterator it = keys.iterator();
	int i=0;
    if (it.hasNext()) {
      Map map1 = (Map)map.get((String) it.next());
      Double prev = Double.parseDouble(map1.get("SMA").toString());	     
//      System.out.println("SMA " + prev);
      while (it.hasNext()) {
    	  map1 = (Map)map.get((String) it.next());
	      Double next = Double.parseDouble(map1.get("SMA").toString());		    	 
//    	  System.out.println("next " +next);
        if (prev >= next) {
          return false;
        }
        prev = next;
        i++;
        if (i== 7)
	    	  break;
      }	     
    }
    return true;
	  }
	
	
	
}
