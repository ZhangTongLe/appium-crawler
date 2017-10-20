package com.wosai.automation.crawler.parser;


import com.wosai.automation.crawler.beans.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by yangzhixiang on 2017/9/13.
 */
public class ConfigProvider {
    private static Logger logger = LoggerFactory.getLogger(ConfigProvider.class);

    public Config getConfig(String configFileName) {
        Config config = new Config();
        String host = "127.0.0.1";
        String basePath = System.getProperty("user.dir") + "/config/";
        int port = 4327, implicitlyWait = 15, timeout = 30, wait = 3, interval = 3, backMaxRetry = 0;
        long duration = 3600;
        String udid = "", deviceName = "", appPackage = "", platformName = "", rootPage = "";
        Node capabilitiesNode;
        NodeList capabilitiesNodeList;
        Node node;
        Map<String, String> capabilitiesMap = new HashMap<>();
        Map<String, String> tabBarMap = new HashMap<>();
        Map<String, String> preProcessMap = new HashMap<>();
        List<String> clickList = null, inputList = null, guideFlowList = null, blackPageList = null, blackElementList
                = null;
        DocumentBuilderFactory dBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;
        try {
            dBuilder = dBuilderFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(basePath + configFileName);
            Document doc1 = dBuilder.parse(Thread.currentThread().getContextClassLoader().getResourceAsStream("config" +
                    ".xml"));
            if (doc.getElementsByTagName("host").item(0) != null) {
                host = doc.getElementsByTagName("host").item(0).getTextContent().trim();
            }
            if (doc.getElementsByTagName("port").item(0) != null) {
                port = Integer.parseInt(doc.getElementsByTagName("port").item(0).getTextContent().trim());
            }
            if (doc.getElementsByTagName("implicitlyWait").item(0) != null) {
                implicitlyWait = Integer.parseInt(doc.getElementsByTagName("implicitlyWait").item(0).getTextContent()
                        .trim());
            }
            if (doc.getElementsByTagName("timeout").item(0) != null) {
                timeout = Integer.parseInt(doc.getElementsByTagName("timeout").item(0).getTextContent().trim());
            }
            if (doc.getElementsByTagName("wait").item(0) != null) {
                wait = Integer.parseInt(doc.getElementsByTagName("wait").item(0).getTextContent().trim());
            }
            if (doc.getElementsByTagName("interval").item(0) != null) {
                interval = Integer.parseInt(doc.getElementsByTagName("interval").item(0).getTextContent().trim());
            }
            if (doc.getElementsByTagName("duration").item(0) != null) {
                duration = Long.parseLong(doc.getElementsByTagName("duration").item(0).getTextContent().trim());
            }
            if (doc.getElementsByTagName("backMaxRetry").item(0) != null) {
                backMaxRetry = Integer.parseInt(doc.getElementsByTagName("backMaxRetry").item(0).getTextContent()
                        .trim());
            }
            if (doc.getElementsByTagName("rootPage").item(0) != null) {
                rootPage = doc.getElementsByTagName("rootPage").item(0).getTextContent().trim();
            }
            capabilitiesNode = doc.getElementsByTagName("capabilities").item(0);
            if (capabilitiesNode != null) {
                capabilitiesNodeList = capabilitiesNode.getChildNodes();
                for (int i = 0; i < capabilitiesNodeList.getLength(); i++) {
                    if (capabilitiesNodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        capabilitiesMap.put(capabilitiesNodeList.item(i).getNodeName().trim(), capabilitiesNodeList
                                .item(i).getTextContent().trim());
                    }
                }
            }
            if (capabilitiesMap != null) {
                udid = capabilitiesMap.get("udid");
                deviceName = capabilitiesMap.get("deviceName");
                appPackage = capabilitiesMap.get("appPackage");
                platformName = capabilitiesMap.get("platformName");
            }
            node = doc.getElementsByTagName("guideFlow").item(0);
            if (node != null) {
                guideFlowList = getNodeList(node);
            }
            node = doc.getElementsByTagName("blackPage").item(0);
            if (node != null) {
                blackPageList = getNodeList(node);
            }
            node = doc.getElementsByTagName("blackElement").item(0);
            if (node != null) {
                blackElementList = getNodeList(node);
            }
            node = doc.getElementsByTagName("tabBar").item(0);
            if (node != null) {
                List<String> tempList = getNodeList(node);
                for (String temp : tempList) {
                    String[] s = temp.split(">>");
                    tabBarMap.put(s[0].trim(), s[1].trim());
                }
            }
            node = doc.getElementsByTagName("preProcessPage").item(0);
            if (node != null) {
                List<String> tempList = getNodeList(node);
                for (String temp : tempList) {
                    String[] s = temp.split(">>");
                    preProcessMap.put(s[0].trim(), s[1].trim());
                }
            }
            node = doc1.getElementsByTagName("click").item(0);
            if (node != null) {
                clickList = getNodeList(node);
            }
            node = doc1.getElementsByTagName("input").item(0);
            if (node != null) {
                inputList = getNodeList(node);
            }
            config.setHost(host);
            config.setPort(port);
            config.setImplicitlyWait(implicitlyWait);
            config.setCapabilitiesMap(capabilitiesMap);
            config.setTimeout(timeout);
            config.setWait(wait);
            config.setDuration(duration);
            config.setInterval(interval);
            config.setGuideFlowList(guideFlowList);
            config.setClickList(clickList);
            config.setInputList(inputList);
            config.setUdid(udid);
            config.setAppPackage(appPackage);
            config.setDeviceName(deviceName);
            config.setPlatformName(platformName);
            config.setBlackPageList(blackPageList);
            config.setBackMaxRetry(backMaxRetry);
            config.setTabBarMap(tabBarMap);
            config.setBlackElementList(blackElementList);
            config.setPreProcessPageMap(preProcessMap);
            config.setRootPage(rootPage);
        } catch (ParserConfigurationException e) {
            logger.error("ParserConfigurationException occurs: ", e);
        } catch (SAXException e) {
            logger.error("SAXException occurs: ", e);
        } catch (IOException e) {
            logger.error("IOException occurs : ", e);
        }
        return config;
    }

    private List<String> getNodeList(Node node) {
        List<String> list = null;
        if (node != null && node.getTextContent().length() != 0) {
            list = new ArrayList<>();
            NodeList nodeList = node.getChildNodes();
            for (int i = 0; i < nodeList.getLength(); i++) {
                if (nodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    list.add(nodeList.item(i).getTextContent().trim());
                }
            }
        }
        return list;
    }
}
