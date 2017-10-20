package com.wosai.automation.crawler.beans;

import java.util.List;
import java.util.Map;

/**
 * Created by yangzhixiang on 2017/9/13.
 */
public class Config {

    //appium params
    private String host;
    private int port;
    private int implicitlyWait;

    //capabilities params
    private Map<String, String> capabilitiesMap;
    private Map<String, String> tabBarMap;
    private Map<String, String> preProcessPageMap;

    //global params
    private int timeout;
    private int wait;
    private long duration;
    private int interval;
    private int backMaxRetry;

    //guideFlow params
    private List<String> guideFlowList;

    //rule params
    private List<String> clickList;
    private List<String> inputList;
    private List<String> blackPageList;
    private List<String> blackElementList;
    private String rootPage;

    //device info params
    private String udid;
    private String appPackage;
    private String deviceName;
    private String platformName;

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getImplicitlyWait() {
        return this.implicitlyWait;
    }

    public void setImplicitlyWait(int implicitlyWait) {
        this.implicitlyWait = implicitlyWait;
    }

    public Map<String, String> getCapabilitiesMap() {
        return this.capabilitiesMap;
    }

    public void setCapabilitiesMap(Map<String, String> capabilitiesMap) {
        this.capabilitiesMap = capabilitiesMap;
    }

    public int getTimeout() {
        return this.timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getWait() {
        return this.wait;
    }

    public void setWait(int wait) {
        this.wait = wait;
    }

    public long getDuration() {
        return this.duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getInterval() {
        return this.interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public List<String> getGuideFlowList() {
        return this.guideFlowList;
    }

    public void setGuideFlowList(List<String> guideFlowList) {
        this.guideFlowList = guideFlowList;
    }

    public List<String> getClickList() {
        return this.clickList;
    }

    public void setClickList(List<String> clickList) {
        this.clickList = clickList;
    }

    public List<String> getInputList() {
        return this.inputList;
    }

    public void setInputList(List<String> inputList) {
        this.inputList = inputList;
    }

    public String getUdid() {
        return this.udid;
    }

    public void setUdid(String udid) {
        this.udid = udid;
    }

    public String getAppPackage() {
        return this.appPackage;
    }

    public void setAppPackage(String appPackage) {
        this.appPackage = appPackage;
    }

    public String getDeviceName() {
        return this.deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getPlatformName() {
        return this.platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public List<String> getBlackPageList() {
        return blackPageList;
    }

    public void setBlackPageList(List<String> blackPageList) {
        this.blackPageList = blackPageList;
    }

    public int getBackMaxRetry() {
        return backMaxRetry;
    }

    public void setBackMaxRetry(int backMaxRetry) {
        this.backMaxRetry = backMaxRetry;
    }

    public Map<String, String> getTabBarMap() {
        return tabBarMap;
    }

    public void setTabBarMap(Map<String, String> tabBarMap) {
        this.tabBarMap = tabBarMap;
    }

    public List<String> getBlackElementList() {
        return blackElementList;
    }

    public void setBlackElementList(List<String> blackElementList) {
        this.blackElementList = blackElementList;
    }

    public Map<String, String> getPreProcessPageMap() {
        return preProcessPageMap;
    }

    public void setPreProcessPageMap(Map<String, String> preProcessPageMap) {
        this.preProcessPageMap = preProcessPageMap;
    }

    public String getRootPage() {
        return rootPage;
    }

    public void setRootPage(String rootPage) {
        this.rootPage = rootPage;
    }
}
