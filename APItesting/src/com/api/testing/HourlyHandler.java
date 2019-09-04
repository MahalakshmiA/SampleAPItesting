/**
 * 
 */
package com.api.testing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * @author 525523
 *
 */
public class HourlyHandler {

	public static final String DATE_FORMAT_YYYYMMDD_WITH_SS = "yyyy-MM-dd HH:mm:ss";
	public static final String DATE_FORMAT_YYYYMMDD = "yyyy-MM-dd";
	public static final int endTimeinHrs = 15;
	private static final int minutestoAdd = 60;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String symbol = "^NSEI";
		String interval = "60min";
		boolean nseStock = true;
		boolean is120 = true;
		try {
			TreeMap<String, Details> sortedCandlesList = getCandlesList(symbol, interval);
			getHourlyCandlesList(sortedCandlesList, nseStock, is120);
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

	public static TreeMap<String, Details> getHourlyCandlesList(TreeMap<String, Details> sortedCandlesList, boolean nseStock,
			boolean is120) {
		TreeMap<String, Details> consolidatedHrlyCandlesList = new TreeMap<String, Details>();
		try {
			
			int noOfDaysToBeCalc = 200;

			String ToDate = getCurrentDateAsString();
			String fromDate = getEndDate(ToDate, -noOfDaysToBeCalc);

			String fromDateWithMins = addStartHrsMins(fromDate);
			String endDateWithMins = addEndHrsMins(ToDate, endTimeinHrs);
			TreeMap<String, Details> istcandlesList = new TreeMap<String, Details>();
			if (nseStock) {
				istcandlesList = getistcandlesList(sortedCandlesList, fromDateWithMins, endDateWithMins);
				consolidatedHrlyCandlesList = getHourlyCandles(fromDateWithMins, endDateWithMins, istcandlesList,
						is120);

			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return consolidatedHrlyCandlesList;

	}

	public static TreeMap<String, Details> getCandlesList(String symbol, String interval)
			throws NoSuchAlgorithmException, KeyManagementException, ParseException {

		TreeMap<String, Details> sortedCandlesList = new TreeMap<String, Details>();
		// Create a trust manager that does not validate certificate chains
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		} };

		// Install the all-trusting trust manager
		SSLContext sc = SSLContext.getInstance("SSL");
		sc.init(null, trustAllCerts, new java.security.SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

		// Create all-trusting host name verifier
		HostnameVerifier allHostsValid = new HostnameVerifier() {
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};

		try {
			// Install the all-trusting host verifier
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
			String urlString = "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=" + symbol
					+ "&interval=" + interval + "&outputsize=full&apikey=O959V1I2ZMN9KIBK";
			MyDeserializer deserializer = new MyDeserializer();
			deserializer.setTimezoneList_key("Time Series (" + interval + ")");

			URL url = new URL(urlString);

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");

			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}

			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			StringBuilder sb = new StringBuilder();

			String output;
//			System.out.println("Output from Server .... \n");
			while ((output = br.readLine()) != null) {

				sb.append(output);
			}
			// System.out.println(sb.toString());
			JsonParser parser = new JsonParser();
			JsonElement jsonElement = parser.parse(sb.toString());
			JsonObject rootObject = jsonElement.getAsJsonObject();

			Gson gson = new Gson();
			Gson gson2 = new GsonBuilder().registerTypeAdapter(OutputObj.class, new MyDeserializer()).create();

			OutputObj obj = gson2.fromJson(sb.toString(), OutputObj.class);

			// Sort the objects
			TreeMap<String, Timezone> sortedtimezoneList = new TreeMap<String, Timezone>();

			// Copy all data from hashMap into TreeMap
			sortedtimezoneList.putAll(obj.getTimezoneList());

			// Sort the objects

			for (Entry<String, Timezone> entry : sortedtimezoneList.entrySet()) {

				Details details = new Details();

				double close = Double.parseDouble(entry.getValue().getClose());
				double open = Double.parseDouble(entry.getValue().getOpen());
				double high = Double.parseDouble(entry.getValue().getHigh());
				double low = Double.parseDouble(entry.getValue().getLow());

				details.setOpen(String.valueOf(open));
				details.setClose(String.valueOf(close));
				details.setHigh(String.valueOf(high));
				details.setLow(String.valueOf(low));


				// Copy all data from hashMap into TreeMap
				sortedCandlesList.put(entry.getKey(), details);

			}

			conn.disconnect();

		} catch (MalformedURLException e) {

			e.printStackTrace();

		} catch (IOException e) {

			e.printStackTrace();

		}
		return sortedCandlesList;
	}

	public static TreeMap<String, Details> getistcandlesList(TreeMap<String, Details> sortedCandlesList,
			String fromDateWithMins, String endDateWithMins) {

		TreeMap<String, Details> istCandleList = new TreeMap<String, Details>();
		try {
			Date fromDateWithMinsDtFormat = getDate(fromDateWithMins);

			Date endDateWithMinsDtFormat = getDate(endDateWithMins);

			for (Entry<String, Details> entry : sortedCandlesList.entrySet()) {

				String istTimeStrFormat = getIstTime(entry.getKey());
				Date istTimeDtFormat = getDate(istTimeStrFormat);

				if ((istTimeDtFormat.equals(fromDateWithMinsDtFormat))
						|| (istTimeDtFormat.after(fromDateWithMinsDtFormat))
								&& ((istTimeDtFormat.before(endDateWithMinsDtFormat)
										|| (istTimeDtFormat.equals(endDateWithMinsDtFormat))))) {
					istCandleList.put(istTimeStrFormat, entry.getValue());
				}
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return istCandleList;

	}

	/**
	 * @throws ParseException
	 * 
	 */
	private static TreeMap<String, Details> getHourlyCandles(String fromDateWithMins, String endDateWithMins,
			TreeMap<String, Details> istCandlesList, boolean is120) throws ParseException {
		TreeMap<String, Details> consolidatedHrlyCandlesList = new TreeMap<String, Details>(Collections.reverseOrder());
		if (is120) {
			TreeMap<String, String> nseStartEndDateKeys = new TreeMap<String, String>();
			nseStartEndDateKeys = getNSEStarEndDateKeys(fromDateWithMins, endDateWithMins, minutestoAdd);

			consolidatedHrlyCandlesList.putAll(getNSEConsolidatedHighLowList(istCandlesList, nseStartEndDateKeys));

		} else {
			ArrayList<String> nseStartTimeList = new ArrayList<String>();
			nseStartTimeList = getNSEStartDateList(fromDateWithMins, endDateWithMins, minutestoAdd);
			for (Entry<String, Details> entry : istCandlesList.entrySet()) {
				if (nseStartTimeList.contains(entry.getKey())) {
					consolidatedHrlyCandlesList.put(entry.getKey(), entry.getValue());
				}

			}
		}
//		System.out.println("consolidatedHighLowList size" + consolidatedHrlyCandlesList.size());
		for (Entry<String, Details> entry : consolidatedHrlyCandlesList.entrySet()) {
			/*System.out.println(entry.getKey() + "| High - " + entry.getValue().getHigh() + "| Low - "
					+ entry.getValue().getLow() + "| Open - " + entry.getValue().getOpen() + "| Close - "
					+ entry.getValue().getClose());*/
			
			Details details = entry.getValue();
			details.setStartDate(entry.getKey());
			double close = Double.parseDouble(entry.getValue().getClose());
            double open = Double.parseDouble(entry.getValue().getOpen());
            double high = Double.parseDouble(entry.getValue().getHigh());
            double low  = Double.parseDouble(entry.getValue().getLow()); 
            

			if (close > open) {
				details.setCandleColour("Green");
			} else {
				details.setCandleColour("Red");
			}

			double candleHeight = high - low;
			double candleBody = close - open;

			details.setCandleHeight(candleHeight);

			details.setCandleBody(candleBody);
			
			double candleSize = candleBody / candleHeight;
			if (candleSize > 0.5) {
				details.setCandleType("LG");
			} else if (candleSize < -0.5) {
				details.setCandleType("LR");
			} else {
				details.setCandleType("SL");
			}

		}
		return consolidatedHrlyCandlesList;

	}

	public static String getCurrentDateAsString() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_YYYYMMDD);
		return sdf.format(cal.getTime());
	}

	private static String getEndDate(String strtDate, int noOfDays) {

		// Specifying date format that matches the given date
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_YYYYMMDD);
		Calendar c = Calendar.getInstance();
		try {
			// Setting the date to the given date
			c.setTime(sdf.parse(strtDate));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		// Number of Days to add
		c.add(Calendar.DAY_OF_MONTH, noOfDays);
		// Date after adding the days to the given date
		String calcDate = sdf.format(c.getTime());

		return calcDate;
	}

	private static String addStartHrsMins(String fromDate) {

		// Specifying date format that matches the given date
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_YYYYMMDD);
		Calendar calendar = Calendar.getInstance();
		try {
			// Setting the date to the given date
			calendar.setTime(sdf.parse(fromDate));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		// System.out.println("Original = " + calendar.getTime());

		// Substract 2 hour from the current time
		calendar.add(Calendar.HOUR, 9);

		// Add 30 minutes to the calendar time
		calendar.add(Calendar.MINUTE, 15);

		// Add 300 seconds to the calendar time
		calendar.add(Calendar.SECOND, 00);
		// System.out.println("Updated = " + calendar.getTime());
		DateFormat timeFormat = new SimpleDateFormat(DATE_FORMAT_YYYYMMDD_WITH_SS);
		// Date after adding the days to the given date
		String newDate = timeFormat.format(calendar.getTime());
		// Displaying the new Date after addition of Days
		// System.out.println("Date after Addition: "+newDate);
		return newDate;

	}

	private static String addEndHrsMins(String dateString, int endtimeInhrs) {

		// Specifying date format that matches the given date
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_YYYYMMDD);
		Calendar calendar = Calendar.getInstance();
		try {
			// Setting the date to the given date
			calendar.setTime(sdf.parse(dateString));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		// System.out.println("Original = " + calendar.getTime());

		// Substract 2 hour from the current time
		calendar.add(Calendar.HOUR, endtimeInhrs);

		// Add 30 minutes to the calendar time
		calendar.add(Calendar.MINUTE, 15);

		// Add 300 seconds to the calendar time
		calendar.add(Calendar.SECOND, 00);
		// System.out.println("Updated = " + calendar.getTime());

		DateFormat timeFormat = new SimpleDateFormat(DATE_FORMAT_YYYYMMDD_WITH_SS);
		// Date after adding the days to the given date
		String newDate = timeFormat.format(calendar.getTime());
		// Displaying the new Date after addition of Days
		// System.out.println("Date after Addition: "+newDate);
		return newDate;

	}

	private static String getIstTime(String estTime) {

		SimpleDateFormat dateTimeFormat = new SimpleDateFormat(DATE_FORMAT_YYYYMMDD_WITH_SS);
		dateTimeFormat.setTimeZone(TimeZone.getTimeZone("America/New_York"));
		// (EST)
		Date date = null;
		try {
			date = dateTimeFormat.parse(estTime);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DateFormat timeFormat = new SimpleDateFormat(DATE_FORMAT_YYYYMMDD_WITH_SS);
		timeFormat.setTimeZone(TimeZone.getTimeZone("Asia/Calcutta"));
		String istTime = timeFormat.format(date);

		return istTime;
	}

	private static Date getDate(String dateString) throws ParseException {

		DateFormat inFormat = new SimpleDateFormat(DATE_FORMAT_YYYYMMDD_WITH_SS);

		Date tempdate = inFormat.parse(dateString);
		return tempdate;
	}

	private static TreeMap<String, String> getNSEStarEndDateKeys(String starttime, String endTime, int minutes)
			throws ParseException {
		TreeMap<String, String> keys = new TreeMap<String, String>();

		String endate = null;
		String startDate = starttime;

		do {
			endate = getDateAfterAddingMins(startDate, minutes);

			keys.put(startDate, endate);
			String startdateWithEndHrs = addEndHrsMins(getyyyyMMddFormat(startDate), 14);

			if (getDate(endate).before(getDate(endTime))) {
				if (getDate(endate).after(getDate(startdateWithEndHrs))
						|| getDate(endate).equals(getDate(startdateWithEndHrs))) {
					startDate = addADayWithMins(getyyyyMMddFormat(startdateWithEndHrs));
				} else {

					startDate = getDateAfterAddingMins(endate, 60);

				}
			}

		} while (getDate(endate).before(getDate(endTime))
				&& !(getDate(getDateAfterAddingMins(startDate, 60)).equals(getDate(endTime))));

		return keys;
	}

	private static String getDateAfterAddingMins(String dateString, int minutes) throws ParseException {

		DateFormat inFormat = new SimpleDateFormat(DATE_FORMAT_YYYYMMDD_WITH_SS);

		Date tempdate = inFormat.parse(dateString);
		final long ONE_MINUTE_IN_MILLIS = 60000;// millisecs

		Calendar cal = Calendar.getInstance();
		cal.setTime(tempdate);
		long t = cal.getTimeInMillis();
		Date afteradding = new Date(t + (minutes * ONE_MINUTE_IN_MILLIS));
		DateFormat outFormat = new SimpleDateFormat(DATE_FORMAT_YYYYMMDD_WITH_SS);
		String outdateString = outFormat.format(afteradding);
		return outdateString;
	}

	private static String getyyyyMMddFormat(String fromDate) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar calendar = Calendar.getInstance();
		try {
			// Setting the date to the given date
			calendar.setTime(sdf.parse(fromDate));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		DateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd");
		// Date after adding the days to the given date
		String newDate = timeFormat.format(calendar.getTime());
		return newDate;

	}

	private static String addADayWithMins(String fromDate) {

		// Specifying date format that matches the given date
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar calendar = Calendar.getInstance();
		try {
			// Setting the date to the given date
			calendar.setTime(sdf.parse(fromDate));
		} catch (ParseException e) {
			e.printStackTrace();
		}

		// System.out.println("Original = " + calendar.getTime());
		calendar.add(Calendar.DATE, 1);

		// Substract 2 hour from the current time
		calendar.add(Calendar.HOUR, 9);

		// Add 30 minutes to the calendar time
		calendar.add(Calendar.MINUTE, 15);

		// Add 300 seconds to the calendar time
		calendar.add(Calendar.SECOND, 00);
		// System.out.println("Updated = " + calendar.getTime());
		DateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// Date after adding the days to the given date
		String newDate = timeFormat.format(calendar.getTime());
		return newDate;

	}

	private static TreeMap<String, Details> getNSEConsolidatedHighLowList(TreeMap<String, Details> IstlevelList,
			TreeMap<String, String> keys) {
		TreeMap<String, Details> consolidatedHighLowList = getHighLowList(IstlevelList, keys);
		return consolidatedHighLowList;

	}

	private static TreeMap<String, Details> getHighLowList(TreeMap<String, Details> IstlevelList,
			TreeMap<String, String> keys) {
		TreeMap<String, Details> consolidatedHighLowList = new TreeMap<String, Details>();
		try {
			for (Map.Entry<String, String> entry : keys.entrySet()) {
				Details details = new Details();

				details.setStartDate(entry.getKey());
				details.setEndDate(entry.getValue());

				for (Map.Entry<String, Details> entry2 : IstlevelList.entrySet()) {

					if (getDate(entry2.getKey()).equals(getDate(entry.getKey()))) {
						Details zone2 = entry2.getValue();
						details.setOpen(zone2.getOpen());

					}
				}
				for (Map.Entry<String, Details> entry2 : IstlevelList.entrySet()) {
					if (getDate(entry2.getKey()).equals(getDate(entry.getValue()))) {
						// System.out.println("in");
						Details zone2 = entry2.getValue();
						details.setClose(zone2.getClose());

					}
				}

				String highValue = "";
				String lowValue = "";

				Map<String, ArrayList<String>> highLow = gethighGetLow(entry.getKey(), entry.getValue(), IstlevelList);
				ArrayList<String> highList = highLow.get("High");

				ArrayList<String> lowList = highLow.get("Low");
				if (null != highList && !highList.isEmpty()) {

					Collections.sort(highList, Collections.reverseOrder());
					details.setHigh(highList.get(0));
				}
				if (null != lowList && !lowList.isEmpty()) {

					Collections.sort(lowList);
					details.setLow(lowList.get(0));
				}

				if (IstlevelList.containsKey(entry.getKey()) && (IstlevelList.containsKey(entry.getValue()))) {
					consolidatedHighLowList.put(entry.getKey(), details);
				}
			}
		} catch (Exception e) {

		}
		return consolidatedHighLowList;
	}

	private static Map<String, ArrayList<String>> gethighGetLow(String startDate, String endDate,
			TreeMap<String, Details> IstlevelList) {

		Map<String, ArrayList<String>> highLow = new HashMap<String, ArrayList<String>>();

		ArrayList<String> highList = new ArrayList<>();
		ArrayList<String> lowList = new ArrayList<>();

		for (Map.Entry<String, Details> entry2 : IstlevelList.entrySet()) {
			try {
				if ((((getDate(entry2.getKey()).equals(getDate(startDate)))
						|| (getDate(entry2.getKey()).after(getDate(startDate))))
						&& ((getDate(entry2.getKey()).equals(getDate(endDate)))
								|| (getDate(entry2.getKey()).before(getDate(endDate)))))) {
					Details zone2 = entry2.getValue();
					highList.add(zone2.getHigh());
					lowList.add(zone2.getLow());

				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		highLow.put("High", highList);
		highLow.put("Low", lowList);

		return highLow;
	}

	private static ArrayList<String> getNSEStartDateList(String starttime, String endTime, int minutes)
			throws ParseException {
		ArrayList<String> keys = new ArrayList<String>();

		String startDate = starttime;

		do {

			String startdateWithEndHrs = addEndHrsMins(getyyyyMMddFormat(startDate), 14);

			if (getDate(startDate).before(getDate(endTime)) || getDate(startDate).equals(getDate(endTime))) {
				if (getDate(startDate).after(getDate(startdateWithEndHrs))) {
					startDate = addADayWithMins(getyyyyMMddFormat(startdateWithEndHrs));
				} else {
					keys.add(startDate);
					startDate = getDateAfterAddingMins(startDate, 60);

				}
			}

		} while (getDate(startDate).before(getDate(endTime)) || getDate(startDate).equals(getDate(endTime))
				|| (getDate(getDateAfterAddingMins(startDate, 60)).equals(getDate(endTime))));

		return keys;
	}

}
