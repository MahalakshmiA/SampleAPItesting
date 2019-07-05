package com.api.testing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.sample.Timezone;

public class IdentifyTrend {
	
	public static void main(String[] args) {
		
		try {
			HashMap<String, String> map1 = new HashMap<>();
			String interval="&interval=weekly";
			String time_period = "&time_period=50";
			String series_type="&series_type=close";
			String urlString = formURL("SMA", "^NSEI",interval,time_period,series_type, "&apikey=F4ASHUF1BONNF5AQ");
//			https://www.alphavantage.co/query?function=SMA&symbol=MSFT&interval=weekly&time_period=10&series_type=open&apikey=demo
//				https://www.alphavantage.co/query?function=SMA&symbol=^NSEIweekly14close&apikey=F4ASHUF1BONNF5AQ
			System.out.println("URL " +urlString);
			Map map =NiftyStocksDailyLevels2.retriveAPIdata(urlString);
					
			if (map.containsKey("Note")) {
				System.out.println("Note : " + map.get("Note"));
			} else {
				map1 = (HashMap<String, String>) map.get("Technical Analysis: SMA");
				if (null != map1) {
					/*for (Entry<String, Timezone> entry : map2.keySet()) {
						
					}*/
					if(map1.containsKey("2019-06-28")){
						System.out.println(map1.get("2019-06-28"));
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	static String formURL(String fnName, String symbol,String interval,String time_period,String series_type, String apiKey) {
		String urlBase = "https://www.alphavantage.co/query?function=";		
		String urlString = urlBase + fnName + "&symbol=" + symbol + interval+time_period+series_type + apiKey;
		return urlString;
	}
	
	public boolean isOrdered(ArrayList iterable) {
		Iterator it = iterable.iterator();
	    if (it.hasNext()) {
	      String prev = (String) it.next();
	      while (it.hasNext()) {
	    	  String next = (String) it.next();
	        if (!prev.equalsIgnoreCase(next)) {
	          return false;
	        }
	        prev = next;
	      }
	    }
	    return true;
	  }
	
	
}
