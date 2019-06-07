package com.api.testing;

public class CandleLevelDetails {
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
	
	String currCandleType;
	String gapType;
	String lastCandleType;
	
	boolean isLegOutCandle;
	boolean isLegInCandle;
	
	boolean supportLevel ;
	boolean resistLevel;
	boolean markLevel;
	
	int noOfLG;
	int noOfLR;
	int noOfSL;
	
	public Double getSupportHigh() {
		return supportHigh;
	}
	public void setSupportHigh(Double supportHigh) {
		this.supportHigh = supportHigh;
	}
	public Double getSupportLow() {
		return supportLow;
	}
	public void setSupportLow(Double supportLow) {
		this.supportLow = supportLow;
	}
	public Double getResistHigh() {
		return resistHigh;
	}
	public void setResistHigh(Double resistHigh) {
		this.resistHigh = resistHigh;
	}
	public Double getResistLow() {
		return resistLow;
	}
	public void setResistLow(Double resistLow) {
		this.resistLow = resistLow;
	}
	public Double getAllTimeHigh() {
		return allTimeHigh;
	}
	public void setAllTimeHigh(Double allTimeHigh) {
		this.allTimeHigh = allTimeHigh;
	}
	public Double getAllTimeLow() {
		return allTimeLow;
	}
	public void setAllTimeLow(Double allTimeLow) {
		this.allTimeLow = allTimeLow;
	}
	public Double getCandleHigh() {
		return candleHigh;
	}
	public void setCandleHigh(Double candleHigh) {
		this.candleHigh = candleHigh;
	}
	public Double getCandlelow() {
		return candlelow;
	}
	public void setCandlelow(Double candlelow) {
		this.candlelow = candlelow;
	}
	public Double getCandleOpen() {
		return candleOpen;
	}
	public void setCandleOpen(Double candleOpen) {
		this.candleOpen = candleOpen;
	}
	public Double getCandleClose() {
		return candleClose;
	}
	public void setCandleClose(Double candleClose) {
		this.candleClose = candleClose;
	}
	public Double getLastCandleHigh() {
		return lastCandleHigh;
	}
	public void setLastCandleHigh(Double lastCandleHigh) {
		this.lastCandleHigh = lastCandleHigh;
	}
	public Double getLastCandlelow() {
		return lastCandlelow;
	}
	public void setLastCandlelow(Double lastCandlelow) {
		this.lastCandlelow = lastCandlelow;
	}
	public String getCurrCandleType() {
		return currCandleType;
	}
	public void setCurrCandleType(String currCandleType) {
		this.currCandleType = currCandleType;
	}
	public String getGapType() {
		return gapType;
	}
	public void setGapType(String gapType) {
		this.gapType = gapType;
	}
	public String getLastCandleType() {
		return lastCandleType;
	}
	public void setLastCandleType(String lastCandleType) {
		this.lastCandleType = lastCandleType;
	}
	public boolean isLegOutCandle() {
		return isLegOutCandle;
	}
	public void setLegOutCandle(boolean isLegOutCandle) {
		this.isLegOutCandle = isLegOutCandle;
	}
	public boolean isLegInCandle() {
		return isLegInCandle;
	}
	public void setLegInCandle(boolean isLegInCandle) {
		this.isLegInCandle = isLegInCandle;
	}
	public boolean isSupportLevel() {
		return supportLevel;
	}
	public void setSupportLevel(boolean supportLevel) {
		this.supportLevel = supportLevel;
	}
	public boolean isResistLevel() {
		return resistLevel;
	}
	public void setResistLevel(boolean resistLevel) {
		this.resistLevel = resistLevel;
	}
	public boolean isMarkLevel() {
		return markLevel;
	}
	public void setMarkLevel(boolean markLevel) {
		this.markLevel = markLevel;
	}
	public int getNoOfLG() {
		return noOfLG;
	}
	public void setNoOfLG(int noOfLG) {
		this.noOfLG = noOfLG;
	}
	public int getNoOfLR() {
		return noOfLR;
	}
	public void setNoOfLR(int noOfLR) {
		this.noOfLR = noOfLR;
	}
	public int getNoOfSL() {
		return noOfSL;
	}
	public void setNoOfSL(int noOfSL) {
		this.noOfSL = noOfSL;
	}

}
