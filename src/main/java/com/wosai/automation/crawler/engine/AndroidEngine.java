package com.wosai.automation.crawler.engine;

import com.wosai.automation.crawler.beans.Config;
import com.wosai.automation.crawler.parser.XmlParser;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidKeyCode;

/**
 * Created by yangzhixiang on 2017/9/29.
 */
public class AndroidEngine extends Engine {
    public AndroidEngine(AppiumDriver appiumDriver, Config config, XmlParser xmlParser) {
        super(appiumDriver, config, xmlParser);
    }

    protected void goBack() {
        if (isKeyboardPopsUp()) {
            appiumDriver.hideKeyboard();
        }
        ((AndroidDriver) appiumDriver).pressKeyCode(AndroidKeyCode.BACK);
    }
}
