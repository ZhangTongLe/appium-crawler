package com.wosai.automation.crawler;

import com.wosai.automation.crawler.beans.Config;
import com.wosai.automation.crawler.engine.AndroidEngine;
import com.wosai.automation.crawler.parser.AndroidXmlParser;
import com.wosai.automation.crawler.parser.ConfigProvider;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by yangzhixiang on 2017/9/30.
 */
public class CrawlerLaunch {

    private static Config config;
    private static AppiumDriver appiumDriver;
    private static AndroidEngine androidEngine;
    private static AndroidXmlParser androidXmlParser;

    public static void init() {
        config = new ConfigProvider().getConfig("android.xml");
        Map<String, String> mapCapabilities = config.getCapabilitiesMap();
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        String appiumUrl = "http://" + config.getHost() + ":" + config.getPort() + "/wd/hub";
        for (Map.Entry<String, String> entry : mapCapabilities.entrySet()) {
            desiredCapabilities.setCapability(entry.getKey(), entry.getValue());
        }
        try {
            appiumDriver = new AndroidDriver(new URL(appiumUrl), desiredCapabilities);
            appiumDriver.manage().timeouts().implicitlyWait(config.getImplicitlyWait(), TimeUnit.SECONDS);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public static void working() {
        androidXmlParser = new AndroidXmlParser(appiumDriver, config);
        androidEngine = new AndroidEngine(appiumDriver, config, androidXmlParser);
        androidEngine.doGuideFlow(config);
        androidEngine.bfsTraverse();
    }


    public static void main(String[] args) {
        init();
        working();
    }
}
