package com.wosai.automation.crawler.parser;

import com.wosai.automation.crawler.beans.Config;
import com.wosai.automation.crawler.beans.ElementNode;
import io.appium.java_client.AppiumDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Queue;

/**
 * Created by yangzhixiang on 2017/9/13.
 */
public abstract class XmlParser {

    protected static Logger logger = LoggerFactory.getLogger(XmlParser.class);
    protected AppiumDriver appiumDriver;
    protected Config config;

    public XmlParser(AppiumDriver appiumDriver, Config config) {
        this.appiumDriver = appiumDriver;
        this.config = config;
    }

    public abstract Queue<ElementNode> getCurrentPageNodes(String pageSource, String pageId);

    public abstract String getPageMD5Code(String pageSource);

    public String EncoderByMD5(String str) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            BASE64Encoder base64Encoder = new BASE64Encoder();
            return base64Encoder.encode(md5.digest(str.getBytes("utf-8")));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    public abstract boolean isJumpPage(String orgPageSource, String currentPageSource);

    public abstract boolean isNewPageLoad(List<String> visitedListPage, String currentPageId);
}
